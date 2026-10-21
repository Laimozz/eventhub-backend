package com.eventhub.backend.dto.response;

import java.time.LocalDateTime;

public record StaffAssignedEventResponse(
        Integer assignmentId,
        Integer eventId,
        String eventName,
        String description,
        String thumbnailImageUrl,
        String bannerImageUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String eventStatus,
        String venueCity,
        String venueAddress,
        String staffStatus,
        LocalDateTime assignedAt,
        String organizerName,
        long totalTickets,
        long checkedInTickets
) {}
