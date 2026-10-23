package com.eventhub.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrganizerDashboardRepository {

    private final JdbcTemplate jdbc;

    public record MonthlyRevenueRow(
            String monthKey,
            String monthLabel,
            long totalOrders,
            BigDecimal grossRevenue,
            BigDecimal netRevenue
    ) {}

    public record EventPerformanceRow(
            Integer eventId,
            String eventName,
            String eventStatus,
            int totalTickets,
            int soldTickets,
            int remainingTickets,
            double fillRatePercent,
            BigDecimal revenue
    ) {}

    public record CheckinAlertRow(
            Integer eventId,
            String eventName,
            int totalTickets,
            int checkedInTickets,
            double checkinRatePercent
    ) {}

    public List<MonthlyRevenueRow> getMonthlyStats(Integer organizerId, String interval) {
        String dateFilter = switch (interval != null ? interval.toLowerCase() : "6m") {
            case "year" -> "AND b.created_at >= DATE_TRUNC('year', CURRENT_DATE)";
            case "all" -> "";
            default -> "AND b.created_at >= CURRENT_DATE - INTERVAL '6 month'";
        };

        String sql = """
                SELECT 
                    TO_CHAR(b.created_at, 'YYYY-MM') AS month_key,
                    'T' || TO_CHAR(b.created_at, 'MM/YY') AS month_label,
                    COUNT(b.id) AS total_orders,
                    COALESCE(SUM(b.total_amount), 0) AS gross_revenue,
                    COALESCE(SUM(b.total_amount) * 0.95, 0) AS net_revenue
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                WHERE e.organizer_id = ?
                  AND b.status = 'PAID'
                """ + " " + dateFilter + " " + """
                GROUP BY TO_CHAR(b.created_at, 'YYYY-MM'), TO_CHAR(b.created_at, 'MM/YY')
                ORDER BY month_key ASC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new MonthlyRevenueRow(
                rs.getString("month_key"),
                rs.getString("month_label"),
                rs.getLong("total_orders"),
                rs.getBigDecimal("gross_revenue"),
                rs.getBigDecimal("net_revenue")
        ), organizerId);
    }

    public List<EventPerformanceRow> getEventPerformance(Integer organizerId, int limit) {
        String sql = """
                SELECT 
                    e.id AS event_id,
                    e.name AS event_name,
                    e.status AS event_status,
                    COALESCE(SUM(tt.quantity), 0) AS total_tickets,
                    COALESCE(SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity), 0) AS sold_tickets,
                    COALESCE(SUM(tt.remaining_quantity), 0) AS remaining_tickets,
                    CASE WHEN SUM(tt.quantity) > 0 
                         THEN ROUND((SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity)::DECIMAL / SUM(tt.quantity)) * 100, 1) 
                         ELSE 0 END AS fill_rate_percent,
                    COALESCE(SUM((tt.quantity - tt.remaining_quantity - tt.reserved_quantity) * tt.price), 0) AS revenue
                FROM events e
                JOIN ticket_types tt ON e.id = tt.events_id
                WHERE e.organizer_id = ?
                GROUP BY e.id, e.name, e.status
                ORDER BY sold_tickets DESC
                LIMIT ?
                """;

        return jdbc.query(sql, (rs, rowNum) -> new EventPerformanceRow(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("event_status"),
                rs.getInt("total_tickets"),
                rs.getInt("sold_tickets"),
                rs.getInt("remaining_tickets"),
                rs.getDouble("fill_rate_percent"),
                rs.getBigDecimal("revenue")
        ), organizerId, limit);
    }

    public CheckinAlertRow getLatestCheckinAlert(Integer organizerId) {
        String sql = """
                SELECT 
                    e.id AS event_id,
                    e.name AS event_name,
                    COUNT(t.id) AS total_tickets,
                    COUNT(t.checked_in_at) AS checked_in_tickets,
                    ROUND((COUNT(t.checked_in_at)::DECIMAL / NULLIF(COUNT(t.id), 0)) * 100, 1) AS checkin_rate_percent
                FROM events e
                JOIN bookings b ON b.events_id = e.id AND b.status = 'PAID'
                JOIN booking_items bi ON bi.bookings_id = b.id
                JOIN tickets t ON t.booking_items_id = bi.id AND t.status != 'CANCELED'
                WHERE e.organizer_id = ?
                  AND e.status IN ('APPROVED', 'ONGOING')
                GROUP BY e.id, e.name
                ORDER BY checkin_rate_percent ASC
                LIMIT 1
                """;

        List<CheckinAlertRow> rows = jdbc.query(sql, (rs, rowNum) -> new CheckinAlertRow(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getInt("total_tickets"),
                rs.getInt("checked_in_tickets"),
                rs.getDouble("checkin_rate_percent")
        ), organizerId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public record StaffDispatchSummaryRow(
            long totalEventsNeedingStaff,
            long totalAssignedStaff,
            long activeStaff,
            double avgCheckinRate
    ) {}

    public record EventStaffAllocationRow(
            Integer eventId,
            String eventName,
            String eventStatus,
            int assignedStaffCount,
            int activeStaffCount,
            String staffNames
    ) {}

    public StaffDispatchSummaryRow getStaffDispatchStats(Integer organizerId) {
        String sql = """
                SELECT 
                    COUNT(DISTINCT e.id) AS total_events,
                    COUNT(DISTINCT es.id) AS total_assigned_staff,
                    COUNT(DISTINCT CASE WHEN es.status = 'ACTIVE' THEN es.id END) AS active_staff,
                    COALESCE(
                        ROUND((COUNT(DISTINCT CASE WHEN t.checked_in_at IS NOT NULL THEN t.id END)::DECIMAL / 
                               NULLIF(COUNT(DISTINCT t.id), 0)) * 100, 1),
                        0.0
                    ) AS avg_checkin_rate
                FROM events e
                LEFT JOIN event_staffs es ON e.id = es.events_id
                LEFT JOIN bookings b ON b.events_id = e.id AND b.status = 'PAID'
                LEFT JOIN booking_items bi ON bi.bookings_id = b.id
                LEFT JOIN tickets t ON t.booking_items_id = bi.id AND t.status != 'CANCELED'
                WHERE e.organizer_id = ?
                  AND e.status IN ('APPROVED', 'ONGOING', 'COMPLETED')
                """;

        return jdbc.queryForObject(sql, (rs, rowNum) -> new StaffDispatchSummaryRow(
                rs.getLong("total_events"),
                rs.getLong("total_assigned_staff"),
                rs.getLong("active_staff"),
                rs.getDouble("avg_checkin_rate")
        ), organizerId);
    }

    public List<EventStaffAllocationRow> getEventStaffAllocations(Integer organizerId) {
        String sql = """
                SELECT 
                    e.id AS event_id,
                    e.name AS event_name,
                    e.status AS event_status,
                    COUNT(es.id) AS assigned_staff_count,
                    COUNT(CASE WHEN es.status = 'ACTIVE' THEN 1 END) AS active_staff_count,
                    COALESCE(STRING_AGG(DISTINCT u.full_name, ', '), '') AS staff_names
                FROM events e
                LEFT JOIN event_staffs es ON e.id = es.events_id
                LEFT JOIN users u ON es.staff_id = u.id
                WHERE e.organizer_id = ?
                GROUP BY e.id, e.name, e.status, e.start_time
                ORDER BY e.start_time DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new EventStaffAllocationRow(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("event_status"),
                rs.getInt("assigned_staff_count"),
                rs.getInt("active_staff_count"),
                rs.getString("staff_names")
        ), organizerId);
    }
}

