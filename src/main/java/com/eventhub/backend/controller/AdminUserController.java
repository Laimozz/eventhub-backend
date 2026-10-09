package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AdminRequests.*;
import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService users;
    @GetMapping
    public PageResponse<AdminUserResponse> list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Role role, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return users.list(keyword, role, page, pageSize);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse create(@Valid @RequestBody CreateUserRequest request) { return users.create(request); }
    @PatchMapping("/{userId}/status")
    public AdminUserResponse status(@AuthenticationPrincipal UserResponse admin, @PathVariable Integer userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return users.updateStatus(admin.id(), userId, request);
    }
}
