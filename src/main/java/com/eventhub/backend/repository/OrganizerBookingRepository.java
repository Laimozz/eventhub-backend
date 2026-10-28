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
public class OrganizerBookingRepository {

    private final JdbcTemplate jdbc;

    public record BookingSummaryRow(
            Integer id,
            String status,
            BigDecimal totalAmount,
            int totalTickets,
            LocalDateTime createdAt,
            LocalDateTime expiredAt,
            Integer eventId,
            String eventName,
            Integer customerId,
            String customerName,
            String customerEmail,
            String customerPhone
    ) {}

    public record BookingOverviewRow(
            long totalBookings,
            BigDecimal totalRevenue,
            long paidCount,
            long pendingCount,
            long cancelledCount,
            long otherCount
    ) {}

    public record BookingDetailBaseRow(
            Integer id,
            String status,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            LocalDateTime expiredAt,
            Integer eventId,
            String eventName,
            String venueName,
            LocalDateTime eventStartTime,
            LocalDateTime eventEndTime,
            Integer customerId,
            String customerName,
            String customerEmail,
            String customerPhone
    ) {}

    public record BookingItemRow(
            Integer id,
            Integer ticketTypeId,
            String ticketTypeName,
            int quantity,
            BigDecimal unitPrice
    ) {}

    public record BookingTicketRow(
            Integer id,
            String ticketCode,
            String ticketTypeName,
            String status,
            LocalDateTime checkedInAt,
            String checkedInByName
    ) {}

    public record BookingPaymentRow(
            Integer id,
            String transactionId,
            String paymentMethod,
            BigDecimal amount,
            String status,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {}

    public record BookingRefundRow(
            Integer id,
            String refundTransactionId,
            BigDecimal amount,
            String status,
            LocalDateTime refundedAt
    ) {}

    public List<BookingSummaryRow> findBookings(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    b.id,
                    b.status,
                    b.total_amount,
                    COALESCE(bi_agg.tickets_count, 0) AS total_tickets,
                    b.created_at,
                    b.expired_at,
                    e.id AS event_id,
                    e.name AS event_name,
                    u.id AS customer_id,
                    u.full_name AS customer_name,
                    u.email AS customer_email,
                    u.phone AS customer_phone
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                JOIN users u ON b.users_id = u.id
                LEFT JOIN (
                    SELECT bookings_id, SUM(quantity) AS tickets_count
                    FROM booking_items
                    GROUP BY bookings_id
                ) bi_agg ON bi_agg.bookings_id = b.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        applyFilters(sql, params, eventId, status, keyword, startDate, endDate);

        sql.append(" ORDER BY b.created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbc.query(sql.toString(), (rs, rowNum) -> new BookingSummaryRow(
                rs.getInt("id"),
                rs.getString("status"),
                rs.getBigDecimal("total_amount"),
                rs.getInt("total_tickets"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("expired_at") != null ? rs.getTimestamp("expired_at").toLocalDateTime() : null,
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("customer_phone")
        ), params.toArray());
    }

    public long countBookings(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(b.id)
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                JOIN users u ON b.users_id = u.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        applyFilters(sql, params, eventId, status, keyword, startDate, endDate);

        Long count = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    public BookingOverviewRow getBookingOverview(
            Integer organizerId,
            Integer eventId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    COUNT(b.id) AS total_bookings,
                    COALESCE(SUM(CASE WHEN b.status = 'PAID' THEN b.total_amount ELSE 0 END), 0) AS total_revenue,
                    COUNT(CASE WHEN b.status = 'PAID' THEN 1 END) AS paid_count,
                    COUNT(CASE WHEN b.status = 'PENDING' THEN 1 END) AS pending_count,
                    COUNT(CASE WHEN b.status IN ('CANCELLED', 'CANCELED', 'EXPIRED') THEN 1 END) AS cancelled_count,
                    COUNT(CASE WHEN b.status NOT IN ('PAID', 'PENDING', 'CANCELLED', 'CANCELED', 'EXPIRED') THEN 1 END) AS other_count
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
        if (startDate != null && endDate != null) {
            sql.append(" AND b.created_at >= ? AND b.created_at < ?");
            params.add(startDate.atStartOfDay());
            params.add(endDate.plusDays(1).atStartOfDay());
        }

        return jdbc.queryForObject(sql.toString(), (rs, rowNum) -> new BookingOverviewRow(
                rs.getLong("total_bookings"),
                rs.getBigDecimal("total_revenue"),
                rs.getLong("paid_count"),
                rs.getLong("pending_count"),
                rs.getLong("cancelled_count"),
                rs.getLong("other_count")
        ), params.toArray());
    }

    public BookingDetailBaseRow getBookingDetailBase(Integer organizerId, Integer bookingId) {
        String sql = """
                SELECT 
                    b.id,
                    b.status,
                    b.total_amount,
                    b.created_at,
                    b.expired_at,
                    e.id AS event_id,
                    e.name AS event_name,
                    v.city || ' - ' || v.address AS venue_name,
                    e.start_time AS event_start_time,
                    e.end_time AS event_end_time,
                    u.id AS customer_id,
                    u.full_name AS customer_name,
                    u.email AS customer_email,
                    u.phone AS customer_phone
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                JOIN venues v ON e.venues_id = v.id
                JOIN users u ON b.users_id = u.id
                WHERE e.organizer_id = ?
                  AND b.id = ?
                """;

        List<BookingDetailBaseRow> rows = jdbc.query(sql, (rs, rowNum) -> new BookingDetailBaseRow(
                rs.getInt("id"),
                rs.getString("status"),
                rs.getBigDecimal("total_amount"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("expired_at") != null ? rs.getTimestamp("expired_at").toLocalDateTime() : null,
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getString("venue_name"),
                rs.getTimestamp("event_start_time") != null ? rs.getTimestamp("event_start_time").toLocalDateTime() : null,
                rs.getTimestamp("event_end_time") != null ? rs.getTimestamp("event_end_time").toLocalDateTime() : null,
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("customer_phone")
        ), organizerId, bookingId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<BookingItemRow> getBookingItems(Integer bookingId) {
        String sql = """
                SELECT 
                    bi.id,
                    bi.ticket_types_id,
                    tt.name AS ticket_type_name,
                    bi.quantity,
                    bi.unit_price
                FROM booking_items bi
                JOIN ticket_types tt ON bi.ticket_types_id = tt.id
                WHERE bi.bookings_id = ?
                ORDER BY bi.id ASC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new BookingItemRow(
                rs.getInt("id"),
                rs.getInt("ticket_types_id"),
                rs.getString("ticket_type_name"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price")
        ), bookingId);
    }

    public List<BookingTicketRow> getBookingTickets(Integer bookingId) {
        String sql = """
                SELECT 
                    t.id,
                    t.ticket_code,
                    tt.name AS ticket_type_name,
                    t.status,
                    t.checked_in_at,
                    staff.full_name AS checked_in_by_name
                FROM tickets t
                JOIN booking_items bi ON t.booking_items_id = bi.id
                JOIN ticket_types tt ON bi.ticket_types_id = tt.id
                LEFT JOIN users staff ON t.checked_in_by = staff.id
                WHERE bi.bookings_id = ?
                ORDER BY t.id ASC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new BookingTicketRow(
                rs.getInt("id"),
                rs.getString("ticket_code"),
                rs.getString("ticket_type_name"),
                rs.getString("status"),
                rs.getTimestamp("checked_in_at") != null ? rs.getTimestamp("checked_in_at").toLocalDateTime() : null,
                rs.getString("checked_in_by_name")
        ), bookingId);
    }

    public List<BookingPaymentRow> getBookingPayments(Integer bookingId) {
        String sql = """
                SELECT 
                    p.id,
                    p.transaction_id,
                    p.payment_method,
                    p.amount,
                    p.status,
                    p.paid_at,
                    p.created_at
                FROM payments p
                WHERE p.bookings_id = ?
                ORDER BY p.id DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new BookingPaymentRow(
                rs.getInt("id"),
                rs.getString("transaction_id"),
                rs.getString("payment_method"),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getTimestamp("paid_at") != null ? rs.getTimestamp("paid_at").toLocalDateTime() : null,
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
        ), bookingId);
    }

    public BookingRefundRow getBookingRefund(Integer bookingId) {
        String sql = """
                SELECT 
                    r.id,
                    r.refund_transaction_id,
                    r.amount,
                    r.status,
                    r.refunded_at
                FROM refunds r
                JOIN payments p ON r.payments_id = p.id
                WHERE p.bookings_id = ?
                ORDER BY r.id DESC
                LIMIT 1
                """;

        List<BookingRefundRow> rows = jdbc.query(sql, (rs, rowNum) -> new BookingRefundRow(
                rs.getInt("id"),
                rs.getString("refund_transaction_id"),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getTimestamp("refunded_at") != null ? rs.getTimestamp("refunded_at").toLocalDateTime() : null
        ), bookingId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<BookingSummaryRow> findAllBookingsForExport(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    b.id,
                    b.status,
                    b.total_amount,
                    COALESCE(bi_agg.tickets_count, 0) AS total_tickets,
                    b.created_at,
                    b.expired_at,
                    e.id AS event_id,
                    e.name AS event_name,
                    u.id AS customer_id,
                    u.full_name AS customer_name,
                    u.email AS customer_email,
                    u.phone AS customer_phone
                FROM bookings b
                JOIN events e ON b.events_id = e.id
                JOIN users u ON b.users_id = u.id
                LEFT JOIN (
                    SELECT bookings_id, SUM(quantity) AS tickets_count
                    FROM booking_items
                    GROUP BY bookings_id
                ) bi_agg ON bi_agg.bookings_id = b.id
                WHERE e.organizer_id = ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(organizerId);

        applyFilters(sql, params, eventId, status, keyword, startDate, endDate);

        sql.append(" ORDER BY b.created_at DESC LIMIT 2000");

        return jdbc.query(sql.toString(), (rs, rowNum) -> new BookingSummaryRow(
                rs.getInt("id"),
                rs.getString("status"),
                rs.getBigDecimal("total_amount"),
                rs.getInt("total_tickets"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("expired_at") != null ? rs.getTimestamp("expired_at").toLocalDateTime() : null,
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("customer_phone")
        ), params.toArray());
    }

    private void applyFilters(
            StringBuilder sql,
            List<Object> params,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (eventId != null) {
            sql.append(" AND e.id = ?");
            params.add(eventId);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND UPPER(b.status) = UPPER(?)");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            sql.append(" AND (CAST(b.id AS TEXT) ILIKE ? OR u.full_name ILIKE ? OR u.email ILIKE ? OR u.phone ILIKE ? OR e.name ILIKE ?)");
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (startDate != null && endDate != null) {
            sql.append(" AND b.created_at >= ? AND b.created_at < ?");
            params.add(startDate.atStartOfDay());
            params.add(endDate.plusDays(1).atStartOfDay());
        } else if (startDate != null) {
            sql.append(" AND b.created_at >= ?");
            params.add(startDate.atStartOfDay());
        } else if (endDate != null) {
            sql.append(" AND b.created_at < ?");
            params.add(endDate.plusDays(1).atStartOfDay());
        }
    }
}
