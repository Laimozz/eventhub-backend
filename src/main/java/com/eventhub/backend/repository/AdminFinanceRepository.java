package com.eventhub.backend.repository;

import com.eventhub.backend.dto.response.AdminFinanceResponses.OverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminFinanceRepository {
    private final JdbcTemplate jdbc;

    public record AdminMonthlyStatsRow(
            String monthKey,
            String monthLabel,
            long totalTransactions,
            BigDecimal grossRevenue,
            BigDecimal netRevenue
    ) {}

    public record EventStatusBreakdownRow(
            String status,
            long totalEvents,
            double percentage
    ) {}

    public record CategoryStatsRow(
            Integer categoryId,
            String categoryName,
            long totalEvents,
            long totalOrders,
            BigDecimal grossRevenue
    ) {}

    public OverviewResponse overview() {
        // One statement gives all aggregates the same PostgreSQL snapshot.
        return jdbc.queryForObject("""
                select (select count(*) from events) as total_events,
                       (select count(*) from users) as total_users,
                       (select coalesce(sum(cast(quantity as bigint) - remaining_quantity - reserved_quantity), 0)
                        from ticket_types) as tickets_sold,
                       coalesce(sum(pending_balance), 0) as pending_balance,
                       coalesce(sum(available_balance), 0) as available_balance,
                       coalesce(sum(withdrawal_pending_balance), 0) as withdrawal_pending_balance,
                       coalesce(sum(total_withdrawn), 0) as total_withdrawn,
                       (select coalesce(sum(amount),0) from payments where status='SUCCESS') as gross_revenue,
                       (select coalesce(sum(amount),0) from refunds where status='COMPLETED') as total_refunded,
                       (select coalesce(sum(platform_fee),0) from payment_finances where not reversed) as net_revenue
                from organizer_wallets
                """, (rs, row) -> new OverviewResponse(rs.getLong("total_events"), rs.getLong("total_users"),
                        rs.getLong("tickets_sold"), rs.getBigDecimal("pending_balance"), rs.getBigDecimal("available_balance"),
                        rs.getBigDecimal("withdrawal_pending_balance"), rs.getBigDecimal("total_withdrawn"),
                        rs.getBigDecimal("gross_revenue"), rs.getBigDecimal("net_revenue"), null, rs.getBigDecimal("total_refunded")));
    }

    public List<AdminMonthlyStatsRow> getMonthlyStats(int months) {
        String sql = """
                SELECT 
                    TO_CHAR(paid_at, 'YYYY-MM') AS month_key,
                    'T' || TO_CHAR(paid_at, 'MM/YY') AS month_label,
                    COUNT(id) AS total_transactions,
                    COALESCE(SUM(amount), 0) AS gross_revenue,
                    COALESCE(SUM(amount) * 0.05, 0) AS net_revenue
                FROM payments
                WHERE status = 'SUCCESS'
                  AND paid_at >= CURRENT_DATE - (? || ' month')::INTERVAL
                GROUP BY TO_CHAR(paid_at, 'YYYY-MM'), TO_CHAR(paid_at, 'MM/YY')
                ORDER BY month_key ASC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new AdminMonthlyStatsRow(
                rs.getString("month_key"),
                rs.getString("month_label"),
                rs.getLong("total_transactions"),
                rs.getBigDecimal("gross_revenue"),
                rs.getBigDecimal("net_revenue")
        ), String.valueOf(months > 0 ? months : 6));
    }

    public List<EventStatusBreakdownRow> getEventStatusBreakdown() {
        String sql = """
                SELECT 
                    status,
                    COUNT(id) AS total_events,
                    COALESCE(ROUND((COUNT(id)::DECIMAL / NULLIF((SELECT COUNT(*) FROM events), 0)) * 100, 1), 0.0) AS percentage
                FROM events
                GROUP BY status
                ORDER BY total_events DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new EventStatusBreakdownRow(
                rs.getString("status"),
                rs.getLong("total_events"),
                rs.getDouble("percentage")
        ));
    }

    public List<CategoryStatsRow> getCategoryStats() {
        String sql = """
                SELECT 
                    c.id AS category_id,
                    c.name AS category_name,
                    COUNT(DISTINCT e.id) AS total_events,
                    COUNT(b.id) AS total_orders,
                    COALESCE(SUM(b.total_amount), 0) AS gross_revenue
                FROM categories c
                LEFT JOIN events e ON c.id = e.categories_id
                LEFT JOIN bookings b ON e.id = b.events_id AND b.status = 'PAID'
                GROUP BY c.id, c.name
                ORDER BY gross_revenue DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new CategoryStatsRow(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getLong("total_events"),
                rs.getLong("total_orders"),
                rs.getBigDecimal("gross_revenue")
        ));
    }

    public record RefundReconciliationRow(
            Integer eventId,
            String eventName,
            long totalPayments,
            BigDecimal totalAmount,
            long refundedCount,
            BigDecimal refundedAmount,
            long pendingRefundCount,
            long retryNeededCount
    ) {}

    public RefundReconciliationRow getEventRefundReconciliation(Integer eventId) {
        String sql = """
                SELECT 
                    e.id AS event_id,
                    e.name AS event_name,
                    COUNT(p.id) AS total_payments,
                    COALESCE(SUM(p.amount), 0) AS total_amount,
                    COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) AS refunded_count,
                    COALESCE(SUM(CASE WHEN r.status = 'COMPLETED' THEN r.amount ELSE 0 END), 0) AS refunded_amount,
                    COUNT(CASE WHEN r.status IN ('WAITING', 'QUEUED', 'PROCESSING') THEN 1 END) AS pending_refund_count,
                    COUNT(CASE WHEN r.status IN ('FAILED', 'RECONCILIATION') THEN 1 END) AS retry_needed_count
                FROM events e
                JOIN bookings b ON e.id = b.events_id
                JOIN payments p ON b.id = p.bookings_id AND p.status = 'SUCCESS'
                LEFT JOIN refunds r ON p.id = r.payments_id
                WHERE e.id = ?
                GROUP BY e.id, e.name
                """;

        List<RefundReconciliationRow> rows = jdbc.query(sql, (rs, rowNum) -> new RefundReconciliationRow(
                rs.getInt("event_id"),
                rs.getString("event_name"),
                rs.getLong("total_payments"),
                rs.getBigDecimal("total_amount"),
                rs.getLong("refunded_count"),
                rs.getBigDecimal("refunded_amount"),
                rs.getLong("pending_refund_count"),
                rs.getLong("retry_needed_count")
        ), eventId);

        return rows.isEmpty() ? null : rows.get(0);
    }
}

