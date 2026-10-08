package com.eventhub.backend.controller;

import com.eventhub.backend.dto.UserDetailDto;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EmailAlreadyExistsException;
import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.UserService;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.mock.web.MockMultipartFile;
import com.eventhub.backend.dto.request.UploadAvatarRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserResponse mockAuthUser = new UserResponse(
            1,
            "namnguyenhai08012004@gmail.com",
            "Nguyễn Hải Nam",
            "0522899888",
            Role.CUSTOMER
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
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return mockAuthUser;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/users/me should return 200 and UserDetailDto with dateOfBirth")
    void getMyProfile_ShouldReturn200AndDetail() throws Exception {
        UserDetailDto responseDto = new UserDetailDto(
                1,
                "Nguyễn Hải Nam",
                "namnguyenhai08012004@gmail.com",
                "0522899888",
                Role.CUSTOMER,
                "ACTIVE",
                LocalDate.of(1996, 8, 15),
                "Nam",
                null
        );

        when(userService.getUserDetail(1)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Hải Nam"))
                .andExpect(jsonPath("$.email").value("namnguyenhai08012004@gmail.com"))
                .andExpect(jsonPath("$.phone").value("0522899888"))
                .andExpect(jsonPath("$.dateOfBirth").value("1996-08-15"))
                .andExpect(jsonPath("$.gender").value("Nam"));

        verify(userService).getUserDetail(1);
    }

    @Test
    @DisplayName("PUT /api/users/me with valid dateOfBirth should return 200")
    void updateMyProfile_WithValidDateOfBirth_ShouldReturn200() throws Exception {
        LocalDate dob = LocalDate.of(1996, 8, 15);
        UserDetailDto responseDto = new UserDetailDto(
                1,
                "Nguyễn Hải Nam",
                "namnguyenhai08012004@gmail.com",
                "0522899888",
                Role.CUSTOMER,
                "ACTIVE",
                dob,
                "Nam",
                null
        );

        when(userService.updateUserDetail(eq(1), any(UserDetailDto.class))).thenReturn(responseDto);

        String jsonPayload = """
                {
                    "fullName": "Nguyễn Hải Nam",
                    "email": "namnguyenhai08012004@gmail.com",
                    "phone": "0522899888",
                    "dateOfBirth": "1996-08-15",
                    "gender": "Nam"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfBirth").value("1996-08-15"))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Hải Nam"));

        verify(userService).updateUserDetail(eq(1), any(UserDetailDto.class));
    }

    @Test
    @DisplayName("PUT /api/users/me with future dateOfBirth should return 400 validation error")
    void updateMyProfile_WithFutureDateOfBirth_ShouldReturn400() throws Exception {
        String futureDate = LocalDate.now().plusDays(2).toString();
        String jsonPayload = """
                {
                    "fullName": "Nguyễn Hải Nam",
                    "email": "namnguyenhai08012004@gmail.com",
                    "phone": "0522899888",
                    "dateOfBirth": "%s"
                }
                """.formatted(futureDate);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.dateOfBirth").value("Ngày sinh phải trước ngày hiện tại"));
    }

    @Test
    @DisplayName("PUT /api/users/me with invalid email should return 400 validation error")
    void updateMyProfile_WithInvalidEmail_ShouldReturn400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "Nguyễn Hải Nam",
                    "email": "not-valid-email",
                    "phone": "0522899888",
                    "dateOfBirth": "1996-08-15"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("Email không đúng định dạng (ví dụ: example@domain.com)"));
    }

    @Test
    @DisplayName("PUT /api/users/me with invalid phone should return 400 validation error")
    void updateMyProfile_WithInvalidPhone_ShouldReturn400() throws Exception {
        String jsonPayload = """
                {
                    "fullName": "Nguyễn Hải Nam",
                    "email": "namnguyenhai08012004@gmail.com",
                    "phone": "12345",
                    "dateOfBirth": "1996-08-15"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.phone").value("Số điện thoại không đúng định dạng (gồm 10 chữ số, ví dụ: 0908123456)"));
    }

    @Test
    @DisplayName("PUT /api/users/me when duplicate email should return 409 conflict")
    void updateMyProfile_WhenDuplicateEmail_ShouldReturn409() throws Exception {
        when(userService.updateUserDetail(eq(1), any(UserDetailDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email đã được sử dụng bởi tài khoản khác"));

        String jsonPayload = """
                {
                    "fullName": "Nguyễn Hải Nam",
                    "email": "existing@gmail.com",
                    "phone": "0522899888",
                    "dateOfBirth": "1996-08-15"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email đã được sử dụng bởi tài khoản khác"));
    }

    @Test
    @DisplayName("PUT /api/users/me/password with valid passwords should return 200")
    void changeMyPassword_WhenValid_ShouldReturn200() throws Exception {
        String jsonPayload = """
                {
                    "currentPassword": "OldPassword123@",
                    "newPassword": "NewPassword456@",
                    "confirmPassword": "NewPassword456@"
                }
                """;

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));
    }

    @Test
    @DisplayName("PUT /api/users/me/password with short new password should return 400 validation error")
    void changeMyPassword_WhenShortPassword_ShouldReturn400() throws Exception {
        String jsonPayload = """
                {
                    "currentPassword": "OldPassword123@",
                    "newPassword": "short",
                    "confirmPassword": "short"
                }
                """;

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.newPassword").value("Mật khẩu mới phải từ 8 đến 72 ký tự"));
    }

    @Test
    @DisplayName("POST /api/users/me/avatar should return 200 and avatarUrl")
    void uploadMyAvatar_WhenValidFile_ShouldReturn200AndAvatarUrl() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "image content".getBytes()
        );

        when(userService.updateAvatar(any(UploadAvatarRequest.class)))
                .thenReturn("https://res.cloudinary.com/dnumysqsn/image/upload/v123/avatar.jpg");

        mockMvc.perform(multipart("/api/users/me/avatar").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("https://res.cloudinary.com/dnumysqsn/image/upload/v123/avatar.jpg"))
                .andExpect(jsonPath("$.message").value("Cập nhật ảnh đại diện thành công"));

        verify(userService).updateAvatar(any(UploadAvatarRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/avatar with userId param should return 200 and avatarUrl")
    void uploadAvatar_WithUserIdParam_ShouldReturn200() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "avatar.png", "image/png", "png image content".getBytes()
        );

        when(userService.updateAvatar(any(UploadAvatarRequest.class)))
                .thenReturn("https://res.cloudinary.com/dnumysqsn/image/upload/v456/avatar.png");

        mockMvc.perform(multipart("/api/users/avatar").file(mockFile).param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("https://res.cloudinary.com/dnumysqsn/image/upload/v456/avatar.png"))
                .andExpect(jsonPath("$.message").value("Cập nhật ảnh đại diện thành công"));

        verify(userService).updateAvatar(any(UploadAvatarRequest.class));
    }
}
