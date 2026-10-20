package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.CreateWithdrawalRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestPreviewResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.exception.WithdrawalRequestException;
import com.eventhub.backend.service.WithdrawalRequestService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WithdrawalRequestControllerTest {

    @Mock
    private WithdrawalRequestService withdrawalRequestService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserResponse mockOrganizer = new UserResponse(
            1,
            "organizer@example.com",
            "Organizer Name",
            "0901234567",
            Role.ORGANIZER
    );

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockOrganizer;
            }
        };

        var controller = new WithdrawalRequestController(withdrawalRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals - Trả về 201 Created khi gửi yêu cầu rút tiền hợp lệ")
    void shouldReturn201WhenCreateWithdrawalRequestSuccess() throws Exception {
        var response = new WithdrawalRequestResponse(
                100,
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                "Vietcombank",
                "1234567890",
                "PENDING",
                LocalDateTime.now(),
                new BigDecimal("37300000.00"),
                "37.300.000đ"
        );

        when(withdrawalRequestService.createWithdrawalRequest(eq(1), any(CreateWithdrawalRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "amount": 5000000,
                    "bankName": "Vietcombank",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.amount").value(5000000.00))
                .andExpect(jsonPath("$.formattedAmount").value("5.000.000đ"))
                .andExpect(jsonPath("$.bankName").value("Vietcombank"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.availableBalanceAfter").value(37300000.00))
                .andExpect(jsonPath("$.formattedAvailableBalanceAfter").value("37.300.000đ"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals - Trả về 400 Bad Request khi amount null hoặc <= 0")
    void shouldReturn400WhenAmountIsInvalid() throws Exception {
        String requestJson = """
                {
                    "amount": 0,
                    "bankName": "Vietcombank",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals - Trả về 400 Bad Request khi thiếu thông tin ngân hàng")
    void shouldReturn400WhenBankNameIsBlank() throws Exception {
        String requestJson = """
                {
                    "amount": 1000000,
                    "bankName": "   ",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals - Trả về 400 Bad Request khi số tiền rút vượt quá số dư khả dụng")
    void shouldReturn400WhenAmountExceedsAvailableBalance() throws Exception {
        when(withdrawalRequestService.createWithdrawalRequest(eq(1), any(CreateWithdrawalRequest.class)))
                .thenThrow(new WithdrawalRequestException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE",
                        "Số tiền yêu cầu rút vượt quá số dư khả dụng"));

        String requestJson = """
                {
                    "amount": 100000000,
                    "bankName": "Vietcombank",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_BALANCE"))
                .andExpect(jsonPath("$.message").value("Số tiền yêu cầu rút vượt quá số dư khả dụng"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals - Trả về 403 Forbidden khi tài khoản organizer bị khóa")
    void shouldReturn403WhenOrganizerIsLocked() throws Exception {
        when(withdrawalRequestService.createWithdrawalRequest(eq(1), any(CreateWithdrawalRequest.class)))
                .thenThrow(new WithdrawalRequestException(HttpStatus.FORBIDDEN, "ORGANIZER_LOCKED",
                        "Tài khoản Nhà tổ chức đã bị khóa"));

        String requestJson = """
                {
                    "amount": 5000000,
                    "bankName": "Vietcombank",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ORGANIZER_LOCKED"))
                .andExpect(jsonPath("$.message").value("Tài khoản Nhà tổ chức đã bị khóa"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals/preview - Trả về 200 OK cùng dữ liệu xác nhận trước khi rút")
    void shouldReturn200WhenPreviewSuccess() throws Exception {
        var previewResponse = new WithdrawalRequestPreviewResponse(
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                "Vietcombank",
                "1234567890",
                new BigDecimal("42300000.00"),
                "42.300.000đ",
                new BigDecimal("37300000.00"),
                "37.300.000đ"
        );

        when(withdrawalRequestService.previewWithdrawalRequest(eq(1), any(CreateWithdrawalRequest.class)))
                .thenReturn(previewResponse);

        String requestJson = """
                {
                    "amount": 5000000,
                    "bankName": "Vietcombank",
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/organizer/withdrawals/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000000.00))
                .andExpect(jsonPath("$.formattedAmount").value("5.000.000đ"))
                .andExpect(jsonPath("$.bankName").value("Vietcombank"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.currentAvailableBalance").value(42300000.00))
                .andExpect(jsonPath("$.formattedCurrentAvailableBalance").value("42.300.000đ"))
                .andExpect(jsonPath("$.availableBalanceAfter").value(37300000.00))
                .andExpect(jsonPath("$.formattedAvailableBalanceAfter").value("37.300.000đ"));
    }

    @Test
    @DisplayName("GET /api/organizer/withdrawals - Trả về 200 OK và danh sách yêu cầu rút tiền")
    void shouldReturn200AndListOfWithdrawals() throws Exception {
        var r1 = new WithdrawalRequestResponse(
                101,
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                "Vietcombank",
                "1234567890",
                "PENDING",
                LocalDateTime.of(2026, 10, 20, 10, 0),
                null,
                null
        );

        when(withdrawalRequestService.getWithdrawalRequests(eq(1), eq(null)))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/api/organizer/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].formattedAmount").value("5.000.000đ"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].statusDisplay").value("Chờ duyệt"));
    }

    @Test
    @DisplayName("GET /api/organizer/withdrawals?status=PENDING - Lọc theo trạng thái và trả về 200 OK")
    void shouldFilterByStatusQueryParam() throws Exception {
        var r1 = new WithdrawalRequestResponse(
                101,
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                "Vietcombank",
                "1234567890",
                "PENDING",
                LocalDateTime.of(2026, 10, 20, 10, 0),
                null,
                null
        );

        when(withdrawalRequestService.getWithdrawalRequests(eq(1), eq("PENDING")))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/api/organizer/withdrawals")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/organizer/withdrawals/{id} - Trả về 200 OK và chi tiết yêu cầu rút tiền")
    void shouldReturn200AndWithdrawalDetail() throws Exception {
        var detail = new WithdrawalRequestResponse(
                5,
                new BigDecimal("10000000.00"),
                "10.000.000đ",
                "Vietcombank",
                "0123456789",
                "PENDING",
                "Chờ duyệt",
                LocalDateTime.of(2026, 9, 10, 14, 0),
                null,
                null,
                null,
                null,
                null,
                true
        );

        when(withdrawalRequestService.getWithdrawalRequestDetail(eq(1), eq(5)))
                .thenReturn(detail);

        mockMvc.perform(get("/api/organizer/withdrawals/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.formattedAmount").value("10.000.000đ"))
                .andExpect(jsonPath("$.bankName").value("Vietcombank"))
                .andExpect(jsonPath("$.accountNumber").value("0123456789"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.statusDisplay").value("Chờ duyệt"))
                .andExpect(jsonPath("$.canCancel").value(true));
    }

    @Test
    @DisplayName("GET /api/organizer/withdrawals/{id} - Trả về 404 NOT_FOUND khi không tìm thấy yêu cầu")
    void shouldReturn404WhenDetailNotFound() throws Exception {
        when(withdrawalRequestService.getWithdrawalRequestDetail(eq(1), eq(999)))
                .thenThrow(new WithdrawalRequestException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "Không tìm thấy yêu cầu rút tiền"));

        mockMvc.perform(get("/api/organizer/withdrawals/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Không tìm thấy yêu cầu rút tiền"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals/{id}/cancel - Hủy yêu cầu PENDING thành công trả về 200 OK")
    void shouldCancelPendingWithdrawalSuccessfully() throws Exception {
        var cancelled = new WithdrawalRequestResponse(
                5,
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                "Vietcombank",
                "0123456789",
                "CANCELLED",
                "Đã hủy",
                LocalDateTime.of(2026, 9, 10, 14, 0),
                new BigDecimal("47300000.00"),
                "47.300.000đ",
                null,
                null,
                null,
                false
        );

        when(withdrawalRequestService.cancelWithdrawalRequest(eq(1), eq(5)))
                .thenReturn(cancelled);

        mockMvc.perform(post("/api/organizer/withdrawals/5/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.statusDisplay").value("Đã hủy"))
                .andExpect(jsonPath("$.canCancel").value(false))
                .andExpect(jsonPath("$.availableBalanceAfter").value(47300000.00))
                .andExpect(jsonPath("$.formattedAvailableBalanceAfter").value("47.300.000đ"));
    }

    @Test
    @DisplayName("POST /api/organizer/withdrawals/{id}/cancel - Trả về 400 Bad Request khi hủy yêu cầu đã duyệt")
    void shouldReturn400WhenCancellingApprovedWithdrawal() throws Exception {
        when(withdrawalRequestService.cancelWithdrawalRequest(eq(1), eq(5)))
                .thenThrow(new WithdrawalRequestException(HttpStatus.BAD_REQUEST, "CANNOT_CANCEL_REQUEST",
                        "Chỉ có thể hủy yêu cầu rút tiền đang ở trạng thái Chờ duyệt"));

        mockMvc.perform(post("/api/organizer/withdrawals/5/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CANNOT_CANCEL_REQUEST"))
                .andExpect(jsonPath("$.message").value("Chỉ có thể hủy yêu cầu rút tiền đang ở trạng thái Chờ duyệt"));
    }
}
