package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingDetail;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingOverview;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingPageResponse;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingSummary;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.OrganizerBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizerBookingControllerTest {

    @Mock
    private OrganizerBookingService bookingService;

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

        var controller = new OrganizerBookingController(bookingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/organizer/bookings - Trả về 200 OK và danh sách booking phân trang")
    void shouldReturn200AndBookingPage() throws Exception {
        var summary = new OrganizerBookingSummary(
                100,
                "PAID",
                "Đã thanh toán",
                new BigDecimal("500000.00"),
                "500.000 đ",
                2,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                1,
                "Sự kiện âm nhạc",
                10,
                "Khách hàng A",
                "customerA@example.com",
                "0909999999"
        );
        var overview = new OrganizerBookingOverview(
                1, new BigDecimal("500000.00"), "500.000 đ", 1, 0, 0, 0
        );
        var pageRes = new OrganizerBookingPageResponse(
                List.of(summary), 0, 10, 1, 1, overview
        );

        when(bookingService.getBookings(eq(1), any(), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(pageRes);

        mockMvc.perform(get("/api/organizer/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(100))
                .andExpect(jsonPath("$.items[0].status").value("PAID"))
                .andExpect(jsonPath("$.items[0].customerName").value("Khách hàng A"))
                .andExpect(jsonPath("$.overview.totalBookings").value(1));
    }

    @Test
    @DisplayName("GET /api/organizer/bookings/{id} - Trả về 200 OK và chi tiết booking")
    void shouldReturn200AndBookingDetail() throws Exception {
        var detail = new OrganizerBookingDetail(
                100,
                "PAID",
                "Đã thanh toán",
                new BigDecimal("500000.00"),
                "500.000 đ",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                1,
                "Sự kiện âm nhạc",
                "TP.HCM",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(3),
                10,
                "Khách hàng A",
                "customerA@example.com",
                "0909999999",
                List.of(),
                List.of(),
                List.of(),
                null
        );

        when(bookingService.getBookingDetail(eq(1), eq(100))).thenReturn(detail);

        mockMvc.perform(get("/api/organizer/bookings/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.eventName").value("Sự kiện âm nhạc"));
    }

    @Test
    @DisplayName("GET /api/organizer/bookings/export-csv - Xuất CSV thành công")
    void shouldExportCsv() throws Exception {
        byte[] csv = "\uFEFFMã đơn,Tên khách hàng\n#100,Khách hàng A".getBytes(StandardCharsets.UTF_8);
        when(bookingService.exportBookingsCsv(eq(1), any(), any(), any(), any(), any())).thenReturn(csv);

        mockMvc.perform(get("/api/organizer/bookings/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=")))
                .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }
}
