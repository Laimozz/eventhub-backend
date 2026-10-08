package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    public PageResponse<NotificationResponse> list(Integer userId, int page, int pageSize) {
        return PageResponse.from(notifications.findByUserId(userId, AdminPagination.page(page, pageSize))
                .map(NotificationResponse::from));
    }
}
