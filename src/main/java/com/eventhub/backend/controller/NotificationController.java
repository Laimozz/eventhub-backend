package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('ORGANIZER')")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notifications;
    @GetMapping
    public PageResponse<NotificationResponse> list(@AuthenticationPrincipal UserResponse user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        return notifications.list(user.id(), page, pageSize);
    }
}
