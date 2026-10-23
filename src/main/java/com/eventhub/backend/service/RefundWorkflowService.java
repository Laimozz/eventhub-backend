package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.RefundBankAccountRequest;
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
    private final PaymentRepository payments;
    private final EventRepository events;
    private final FinanceLedgerRepository ledger;
    private final FinanceLedgerService finance;
    private final NotificationService notifications;
    private final VnPayService vnpay;
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

    public Detail queue(Integer paymentId,Integer adminId) {
        vnpay.requireApiConfiguration();
        var refund=locked(paymentId);
        queueLocked(refund,adminId);
        return response(refund);
    }

    public int queueEvent(Integer eventId,Integer adminId) {
        vnpay.requireApiConfiguration();
        var event=events.findForUpdate(eventId).orElseThrow(() -> error("EVENT_NOT_FOUND","Không tìm thấy sự kiện"));
        if(event.getStatus()!=EventStatus.CANCELED) throw error("EVENT_NOT_CANCELED","Sự kiện chưa được hủy");
        workflow.seedEvent(eventId);
        int count=0;
        for(Integer paymentId:workflow.waitingEvent(eventId)) {
            var refund=refunds.findByPaymentId(paymentId).orElseThrow();
            // Unsupported payment methods remain available for manual reconciliation.
            if(!"VNPAY".equals(refund.getPayment().getPaymentMethod())) continue;
            queueLocked(refund,adminId);
            count++;
        }
        return count;
    }

    private void queueLocked(Refund refund,Integer adminId) {
        if(List.of("QUEUED","PROCESSING","RECONCILIATION","COMPLETED").contains(refund.getStatus())) return;
        if(!"WAITING".equals(refund.getStatus())) throw error("REFUND_NOT_WAITING","Khoản hoàn không còn chờ xử lý tự động");
        eligible(refund.getPayment());
        if(!"VNPAY".equals(refund.getPayment().getPaymentMethod())) throw error("REFUND_MANUAL_REQUIRED","Giao dịch cần hoàn thủ công");
        refund.setMethod("VNPAY"); refund.setRequestedBy(adminId); refund.setStatus("QUEUED");
        refunds.saveAndFlush(refund);
    }

    public Work claim(Integer paymentId,boolean query) {
        vnpay.requireApiConfiguration();
        var refund=locked(paymentId);
        if(query) {
            if(!"RECONCILIATION".equals(refund.getStatus()) || !"VNPAY".equals(refund.getMethod()))
                throw error("REFUND_NOT_RECONCILABLE","Khoản hoàn chưa ở trạng thái đối soát VNPAY");
        } else if(!"QUEUED".equals(refund.getStatus())) return null;
        refund.setStatus("PROCESSING");
        refund.setRetryCount(refund.getRetryCount()+1);
        String requestId=UUID.randomUUID().toString().replace("-","");
        workflow.beginAttempt(refund.getId(),requestId,query?"querydr":"refund");
        refunds.saveAndFlush(refund);
        return new Work(refund.getPayment(),requestId,refund.getRequestedBy(),query);
    }

    public void finish(Work work,VnPayService.ApiResult result) {
        var refund=locked(work.payment().getId());
        if("COMPLETED".equals(refund.getStatus())) return;
        if(!List.of("PROCESSING","RECONCILIATION").contains(refund.getStatus()))
            throw error("REFUND_STATE_CONFLICT","Khoản hoàn đã thay đổi, cần đối soát");
        if(result==null) {
            refund.setStatus("RECONCILIATION");
            refund.setLastError("Chưa xác minh kết quả cổng thanh toán; không chuyển khoản hoặc gửi hoàn lần nữa.");
        } else {
            refund.setResponseCode(result.responseCode());
            refund.setTransactionStatus(result.transactionStatus());
            refund.setProviderReference(result.reference());
            if(result.completed()) complete(refund);
            else refund.setStatus(result.rejected()?"FAILED":"RECONCILIATION");
        }
        workflow.endAttempt(work.requestId(),refund.getStatus(),result==null?null:result.responseCode(),
                result==null?null:result.transactionStatus(),LocalDateTime.now(clock));
        refunds.saveAndFlush(refund);
    }

    public void recover(Integer paymentId) {
        var refund=locked(paymentId);
        if("PROCESSING".equals(refund.getStatus()) && refund.getUpdatedAt().isBefore(LocalDateTime.now(clock).minusMinutes(10))) {
            refund.setStatus("RECONCILIATION");
            refund.setLastError("Phiên xử lý bị gián đoạn; cần truy vấn kết quả trước khi xử lý tiếp.");
        }
    }

    public Detail saveBank(Integer paymentId,Integer adminId,RefundBankAccountRequest request) {
        var refund=locked(paymentId);
        if(!List.of("WAITING","FAILED").contains(refund.getStatus()))
            throw error("REFUND_ACCOUNT_LOCKED","Không đổi tài khoản khi khoản hoàn đang xử lý hoặc đã hoàn tất");
        if(!request.originalAccountVerified()) throw error("ACCOUNT_NOT_VERIFIED","Cần xác minh tài khoản gốc");
        workflow.saveBankAccount(paymentId,adminId,request,LocalDateTime.now(clock));
        return response(refund);
    }

    public Detail reserveManual(Integer paymentId,Integer adminId) {
        var refund=locked(paymentId);
        if("MANUAL_PENDING".equals(refund.getStatus())) return response(refund);
        if(!List.of("WAITING","FAILED").contains(refund.getStatus()))
            throw error("REFUND_ALREADY_STARTED","Phải đối soát khoản hoàn tự động trước khi chuyển sang thủ công");
        eligible(refund.getPayment());
        if(workflow.bankAccount(paymentId).isEmpty()) throw error("ACCOUNT_NOT_VERIFIED","Chưa xác minh tài khoản mua vé");
        refund.setStatus("MANUAL_PENDING"); refund.setMethod("MANUAL"); refund.setRequestedBy(adminId);
        refunds.saveAndFlush(refund);
        return response(refund);
    }

    public Detail confirmManual(Integer paymentId,Integer adminId,String code,String contentType,byte[] evidence) {
        var refund=locked(paymentId);
        if("COMPLETED".equals(refund.getStatus())) return response(refund);
        if(!"MANUAL_PENDING".equals(refund.getStatus())) throw error("REFUND_NOT_RESERVED","Phải giữ khoản hoàn thủ công trước khi chuyển khoản");
        if(code==null || code.isBlank() || code.trim().length()>255) throw error("INVALID_TRANSFER_CODE","Mã chuyển khoản không hợp lệ");
        validateEvidence(contentType,evidence);
        workflow.saveEvidence(refund.getId(),code.trim(),contentType,evidence,adminId);
        refund.setProviderReference(code.trim());
        complete(refund);
        refunds.saveAndFlush(refund);
        return response(refund);
    }

    public RefundWorkflowRepository.Evidence evidence(Integer paymentId) {
        var refund=locked(paymentId);
        return workflow.evidence(refund.getId()).orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND,"EVIDENCE_NOT_FOUND","Chưa có chứng từ"));
    }

    private void validateEvidence(String type,byte[] bytes) {
        boolean valid=bytes!=null && bytes.length>=8 && bytes.length<=5*1024*1024;
        if(valid) valid=switch(type==null?"":type) {
            case "application/pdf" -> new String(bytes,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-");
            case "image/png" -> bytes[0]==(byte)137 && bytes[1]==80 && bytes[2]==78 && bytes[3]==71;
            case "image/jpeg" -> bytes[0]==(byte)255 && bytes[1]==(byte)216 && bytes[2]==(byte)255;
            default -> false;
        };
        if(!valid) throw new AdminException(HttpStatus.BAD_REQUEST,"INVALID_EVIDENCE","Chứng từ phải là PDF, PNG hoặc JPEG, tối đa 5 MB");
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
        if(entry.settled()) throw error("FINANCE_REQUIRES_RECONCILIATION","Khoản này đã giải ngân; cần đối soát riêng, không tự trừ ví");
    }

    private void complete(Refund refund) {
        finance.reverse(refund.getPayment());
        refund.setStatus("COMPLETED"); refund.setRefundedAt(LocalDateTime.now(clock)); refund.setLastError(null);
        notifications.create(refund.getPayment().getBooking().getUser(),"Hoàn tiền thành công",
                "Đã hoàn toàn bộ giao dịch #"+refund.getPayment().getId()+".","REFUND_COMPLETED");
    }

    private Detail response(Refund refund) {
        return new Detail(refund.getPayment().getId(),refund.getAmount(),refund.getStatus(),refund.getMethod(),
                refund.getProviderReference(),refund.getRefundedAt(),workflow.bankAccount(refund.getPayment().getId()).orElse(null),
                workflow.attempts(refund.getId()),workflow.hasEvidence(refund.getId()));
    }
    private AdminException error(String code,String message) { return new AdminException(HttpStatus.CONFLICT,code,message); }
    public record Work(Payment payment,String requestId,Integer adminId,boolean query) {}
    public record Detail(Integer paymentId,java.math.BigDecimal amount,String status,String method,String reference,
                         LocalDateTime refundedAt,RefundWorkflowRepository.BankAccount bankAccount,
                         List<RefundWorkflowRepository.Attempt> attempts,boolean hasEvidence) {}
    public record RefundPage(List<RefundWorkflowRepository.RefundRow> items,int page,int pageSize,long totalElements,int totalPages) {}
}
