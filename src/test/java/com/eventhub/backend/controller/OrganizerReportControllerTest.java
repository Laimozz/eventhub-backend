package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerReportResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.EventFilterOptionResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.ReportSummaryResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.OrganizerReportService;
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
class OrganizerReportControllerTest {

    @Mock
    private OrganizerReportService reportService;

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

        var controller = new OrganizerReportController(reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/organizer/reports - Trả về 200 OK và báo cáo tổng hợp")
    void shouldReturn200AndReports() throws Exception {
        var summary = new ReportSummaryResponse(
                new BigDecimal("50000000.00"),
                "50.000.000 đ",
                new BigDecimal("47500000.00"),
                "47.500.000 đ",
                new BigDecimal("2500000.00"),
                "2.500.000 đ",
                120,
                250,
                300,
                83.3,
                "83.3%",
                200,
                80.0,
                "80%",
                new BigDecimal("416666.67"),
                "416.667 đ",
                BigDecimal.ZERO,
                "0 đ"
        );
        var mockResponse = new OrganizerReportResponse(
                summary,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        when(reportService.getReports(eq(1), any(), eq("30d"), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/organizer/reports")
                        .param("timeRange", "30d")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.formattedTotalRevenue").value("50.000.000 đ"))
                .andExpect(jsonPath("$.summary.totalTicketsSold").value(250))
                .andExpect(jsonPath("$.summary.formattedFillRate").value("83.3%"));
    }

    @Test
    @DisplayName("GET /api/organizer/reports/events-filter - Trả về danh sách options filter sự kiện")
    void shouldReturnFilterOptions() throws Exception {
        var option = new EventFilterOptionResponse(10, "Sự kiện âm nhạc", "APPROVED", LocalDateTime.now());
        when(reportService.getEventFilterOptions(eq(1))).thenReturn(List.of(option));

        mockMvc.perform(get("/api/organizer/reports/events-filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Sự kiện âm nhạc"));
    }

    @Test
    @DisplayName("GET /api/organizer/reports/export-csv - Xuất file CSV thành công")
    void shouldExportCsv() throws Exception {
        byte[] csvBytes = "\uFEFFHeader,Col1\nVal1,Val2".getBytes(StandardCharsets.UTF_8);
        when(reportService.exportReportCsv(eq(1), any(), any(), any(), any())).thenReturn(csvBytes);

        mockMvc.perform(get("/api/organizer/reports/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=")))
                .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }
}
