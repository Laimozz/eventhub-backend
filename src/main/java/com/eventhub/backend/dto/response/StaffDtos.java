package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class StaffDtos {
    private StaffDtos() {}

    public record StaffEventDetailDto(
            Integer eventId,
            String eventName,
            String description,
            String thumbnailImageUrl,
            String bannerImageUrl,
            String imageZoneUrl,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String eventStatus,
            String venueCity,
            String venueAddress,
            Integer venueCapacity,
            String organizerName,
            String staffStatus,
            LocalDateTime assignedAt
    ) {}

    public record StaffCustomerDto(
            Integer id,
            String fullName,
            String email,
            String phone,
            Long ticketCount
    ) {}

    public record StaffTicketTypeDto(
            Integer id,
            String name,
            BigDecimal price,
            Integer quantity
    ) {}

    public record StaffTicketStatsDto(
            Long totalTickets,
            Long checkedInTickets,
            Long remainingTickets,
            Double checkInRate
    ) {}

    public record StaffTicketDto(
            Integer ticketId,
            String ticketCode,
            String status,
            LocalDateTime checkedInAt,
            Integer customerId,
            String customerName,
            String customerEmail,
            String customerPhone,
            Integer ticketTypeId,
            String ticketTypeName,
            BigDecimal ticketPrice,
            String checkedInByName
    ) {}

    public record StaffTicketPageResponse(
            List<StaffTicketDto> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record StaffCheckInResultDto(
            boolean success,
            String message,
            Integer ticketId,
            String ticketCode,
            String attendeeName,
            String ticketType,
            LocalDateTime checkedInAt
    ) {}
}
