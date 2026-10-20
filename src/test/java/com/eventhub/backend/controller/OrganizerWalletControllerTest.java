package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerWalletResponse;
import com.eventhub.backend.dto.response.OrganizerWalletResponse.EventRevenueResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.exception.OrganizerWalletException;
import com.eventhub.backend.service.OrganizerWalletService;
import java.math.BigDecimal;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizerWalletControllerTest {

    @Mock
    private OrganizerWalletService organizerWalletService;

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

        var controller = new OrganizerWalletController(organizerWalletService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/organizer/wallet - Trả về 200 OK với thông tin số dư ví và doanh thu sự kiện")
    void shouldReturn200AndWalletInformation() throws Exception {
        var rev1 = new EventRevenueResponse(10, "Đêm nhạc Acoustic Thu", new BigDecimal("97500000.00"), "97.500.000đ", 65, 100);
        var rev2 = new EventRevenueResponse(20, "Hội thảo Công nghệ AI", new BigDecimal("105000000.00"), "105.000.000đ", 210, 300);

        var response = new OrganizerWalletResponse(
                new BigDecimal("15000000.00"),
                "15.000.000đ",
                new BigDecimal("42300000.00"),
                "42.300.000đ",
                new BigDecimal("5000000.00"),
                "5.000.000đ",
                new BigDecimal("80000000.00"),
                "80.000.000đ",
                List.of(rev1, rev2)
        );

        when(organizerWalletService.getWalletAndRevenue(eq(1))).thenReturn(response);

        mockMvc.perform(get("/api/organizer/wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingBalance").value(15000000.00))
                .andExpect(jsonPath("$.formattedPendingBalance").value("15.000.000đ"))
                .andExpect(jsonPath("$.availableBalance").value(42300000.00))
                .andExpect(jsonPath("$.formattedAvailableBalance").value("42.300.000đ"))
                .andExpect(jsonPath("$.withdrawalPendingBalance").value(5000000.00))
                .andExpect(jsonPath("$.formattedWithdrawalPendingBalance").value("5.000.000đ"))
                .andExpect(jsonPath("$.totalWithdrawn").value(80000000.00))
                .andExpect(jsonPath("$.formattedTotalWithdrawn").value("80.000.000đ"))
                .andExpect(jsonPath("$.eventRevenues.length()").value(2))
                .andExpect(jsonPath("$.eventRevenues[0].eventName").value("Đêm nhạc Acoustic Thu"))
                .andExpect(jsonPath("$.eventRevenues[0].formattedRevenue").value("97.500.000đ"))
                .andExpect(jsonPath("$.eventRevenues[1].eventName").value("Hội thảo Công nghệ AI"))
                .andExpect(jsonPath("$.eventRevenues[1].formattedRevenue").value("105.000.000đ"));
    }

    @Test
    @DisplayName("GET /api/organizer/revenue - Định tuyến thành công và trả về 200 OK")
    void shouldReturn200ForRevenuePath() throws Exception {
        var response = new OrganizerWalletResponse(
                BigDecimal.ZERO, "0đ",
                BigDecimal.ZERO, "0đ",
                BigDecimal.ZERO, "0đ",
                BigDecimal.ZERO, "0đ",
                List.of()
        );

        when(organizerWalletService.getWalletAndRevenue(eq(1))).thenReturn(response);

        mockMvc.perform(get("/api/organizer/revenue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(0))
                .andExpect(jsonPath("$.eventRevenues").isEmpty());
    }

    @Test
    @DisplayName("GET /api/organizer/wallet - Trả về 403 FORBIDDEN khi người dùng không có quyền")
    void shouldReturn403WhenForbidden() throws Exception {
        when(organizerWalletService.getWalletAndRevenue(eq(1)))
                .thenThrow(new OrganizerWalletException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Chỉ Organizer đang hoạt động mới có quyền truy cập"));

        mockMvc.perform(get("/api/organizer/wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Chỉ Organizer đang hoạt động mới có quyền truy cập"));
    }
}
