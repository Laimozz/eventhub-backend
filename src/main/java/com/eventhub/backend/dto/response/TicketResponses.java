package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class TicketResponses {
    private TicketResponses() {}

    public record PageResponse(List<TicketResponse> items, int page, int size, long totalElements, int totalPages) {}
    public record TicketResponse(Integer id, Integer bookingId, String ticketCode, String qrToken, String status,
                                 String ticketTypeName, BigDecimal unitPrice, String ownerName,
                                 LocalDateTime checkedInAt, boolean canCheckIn, BookingResponses.EventInfo event) {}
}
