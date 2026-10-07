package com.eventhub.backend.controller;

import com.eventhub.backend.dto.UserDetailDto;
import com.eventhub.backend.dto.request.ChangePasswordRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDetailDto> getMyProfile(@AuthenticationPrincipal UserResponse currentUser) {
        return ResponseEntity.ok(userService.getUserDetail(currentUser.id()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDetailDto> updateMyProfile(
            @AuthenticationPrincipal UserResponse currentUser,
            @Valid @RequestBody UserDetailDto request) {
        return ResponseEntity.ok(userService.updateUserDetail(currentUser.id(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changeMyPassword(
            @AuthenticationPrincipal UserResponse currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.id(), request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }
}
