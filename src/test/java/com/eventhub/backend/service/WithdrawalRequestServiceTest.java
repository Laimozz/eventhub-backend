package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.CreateWithdrawalRequest;
import com.eventhub.backend.dto.response.WithdrawalRequestResponse;
import com.eventhub.backend.entity.OrganizerWallet;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.WithdrawalRequest;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.WithdrawalRequestException;
import com.eventhub.backend.repository.OrganizerWalletRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.WithdrawalRequestRepository;
import com.eventhub.backend.service.impl.WithdrawalRequestServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalRequestServiceTest {

        @Mock
        private WithdrawalRequestRepository withdrawalRequestRepository;

        @Mock
        private OrganizerWalletRepository organizerWalletRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private WithdrawalRequestServiceImpl withdrawalRequestService;

        private User organizer;
        private OrganizerWallet wallet;

        @BeforeEach
        void setUp() {
                organizer = new User();
                organizer.setId(1);
                organizer.setEmail("organizer@gmail.com");
                organizer.setFullName("Nguyễn Văn Tổ Chức");
                organizer.setRole(Role.ORGANIZER);
                organizer.setStatus("ACTIVE");

                wallet = new OrganizerWallet();
                wallet.setId(10);
                wallet.setOrganizer(organizer);
                wallet.setAvailableBalance(new BigDecimal("42300000.00"));
                wallet.setWithdrawalPendingBalance(new BigDecimal("5000000.00"));
                wallet.setPendingBalance(BigDecimal.ZERO);
                wallet.setTotalWithdrawn(BigDecimal.ZERO);
        }

        @Nested
        @DisplayName("Pre-conditions & Access Control")
        class PreconditionsTests {

                @Test
                @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản organizer không tồn tại")
                void shouldThrowForbiddenWhenOrganizerNotFound() {
                        when(userRepository.findById(1)).thenReturn(Optional.empty());

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                                                assertThat(e.getCode()).isEqualTo("FORBIDDEN");
                                        });
                }

                @Test
                @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản không phải ORGANIZER")
                void shouldThrowForbiddenWhenNotOrganizer() {
                        organizer.setRole(Role.CUSTOMER);
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                                                assertThat(e.getCode()).isEqualTo("FORBIDDEN");
                                        });
                }

                @Test
                @DisplayName("Ném lỗi FORBIDDEN nếu tài khoản bị khóa")
                void shouldThrowForbiddenWhenAccountInactive() {
                        organizer.setStatus("LOCKED");
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                                        });
                }
        }

        @Nested
        @DisplayName("Exception 3.1 — Số tiền > số dư khả dụng")
        class InsufficientBalanceTests {

                @Test
                @DisplayName("Ném lỗi INSUFFICIENT_BALANCE khi số tiền vượt quá số dư khả dụng")
                void shouldThrowInsufficientBalanceWhenAmountExceedsAvailable() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("50000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INSUFFICIENT_BALANCE");
                                        });

                        verify(withdrawalRequestRepository, never()).save(any());
                }

                @Test
                @DisplayName("Ném lỗi INSUFFICIENT_BALANCE khi ví không tồn tại")
                void shouldThrowInsufficientBalanceWhenWalletNotFound() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.empty());

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INSUFFICIENT_BALANCE");
                                        });
                }
        }

        @Nested
        @DisplayName("Exception 3.2 — Số tiền <= 0")
        class InvalidAmountTests {

                @Test
                @DisplayName("Ném lỗi INVALID_AMOUNT khi số tiền bằng 0")
                void shouldThrowInvalidAmountWhenAmountIsZero() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        var request = new CreateWithdrawalRequest(
                                        BigDecimal.ZERO, "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INVALID_AMOUNT");
                                        });
                }

                @Test
                @DisplayName("Ném lỗi INVALID_AMOUNT khi số tiền âm")
                void shouldThrowInvalidAmountWhenAmountIsNegative() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("-5000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.createWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INVALID_AMOUNT");
                                        });
                }
        }

        @Nested
        @DisplayName("Luồng chính — Tạo yêu cầu rút tiền thành công")
        class SuccessTests {

                @Test
                @DisplayName("Tạo yêu cầu rút tiền thành công, trừ số dư khả dụng, cộng chờ rút")
                void shouldCreateWithdrawalRequestSuccessfully() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        WithdrawalRequest saved = new WithdrawalRequest();
                        saved.setId(1);
                        saved.setOrganizerWallet(wallet);
                        saved.setAmount(new BigDecimal("10000000.00"));
                        saved.setBankName("Vietcombank");
                        saved.setAccountNumber("0123456789");
                        saved.setStatus("PENDING");

                        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenReturn(saved);

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        WithdrawalRequestResponse response = withdrawalRequestService.createWithdrawalRequest(1,
                                        request);

                        assertThat(response.id()).isEqualTo(1);
                        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
                        assertThat(response.bankName()).isEqualTo("Vietcombank");
                        assertThat(response.accountNumber()).isEqualTo("0123456789");
                        assertThat(response.status()).isEqualTo("PENDING");
                        assertThat(response.availableBalanceAfter())
                                        .isEqualByComparingTo(new BigDecimal("32300000.00"));

                        // Kiểm tra ví đã được cập nhật đúng
                        assertThat(wallet.getAvailableBalance())
                                        .isEqualByComparingTo(new BigDecimal("32300000.00"));
                        assertThat(wallet.getWithdrawalPendingBalance())
                                        .isEqualByComparingTo(new BigDecimal("15000000.00"));

                        verify(organizerWalletRepository).save(wallet);
                        verify(withdrawalRequestRepository).save(any(WithdrawalRequest.class));
                }

                @Test
                @DisplayName("Rút hết số dư khả dụng thành công")
                void shouldAllowWithdrawingEntireAvailableBalance() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        WithdrawalRequest saved = new WithdrawalRequest();
                        saved.setId(2);
                        saved.setOrganizerWallet(wallet);
                        saved.setAmount(new BigDecimal("42300000.00"));
                        saved.setBankName("Techcombank");
                        saved.setAccountNumber("9876543210");
                        saved.setStatus("PENDING");

                        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenReturn(saved);

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("42300000"), "Techcombank", "9876543210");

                        WithdrawalRequestResponse response = withdrawalRequestService.createWithdrawalRequest(1,
                                        request);

                        assertThat(response.availableBalanceAfter())
                                        .isEqualByComparingTo(BigDecimal.ZERO);
                        assertThat(wallet.getAvailableBalance())
                                        .isEqualByComparingTo(BigDecimal.ZERO);
                        assertThat(wallet.getWithdrawalPendingBalance())
                                        .isEqualByComparingTo(new BigDecimal("47300000.00"));
                }

                @Test
                @DisplayName("Kiểm tra thông tin WithdrawalRequest được lưu đúng")
                void shouldSaveWithdrawalRequestWithCorrectFields() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        WithdrawalRequest saved = new WithdrawalRequest();
                        saved.setId(3);
                        saved.setAmount(new BigDecimal("15000000.00"));
                        saved.setBankName("BIDV");
                        saved.setAccountNumber("1122334455");
                        saved.setStatus("PENDING");

                        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenReturn(saved);

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("15000000"), "BIDV", "1122334455");

                        withdrawalRequestService.createWithdrawalRequest(1, request);

                        ArgumentCaptor<WithdrawalRequest> captor = ArgumentCaptor.forClass(WithdrawalRequest.class);
                        verify(withdrawalRequestRepository).save(captor.capture());

                        WithdrawalRequest captured = captor.getValue();
                        assertThat(captured.getOrganizerWallet()).isEqualTo(wallet);
                        assertThat(captured.getAmount()).isEqualByComparingTo(new BigDecimal("15000000.00"));
                        assertThat(captured.getBankName()).isEqualTo("BIDV");
                        assertThat(captured.getAccountNumber()).isEqualTo("1122334455");
                        assertThat(captured.getStatus()).isEqualTo("PENDING");
                        assertThat(captured.getRequestedAt()).isNotNull();
                }
        }

        @Nested
        @DisplayName("Bước 4 — Xác nhận & Xem trước yêu cầu rút tiền")
        class PreviewWithdrawalRequestTests {

                @Test
                @DisplayName("Xem trước thành công khi thông tin hợp lệ và đủ số dư")
                void shouldPreviewSuccessfully() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("10000000"), "Vietcombank", "0123456789");

                        var preview = withdrawalRequestService.previewWithdrawalRequest(1, request);

                        assertThat(preview.amount()).isEqualByComparingTo(new BigDecimal("10000000.00"));
                        assertThat(preview.formattedAmount()).isEqualTo("10.000.000đ");
                        assertThat(preview.bankName()).isEqualTo("Vietcombank");
                        assertThat(preview.accountNumber()).isEqualTo("0123456789");
                        assertThat(preview.currentAvailableBalance())
                                        .isEqualByComparingTo(new BigDecimal("42300000.00"));
                        assertThat(preview.formattedCurrentAvailableBalance()).isEqualTo("42.300.000đ");
                        assertThat(preview.availableBalanceAfter()).isEqualByComparingTo(new BigDecimal("32300000.00"));
                        assertThat(preview.formattedAvailableBalanceAfter()).isEqualTo("32.300.000đ");

                        // Không thay đổi số dư ví trong DB khi xem trước
                        verify(organizerWalletRepository, never()).save(any());
                        verify(withdrawalRequestRepository, never()).save(any());
                }

                @Test
                @DisplayName("Ném lỗi INSUFFICIENT_BALANCE khi xem trước với số tiền vượt quá số dư")
                void shouldThrowInsufficientBalanceOnPreview() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(organizerWalletRepository.findByOrganizerId(1)).thenReturn(Optional.of(wallet));

                        var request = new CreateWithdrawalRequest(
                                        new BigDecimal("50000000"), "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.previewWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INSUFFICIENT_BALANCE");
                                        });
                }

                @Test
                @DisplayName("Ném lỗi INVALID_AMOUNT khi xem trước với số tiền <= 0")
                void shouldThrowInvalidAmountOnPreview() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        var request = new CreateWithdrawalRequest(
                                        BigDecimal.ZERO, "Vietcombank", "0123456789");

                        assertThatThrownBy(() -> withdrawalRequestService.previewWithdrawalRequest(1, request))
                                        .isInstanceOf(WithdrawalRequestException.class)
                                        .satisfies(ex -> {
                                                var e = (WithdrawalRequestException) ex;
                                                assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                                                assertThat(e.getCode()).isEqualTo("INVALID_AMOUNT");
                                        });
                }
        }

        @Nested
        @DisplayName("Lấy danh sách yêu cầu rút tiền của Organizer")
        class GetWithdrawalRequestsTests {

                @Test
                @DisplayName("Trả về danh sách yêu cầu rút tiền kèm format tiền tệ và trạng thái")
                void shouldReturnListOfWithdrawalRequests() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));

                        WithdrawalRequest r1 = new WithdrawalRequest();
                        r1.setId(10);
                        r1.setAmount(new BigDecimal("5000000.00"));
                        r1.setBankName("Vietcombank");
                        r1.setAccountNumber("1234567890");
                        r1.setStatus("PENDING");
                        r1.setRequestedAt(LocalDateTime.of(2026, 10, 20, 10, 0));

                        WithdrawalRequest r2 = new WithdrawalRequest();
                        r2.setId(9);
                        r2.setAmount(new BigDecimal("20000000.00"));
                        r2.setBankName("Techcombank");
                        r2.setAccountNumber("0987654321");
                        r2.setStatus("APPROVED");
                        r2.setRequestedAt(LocalDateTime.of(2026, 10, 15, 8, 30));

                        when(withdrawalRequestRepository.findByOrganizerIdOrderByRequestedAtDesc(1))
                                        .thenReturn(List.of(r1, r2));

                        List<WithdrawalRequestResponse> result = withdrawalRequestService.getWithdrawalRequests(1);

                        assertThat(result).hasSize(2);
                        assertThat(result.get(0).id()).isEqualTo(10);
                        assertThat(result.get(0).formattedAmount()).isEqualTo("5.000.000đ");
                        assertThat(result.get(0).status()).isEqualTo("PENDING");
                        assertThat(result.get(0).statusDisplay()).isEqualTo("Chờ duyệt");

                        assertThat(result.get(1).id()).isEqualTo(9);
                        assertThat(result.get(1).formattedAmount()).isEqualTo("20.000.000đ");
                        assertThat(result.get(1).status()).isEqualTo("APPROVED");
                        assertThat(result.get(1).statusDisplay()).isEqualTo("Đã duyệt");
                }

                @Test
                @DisplayName("Trả về danh sách rỗng nếu Organizer chưa từng gửi yêu cầu rút tiền")
                void shouldReturnEmptyListWhenNoRequests() {
                        when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
                        when(withdrawalRequestRepository.findByOrganizerIdOrderByRequestedAtDesc(1))
                                        .thenReturn(List.of());

                        List<WithdrawalRequestResponse> result = withdrawalRequestService.getWithdrawalRequests(1);

                        assertThat(result).isEmpty();
                }
        }
}
