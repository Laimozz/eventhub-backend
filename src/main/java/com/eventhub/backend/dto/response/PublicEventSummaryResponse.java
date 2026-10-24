package com.eventhub.backend.dto.response;

import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.TicketSalesStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicEventSummaryResponse(
        Integer id,
        String name,
        String thumbnailImageUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String city,
        BigDecimal startingPrice,
        EventStatus status,
        TicketSalesStatus salesStatus
) {
}
