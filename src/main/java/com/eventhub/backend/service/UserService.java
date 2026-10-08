package com.eventhub.backend.service;

import com.eventhub.backend.dto.UserDetailDto;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.exception.EmailAlreadyExistsException;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.dto.request.ChangePasswordRequest;
import com.eventhub.backend.dto.request.UploadAvatarRequest;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+84|0)[35789][0-9]{8}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventImageService eventImageService;

    @Transactional(readOnly = true)
    public UserDetailDto getUserDetail(Integer userId) {
        User user = userRepository.findUserByIdNative(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        return UserDetailDto.from(user);
    }

    @Transactional
    public UserDetailDto updateUserDetail(Integer userId, UserDetailDto request) {
        User existingUser = userRepository.findUserByIdNative(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Họ và tên không được để trống");
        }

        String normalizedEmail = request.email() == null ? "" : request.email().trim().toLowerCase();
        if (normalizedEmail.isBlank() || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng (ví dụ: example@domain.com)");
        }

        String cleanPhone = request.phone() == null ? "" : request.phone().trim().replaceAll("[\\s.-]+", "");
        if (cleanPhone.isBlank() || !PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại không đúng định dạng (gồm 10 chữ số, ví dụ: 0908123456)");
        }

        if (request.dateOfBirth() != null && !request.dateOfBirth().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày sinh phải trước ngày hiện tại");
        }

        boolean emailExists = userRepository.existsByEmailAndIdNotNative(normalizedEmail, userId);
        if (emailExists) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng bởi tài khoản khác");
        }

        String phone = cleanPhone;
        String genderToUpdate = (request.gender() != null && !request.gender().isBlank())
                ? request.gender().trim()
                : existingUser.getGender();

        userRepository.updateUserDetailNative(
                userId,
                request.fullName().trim(),
                phone,
                normalizedEmail,
                request.dateOfBirth(),
                genderToUpdate
        );

        User updatedUser = userRepository.findUserByIdNative(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        return UserDetailDto.from(updatedUser);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findUserByIdNative(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không được để trống");
        }

        // 1. So khớp mật khẩu hiện tại với hash trong DB
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác");
        }

        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được để trống");
        }

        // 2. Chặn đặt lại mật khẩu mới trùng mật khẩu hiện tại
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        // 3. Kiểm tra xác nhận mật khẩu khớp nhau
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu mới không trùng khớp");
        }

        // 4. Kiểm tra độ mạnh mật khẩu (8-72 ký tự, gồm chữ hoa, chữ thường, số, ký tự đặc biệt)
        if (request.newPassword().length() < 8 || request.newPassword().length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải từ 8 đến 72 ký tự");
        }
        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải bao gồm ít nhất 8 ký tự, có chữ hoa, chữ thường, chữ số và ký tự đặc biệt");
        }

        // 5. Băm mật khẩu mới bằng BCrypt và lưu vào DB thông qua Native SQL
        String hashedNewPassword = passwordEncoder.encode(request.newPassword());
        userRepository.updatePasswordNative(userId, hashedNewPassword);
    }

    @Transactional
    public String updateAvatar(UploadAvatarRequest request) {
        if (request == null || request.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID không được để trống");
        }
        return updateAvatar(request.userId(), request.file());
    }

    @Transactional
    public String updateAvatar(Integer userId, MultipartFile file) {
        User user = userRepository.findUserByIdNative(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File ảnh đại diện không được để trống");
        }

        EventImageService.PreparedImage prepared = eventImageService.prepare(file);
        String imageId = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
        String avatarUrl = eventImageService.upload(prepared, "eventhub/avatars", imageId);

        userRepository.updateAvatarUrlNative(userId, avatarUrl);
        return avatarUrl;
    }
}
