package com.eventhub.backend.repository;

import com.eventhub.backend.dto.response.AdminFinanceResponses.OverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminFinanceRepository {
    private final JdbcTemplate jdbc;

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
                       coalesce(sum(total_withdrawn), 0) as total_withdrawn
                from organizer_wallets
                """, (rs, row) -> new OverviewResponse(rs.getLong("total_events"), rs.getLong("total_users"),
                        rs.getLong("tickets_sold"), rs.getBigDecimal("pending_balance"), rs.getBigDecimal("available_balance"),
                        rs.getBigDecimal("withdrawal_pending_balance"), rs.getBigDecimal("total_withdrawn"), null, null,
                        "Chưa có định nghĩa doanh thu và trạng thái thanh toán thành công trong tài liệu tích hợp."));
    }
}
