package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.EventStatisticResponse;
import com.eventhub.backend.dto.response.EventStatisticResponse.TicketTypeStatisticResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStatisticException;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.EventStatisticService;
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
class EventStatisticControllerTest {

    @Mock
    private EventStatisticService eventStatisticService;

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

        var controller = new EventStatisticController(eventStatisticService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/events/{eventId}/statistics - Trả về 200 OK và thống kê sự kiện thành công")
    void shouldReturn200AndEventStatistics() throws Exception {
        var vip = new TicketTypeStatisticResponse(
                101, "Vé VIP", new BigDecimal("1500000.00"), 100, 65, 35, 65.0, "65%", new BigDecimal("97500000.00")
        );
        var standard = new TicketTypeStatisticResponse(
                102, "Vé Thường", new BigDecimal("500000.00"), 300, 210, 90, 70.0, "70%", new BigDecimal("105000000.00")
        );
        var response = new EventStatisticResponse(
                10,
                "Đại Nhạc Hội Mùa Thu",
                275,
                400,
                125,
                "275 / 400",
                new BigDecimal("202500000.00"),
                68.75,
                "68.75%",
                List.of(vip, standard)
        );

        when(eventStatisticService.getEventStatistics(eq(1), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/events/10/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(10))
                .andExpect(jsonPath("$.eventName").value("Đại Nhạc Hội Mùa Thu"))
                .andExpect(jsonPath("$.totalSoldTickets").value(275))
                .andExpect(jsonPath("$.totalTickets").value(400))
                .andExpect(jsonPath("$.totalRemainingTickets").value(125))
                .andExpect(jsonPath("$.soldRatioFormatted").value("275 / 400"))
                .andExpect(jsonPath("$.totalRevenue").value(202500000.00))
                .andExpect(jsonPath("$.overallSalesRate").value(68.75))
                .andExpect(jsonPath("$.formattedOverallSalesRate").value("68.75%"))
                .andExpect(jsonPath("$.ticketTypes.length()").value(2))
                .andExpect(jsonPath("$.ticketTypes[0].name").value("Vé VIP"))
                .andExpect(jsonPath("$.ticketTypes[0].soldQuantity").value(65))
                .andExpect(jsonPath("$.ticketTypes[0].formattedSalesRate").value("65%"))
                .andExpect(jsonPath("$.ticketTypes[1].name").value("Vé Thường"))
                .andExpect(jsonPath("$.ticketTypes[1].soldQuantity").value(210))
                .andExpect(jsonPath("$.ticketTypes[1].formattedSalesRate").value("70%"));
    }

    @Test
    @DisplayName("GET /api/events/{eventId}/statistics - Trả về 400 khi sự kiện chưa duyệt hoặc chưa mở bán")
    void shouldReturn400WhenEventNotApprovedOrNotOpenForSale() throws Exception {
        when(eventStatisticService.getEventStatistics(eq(1), eq(10)))
                .thenThrow(new EventStatisticException(HttpStatus.BAD_REQUEST, "EVENT_NOT_OPEN_FOR_SALE",
                        "Sự kiện chưa được duyệt hoặc chưa mở bán"));

        mockMvc.perform(get("/api/events/10/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_OPEN_FOR_SALE"))
                .andExpect(jsonPath("$.message").value("Sự kiện chưa được duyệt hoặc chưa mở bán"));
    }

    @Test
    @DisplayName("GET /api/events/{eventId}/statistics - Trả về 404 khi không tìm thấy sự kiện")
    void shouldReturn404WhenEventNotFound() throws Exception {
        when(eventStatisticService.getEventStatistics(eq(1), eq(999)))
                .thenThrow(new EventStatisticException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND",
                        "Không tìm thấy sự kiện"));

        mockMvc.perform(get("/api/events/999/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/events/{eventId}/statistics - Trả về 403 khi organizer không có quyền truy cập sự kiện")
    void shouldReturn403WhenForbidden() throws Exception {
        when(eventStatisticService.getEventStatistics(eq(1), eq(10)))
                .thenThrow(new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Bạn không có quyền xem thống kê sự kiện này"));

        mockMvc.perform(get("/api/events/10/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
