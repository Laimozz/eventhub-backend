package com.eventhub.backend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrganizerReportRepository {

    private final JdbcTemplate jdbc;

    public record SummaryRow(
            BigDecimal totalRevenue,
            long totalOrders,
            long totalTicketsSold,
            long totalTicketsCapacity,
            long totalCheckedIn,
            BigDecimal totalRefundAmount
    ) {}

    public record TimeSeriesRow(
            String dateKey,
            String dateLabel,
            BigDecimal revenue,
            long ordersCount,
            long ticketsSold
    ) {}

    public record EventPerformanceRow(
            Integer eventId,
            String eventName,
            String categoryName,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String venueName,
            int totalCapacity,
            int soldTickets,
            int remainingTickets,
            double fillRate,
            BigDecimal revenue,
            int checkedInTickets,
            double checkinRate
    ) {}

    public record TicketTypeRow(
            Integer ticketTypeId,
            String ticketName,
            String eventName,
            BigDecimal price,
            int totalQuantity,
            int soldQuantity,
            int remainingQuantity,
            double salesRate,
            BigDecimal revenue
    ) {}

    public record OrderStatusRow(
            String status,
            long orderCount,
            BigDecimal totalAmount
    ) {}

    public record CategoryRow(
            Integer categoryId,
            String categoryName,
            long eventCount,
            long soldTickets,
            BigDecimal revenue
    ) {}

    public record EventFilterOptionRow(
            Integer id,
            String name,
            String status,
            LocalDateTime startTime
    ) {}

    public SummaryRow getSummaryMetrics(Integer organizerId, Integer eventId, String timeRange, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    COALESCE(SUM(b.total_amount), 0) AS total_revenue,
                    COUNT(b.id) AS total_orders,
                    COALESCE(SUM(bi_agg.tickets_sold), 0) AS total_tickets_sold,
                    COALESCE((
                        SELECT SUM(tt.quantity) 
                        FROM events ev 
                        JOIN ticket_types tt ON tt.events_id = ev.id 
                        WHERE ev.organizer_id = ?
                """);
        List<Object> capParams = new ArrayList<>();
        capParams.add(organizerId);
        if (eventId != null) {
            sql.append(" AND ev.id = ?");
            capParams.add(eventId);
        }
        sql.append("""
                    ), 0) AS total_capacity,
                    COALESCE((
                        SELECT COUNT(t.id)
                        FROM events ev2
                        JOIN bookings b2 ON b2.events_id = ev2.id AND b2.status = 'PAID'
                        JOIN booking_items bi2 ON bi2.bookings_id = b2.id
                        JOIN tickets t ON t.booking_items_id = bi2.id AND (t.status = 'USED' OR t.checked_in_at IS NOT NULL)
                        WHERE ev2.organizer_id = ?
                """);
        List<Object> checkinParams = new ArrayList<>();
        checkinParams.add(organizerId);
        if (eventId != null) {
            sql.append(" AND ev2.id = ?");
            checkinParams.add(eventId);
        }
        appendDateFilterToSubquery(sql, checkinParams, "b2.created_at", timeRange, startDate, endDate);

        sql.append("""
                    ), 0) AS total_checked_in,
                    COALESCE((
                        SELECT SUM(r.amount)
                        FROM events ev3
                        JOIN bookings b3 ON b3.events_id = ev3.id
                        JOIN payments p3 ON p3.bookings_id = b3.id
                        JOIN refunds r ON r.payments_id = p3.id AND r.status IN ('COMPLETED', 'SUCCESS')
                        WHERE ev3.organizer_id = ?
                """);
        List<Object> refundParams = new ArrayList<>();
        refundParams.add(organizerId);
        if (eventId != null) {
            sql.append(" AND ev3.id = ?");
            refundParams.add(eventId);
        }
        appendDateFilterToSubquery(sql, refundParams, "b3.created_at", timeRange, startDate, endDate);

        sql.append("""
                    ), 0) AS total_refund_amount
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                LEFT JOIN (
                    SELECT bookings_id, SUM(quantity) AS tickets_sold
                    FROM booking_items
                    GROUP BY bookings_id
                ) bi_agg ON bi_agg.bookings_id = b.id
                WHERE e.organizer_id = ?
                  AND b.status = 'PAID'
                """);

        List<Object> mainParams = new ArrayList<>();
        mainParams.addAll(capParams);
        mainParams.addAll(checkinParams);
        mainParams.addAll(refundParams);
        mainParams.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            mainParams.add(eventId);
        }
        appendDateFilterToSubquery(sql, mainParams, "b.created_at", timeRange, startDate, endDate);

        return jdbc.queryForObject(sql.toString(), (rs, rowNum) -> new SummaryRow(
                rs.getBigDecimal("total_revenue"),
                rs.getLong("total_orders"),
                rs.getLong("total_tickets_sold"),
                rs.getLong("total_capacity"),
                rs.getLong("total_checked_in"),
                rs.getBigDecimal("total_refund_amount")
        ), mainParams.toArray());
    }

    public List<TimeSeriesRow> getTimeSeriesTrend(Integer organizerId, Integer eventId, String timeRange, LocalDate startDate, LocalDate endDate) {
        boolean isDaily = "7d".equalsIgnoreCase(timeRange) || "30d".equalsIgnoreCase(timeRange)
                || (startDate != null && endDate != null && java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) <= 62);

        String dateKeyFormat = isDaily ? "YYYY-MM-DD" : "YYYY-MM";
        String dateLabelExpr = isDaily ? "TO_CHAR(b.created_at, 'DD/MM')" : "'T' || TO_CHAR(b.created_at, 'MM/YY')";

        StringBuilder sql = new StringBuilder("""
                SELECT 
                    TO_CHAR(b.created_at, '""" + dateKeyFormat + """
                    ') AS date_key,
                    """ + dateLabelExpr + """
                     AS date_label,
                    COALESCE(SUM(b.total_amount), 0) AS revenue,
                    COUNT(b.id) AS orders_count,
                    COALESCE(SUM(bi_agg.tickets_sold), 0) AS tickets_sold
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                LEFT JOIN (
                    SELECT bookings_id, SUM(quantity) AS tickets_sold
                    FROM booking_items
                    GROUP BY bookings_id
                ) bi_agg ON bi_agg.bookings_id = b.id
                WHERE e.organizer_id = ?
                  AND b.status = 'PAID'
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }
        appendDateFilterToSubquery(sql, params, "b.created_at", timeRange, startDate, endDate);

        sql.append("""
                GROUP BY date_key, date_label
                ORDER BY date_key ASC
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new TimeSeriesRow(
                rs.getString("date_key"),
                rs.getString("date_label"),
                rs.getBigDecimal("revenue"),
                rs.getLong("orders_count"),
                rs.getLong("tickets_sold")
        ), params.toArray());
    }

    public List<EventPerformanceRow> getEventPerformanceList(Integer organizerId, Integer eventId) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    e.id AS event_id,
                    e.name AS event_name,
                    c.name AS category_name,
                    e.status AS event_status,
                    e.start_time,
                    e.end_time,
                    v.city || ' - ' || v.address AS venue_name,
                    COALESCE(SUM(tt.quantity), 0) AS total_capacity,
                    COALESCE(SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity), 0) AS sold_tickets,
                    COALESCE(SUM(tt.remaining_quantity), 0) AS remaining_tickets,
                    CASE WHEN SUM(tt.quantity) > 0 
                         THEN ROUND((SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity)::DECIMAL / SUM(tt.quantity)) * 100, 1) 
                         ELSE 0 END AS fill_rate,
                    COALESCE(SUM((tt.quantity - tt.remaining_quantity - tt.reserved_quantity) * tt.price), 0) AS revenue,
                    COALESCE(ci.checked_in_count, 0) AS checked_in_tickets,
                    CASE WHEN SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity) > 0
                         THEN ROUND((COALESCE(ci.checked_in_count, 0)::DECIMAL / NULLIF(SUM(tt.quantity - tt.remaining_quantity - tt.reserved_quantity), 0)) * 100, 1)
                         ELSE 0 END AS checkin_rate
                FROM events e
                JOIN categories c ON e.categories_id = c.id
                JOIN venues v ON e.venues_id = v.id
                LEFT JOIN ticket_types tt ON e.id = tt.events_id
                LEFT JOIN (
                    SELECT b.events_id, COUNT(t.id) AS checked_in_count
                    FROM bookings b
                    JOIN booking_items bi ON bi.bookings_id = b.id
                    JOIN tickets t ON t.booking_items_id = bi.id AND (t.status = 'USED' OR t.checked_in_at IS NOT NULL)
                    WHERE b.status = 'PAID'
                    GROUP BY b.events_id
                ) ci ON ci.events_id = e.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }

        sql.append("""
                GROUP BY e.id, e.name, c.name, e.status, e.start_time, e.end_time, v.city, v.address, ci.checked_in_count
                ORDER BY revenue DESC, sold_tickets DESC
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new EventPerformanceRow(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("category_name"),
                rs.getString("event_status"),
                rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null,
                rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null,
                rs.getString("venue_name"),
                rs.getInt("total_capacity"),
                rs.getInt("sold_tickets"),
                rs.getInt("remaining_tickets"),
                rs.getDouble("fill_rate"),
                rs.getBigDecimal("revenue"),
                rs.getInt("checked_in_tickets"),
                rs.getDouble("checkin_rate")
        ), params.toArray());
    }

    public List<TicketTypeRow> getTicketTypePerformanceList(Integer organizerId, Integer eventId) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    tt.id AS ticket_type_id,
                    tt.name AS ticket_name,
                    e.name AS event_name,
                    tt.price,
                    tt.quantity AS total_quantity,
                    (tt.quantity - tt.remaining_quantity - tt.reserved_quantity) AS sold_quantity,
                    tt.remaining_quantity,
                    CASE WHEN tt.quantity > 0 
                         THEN ROUND(((tt.quantity - tt.remaining_quantity - tt.reserved_quantity)::DECIMAL / tt.quantity) * 100, 1)
                         ELSE 0 END AS sales_rate,
                    ((tt.quantity - tt.remaining_quantity - tt.reserved_quantity) * tt.price) AS revenue
                FROM ticket_types tt
                JOIN events e ON tt.events_id = e.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }

        sql.append(" ORDER BY revenue DESC, sold_quantity DESC");

        return jdbc.query(sql.toString(), (rs, rowNum) -> new TicketTypeRow(
                rs.getInt("ticket_type_id"),
                rs.getString("ticket_name"),
                rs.getString("event_name"),
                rs.getBigDecimal("price"),
                rs.getInt("total_quantity"),
                rs.getInt("sold_quantity"),
                rs.getInt("remaining_quantity"),
                rs.getDouble("sales_rate"),
                rs.getBigDecimal("revenue")
        ), params.toArray());
    }

    public List<OrderStatusRow> getOrderStatusBreakdown(Integer organizerId, Integer eventId, String timeRange, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    b.status,
                    COUNT(b.id) AS order_count,
                    COALESCE(SUM(b.total_amount), 0) AS total_amount
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }
        appendDateFilterToSubquery(sql, params, "b.created_at", timeRange, startDate, endDate);

        sql.append("""
                GROUP BY b.status
                ORDER BY order_count DESC
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new OrderStatusRow(
                rs.getString("status"),
                rs.getLong("order_count"),
                rs.getBigDecimal("total_amount")
        ), params.toArray());
    }

    public List<CategoryRow> getCategoryBreakdown(Integer organizerId, Integer eventId, String timeRange, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    c.id AS category_id,
                    c.name AS category_name,
                    COUNT(DISTINCT e.id) AS event_count,
                    COALESCE(SUM(bi_agg.tickets_sold), 0) AS sold_tickets,
                    COALESCE(SUM(b.total_amount), 0) AS revenue
                FROM categories c
                JOIN events e ON e.categories_id = c.id
                LEFT JOIN bookings b ON b.events_id = e.id AND b.status = 'PAID'
                LEFT JOIN (
                    SELECT bookings_id, SUM(quantity) AS tickets_sold
                    FROM booking_items
                    GROUP BY bookings_id
                ) bi_agg ON bi_agg.bookings_id = b.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }
        if (timeRange != null || startDate != null || endDate != null) {
            StringBuilder dateSub = new StringBuilder();
            appendDateFilterToSubquery(dateSub, params, "b.created_at", timeRange, startDate, endDate);
            if (dateSub.length() > 0) {
                // when date filter applies, b must match or b is null
                sql.append(dateSub);
            }
        }

        sql.append("""
                GROUP BY c.id, c.name
                ORDER BY revenue DESC, sold_tickets DESC
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new CategoryRow(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getLong("event_count"),
                rs.getLong("sold_tickets"),
                rs.getBigDecimal("revenue")
        ), params.toArray());
    }

    public List<EventFilterOptionRow> getOrganizerEventsForFilter(Integer organizerId) {
        String sql = """
                SELECT id, name, status, start_time
                FROM events
                WHERE organizer_id = ?
                ORDER BY created_at DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new EventFilterOptionRow(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("status"),
                rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null
        ), organizerId);
    }

    private void appendDateFilterToSubquery(StringBuilder sql, List<Object> params, String column, String timeRange, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            sql.append(" AND ").append(column).append(" >= ? AND ").append(column).append(" < ?");
            params.add(startDate.atStartOfDay());
            params.add(endDate.plusDays(1).atStartOfDay());
        } else if (startDate != null) {
            sql.append(" AND ").append(column).append(" >= ?");
            params.add(startDate.atStartOfDay());
        } else if (endDate != null) {
            sql.append(" AND ").append(column).append(" < ?");
            params.add(endDate.plusDays(1).atStartOfDay());
        } else if (timeRange != null) {
            switch (timeRange.toLowerCase()) {
                case "today" -> sql.append(" AND ").append(column).append(" >= DATE_TRUNC('day', CURRENT_DATE)");
                case "7d" -> sql.append(" AND ").append(column).append(" >= CURRENT_DATE - INTERVAL '7 day'");
                case "30d" -> sql.append(" AND ").append(column).append(" >= CURRENT_DATE - INTERVAL '30 day'");
                case "this_month" -> sql.append(" AND ").append(column).append(" >= DATE_TRUNC('month', CURRENT_DATE)");
                case "last_month" -> sql.append(" AND ").append(column).append(" >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month') AND ").append(column).append(" < DATE_TRUNC('month', CURRENT_DATE)");
                case "3m" -> sql.append(" AND ").append(column).append(" >= CURRENT_DATE - INTERVAL '3 month'");
                case "6m" -> sql.append(" AND ").append(column).append(" >= CURRENT_DATE - INTERVAL '6 month'");
                case "year" -> sql.append(" AND ").append(column).append(" >= DATE_TRUNC('year', CURRENT_DATE)");
                case "all" -> { /* no filter */ }
                default -> sql.append(" AND ").append(column).append(" >= CURRENT_DATE - INTERVAL '30 day'");
            }
        }
    }
}
