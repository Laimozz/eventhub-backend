package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record EventListResponse(List<EventSummaryResponse> content, int page, int size,
        long totalElements, int totalPages, Map<EventStatus, Long> statusCounts) {

    public record EventSummaryResponse(Integer id, String name, String description, String thumbnailImageUrl,
            String categoryName, String city, String address, LocalDateTime startTime, LocalDateTime endTime,
            EventStatus status, boolean canEdit, boolean canCancel) {
        public static EventSummaryResponse from(Event event, LocalDateTime now) {
            return new EventSummaryResponse(event.getId(), event.getName(), event.getDescription(),
                    event.getThumbnailImageUrl(), event.getCategory().getName(), event.getVenue().getCity(),
                    event.getVenue().getAddress(), event.getStartTime(), event.getEndTime(), event.getStatus(),
                    EventResponse.canEdit(event, now), EventResponse.canCancel(event, now));
        }
    }
}
