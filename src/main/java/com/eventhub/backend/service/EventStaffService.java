package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.AddEventStaffRequest;
import com.eventhub.backend.dto.request.UpdateEventStaffStatusRequest;
import com.eventhub.backend.dto.response.EventStaffResponse;
import java.util.List;

public interface EventStaffService {
    List<EventStaffResponse> getStaffs(Integer organizerId, Integer eventId);

    EventStaffResponse addStaff(Integer organizerId, Integer eventId, AddEventStaffRequest request);

    EventStaffResponse updateStaffStatus(Integer organizerId, Integer eventId, Integer staffId,
            UpdateEventStaffStatusRequest request);

    void deleteStaff(Integer organizerId, Integer eventId, Integer staffId);
}
