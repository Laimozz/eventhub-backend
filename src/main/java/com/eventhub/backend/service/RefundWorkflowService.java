package com.eventhub.backend.service;

import com.eventhub.backend.entity.Payment;
import com.eventhub.backend.entity.Refund;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundWorkflowService {
    private final RefundWorkflowRepository workflow;
    private final RefundRepository refunds;
    private final EventRepository events;
    private final FinanceLedgerRepository ledger;
    private final FinanceLedgerService finance;
    private final NotificationService notifications;
    private final Clock clock;

    @Transactional(readOnly = true)
    public RefundPage list(Integer eventId, int page, int pageSize) {
        AdminPagination.page(page,pageSize);
        long count=workflow.count(eventId);
        return new RefundPage(workflow.list(eventId,pageSize,(long)page*pageSize),page,pageSize,count,
                (int)((count+pageSize-1)/pageSize));
    }

    public Detail detail(Integer paymentId) {
        var refund=locked(paymentId);
        return response(refund);
    }

    // Local refund only: no gateway request or background provider job.
    public Detail complete(Integer paymentId, Integer adminId) {
        AdminPagination.validId(adminId);
        var refund = locked(paymentId);
        if ("COMPLETED".equals(refund.getStatus())) return response(refund);
        eligible(refund.getPayment());
        refund.setMethod("INTERNAL");
        refund.setRequestedBy(adminId);
        refund.setProviderReference("RF-" + UUID.randomUUID().toString().replace("-", ""));
        refund.setResponseCode(null);
        refund.setTransactionStatus(null);
        finance.reverse(refund.getPayment());
        refund.setStatus("COMPLETED");
        refund.setRefundedAt(LocalDateTime.now(clock));
        refund.setLastError(null);
        notifications.create(refund.getPayment().getBooking().getUser(), "Hoàn tiền thành công",
                "Đã hoàn toàn bộ giao dịch #" + paymentId + ".", "REFUND_COMPLETED");
        refunds.saveAndFlush(refund);
        return response(refund);
    }

    public int completeEvent(Integer eventId, Integer adminId) {
        AdminPagination.validId(eventId);
        AdminPagination.validId(adminId);
        var event = events.findForUpdate(eventId)
                .orElseThrow(() -> error("EVENT_NOT_FOUND", "Không tìm thấy sự kiện"));
        if (event.getStatus() != EventStatus.CANCELED)
            throw error("EVENT_NOT_CANCELED", "Sự kiện chưa được hủy");
        workflow.seedEvent(eventId);
        var ids = workflow.refundableEvent(eventId);
        for (Integer paymentId : ids) complete(paymentId, adminId);
        return ids.size();
    }

    private Refund locked(Integer paymentId) {
        AdminPagination.validId(paymentId);
        Integer eventId=workflow.eventId(paymentId).orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND,"PAYMENT_NOT_FOUND","Không tìm thấy giao dịch"));
        events.findForUpdate(eventId).orElseThrow();
        return refunds.findByPaymentId(paymentId).orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND,"REFUND_NOT_FOUND","Giao dịch chưa có hồ sơ hoàn tiền"));
    }

    private void eligible(Payment payment) {
        if(!"SUCCESS".equals(payment.getStatus()) || payment.getAmount().signum()<=0
                || !(payment.getBooking().getEvent().getStatus()==EventStatus.CANCELED || "PAYMENT_REVIEW".equals(payment.getBooking().getStatus())))
            throw error("REFUND_NOT_ELIGIBLE","Giao dịch chưa đủ điều kiện hoàn tiền");
        var entry=ledger.entry(payment.getId());
        if(entry.settled() || entry.reversed()) throw error("FINANCE_REQUIRES_RECONCILIATION","Khoản này đã giải ngân hoặc đảo sổ; cần đối soát riêng");
    }

    private Detail response(Refund refund) {
        return new Detail(refund.getPayment().getId(), refund.getAmount(), refund.getStatus(),
                refund.getMethod(), refund.getProviderReference(), refund.getRefundedAt(),
                refund.getPayment().getPaymentMethod(), refund.getRequestedBy(),
                refund.getPayment().getBooking().getEvent().getId(),
                refund.getPayment().getBooking().getEvent().getStatus() == EventStatus.CANCELED);
    }

    private AdminException error(String code, String message) {
        return new AdminException(HttpStatus.CONFLICT, code, message);
    }
    public record Detail(Integer paymentId, java.math.BigDecimal amount, String status, String method,
                         String reference, LocalDateTime refundedAt, String paymentMethod,
                         Integer processedBy, Integer eventId, boolean eventCanceled) {}
    public record RefundPage(List<RefundWorkflowRepository.RefundRow> items, int page, int pageSize,
                             long totalElements, int totalPages) {}
}
