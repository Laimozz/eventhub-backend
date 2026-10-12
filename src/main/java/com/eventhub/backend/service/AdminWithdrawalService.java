package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminFinanceResponses.*;
import com.eventhub.backend.dto.response.AdminResponses.PageResponse;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminWithdrawalService {
    private final WithdrawalRequestRepository requests;
    private final OrganizerWalletRepository wallets;
    private final PayoutTransactionRepository payouts;
    private final UserRepository users;
    private final NotificationRepository notifications;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<WithdrawalResponse> list(String status, int page, int pageSize) {
        if (status != null && !List.of("PENDING", "APPROVED", "REJECTED").contains(status))
            throw error(HttpStatus.BAD_REQUEST, "INVALID_STATUS", "Trạng thái không hợp lệ");
        return PageResponse.from(requests.search(status, AdminPagination.page(page, pageSize))
                .map(r -> WithdrawalResponse.from(r, false, List.of())));
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public WithdrawalResponse detail(Integer id) {
        AdminPagination.validId(id);
        return response(requests.findById(id).orElseThrow(() ->
                error(HttpStatus.NOT_FOUND, "WITHDRAWAL_NOT_FOUND", "Không tìm thấy yêu cầu")));
    }

    public WithdrawalResponse decide(Integer adminId, Integer id, String transactionCode, String reason) {
        AdminPagination.validId(id);
        boolean approved = reason == null;
        String value = approved ? transactionCode : reason;
        if (value == null || value.isBlank() || value.length() > 255)
            throw error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Vui lòng nhập thông tin xử lý hợp lệ");
        value = value.trim();
        WithdrawalRequest request = requests.findForUpdate(id).orElseThrow(() ->
                error(HttpStatus.NOT_FOUND, "WITHDRAWAL_NOT_FOUND", "Không tìm thấy yêu cầu"));
        if (!"PENDING".equals(request.getStatus()))
            throw error(HttpStatus.CONFLICT, "WITHDRAWAL_NOT_PENDING", "Yêu cầu không còn chờ duyệt");
        OrganizerWallet wallet = wallets.findForUpdate(request.getOrganizerWallet().getId()).orElseThrow();
        if (request.getAmount() == null || request.getAmount().signum() <= 0
                || wallet.getWithdrawalPendingBalance().compareTo(request.getAmount()) < 0)
            throw error(HttpStatus.CONFLICT, "INSUFFICIENT_RESERVED_BALANCE", "Số dư chờ rút không đủ. Cần đối soát ví.");
        // Existing payout attempts require reconciliation before releasing a reservation or recording another transfer.
        if (!payouts.findByWithdrawalRequestIdOrderByIdAsc(id).isEmpty())
            throw error(HttpStatus.CONFLICT, "PAYOUT_REQUIRES_RECONCILIATION", "Yêu cầu đã có giao dịch chi trả. Cần đối soát trước khi xử lý.");
        if (approved && payouts.existsByTransactionCode(value))
            throw error(HttpStatus.CONFLICT, "TRANSACTION_CODE_IN_USE", "Mã giao dịch đã được ghi nhận");
        LocalDateTime now = LocalDateTime.now(clock);
        wallet.setWithdrawalPendingBalance(wallet.getWithdrawalPendingBalance().subtract(request.getAmount()));
        if (approved) {
            wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(request.getAmount()));
            PayoutTransaction payout = new PayoutTransaction();
            payout.setWithdrawalRequest(request);
            payout.setAmount(request.getAmount());
            payout.setTransactionCode(value);
            payout.setStatus("COMPLETED");
            payout.setInitiatedAt(now);
            payout.setCompletedAt(now);
            payouts.saveAndFlush(payout);
            request.setTransactionCode(value);
        } else {
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(request.getAmount()));
            request.setRejectReason(value);
        }
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        request.setProcessedBy(users.getReferenceById(adminId));
        request.setProcessedAt(now);
        wallets.saveAndFlush(wallet);
        requests.saveAndFlush(request);
        Notification notification = new Notification();
        notification.setUser(wallet.getOrganizer());
        notification.setTitle("Yêu cầu rút tiền #" + id + (approved ? " đã được duyệt" : " bị từ chối"));
        notification.setType(approved ? "WITHDRAWAL_APPROVED" : "WITHDRAWAL_REJECTED");
        notification.setContent(approved ? "Admin đã ghi nhận chuyển khoản cho yêu cầu #" + id + "." : value);
        notifications.saveAndFlush(notification);
        return response(request);
    }

    private WithdrawalResponse response(WithdrawalRequest request) {
        return WithdrawalResponse.from(request, true, payouts.findByWithdrawalRequestIdOrderByIdAsc(request.getId())
                .stream().map(PayoutResponse::from).toList());
    }
    private static AdminException error(HttpStatus status, String code, String message) {
        return new AdminException(status, code, message);
    }
}
