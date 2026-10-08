package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.AdminRequests.*;
import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> list(String keyword, Role role, int page, int pageSize) {
        String search = "%" + keyword.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return PageResponse.from(users.searchAdminUsers(role, search, AdminPagination.page(page, pageSize))
                .map(AdminUserResponse::from));
    }

    public AdminUserResponse create(CreateUserRequest request) {
        if (users.findByNormalizedEmail(request.email()).isPresent()) {
            throw new AdminException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng");
        }
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwords.encode(request.password()));
        user.setRole(Role.valueOf(request.role()));
        user.setStatus("ACTIVE");
        return AdminUserResponse.from(users.saveAndFlush(user));
    }

    public AdminUserResponse updateStatus(Integer adminId, Integer userId, UpdateUserStatusRequest request) {
        AdminPagination.validId(userId);
        if (adminId.equals(userId) && "LOCKED".equals(request.status())) {
            throw new AdminException(HttpStatus.CONFLICT, "CANNOT_LOCK_SELF", "Không thể khóa tài khoản đang đăng nhập");
        }
        User user = users.findById(userId).orElseThrow(() ->
                new AdminException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy người dùng"));
        user.setStatus(request.status());
        return AdminUserResponse.from(users.saveAndFlush(user));
    }
}
