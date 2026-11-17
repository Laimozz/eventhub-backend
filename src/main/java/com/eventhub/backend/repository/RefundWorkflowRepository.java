package com.eventhub.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefundWorkflowRepository {
    private final JdbcTemplate jdbc;

    public Optional<Integer> eventId(Integer paymentId) {
        return jdbc.queryForList("select b.events_id from bookings b join payments p on p.bookings_id=b.id where p.id=?",
                Integer.class, paymentId).stream().findFirst();
    }

    public int seedEvent(Integer eventId) {
        return jdbc.update("""
                insert into refunds(payments_id,refund_transaction_id,amount,status)
                select p.id, 'refund-' || p.id, p.amount, 'WAITING' from payments p
                join bookings b on b.id=p.bookings_id where b.events_id=? and p.status='SUCCESS' and p.amount>0
                on conflict(payments_id) do nothing
                """, eventId);
    }

    public void seedPayment(Integer paymentId) {
        jdbc.update("""
                insert into refunds(payments_id,refund_transaction_id,amount,status)
                select id, 'refund-' || id, amount, 'WAITING' from payments where id=? and status='SUCCESS' and amount>0
                on conflict(payments_id) do nothing
                """, paymentId);
    }

    public List<Integer> refundableEvent(Integer eventId) {
        return jdbc.queryForList("""
                select r.payments_id from refunds r join payments p on p.id=r.payments_id
                join bookings b on b.id=p.bookings_id join payment_finances f on f.payments_id=p.id
                where b.events_id=? and r.status in ('WAITING','QUEUED','PROCESSING','FAILED','RECONCILIATION')
                  and p.status='SUCCESS' and p.payment_method='VNPAY' and p.amount>0
                  and not f.settled and not f.reversed order by r.id
                """,Integer.class,eventId);
    }

    public List<RefundRow> list(Integer eventId, int limit, long offset) {
        return jdbc.query("""
                select r.id,r.payments_id,p.bookings_id,b.events_id,e.name,u.full_name,r.amount,r.status,r.method,r.refunded_at
                from refunds r join payments p on p.id=r.payments_id join bookings b on b.id=p.bookings_id
                join events e on e.id=b.events_id join users u on u.id=b.users_id
                where (?::integer is null or b.events_id=?) order by r.id desc limit ? offset ?
                """, (rs, row) -> new RefundRow(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getString(5),
                        rs.getString(6),rs.getBigDecimal(7),rs.getString(8),rs.getString(9),rs.getObject(10,LocalDateTime.class)),
                eventId,eventId,limit,offset);
    }

    public long count(Integer eventId) {
        return jdbc.queryForObject("""
                select count(*) from refunds r join payments p on p.id=r.payments_id join bookings b on b.id=p.bookings_id
                where (?::integer is null or b.events_id=?)
                """, Long.class,eventId,eventId);
    }

    public record RefundRow(Integer id,Integer paymentId,Integer bookingId,Integer eventId,String eventName,
                            String customerName,BigDecimal amount,String status,String method,LocalDateTime refundedAt) {}
}
