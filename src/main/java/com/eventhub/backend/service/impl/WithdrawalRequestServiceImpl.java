package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.request.CreateWithdrawalRequest;
import com.eventhub.backend.dto.response.WithdrawalRequestPreviewResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestResponse;
import com.eventhub.backend.entity.OrganizerWallet;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.WithdrawalRequest;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.WithdrawalRequestException;
import com.eventhub.backend.repository.OrganizerWalletRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.WithdrawalRequestRepository;
import com.eventhub.backend.service.WithdrawalRequestService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawalRequestServiceImpl implements WithdrawalRequestService {

        private final WithdrawalRequestRepository withdrawalRequestRepository;
        private final OrganizerWalletRepository organizerWalletRepository;
        private final UserRepository userRepository;

        @Override
        public WithdrawalRequestResponse createWithdrawalRequest(Integer organizerId,
                        CreateWithdrawalRequest request) {
                validateOrganizer(organizerId);

                BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);

                // Exception 3.2: Số tiền <= 0
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                        "INVALID_AMOUNT", "Số tiền phải lớn hơn 0");
                }

                OrganizerWallet wallet = organizerWalletRepository.findByOrganizerIdForUpdate(organizerId)
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                                "INSUFFICIENT_BALANCE",
                                                "Số dư khả dụng không đủ để thực hiện rút tiền"));

                BigDecimal availableBalance = wallet.getAvailableBalance() != null
                                ? wallet.getAvailableBalance()
                                : BigDecimal.ZERO;

                // Exception 3.1: Số tiền > số dư khả dụng
                if (amount.compareTo(availableBalance) > 0) {
                        throw new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                        "INSUFFICIENT_BALANCE", "Số tiền rút vượt quá số dư khả dụng");
                }

                // Trừ số dư khả dụng, cộng vào số dư đang chờ rút
                BigDecimal newAvailableBalance = availableBalance.subtract(amount);
                wallet.setAvailableBalance(newAvailableBalance);

                BigDecimal currentWithdrawalPending = wallet.getWithdrawalPendingBalance() != null
                                ? wallet.getWithdrawalPendingBalance()
                                : BigDecimal.ZERO;
                wallet.setWithdrawalPendingBalance(currentWithdrawalPending.add(amount));

                organizerWalletRepository.save(wallet);

                // Tạo yêu cầu rút tiền với trạng thái "Chờ duyệt"
                WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
                withdrawalRequest.setOrganizerWallet(wallet);
                withdrawalRequest.setAmount(amount);
                withdrawalRequest.setBankName(request.bankName());
                withdrawalRequest.setAccountNumber(request.accountNumber());
                withdrawalRequest.setStatus("PENDING");
                withdrawalRequest.setRequestedAt(LocalDateTime.now(ZoneOffset.UTC));

                WithdrawalRequest saved = withdrawalRequestRepository.save(withdrawalRequest);

                BigDecimal finalAvailableBalance = newAvailableBalance.setScale(2, RoundingMode.HALF_UP);

                return new WithdrawalRequestResponse(
                                saved.getId(),
                                amount,
                                OrganizerWalletServiceImpl.formatCurrency(amount),
                                saved.getBankName(),
                                saved.getAccountNumber(),
                                saved.getStatus(),
                                WithdrawalRequestResponse.formatStatus(saved.getStatus()),
                                saved.getRequestedAt(),
                                finalAvailableBalance,
                                OrganizerWalletServiceImpl.formatCurrency(finalAvailableBalance),
                                saved.getProcessedAt(),
                                saved.getRejectReason(),
                                saved.getTransactionCode(),
                                true);
        }

        @Override
        @Transactional(readOnly = true)
        public WithdrawalRequestPreviewResponse previewWithdrawalRequest(Integer organizerId,
                        CreateWithdrawalRequest request) {
                validateOrganizer(organizerId);

                BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);

                // Exception 3.2: Số tiền <= 0
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                        "INVALID_AMOUNT", "Số tiền phải lớn hơn 0");
                }

                OrganizerWallet wallet = organizerWalletRepository.findByOrganizerId(organizerId)
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                                "INSUFFICIENT_BALANCE",
                                                "Số dư khả dụng không đủ để thực hiện rút tiền"));

                BigDecimal availableBalance = wallet.getAvailableBalance() != null
                                ? wallet.getAvailableBalance()
                                : BigDecimal.ZERO;

                // Exception 3.1: Số tiền > số dư khả dụng
                if (amount.compareTo(availableBalance) > 0) {
                        throw new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                        "INSUFFICIENT_BALANCE", "Số tiền rút vượt quá số dư khả dụng");
                }

                BigDecimal availableBalanceAfter = availableBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP);

                return new WithdrawalRequestPreviewResponse(
                                amount,
                                OrganizerWalletServiceImpl.formatCurrency(amount),
                                request.bankName(),
                                request.accountNumber(),
                                availableBalance,
                                OrganizerWalletServiceImpl.formatCurrency(availableBalance),
                                availableBalanceAfter,
                                OrganizerWalletServiceImpl.formatCurrency(availableBalanceAfter));
        }

        @Override
        @Transactional(readOnly = true)
        public List<WithdrawalRequestResponse> getWithdrawalRequests(Integer organizerId) {
                return getWithdrawalRequests(organizerId, null);
        }

        @Override
        @Transactional(readOnly = true)
        public List<WithdrawalRequestResponse> getWithdrawalRequests(Integer organizerId, String status) {
                validateOrganizer(organizerId);

                String normalizedStatus = normalizeStatus(status);

                List<WithdrawalRequest> requests = (normalizedStatus == null)
                                ? withdrawalRequestRepository.findByOrganizerIdOrderByRequestedAtDesc(organizerId)
                                : withdrawalRequestRepository.findByOrganizerIdAndStatus(organizerId, normalizedStatus);

                return requests.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public WithdrawalRequestResponse getWithdrawalRequestDetail(Integer organizerId, Integer requestId) {
                validateOrganizer(organizerId);

                WithdrawalRequest request = withdrawalRequestRepository.findByIdAndOrganizerId(requestId, organizerId)
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.NOT_FOUND,
                                                "NOT_FOUND", "Không tìm thấy yêu cầu rút tiền"));

                return mapToResponse(request);
        }

        @Override
        public WithdrawalRequestResponse cancelWithdrawalRequest(Integer organizerId, Integer requestId) {
                validateOrganizer(organizerId);

                WithdrawalRequest request = withdrawalRequestRepository.findOwnedForUpdate(requestId, organizerId)
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.NOT_FOUND,
                                                "NOT_FOUND", "Không tìm thấy yêu cầu rút tiền"));

                // Exception 3.1: Yêu cầu đã được Admin duyệt hoặc từ chối -> không cho hủy
                if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                        throw new WithdrawalRequestException(HttpStatus.BAD_REQUEST,
                                        "CANNOT_CANCEL_REQUEST",
                                        "Chỉ có thể hủy yêu cầu rút tiền đang ở trạng thái Chờ duyệt");
                }

                // Bước 4: Hủy yêu cầu rút tiền, hoàn lại số tiền từ số dư đang chờ rút về số dư
                // khả dụng
                OrganizerWallet wallet = organizerWalletRepository.findForUpdate(request.getOrganizerWallet().getId())
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.CONFLICT,
                                                "WALLET_NOT_FOUND", "Không tìm thấy ví cần đối soát"));
                BigDecimal amount = request.getAmount();

                BigDecimal availableBalance = wallet.getAvailableBalance() != null
                                ? wallet.getAvailableBalance()
                                : BigDecimal.ZERO;
                BigDecimal pendingWithdrawal = wallet.getWithdrawalPendingBalance() != null
                                ? wallet.getWithdrawalPendingBalance()
                                : BigDecimal.ZERO;

                BigDecimal newAvailable = availableBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
                BigDecimal newPendingWithdrawal = pendingWithdrawal.subtract(amount);
                if (newPendingWithdrawal.compareTo(BigDecimal.ZERO) < 0) {
                        throw new WithdrawalRequestException(HttpStatus.CONFLICT,
                                        "INSUFFICIENT_RESERVED_BALANCE", "Số dư chờ rút không đủ. Cần đối soát ví.");
                }
                newPendingWithdrawal = newPendingWithdrawal.setScale(2, RoundingMode.HALF_UP);

                request.setStatus("CANCELLED");
                wallet.setAvailableBalance(newAvailable);
                wallet.setWithdrawalPendingBalance(newPendingWithdrawal);
                organizerWalletRepository.save(wallet);

                WithdrawalRequest saved = withdrawalRequestRepository.save(request);

                return new WithdrawalRequestResponse(
                                saved.getId(),
                                saved.getAmount(),
                                OrganizerWalletServiceImpl.formatCurrency(saved.getAmount()),
                                saved.getBankName(),
                                saved.getAccountNumber(),
                                saved.getStatus(),
                                WithdrawalRequestResponse.formatStatus(saved.getStatus()),
                                saved.getRequestedAt(),
                                newAvailable,
                                OrganizerWalletServiceImpl.formatCurrency(newAvailable),
                                saved.getProcessedAt(),
                                saved.getRejectReason(),
                                saved.getTransactionCode(),
                                false);
        }

        private WithdrawalRequestResponse mapToResponse(WithdrawalRequest r) {
                boolean canCancel = WithdrawalRequestResponse.isPending(r.getStatus());
                return new WithdrawalRequestResponse(
                                r.getId(),
                                r.getAmount(),
                                OrganizerWalletServiceImpl.formatCurrency(r.getAmount()),
                                r.getBankName(),
                                r.getAccountNumber(),
                                r.getStatus(),
                                WithdrawalRequestResponse.formatStatus(r.getStatus()),
                                r.getRequestedAt(),
                                null,
                                null,
                                r.getProcessedAt(),
                                r.getRejectReason(),
                                r.getTransactionCode(),
                                canCancel);
        }

        private String normalizeStatus(String status) {
                if (status == null || status.isBlank()) {
                        return null;
                }
                String s = status.trim().toUpperCase();
                if (s.equals("ALL") || s.equals("TAT_CA") || s.equals("TẤT CẢ")) {
                        return null;
                }
                return switch (s) {
                        case "CHO_DUYET", "CHỜ DUYỆT" -> "PENDING";
                        case "DA_DUYET", "ĐÃ DUYỆT" -> "APPROVED";
                        case "BI_TU_CHOI", "TU_CHOI", "BỊ TỪ CHỐI", "TỪ CHỐI" -> "REJECTED";
                        case "DA_HUY", "HUY", "ĐÃ HỦY", "HỦY", "CANCELED" -> "CANCELLED";
                        default -> s;
                };
        }

        private void validateOrganizer(Integer organizerId) {
                User organizer = userRepository.findById(organizerId)
                                .orElseThrow(() -> new WithdrawalRequestException(HttpStatus.FORBIDDEN,
                                                "FORBIDDEN", "Chỉ Organizer đang hoạt động mới có quyền truy cập"));
                if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
                        throw new WithdrawalRequestException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                                        "Chỉ Organizer đang hoạt động mới có quyền truy cập");
                }
        }
}
