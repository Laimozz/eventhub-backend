package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.dto.response.AdminResponses.OrganizerResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminFinanceResponses {
    private AdminFinanceResponses() {}
    public record OverviewResponse(long totalEvents, long totalUsers, long totalTicketsSold,
            BigDecimal pendingBalance, BigDecimal availableBalance, BigDecimal withdrawalPendingBalance,
            BigDecimal totalWithdrawn, BigDecimal grossRevenue, BigDecimal netRevenue, String revenueUnavailableReason) {}
    public record PayoutResponse(Integer id, BigDecimal amount, String status, String transactionCode,
            String failureReason, LocalDateTime initiatedAt, LocalDateTime completedAt) {
        public static PayoutResponse from(PayoutTransaction p) {
            return new PayoutResponse(p.getId(), p.getAmount(), p.getStatus(), p.getTransactionCode(),
                    p.getFailureReason(), p.getInitiatedAt(), p.getCompletedAt());
        }
    }
    public record WithdrawalResponse(Integer id, OrganizerResponse organizer, BigDecimal amount, String bankName,
            String accountNumber, String status, LocalDateTime requestedAt, Integer processedBy,
            LocalDateTime processedAt, String rejectReason, String transactionCode, List<PayoutResponse> payouts) {
        public static WithdrawalResponse from(WithdrawalRequest r, boolean detail, List<PayoutResponse> payouts) {
            return new WithdrawalResponse(r.getId(), OrganizerResponse.from(r.getOrganizerWallet().getOrganizer()),
                    r.getAmount(), r.getBankName(), detail ? r.getAccountNumber() : null, r.getStatus(), r.getRequestedAt(),
                    r.getProcessedBy() == null ? null : r.getProcessedBy().getId(), r.getProcessedAt(),
                    r.getRejectReason(), r.getTransactionCode(), payouts);
        }
    }
    public record CanceledEventResponse(Integer id, String name, OrganizerResponse organizer, String cancelReason) {}
    public record RefundPaymentResponse(Integer id, Integer bookingId, String customerName, BigDecimal paidAmount,
            String paymentStatus, LocalDateTime paidAt, Integer refundId, BigDecimal refundAmount,
            String refundStatus, Integer retryCount, LocalDateTime refundedAt) {}
    public record RefundDetailResponse(Integer eventId, String eventName, String cancelReason,
            AdminResponses.PageResponse<RefundPaymentResponse> payments, boolean canRefund, String unavailableReason) {}
}
