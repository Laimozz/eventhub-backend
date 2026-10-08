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

import com.eventhub.backend.dto.request.UploadAvatarRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping(value = {"/me/avatar", "/avatar"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadMyAvatar(
            @AuthenticationPrincipal UserResponse currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer targetUserId = (userId != null) ? userId : (currentUser != null ? currentUser.id() : null);
        if (targetUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID không hợp lệ");
        }
        String avatarUrl = userService.updateAvatar(new UploadAvatarRequest(targetUserId, file));
        return ResponseEntity.ok(Map.of(
                "avatarUrl", avatarUrl,
                "message", "Cập nhật ảnh đại diện thành công"
        ));
    }
}

