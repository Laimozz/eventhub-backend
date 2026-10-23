package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.OrganizerDashboardResponses.CheckinAlertResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventPerformanceResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventStaffDispatchResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.MonthlyStatsResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.StaffDispatchSummaryResponse;

import java.util.List;

public interface OrganizerDashboardService {

    List<MonthlyStatsResponse> getMonthlyStats(Integer organizerId, String interval);

    List<EventPerformanceResponse> getEventPerformance(Integer organizerId, int limit);

    CheckinAlertResponse getCheckinAlert(Integer organizerId);

    StaffDispatchSummaryResponse getStaffDispatchSummary(Integer organizerId);

    List<EventStaffDispatchResponse> getEventStaffAllocations(Integer organizerId);
}

