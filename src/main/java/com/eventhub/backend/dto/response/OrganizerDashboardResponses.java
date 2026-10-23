package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public final class OrganizerDashboardResponses {

    private OrganizerDashboardResponses() {}

    public record MonthlyStatsResponse(
            String monthKey,
            String monthLabel,
            long totalOrders,
            BigDecimal grossRevenue,
            BigDecimal netRevenue,
            String formattedGrossRevenue,
            String formattedNetRevenue
    ) {}

    public record EventPerformanceResponse(
            Integer eventId,
            String eventName,
            String eventStatus,
            int totalTickets,
            int soldTickets,
            int remainingTickets,
            double fillRatePercent,
            BigDecimal revenue,
            String formattedRevenue
    ) {}

    public record CheckinAlertResponse(
            Integer eventId,
            String eventName,
            int totalTickets,
            int checkedInTickets,
            double checkinRatePercent,
            boolean hasAlert,
            String alertMessage
    ) {}

    public record OrganizerDashboardData(
            List<MonthlyStatsResponse> monthlyStats,
            List<EventPerformanceResponse> eventPerformances,
            CheckinAlertResponse checkinAlert
    ) {}

    public record StaffDispatchSummaryResponse(
            long totalEventsNeedingStaff,
            long totalAssignedStaff,
            long activeStaff,
            double avgCheckinRate
    ) {}

    public record EventStaffDispatchResponse(
            Integer eventId,
            String eventName,
            String eventStatus,
            int assignedStaffCount,
            int activeStaffCount,
            String staffNames
    ) {}
}

