package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.CreateEventRequest;
import com.eventhub.backend.dto.response.EventResponse;
import com.eventhub.backend.dto.response.CategoryResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.EventGuest;
import com.eventhub.backend.entity.Notification;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.CategoryRepository;
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

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categories.findAll(Sort.by("name")).stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getDescription()))
                .toList();
    }

    @Transactional
    public EventResponse createEvent(Integer organizerId, CreateEventRequest request, Map<String, MultipartFile> files) {
        var organizer = users.findById(organizerId)
                .orElseThrow(() -> new AccessDeniedException("Organizer account is unavailable"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new AccessDeniedException("Only active organizers can create events");
        }
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
        return EventResponse.from(event, savedTicketTypes, savedGuests);
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
        if (!files.keySet().containsAll(required) || !allowed.containsAll(files.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required images or unexpected image fields");
        }
        var prepared = new LinkedHashMap<String, EventImageService.PreparedImage>();
        // Validate every image before writing data or contacting Cloudinary.
        files.forEach((name, file) -> prepared.put(name, images.prepare(file)));
        return prepared;
    }

    private Map<String, String> uploadImages(Map<String, EventImageService.PreparedImage> prepared) {
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
        users.findFirstByRoleAndStatusOrderByIdAsc(Role.ADMIN, "ACTIVE").ifPresent(admin -> {
            String prefix = "Sự kiện \"";
            String suffix = "\" vừa được gửi lên chờ duyệt.";
            String eventName = event.getName();
            int maxNameLength = 255 - prefix.length() - suffix.length();
            // Keep the message within the existing VARCHAR(255), including long event names.
            if (eventName.codePointCount(0, eventName.length()) > maxNameLength) {
                eventName = eventName.substring(0, eventName.offsetByCodePoints(0, maxNameLength - 3)) + "...";
            }
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setTitle("Có sự kiện mới chờ duyệt");
            notification.setContent(prefix + eventName + suffix);
            notification.setType("EVENT_PENDING_APPROVAL");
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
