package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PublicEventDetailResponse(
        Integer id,
        String name,
        String description,
        String thumbnailImageUrl,
        String bannerImageUrl,
        String imageZoneUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String organizerName,
        String categoryName,
        Venue venue,
        BigDecimal startingPrice,
        List<PublicEventSummaryResponse> suggestedEvents
) {
    public record Venue(
            String city,
            String address
    ) {
    }
}
