package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PublicEventSummaryResponse(
        Integer id,
        String name,
        String thumbnailImageUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String city,
        BigDecimal startingPrice
) {
}
