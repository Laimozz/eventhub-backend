package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.CreateEventRequest;
import com.eventhub.backend.dto.request.UpdateEventRequest;
import com.eventhub.backend.dto.response.EventResponse;
import com.eventhub.backend.dto.response.EventListResponse;
import com.eventhub.backend.dto.response.CategoryResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.EventGuest;
import com.eventhub.backend.entity.Notification;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.CategoryRepository;
import com.eventhub.backend.repository.BookingItemRepository;
import com.eventhub.backend.repository.EventGuestRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.NotificationRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.VenueRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventRepository events;
    private final VenueRepository venues;
    private final CategoryRepository categories;
    private final TicketTypeRepository ticketTypes;
    private final EventGuestRepository guests;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final Clock clock;
    private final EventImageService images;
    private final BookingItemRepository bookingItems;

    @Transactional(readOnly = true)
    public EventListResponse listEvents(Integer organizerId, int page, int size, String search, EventStatus status) {
        requireOrganizer(organizerId);
        if (page < 0 || size < 1 || size > 50 || search.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination or search");
        }
        String pattern = "%" + search.strip().toLowerCase(java.util.Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        var result = events.findOrganizerEvents(organizerId, status, pattern,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        Map<EventStatus, Long> counts = new java.util.EnumMap<>(EventStatus.class);
        for (EventStatus item : EventStatus.values()) counts.put(item, 0L);
        events.countOrganizerStatuses(organizerId).forEach(item -> counts.put(item.getStatus(), item.getTotal()));
        var now = LocalDateTime.now(clock);
        return new EventListResponse(result.map(event -> EventListResponse.EventSummaryResponse.from(event, now))
                .getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages(), counts);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Integer organizerId, Integer eventId) {
        requireOrganizer(organizerId);
        var event = events.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return EventResponse.from(event, ticketTypes.findByEventIdOrderByIdAsc(eventId),
                guests.findByEventIdOrderByIdAsc(eventId), LocalDateTime.now(clock));
    }

    private com.eventhub.backend.entity.User requireOrganizer(Integer organizerId) {
        var organizer = users.findById(organizerId)
                .orElseThrow(() -> new AccessDeniedException("Organizer account is unavailable"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new AccessDeniedException("Only active organizers can manage events");
        }
        return organizer;
    }

    private Event ownedForUpdate(Integer organizerId, Integer eventId) {
        requireOrganizer(organizerId);
        return events.findOwnedForUpdate(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    @Transactional
    public EventResponse cancelEvent(Integer organizerId, Integer eventId, String reason) {
        Event event = ownedForUpdate(organizerId, eventId);
        if (!EventResponse.canCancel(event, LocalDateTime.now(clock))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event cannot be canceled in its current state");
        }
        event.setCancelReason(reason.strip());
        event.setStatus(EventStatus.PENDING_CANCELLATION);
        var savedTickets = ticketTypes.findForUpdate(eventId);
        savedTickets.forEach(ticket -> ticket.setStatus("INACTIVE"));
        notifyAdmin(event, "Có yêu cầu hủy sự kiện", "EVENT_PENDING_CANCELLATION", "\" vừa được gửi yêu cầu hủy.");
        events.flush();
        return EventResponse.from(event, savedTickets, guests.findByEventIdOrderByIdAsc(eventId), LocalDateTime.now(clock));
    }

    @Transactional
    public EventResponse updateEvent(Integer organizerId, Integer eventId, UpdateEventRequest request,
            Map<String, MultipartFile> files) {
        Event event = ownedForUpdate(organizerId, eventId);
        if (!EventResponse.canEdit(event, LocalDateTime.now(clock))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only upcoming events awaiting approval or approved can be edited");
        }
        validateScheduleAndCapacity(request.toCreateRequest());
        var category = categories.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        var currentTickets = ticketTypes.findForUpdate(eventId);
        var currentGuests = guests.findByEventIdOrderByIdAsc(eventId);
        Map<Integer, TicketType> ticketById = new LinkedHashMap<>();
        currentTickets.forEach(ticket -> ticketById.put(ticket.getId(), ticket));
        Map<Integer, EventGuest> guestById = new LinkedHashMap<>();
        currentGuests.forEach(guest -> guestById.put(guest.getId(), guest));
        Set<Integer> keptTickets = new HashSet<>();
        Set<Integer> keptGuests = new HashSet<>();
        for (var input : request.ticketTypes()) {
            if (input.id() != null) {
                if (!ticketById.containsKey(input.id()) || !keptTickets.add(input.id())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or duplicate ticket type ID");
                }
                var ticket = ticketById.get(input.id());
                int allocated = ticket.getQuantity() - ticket.getRemainingQuantity();
                if (input.quantity() < allocated) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket quantity is below sold and reserved quantity");
                }
            }
        }
        var removedTickets = currentTickets.stream().filter(ticket -> !keptTickets.contains(ticket.getId())).toList();
        for (var ticket : removedTickets) {
            if (ticket.getReservedQuantity() > 0 || !ticket.getQuantity().equals(ticket.getRemainingQuantity())
                    || bookingItems.existsByTicketTypeId(ticket.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a ticket type with bookings or reservations");
            }
        }
        var guestInputs = request.guests() == null ? List.<UpdateEventRequest.GuestRequest>of() : request.guests();
        for (var input : guestInputs) {
            if (input.id() != null && (!guestById.containsKey(input.id()) || !keptGuests.add(input.id()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or duplicate guest ID");
            }
        }
        Set<String> required = new HashSet<>();
        Set<String> allowed = new HashSet<>(Set.of("bannerImage", "thumbnailImage", "imageZone"));
        for (int index = 0; index < request.ticketTypes().size(); index++) {
            allowed.add("ticketImage" + index);
            if (request.ticketTypes().get(index).id() == null) required.add("ticketImage" + index);
        }
        for (int index = 0; index < guestInputs.size(); index++) {
            allowed.add("guestImage" + index);
            if (Boolean.TRUE.equals(guestInputs.get(index).removeImage()) && files.containsKey("guestImage" + index)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot replace and remove the same guest image");
            }
        }
        if (Boolean.TRUE.equals(request.removeImageZone()) && files.containsKey("imageZone")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot replace and remove the same zone image");
        }
        var prepared = prepareImages(required, allowed, files);

        // Venues may be shared: editing this event must not modify another event's location.
        Venue venue = event.getVenue();
        if (events.countByVenueId(venue.getId()) > 1) venue = new Venue();
        venue.setCity(request.venue().city().strip());
        venue.setAddress(request.venue().address().strip());
        venue.setCapacity(request.venue().capacity());
        event.setVenue(venues.save(venue));
        event.setCategory(category);
        event.setName(request.name().strip());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setStatus(EventStatus.PENDING_APPROVAL);
        event.setReviewedBy(null);
        event.setReviewedAt(null);
        event.setRejectReason(null);
        if (Boolean.TRUE.equals(request.removeImageZone())) event.setImageZoneUrl(null);
        ticketTypes.deleteAll(removedTickets);
        List<TicketType> savedTickets = new ArrayList<>();
        for (var input : request.ticketTypes()) {
            var ticket = input.id() == null ? new TicketType() : ticketById.get(input.id());
            int allocated = input.id() == null ? 0 : ticket.getQuantity() - ticket.getRemainingQuantity();
            ticket.setEvent(event);
            ticket.setName(input.name().strip());
            ticket.setDescription(input.description());
            ticket.setPrice(input.price());
            ticket.setQuantity(input.quantity());
            ticket.setRemainingQuantity(input.quantity() - allocated);
            ticket.setSaleStartTime(input.saleStartTime());
            ticket.setSaleEndTime(input.saleEndTime());
            ticket.setStatus("INACTIVE");
            if (input.id() == null) ticket.setImageUrl("");
            savedTickets.add(ticket);
        }
        ticketTypes.saveAll(savedTickets);
        guests.deleteAll(currentGuests.stream().filter(guest -> !keptGuests.contains(guest.getId())).toList());
        List<EventGuest> savedGuests = new ArrayList<>();
        for (var input : guestInputs) {
            var guest = input.id() == null ? new EventGuest() : guestById.get(input.id());
            guest.setEvent(event);
            guest.setName(input.name().strip());
            guest.setRole(input.role().strip());
            guest.setDescription(input.description());
            if (Boolean.TRUE.equals(input.removeImage())) guest.setImageUrl(null);
            savedGuests.add(guest);
        }
        guests.saveAll(savedGuests);
        notifyAdmin(event, "Có sự kiện cập nhật chờ duyệt", "EVENT_PENDING_APPROVAL",
                "\" vừa được cập nhật và gửi lên chờ duyệt.");
        events.flush();
        var urls = uploadImages(prepared);
        if (urls.containsKey("bannerImage")) event.setBannerImageUrl(urls.get("bannerImage"));
        if (urls.containsKey("thumbnailImage")) event.setThumbnailImageUrl(urls.get("thumbnailImage"));
        if (urls.containsKey("imageZone")) event.setImageZoneUrl(urls.get("imageZone"));
        for (int index = 0; index < savedTickets.size(); index++) {
            if (urls.containsKey("ticketImage" + index)) savedTickets.get(index).setImageUrl(urls.get("ticketImage" + index));
        }
        for (int index = 0; index < savedGuests.size(); index++) {
            if (urls.containsKey("guestImage" + index)) savedGuests.get(index).setImageUrl(urls.get("guestImage" + index));
        }
        events.flush();
        return EventResponse.from(event, savedTickets, savedGuests, LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categories.findAll(Sort.by("name")).stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getDescription()))
                .toList();
    }

    @Transactional
    public EventResponse createEvent(Integer organizerId, CreateEventRequest request, Map<String, MultipartFile> files) {
        var organizer = requireOrganizer(organizerId);
        validateScheduleAndCapacity(request);
        var category = categories.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        var prepared = prepareImages(request, files);

        Venue venue = new Venue();
        venue.setCity(request.venue().city().strip());
        venue.setAddress(request.venue().address().strip());
        venue.setCapacity(request.venue().capacity());
        venues.save(venue);

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategory(category);
        event.setName(request.name().strip());
        event.setDescription(request.description());
        // These empty values exist only inside this transaction, until Cloudinary returns URLs.
        event.setThumbnailImageUrl("");
        event.setBannerImageUrl("");
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setStatus(EventStatus.PENDING_APPROVAL);
        events.save(event);

        List<TicketType> savedTicketTypes = ticketTypes.saveAll(request.ticketTypes().stream().map(input -> {
            TicketType ticketType = new TicketType();
            ticketType.setEvent(event);
            ticketType.setName(input.name().strip());
            ticketType.setDescription(input.description());
            ticketType.setImageUrl("");
            ticketType.setPrice(input.price());
            ticketType.setQuantity(input.quantity());
            ticketType.setReservedQuantity(0);
            ticketType.setRemainingQuantity(input.quantity());
            ticketType.setSaleStartTime(input.saleStartTime());
            ticketType.setSaleEndTime(input.saleEndTime());
            // Ticket sales remain inactive while the event awaits approval.
            ticketType.setStatus("INACTIVE");
            return ticketType;
        }).toList());

        List<EventGuest> savedGuests = request.guests() == null ? List.of()
                : guests.saveAll(request.guests().stream().map(input -> {
                    EventGuest guest = new EventGuest();
                    guest.setEvent(event);
                    guest.setName(input.name().strip());
                    guest.setRole(input.role().strip());
                    guest.setDescription(input.description());
                    return guest;
                }).toList());
        notifyAdmin(event);
        // Fail on DB constraints before starting any external uploads.
        events.flush();
        Map<String, String> urls = uploadImages(prepared);
        event.setBannerImageUrl(urls.get("bannerImage"));
        event.setThumbnailImageUrl(urls.get("thumbnailImage"));
        event.setImageZoneUrl(urls.get("imageZone"));
        for (int index = 0; index < savedTicketTypes.size(); index++) {
            savedTicketTypes.get(index).setImageUrl(urls.get("ticketImage" + index));
        }
        for (int index = 0; index < savedGuests.size(); index++) {
            savedGuests.get(index).setImageUrl(urls.get("guestImage" + index));
        }
        events.flush();
        return EventResponse.from(event, savedTicketTypes, savedGuests, LocalDateTime.now(clock));
    }

    private Map<String, EventImageService.PreparedImage> prepareImages(CreateEventRequest request,
            Map<String, MultipartFile> files) {
        Set<String> required = new HashSet<>(Set.of("bannerImage", "thumbnailImage"));
        Set<String> allowed = new HashSet<>(required);
        allowed.add("imageZone");
        for (int index = 0; index < request.ticketTypes().size(); index++) {
            required.add("ticketImage" + index);
            allowed.add("ticketImage" + index);
        }
        if (request.guests() != null) {
            for (int index = 0; index < request.guests().size(); index++) allowed.add("guestImage" + index);
        }
        return prepareImages(required, allowed, files);
    }

    private Map<String, EventImageService.PreparedImage> prepareImages(Set<String> required, Set<String> allowed,
            Map<String, MultipartFile> files) {
        if (!files.keySet().containsAll(required) || !allowed.containsAll(files.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required images or unexpected image fields");
        }
        var prepared = new LinkedHashMap<String, EventImageService.PreparedImage>();
        // Validate every image before writing data or contacting Cloudinary.
        files.forEach((name, file) -> prepared.put(name, images.prepare(file)));
        return prepared;
    }

    private Map<String, String> uploadImages(Map<String, EventImageService.PreparedImage> prepared) {
        if (prepared.isEmpty()) return Map.of();
        images.verifyConfiguration();
        List<String> attemptedImageIds = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    for (String id : attemptedImageIds) {
                        try {
                            images.delete(id);
                        } catch (RuntimeException exception) {
                            // Keep the original creation failure; make cleanup failures visible without credentials.
                            log.error("Could not clean up Cloudinary image after event rollback: {}", id);
                        }
                    }
                }
            }
        });
        Map<String, String> urls = new LinkedHashMap<>();
        prepared.forEach((name, image) -> {
            String id = UUID.randomUUID().toString();
            // Track before upload so a timeout after Cloudinary accepts the file is also compensated.
            attemptedImageIds.add(id);
            urls.put(name, images.upload(image, id));
        });
        return urls;
    }

    private void notifyAdmin(Event event) {
        notifyAdmin(event, "Có sự kiện mới chờ duyệt", "EVENT_PENDING_APPROVAL", "\" vừa được gửi lên chờ duyệt.");
    }

    private void notifyAdmin(Event event, String title, String type, String suffix) {
        users.findFirstByRoleAndStatusOrderByIdAsc(Role.ADMIN, "ACTIVE").ifPresent(admin -> {
            String prefix = "Sự kiện \"";
            String eventName = event.getName();
            int maxNameLength = 255 - prefix.length() - suffix.length();
            // Keep the message within the existing VARCHAR(255), including long event names.
            if (eventName.codePointCount(0, eventName.length()) > maxNameLength) {
                eventName = eventName.substring(0, eventName.offsetByCodePoints(0, maxNameLength - 3)) + "...";
            }
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setTitle(title);
            notification.setContent(prefix + eventName + suffix);
            notification.setType(type);
            notification.setRead(false);
            notifications.save(notification);
        });
    }

    private void validateScheduleAndCapacity(CreateEventRequest request) {
        if (!request.startTime().isAfter(LocalDateTime.now(clock))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event start time must be in the future (UTC)");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event end time must be after start time");
        }
        long totalQuantity = 0;
        for (var ticketType : request.ticketTypes()) {
            if (ticketType.saleEndTime().isBefore(ticketType.saleStartTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ticket sale end time must not be before sale start time");
            }
            if (!ticketType.saleStartTime().isBefore(request.startTime())
                    || !ticketType.saleEndTime().isBefore(request.startTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ticket sales must start and end before the event starts");
            }
            totalQuantity += ticketType.quantity();
        }
        if (totalQuantity > request.venue().capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total ticket quantity exceeds venue capacity");
        }
    }
}
