package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AddEventStaffRequest;
import com.eventhub.backend.dto.request.UpdateEventStaffStatusRequest;
import com.eventhub.backend.dto.response.EventStaffResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.EventStaffService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventStaffControllerTest {

    @Mock
    private EventStaffService eventStaffService;

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
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().equals(UserResponse.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return mockOrganizer;
            }
        };

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(objectMapper);
        EventStaffController controller = new EventStaffController(eventStaffService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void shouldListStaffsSuccessfully() throws Exception {
        EventStaffResponse staffResponse = new EventStaffResponse(
                101, 2, "Nguyễn Đăng Nam", "nguyennam60yb@gmail.com", "0900000000",
                "ACTIVE", false, LocalDateTime.now()
        );
        when(eventStaffService.getStaffs(1, 10)).thenReturn(List.of(staffResponse));

        mockMvc.perform(get("/api/events/10/staffs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("nguyennam60yb@gmail.com"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void shouldAddStaffSuccessfully() throws Exception {
        EventStaffResponse staffResponse = new EventStaffResponse(
                101, 2, "Nguyễn Đăng Nam", "nguyennam60yb@gmail.com", "0900000000",
                "ACTIVE", false, LocalDateTime.now()
        );
        when(eventStaffService.addStaff(eq(1), eq(10), any(AddEventStaffRequest.class)))
                .thenReturn(staffResponse);

        AddEventStaffRequest request = new AddEventStaffRequest("nguyennam60yb@gmail.com");

        mockMvc.perform(post("/api/events/10/staffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nguyennam60yb@gmail.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectAddStaffWhenEmailIsBlank() throws Exception {
        mockMvc.perform(post("/api/events/10/staffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldUpdateStaffStatusSuccessfully() throws Exception {
        EventStaffResponse staffResponse = new EventStaffResponse(
                101, 2, "Nguyễn Đăng Nam", "nguyennam60yb@gmail.com", "0900000000",
                "LOCKED", false, LocalDateTime.now()
        );
        when(eventStaffService.updateStaffStatus(eq(1), eq(10), eq(101), any(UpdateEventStaffStatusRequest.class)))
                .thenReturn(staffResponse);

        UpdateEventStaffStatusRequest request = new UpdateEventStaffStatusRequest("LOCKED");

        mockMvc.perform(patch("/api/events/10/staffs/101/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }

    @Test
    void shouldDeleteStaffSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/events/10/staffs/101"))
                .andExpect(status().isNoContent());

        verify(eventStaffService).deleteStaff(1, 10, 101);
    }
}
