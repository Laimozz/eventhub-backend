package com.eventhub.backend.repository;

import com.eventhub.backend.dto.request.RefundBankAccountRequest;
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

    public List<Integer> waitingEvent(Integer eventId) {
        return jdbc.queryForList("""
                select r.payments_id from refunds r join payments p on p.id=r.payments_id
                join bookings b on b.id=p.bookings_id where b.events_id=? and r.status='WAITING' order by r.id
                """, Integer.class, eventId);
    }

    public List<Integer> queued() {
        return jdbc.queryForList("select payments_id from refunds where status='QUEUED' order by id limit 20", Integer.class);
    }

    public List<Integer> interrupted(LocalDateTime cutoff) {
        return jdbc.queryForList("select payments_id from refunds where status='PROCESSING' and updated_at<? order by id limit 100", Integer.class, cutoff);
    }

    public void beginAttempt(Integer refundId, String requestId, String command) {
        jdbc.update("""
                update refunds set active_request_id=?, active_command=?,
                    attempt_started_at=(CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
                    attempt_history=attempt_history || jsonb_build_array(jsonb_build_object(
                        'requestId',?::text,'command',?::text,'outcome','PROCESSING',
                        'createdAt',CURRENT_TIMESTAMP AT TIME ZONE 'UTC')) where id=?
                """, requestId, command, requestId, command, refundId);
    }

    public void endAttempt(String requestId, String outcome, String code, String status, LocalDateTime now) {
        jdbc.update("""
                update refunds set attempt_history=(select jsonb_agg(
                    case when item->>'requestId'=? then item || jsonb_build_object(
                        'outcome',?::text,'responseCode',?::text,'transactionStatus',?::text,'completedAt',?::timestamp)
                    else item end order by ordinal)
                    from jsonb_array_elements(attempt_history) with ordinality as entries(item,ordinal))
                where active_request_id=?
                """, requestId, outcome, code, status, now, requestId);
    }

    public boolean isActiveAttempt(Integer refundId, String requestId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "select active_request_id=? from refunds where id=?", Boolean.class, requestId, refundId));
    }

    public void recordFailure(Integer refundId,String requestId,com.eventhub.backend.service.RefundProviderFailure failure) {
        jdbc.update("""
                update refunds set attempt_history=(select jsonb_agg(
                    case when item->>'requestId'=? then item || jsonb_build_object(
                        'diagnosticCode',?::text,'diagnosticStage',?::text,'diagnosticMessage',?::text,'httpStatus',?::integer)
                    else item end order by ordinal)
                    from jsonb_array_elements(attempt_history) with ordinality as entries(item,ordinal))
                where id=? and active_request_id=?
                """,requestId,failure.code(),failure.stage(),failure.message(),failure.httpStatus(),refundId,requestId);
    }

    public void recoverAttempt(Integer refundId, LocalDateTime now) {
        jdbc.queryForList("select active_request_id from refunds where id=? and active_request_id is not null",
                String.class, refundId).forEach(id -> endAttempt(id,"RECONCILIATION",null,null,now));
    }

    public void recordLateResult(Integer refundId, String requestId, String code, String status, LocalDateTime now) {
        // Preserve late evidence without letting an obsolete request overwrite the active decision.
        jdbc.update("""
                update refunds set attempt_history=attempt_history || jsonb_build_array(jsonb_build_object(
                    'requestId',?::text,'command','late-response','outcome','RECONCILIATION',
                    'responseCode',?::text,'transactionStatus',?::text,'createdAt',?::timestamp)) where id=?
                """, requestId, code, status, now, refundId);
    }

    public List<Attempt> attempts(Integer refundId) {
        return jdbc.query("""
                select item->>'command',item->>'outcome',item->>'responseCode',item->>'transactionStatus',
                    (item->>'createdAt')::timestamp,item->>'diagnosticCode',item->>'diagnosticStage',
                    item->>'diagnosticMessage',(item->>'httpStatus')::integer from refunds,
                    jsonb_array_elements(attempt_history) with ordinality as entries(item,ordinal)
                where id=? order by ordinal desc limit 100
                """, (rs, row) -> new Attempt(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
                        rs.getObject(5, LocalDateTime.class),rs.getString(6),rs.getString(7),rs.getString(8),rs.getObject(9,Integer.class)), refundId);
    }

    public Optional<BankAccount> bankAccount(Integer paymentId) {
        return jdbc.query("select * from refund_bank_accounts where payment_id=?", (rs, row) -> new BankAccount(
                rs.getString("bank_code"), rs.getString("account_number"), rs.getString("account_holder_name"),
                rs.getObject("verified_by",Integer.class), rs.getObject("verified_at", LocalDateTime.class)), paymentId).stream().findFirst();
    }

    public void saveBankAccount(Integer paymentId, Integer adminId, RefundBankAccountRequest request, LocalDateTime now) {
        jdbc.update("""
                update refunds set account_history=account_history || jsonb_build_array(jsonb_build_object(
                    'changedBy',?::integer,'changedAt',?::timestamp,
                    'previous',(select to_jsonb(a) from refund_bank_accounts a where payment_id=?)))
                where payments_id=?
                """,adminId,now,paymentId,paymentId);
        jdbc.update("""
                insert into refund_bank_accounts(payment_id,bank_code,account_number,account_holder_name,provided_by,provided_at,verified_by,verified_at)
                values (?,?,?,?,?,?,?,?) on conflict(payment_id) do update set bank_code=excluded.bank_code,
                account_number=excluded.account_number,account_holder_name=excluded.account_holder_name,
                provided_by=excluded.provided_by,provided_at=excluded.provided_at,
                verified_by=excluded.verified_by,verified_at=excluded.verified_at,updated_at=excluded.verified_at
                """, paymentId, request.bankCode().trim(), request.accountNumber().trim(), request.accountHolderName().trim(), adminId, now, adminId, now);
    }

    public void declareBankAccount(Integer paymentId,Integer userId,
            com.eventhub.backend.dto.request.PurchaseBankAccountRequest request,LocalDateTime now) {
        jdbc.update("""
                insert into refund_bank_accounts(payment_id,bank_code,account_number,account_holder_name,provided_by,provided_at)
                values (?,?,?,?,?,?)
                """,paymentId,request.bankCode().trim(),request.accountNumber().trim(),request.accountHolderName().trim(),userId,now);
    }

    public void saveEvidence(Integer refundId, String code, String type, byte[] content, Integer adminId) {
        jdbc.update("""
                update refunds set manual_transaction_code=?,evidence_content_type=?,evidence_content=?,evidence_recorded_by=?,
                    evidence_recorded_at=(CURRENT_TIMESTAMP AT TIME ZONE 'UTC') where id=?
                """, code, type, content, adminId, refundId);
    }

    public Optional<Evidence> evidence(Integer refundId) {
        return jdbc.query("select evidence_content_type,evidence_content from refunds where id=? and evidence_content is not null",
                (rs, row) -> new Evidence(rs.getString(1), rs.getBytes(2)), refundId).stream().findFirst();
    }

    public boolean hasEvidence(Integer refundId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("select evidence_content is not null from refunds where id=?", Boolean.class, refundId));
    }

    public void invalidateTickets(Integer eventId) {
        jdbc.update("""
                update tickets set status='CANCELED' where status='VALID' and booking_items_id in
                (select i.id from booking_items i join bookings b on b.id=i.bookings_id where b.events_id=?)
                """, eventId);
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
    public record BankAccount(String bankCode,String accountNumber,String accountHolderName,Integer verifiedBy,LocalDateTime verifiedAt) {}
    public record Evidence(String contentType,byte[] content) {}
    public record Attempt(String command,String outcome,String responseCode,String transactionStatus,LocalDateTime createdAt,
                          String diagnosticCode,String diagnosticStage,String diagnosticMessage,Integer httpStatus) {}
}
