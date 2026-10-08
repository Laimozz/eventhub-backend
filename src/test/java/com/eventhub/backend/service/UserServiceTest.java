package com.eventhub.backend.service;

import com.eventhub.backend.dto.UserDetailDto;
import com.eventhub.backend.dto.request.ChangePasswordRequest;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EmailAlreadyExistsException;
import com.eventhub.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import com.eventhub.backend.dto.request.UploadAvatarRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventImageService eventImageService;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        ReflectionTestUtils.setField(sampleUser, "id", 1);
        sampleUser.setEmail("namnguyenhai08012004@gmail.com");
        sampleUser.setPassword("hashedpassword");
        sampleUser.setFullName("Nguyễn Hải Nam");
        sampleUser.setPhone("0522899888");
        sampleUser.setRole(Role.CUSTOMER);
        sampleUser.setStatus("ACTIVE");
        sampleUser.setDateOfBirth(LocalDate.of(2004, 1, 8));
        sampleUser.setGender("Nam");
    }

    @Nested
    @DisplayName("getUserDetail() tests")
    class GetUserDetailTests {

        @Test
        @DisplayName("Should return UserDetailDto with full profile when user exists")
        void getUserDetail_WhenUserExists_ShouldReturnDto() {
            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            UserDetailDto result = userService.getUserDetail(1);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1);
            assertThat(result.fullName()).isEqualTo("Nguyễn Hải Nam");
            assertThat(result.email()).isEqualTo("namnguyenhai08012004@gmail.com");
            assertThat(result.phone()).isEqualTo("0522899888");
            assertThat(result.role()).isEqualTo(Role.CUSTOMER);
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.dateOfBirth()).isEqualTo(LocalDate.of(2004, 1, 8));
            assertThat(result.gender()).isEqualTo("Nam");

            verify(userRepository).findUserByIdNative(1);
        }

        @Test
        @DisplayName("Should throw 404 NOT_FOUND when user does not exist")
        void getUserDetail_WhenUserNotFound_ShouldThrow404() {
            when(userRepository.findUserByIdNative(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserDetail(999))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(ex.getReason()).contains("Người dùng không tồn tại");
                    });

            verify(userRepository).findUserByIdNative(999);
        }
    }

    @Nested
    @DisplayName("updateUserDetail() - Date of Birth tests")
    class DateOfBirthTests {

        @Test
        @DisplayName("Should successfully update date of birth when valid past date is provided")
        void updateUserDetail_WhenValidPastDateOfBirth_ShouldUpdateSuccessfully() {
            LocalDate newDob = LocalDate.of(1996, 8, 15);
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    newDob,
                    "Nam",
                    null
            );

            User updatedUser = new User();
            ReflectionTestUtils.setField(updatedUser, "id", 1);
            updatedUser.setEmail("namnguyenhai08012004@gmail.com");
            updatedUser.setFullName("Nguyễn Hải Nam");
            updatedUser.setPhone("0522899888");
            updatedUser.setRole(Role.CUSTOMER);
            updatedUser.setStatus("ACTIVE");
            updatedUser.setDateOfBirth(newDob);
            updatedUser.setGender("Nam");

            when(userRepository.findUserByIdNative(1))
                    .thenReturn(Optional.of(sampleUser))
                    .thenReturn(Optional.of(updatedUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            UserDetailDto result = userService.updateUserDetail(1, request);

            assertThat(result.dateOfBirth()).isEqualTo(newDob);
            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    eq("0522899888"),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(newDob),
                    eq("Nam")
            );
        }

        @Test
        @DisplayName("Should allow clearing date of birth when dateOfBirth is null")
        void updateUserDetail_WhenDateOfBirthIsNull_ShouldAllowUpdate() {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    null,
                    "Nam",
                    null
            );

            User updatedUser = new User();
            ReflectionTestUtils.setField(updatedUser, "id", 1);
            updatedUser.setEmail("namnguyenhai08012004@gmail.com");
            updatedUser.setFullName("Nguyễn Hải Nam");
            updatedUser.setPhone("0522899888");
            updatedUser.setRole(Role.CUSTOMER);
            updatedUser.setStatus("ACTIVE");
            updatedUser.setDateOfBirth(null);
            updatedUser.setGender("Nam");

            when(userRepository.findUserByIdNative(1))
                    .thenReturn(Optional.of(sampleUser))
                    .thenReturn(Optional.of(updatedUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            UserDetailDto result = userService.updateUserDetail(1, request);

            assertThat(result.dateOfBirth()).isNull();
            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    eq("0522899888"),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(null),
                    eq("Nam")
            );
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when date of birth is in the future")
        void updateUserDetail_WhenDateOfBirthIsInTheFuture_ShouldThrow400() {
            LocalDate futureDob = LocalDate.now().plusDays(1);
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    futureDob,
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Ngày sinh phải trước ngày hiện tại");
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when date of birth is today")
        void updateUserDetail_WhenDateOfBirthIsToday_ShouldThrow400() {
            LocalDate todayDob = LocalDate.now();
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    todayDob,
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Ngày sinh phải trước ngày hiện tại");
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateUserDetail() - Email & Duplicate Email tests")
    class EmailTests {

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is taken by another user")
        void updateUserDetail_WhenEmailTakenByAnotherUser_ShouldThrowConflict() {
            String duplicateEmail = "duplicate@example.com";
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    duplicateEmail,
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNotNative(duplicateEmail, 1)).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("Email đã được sử dụng bởi tài khoản khác");

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should allow updating when email belongs to current user")
        void updateUserDetail_WhenEmailIsSameUserEmail_ShouldSucceed() {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            UserDetailDto result = userService.updateUserDetail(1, request);

            assertThat(result).isNotNull();
            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    eq("0522899888"),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(LocalDate.of(2004, 1, 8)),
                    eq("Nam")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "plainaddress", "@missingusername.com", "username@.com", "user@site"})
        @DisplayName("Should throw 400 BAD_REQUEST when email format is invalid")
        void updateUserDetail_WhenEmailIsInvalid_ShouldThrow400(String invalidEmail) {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    invalidEmail,
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Email không đúng định dạng");
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateUserDetail() - Phone validation tests")
    class PhoneTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "123", "01234567891234", "abcdefghij", "0201234567", "0421234567"})
        @DisplayName("Should throw 400 BAD_REQUEST when phone format is invalid")
        void updateUserDetail_WhenPhoneIsInvalid_ShouldThrow400(String invalidPhone) {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    invalidPhone,
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Số điện thoại không đúng định dạng");
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0908123456", "0322899888", "0798123456", "0868123456", "+84908123456", "090 812 3456", "090-812-3456"})
        @DisplayName("Should accept valid Vietnam phone formats and normalize them")
        void updateUserDetail_WhenPhoneIsValid_ShouldAcceptAndNormalize(String validPhone) {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    validPhone,
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            UserDetailDto result = userService.updateUserDetail(1, request);

            assertThat(result).isNotNull();
            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    anyString(),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(LocalDate.of(2004, 1, 8)),
                    eq("Nam")
            );
        }
    }

    @Nested
    @DisplayName("updateUserDetail() - Full Name & Gender & Edge cases")
    class EdgeCasesTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw 400 BAD_REQUEST when full name is blank")
        void updateUserDetail_WhenFullNameIsBlank_ShouldThrow400(String blankName) {
            UserDetailDto request = new UserDetailDto(
                    null,
                    blankName,
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateUserDetail(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Họ và tên không được để trống");
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should preserve existing gender when request does not specify gender")
        void updateUserDetail_WhenGenderIsNull_ShouldPreserveExistingGender() {
            sampleUser.setGender("Nữ");
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    null,
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            userService.updateUserDetail(1, request);

            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    eq("0522899888"),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(LocalDate.of(2004, 1, 8)),
                    eq("Nữ")
            );
        }

        @Test
        @DisplayName("Should update gender when new gender is specified")
        void updateUserDetail_WhenGenderIsSpecified_ShouldUpdateGender() {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nữ",
                    null
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNotNative("namnguyenhai08012004@gmail.com", 1)).thenReturn(false);

            userService.updateUserDetail(1, request);

            verify(userRepository).updateUserDetailNative(
                    eq(1),
                    eq("Nguyễn Hải Nam"),
                    eq("0522899888"),
                    eq("namnguyenhai08012004@gmail.com"),
                    eq(LocalDate.of(2004, 1, 8)),
                    eq("Nữ")
            );
        }

        @Test
        @DisplayName("Should throw 404 NOT_FOUND when updating non-existent user")
        void updateUserDetail_WhenUserNotFound_ShouldThrow404() {
            UserDetailDto request = new UserDetailDto(
                    null,
                    "Nguyễn Hải Nam",
                    "namnguyenhai08012004@gmail.com",
                    "0522899888",
                    null,
                    null,
                    LocalDate.of(2004, 1, 8),
                    "Nam",
                    null
            );

            when(userRepository.findUserByIdNative(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserDetail(999, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(userRepository, never()).updateUserDetailNative(any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("changePassword() tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should successfully change password when current password matches and new password meets criteria")
        void changePassword_WhenValidRequest_ShouldHashAndSaveUsingNativeSql() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword123@",
                    "NewPassword456@",
                    "NewPassword456@"
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("OldPassword123@", "hashedpassword")).thenReturn(true);
            when(passwordEncoder.matches("NewPassword456@", "hashedpassword")).thenReturn(false);
            when(passwordEncoder.encode("NewPassword456@")).thenReturn("new_bcrypt_hash");

            userService.changePassword(1, request);

            verify(userRepository).updatePasswordNative(1, "new_bcrypt_hash");
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when current password is incorrect")
        void changePassword_WhenCurrentPasswordIncorrect_ShouldThrow400() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "WrongPassword123@",
                    "NewPassword456@",
                    "NewPassword456@"
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("WrongPassword123@", "hashedpassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Mật khẩu hiện tại không chính xác");
                    });

            verify(userRepository, never()).updatePasswordNative(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when new password is identical to old password")
        void changePassword_WhenNewPasswordSameAsOld_ShouldThrow400() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword123@",
                    "OldPassword123@",
                    "OldPassword123@"
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("OldPassword123@", "hashedpassword")).thenReturn(true);

            assertThatThrownBy(() -> userService.changePassword(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Mật khẩu mới không được trùng với mật khẩu hiện tại");
                    });

            verify(userRepository, never()).updatePasswordNative(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when confirmation password does not match")
        void changePassword_WhenConfirmPasswordDoesNotMatch_ShouldThrow400() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword123@",
                    "NewPassword456@",
                    "MismatchPassword456@"
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("OldPassword123@", "hashedpassword")).thenReturn(true);
            when(passwordEncoder.matches("NewPassword456@", "hashedpassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("Xác nhận mật khẩu mới không trùng khớp");
                    });

            verify(userRepository, never()).updatePasswordNative(anyInt(), anyString());
        }

        @ParameterizedTest
        @ValueSource(strings = {"short1!", "NoSpecialChar123", "no_upper_123@", "NO_LOWER_123@"})
        @DisplayName("Should throw 400 BAD_REQUEST when new password does not meet complexity rules")
        void changePassword_WhenNewPasswordWeak_ShouldThrow400(String weakPassword) {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword123@",
                    weakPassword,
                    weakPassword
            );

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("OldPassword123@", "hashedpassword")).thenReturn(true);
            when(passwordEncoder.matches(weakPassword, "hashedpassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });

            verify(userRepository, never()).updatePasswordNative(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw 404 NOT_FOUND when user does not exist")
        void changePassword_WhenUserNotFound_ShouldThrow404() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "OldPassword123@",
                    "NewPassword456@",
                    "NewPassword456@"
            );

            when(userRepository.findUserByIdNative(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(999, request))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    });

            verify(userRepository, never()).updatePasswordNative(anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("updateAvatar() tests")
    class UpdateAvatarTests {

        @Test
        @DisplayName("Should successfully upload avatar and update database with native query")
        void updateAvatar_WhenValid_ShouldUploadAndReturnUrl() {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", "test image content".getBytes()
            );
            EventImageService.PreparedImage prepared = new EventImageService.PreparedImage("test image content".getBytes(), "JPEG");

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(eventImageService.prepare(mockFile)).thenReturn(prepared);
            when(eventImageService.upload(eq(prepared), eq("eventhub/avatars"), anyString()))
                    .thenReturn("https://res.cloudinary.com/dnumysqsn/image/upload/v123/avatar.jpg");

            String result = userService.updateAvatar(1, mockFile);

            assertThat(result).isEqualTo("https://res.cloudinary.com/dnumysqsn/image/upload/v123/avatar.jpg");
            verify(userRepository).updateAvatarUrlNative(1, "https://res.cloudinary.com/dnumysqsn/image/upload/v123/avatar.jpg");
        }

        @Test
        @DisplayName("Should support UploadAvatarRequest DTO successfully")
        void updateAvatar_WithDto_ShouldUploadAndReturnUrl() {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "avatar.png", "image/png", "png image content".getBytes()
            );
            EventImageService.PreparedImage prepared = new EventImageService.PreparedImage("png image content".getBytes(), "PNG");

            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));
            when(eventImageService.prepare(mockFile)).thenReturn(prepared);
            when(eventImageService.upload(eq(prepared), eq("eventhub/avatars"), anyString()))
                    .thenReturn("https://res.cloudinary.com/dnumysqsn/image/upload/v456/avatar.png");

            UploadAvatarRequest request = new UploadAvatarRequest(1, mockFile);
            String result = userService.updateAvatar(request);

            assertThat(result).isEqualTo("https://res.cloudinary.com/dnumysqsn/image/upload/v456/avatar.png");
            verify(userRepository).updateAvatarUrlNative(1, "https://res.cloudinary.com/dnumysqsn/image/upload/v456/avatar.png");
        }

        @Test
        @DisplayName("Should throw 404 NOT_FOUND when user does not exist")
        void updateAvatar_WhenUserNotFound_ShouldThrow404() {
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file", "avatar.jpg", "image/jpeg", "content".getBytes()
            );
            when(userRepository.findUserByIdNative(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateAvatar(999, mockFile))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                        assertThat(ex.getReason()).contains("Người dùng không tồn tại");
                    });

            verify(userRepository, never()).updateAvatarUrlNative(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when file is null or empty")
        void updateAvatar_WhenFileEmpty_ShouldThrow400() {
            when(userRepository.findUserByIdNative(1)).thenReturn(Optional.of(sampleUser));

            MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

            assertThatThrownBy(() -> userService.updateAvatar(1, emptyFile))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("File ảnh đại diện không được để trống");
                    });

            assertThatThrownBy(() -> userService.updateAvatar(1, (MultipartFile) null))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(ex.getReason()).contains("File ảnh đại diện không được để trống");
                    });

            verify(userRepository, never()).updateAvatarUrlNative(anyInt(), anyString());
        }

        @Test
        @DisplayName("Should throw 400 BAD_REQUEST when UploadAvatarRequest is invalid")
        void updateAvatar_WhenRequestInvalid_ShouldThrow400() {
            assertThatThrownBy(() -> userService.updateAvatar((UploadAvatarRequest) null))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });

            UploadAvatarRequest requestWithNullUser = new UploadAvatarRequest(null, null);
            assertThatThrownBy(() -> userService.updateAvatar(requestWithNullUser))
                    .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    });
        }
    }
}
