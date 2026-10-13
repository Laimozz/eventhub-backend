package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.NotificationService;
import com.eventhub.backend.service.NotificationStreamService;
import com.eventhub.backend.security.AuthCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER', 'CUSTOMER')")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notifications;
    private final NotificationStreamService streams;
    private final AuthCookies cookies;
    @GetMapping
    public PageResponse<NotificationResponse> list(@AuthenticationPrincipal UserResponse user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        return notifications.list(user.id(), page, pageSize);
    }

    @GetMapping("/unread-count")
    public long unreadCount(@AuthenticationPrincipal UserResponse user) {
        return notifications.unreadCount(user.id());
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal UserResponse user, @PathVariable Integer id) {
        notifications.markRead(user.id(), id);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal UserResponse user) {
        notifications.markAllRead(user.id());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UserResponse user, HttpServletRequest request,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Accel-Buffering", "no");
        return streams.subscribe(user.id(), cookies.read(request, AuthCookies.ACCESS));
    }
}
