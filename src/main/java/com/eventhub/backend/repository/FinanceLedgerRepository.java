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
                insert into payment_finances(payments_id, gross_amount, platform_fee, organizer_amount, credited)
                values (?, ?, ?, ?, ?) on conflict (payments_id) do nothing
                """, paymentId, gross, fee, gross.subtract(fee), credited) == 1;
    }

    public void ensureWallet(Integer organizerId) {
        jdbc.update("insert into organizer_wallets(organizer_id) values (?) on conflict (organizer_id) do nothing", organizerId);
    }

    public BigDecimal eventRevenue(Integer eventId) {
        BigDecimal rev = jdbc.queryForObject("""
                select coalesce(sum(f.gross_amount),0) from payment_finances f
                join payments p on p.id=f.payments_id join bookings b on b.id=p.bookings_id
                where b.events_id=? and f.credited and not f.reversed
                """, BigDecimal.class, eventId);
        if (rev == null || rev.signum() == 0) {
            return jdbc.queryForObject("""
                    select coalesce(sum(b.total_amount),0) from bookings b
                    where b.events_id=? and b.status='PAID'
                    """, BigDecimal.class, eventId);
        }
        return rev;
    }

    public List<Integer> settlementEvents(LocalDateTime now) {
        return jdbc.queryForList("""
                select distinct e.id from events e join bookings b on b.events_id=e.id
                join payments p on p.bookings_id=b.id join payment_finances f on f.payments_id=p.id
                where e.end_time <= ? and e.status in ('APPROVED','ONGOING','COMPLETED')
                and f.credited and not f.settled and not f.reversed order by e.id limit 100
                """, Integer.class, now);
    }

    public List<Integer> releasablePayments(Integer eventId) {
        return jdbc.queryForList("""
                select p.id from payments p join bookings b on b.id=p.bookings_id
                join payment_finances f on f.payments_id=p.id
                where b.events_id=? and b.status='PAID' and p.status='SUCCESS'
                  and f.credited and not f.settled and not f.reversed
                  and not exists(select 1 from refunds r where r.payments_id=p.id)
                  and (select count(*) from tickets t join booking_items i on i.id=t.booking_items_id
                       where i.bookings_id=b.id and t.status in ('VALID','USED'))
                      = (select coalesce(sum(i.quantity),0) from booking_items i where i.bookings_id=b.id)
                order by p.id
                """, Integer.class, eventId);
    }

    public Entry entry(Integer paymentId) {
        return jdbc.queryForObject("select * from payment_finances where payments_id=? for update",
                (rs, row) -> new Entry(rs.getBigDecimal("organizer_amount"), rs.getBoolean("credited"),
                        rs.getBoolean("settled"), rs.getBoolean("reversed")), paymentId);
    }

    public void settle(Integer paymentId) {
        jdbc.update("update payment_finances set settled=true where payments_id=?", paymentId);
    }

    public void reverse(Integer paymentId) {
        jdbc.update("update payment_finances set reversed=true where payments_id=?", paymentId);
    }

    public record Entry(BigDecimal organizerAmount, boolean credited, boolean settled, boolean reversed) {}
}
