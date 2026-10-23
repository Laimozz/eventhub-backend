package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.response.OrganizerDashboardResponses;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.CheckinAlertResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventPerformanceResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventStaffDispatchResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.MonthlyStatsResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.StaffDispatchSummaryResponse;

import com.eventhub.backend.repository.OrganizerDashboardRepository;
import com.eventhub.backend.repository.OrganizerDashboardRepository.CheckinAlertRow;
import com.eventhub.backend.service.OrganizerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerDashboardServiceImpl implements OrganizerDashboardService {

    private final OrganizerDashboardRepository repository;

    @Override
    public List<MonthlyStatsResponse> getMonthlyStats(Integer organizerId, String interval) {
        var rows = repository.getMonthlyStats(organizerId, interval);
        return rows.stream().map(r -> new MonthlyStatsResponse(
                r.monthKey(),
                r.monthLabel(),
                r.totalOrders(),
                r.grossRevenue(),
                r.netRevenue(),
                formatVnd(r.grossRevenue()),
                formatVnd(r.netRevenue())
        )).toList();
    }

    @Override
    public List<EventPerformanceResponse> getEventPerformance(Integer organizerId, int limit) {
        var rows = repository.getEventPerformance(organizerId, limit <= 0 ? 5 : limit);
        return rows.stream().map(r -> new EventPerformanceResponse(
                r.eventId(),
                r.eventName(),
                r.eventStatus(),
                r.totalTickets(),
                r.soldTickets(),
                r.remainingTickets(),
                r.fillRatePercent(),
                r.revenue(),
                formatVnd(r.revenue())
        )).toList();
    }

    @Override
    public CheckinAlertResponse getCheckinAlert(Integer organizerId) {
        CheckinAlertRow row = repository.getLatestCheckinAlert(organizerId);
        if (row == null || row.totalTickets() == 0) {
            return new CheckinAlertResponse(null, "Không có sự kiện diễn ra", 0, 0, 0.0, false, "Tất cả các cổng soát vé đang ổn định");
        }

        boolean hasAlert = row.checkinRatePercent() < 80.0;
        String alertMessage = String.format(Locale.ROOT,
                "Cảnh báo tiến độ check-in: %.1f%% (Đã soát: %d / %d khách) • Cần theo dõi cổng",
                row.checkinRatePercent(), row.checkedInTickets(), row.totalTickets());

        return new CheckinAlertResponse(
                row.eventId(),
                row.eventName(),
                row.totalTickets(),
                row.checkedInTickets(),
                row.checkinRatePercent(),
                hasAlert,
                alertMessage
        );
    }

    @Override
    public OrganizerDashboardResponses.StaffDispatchSummaryResponse getStaffDispatchSummary(Integer organizerId) {
        var row = repository.getStaffDispatchStats(organizerId);
        if (row == null) {
            return new OrganizerDashboardResponses.StaffDispatchSummaryResponse(0, 0, 0, 0.0);
        }
        return new OrganizerDashboardResponses.StaffDispatchSummaryResponse(
                row.totalEventsNeedingStaff(),
                row.totalAssignedStaff(),
                row.activeStaff(),
                row.avgCheckinRate()
        );
    }

    @Override
    public List<OrganizerDashboardResponses.EventStaffDispatchResponse> getEventStaffAllocations(Integer organizerId) {
        var rows = repository.getEventStaffAllocations(organizerId);
        return rows.stream().map(r -> new OrganizerDashboardResponses.EventStaffDispatchResponse(
                r.eventId(),
                r.eventName(),
                r.eventStatus(),
                r.assignedStaffCount(),
                r.activeStaffCount(),
                r.staffNames()
        )).toList();
    }

    private static String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 đ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount) + " đ";
    }
}

