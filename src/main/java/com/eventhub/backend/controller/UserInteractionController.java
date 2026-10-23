package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.UserInteractionRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.AiOutboxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tiếp nhận luồng stream hành vi tương tác của người dùng từ giao diện
 * và ghi nhanh vào hộp thư Outbox.
 *
 * NOTE: Tạm thời comment các endpoint này vì hệ thống AI đang bảo trì sửa chữa.
 * Sau khi hệ thống AI hoàn thiện sẽ mở lại.
 */
// @RestController
// @RequestMapping("/api/public/events/interactions")
// @RequiredArgsConstructor
public class UserInteractionController {

    // private final AiOutboxService aiOutbox;

    // @PostMapping
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // public void recordInteraction(
    //         @AuthenticationPrincipal UserResponse user,
    //         @Valid @RequestBody UserInteractionRequest request) {
    //     Integer userId = user != null ? user.id() : request.userId();
    //     aiOutbox.logUserInteraction(
    //             userId,
    //             request.eventId(),
    //             request.actionType(),
    //             request.dwellSeconds(),
    //             request.recLogId()
    //     );
    // }
}
