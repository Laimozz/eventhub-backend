package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.EventStatisticResponse;

public interface EventStatisticService {
    EventStatisticResponse getEventStatistics(Integer organizerId, Integer eventId);
}
