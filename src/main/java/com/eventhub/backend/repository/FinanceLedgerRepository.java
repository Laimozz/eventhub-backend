package com.eventhub.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FinanceLedgerRepository {
    private final JdbcTemplate jdbc;

    public boolean record(Integer paymentId, BigDecimal gross, BigDecimal fee, boolean credited) {
        return jdbc.update("""
                update payments set gross_amount=?, platform_fee=?, organizer_amount=?, finance_credited=?,
                    finance_recorded_at=(CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
                where id=? and finance_recorded_at is null
                """, gross, fee, gross.subtract(fee), credited, paymentId) == 1;
    }

    public void ensureWallet(Integer organizerId) {
        jdbc.update("insert into organizer_wallets(organizer_id) values (?) on conflict (organizer_id) do nothing", organizerId);
    }

    public BigDecimal eventRevenue(Integer eventId) {
        return jdbc.queryForObject("""
                select coalesce(sum(p.organizer_amount),0) from payments p
                join bookings b on b.id=p.bookings_id
                where b.events_id=? and p.finance_credited and not p.finance_reversed
                """, BigDecimal.class, eventId);
    }

    public List<Integer> settlementEvents(LocalDateTime now) {
        return jdbc.queryForList("""
                select distinct e.id from events e join bookings b on b.events_id=e.id
                join payments p on p.bookings_id=b.id
                where e.end_time <= ? and e.status in ('APPROVED','ONGOING','COMPLETED')
                and p.finance_credited and not p.finance_settled and not p.finance_reversed order by e.id limit 100
                """, Integer.class, now);
    }

    public List<Integer> releasablePayments(Integer eventId) {
        return jdbc.queryForList("""
                select p.id from payments p join bookings b on b.id=p.bookings_id
                where b.events_id=? and b.status='PAID' and p.status='SUCCESS'
                  and p.finance_credited and not p.finance_settled and not p.finance_reversed
                  and not exists(select 1 from refunds r where r.payments_id=p.id)
                  and (select count(*) from tickets t join booking_items i on i.id=t.booking_items_id
                       where i.bookings_id=b.id and t.status in ('VALID','USED'))
                      = (select coalesce(sum(i.quantity),0) from booking_items i where i.bookings_id=b.id)
                order by p.id
                """, Integer.class, eventId);
    }

    public Entry entry(Integer paymentId) {
        return jdbc.query("select organizer_amount, finance_credited, finance_settled, finance_reversed from payments where id=? and finance_recorded_at is not null for update",
                (rs, row) -> new Entry(rs.getBigDecimal("organizer_amount"), rs.getBoolean("finance_credited"),
                        rs.getBoolean("finance_settled"), rs.getBoolean("finance_reversed")), paymentId).stream().findFirst()
                .orElseThrow(() -> new com.eventhub.backend.exception.AdminException(
                        org.springframework.http.HttpStatus.CONFLICT, "FINANCE_REQUIRES_RECONCILIATION",
                        "Giao dịch thiếu dữ liệu tài chính; cần đối soát trước khi hoàn tiền."));
    }

    public void settle(Integer paymentId) {
        jdbc.update("update payments set finance_settled=true where id=?", paymentId);
    }

    public void reverse(Integer paymentId) {
        jdbc.update("update payments set finance_reversed=true where id=?", paymentId);
    }

    public record Entry(BigDecimal organizerAmount, boolean credited, boolean settled, boolean reversed) {}
}
