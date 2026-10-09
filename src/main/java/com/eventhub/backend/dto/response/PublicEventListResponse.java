package com.eventhub.backend.dto.response;

import java.util.List;

public record PublicEventListResponse(
        List<PublicEventSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
