package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.EventGuest;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.enums.EventStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Integer id, Integer organizerId, Integer categoryId, String name, String description,
        String thumbnailImageUrl, String bannerImageUrl, String imageZoneUrl,
        LocalDateTime startTime, LocalDateTime endTime, EventStatus status,
        LocalDateTime createdAt, VenueResponse venue,
        List<TicketTypeResponse> ticketTypes, List<GuestResponse> guests,
        String categoryName, String cancelReason, LocalDateTime canceledAt, boolean canEdit, boolean canCancel,
        String rejectReason) {

    public static EventResponse from(Event event, List<TicketType> ticketTypes, List<EventGuest> guests,
            LocalDateTime now) {
        var venue = event.getVenue();
        return new EventResponse(event.getId(), event.getOrganizer().getId(), event.getCategory().getId(),
                event.getName(), event.getDescription(), event.getThumbnailImageUrl(), event.getBannerImageUrl(),
                event.getImageZoneUrl(), event.getStartTime(), event.getEndTime(), event.getStatus(),
                event.getCreatedAt(), new VenueResponse(venue.getId(), venue.getCity(), venue.getAddress(),
                        venue.getCapacity()),
                ticketTypes.stream().map(TicketTypeResponse::from).toList(),
                guests.stream().map(GuestResponse::from).toList(), event.getCategory().getName(),
                event.getCancelReason(), event.getCanceledAt(), canEdit(event, now), canCancel(event, now),
                event.getRejectReason());
    }

    public static boolean canEdit(Event event, LocalDateTime now) {
        return (event.getStatus() == EventStatus.PENDING_APPROVAL || event.getStatus() == EventStatus.APPROVED)
                && event.getStartTime().isAfter(now);
    }

    public static boolean canCancel(Event event, LocalDateTime now) {
        return (event.getStatus() == EventStatus.PENDING_APPROVAL || event.getStatus() == EventStatus.APPROVED
                || event.getStatus() == EventStatus.ONGOING) && event.getEndTime().isAfter(now);
    }

    public record VenueResponse(Integer id, String city, String address, Integer capacity) {
    }

    public record TicketTypeResponse(
            Integer id, String name, String description, String imageUrl, BigDecimal price,
            Integer quantity, Integer reservedQuantity, Integer remainingQuantity,
            LocalDateTime saleStartTime, LocalDateTime saleEndTime, String status) {

        static TicketTypeResponse from(TicketType ticketType) {
            return new TicketTypeResponse(ticketType.getId(), ticketType.getName(), ticketType.getDescription(),
                    ticketType.getImageUrl(), ticketType.getPrice(), ticketType.getQuantity(),
                    ticketType.getReservedQuantity(), ticketType.getRemainingQuantity(),
                    ticketType.getSaleStartTime(), ticketType.getSaleEndTime(), ticketType.getStatus());
        }
    }

    public record GuestResponse(Integer id, String name, String role, String description, String imageUrl) {
        static GuestResponse from(EventGuest guest) {
            return new GuestResponse(guest.getId(), guest.getName(), guest.getRole(),
                    guest.getDescription(), guest.getImageUrl());
        }
    }
}
