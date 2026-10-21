package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EventStatisticResponse(
        Integer eventId,
        String eventName,
        Integer totalSoldTickets,
        Integer totalTickets,
        Integer totalRemainingTickets,
        String soldRatioFormatted,
        BigDecimal totalRevenue,
        Double overallSalesRate,
        String formattedOverallSalesRate,
        List<TicketTypeStatisticResponse> ticketTypes
) {
    public record TicketTypeStatisticResponse(
            Integer ticketTypeId,
            String name,
            BigDecimal price,
            Integer totalQuantity,
            Integer soldQuantity,
            Integer remainingQuantity,
            Double salesRate,
            String formattedSalesRate,
            BigDecimal revenue
    ) {}
}
