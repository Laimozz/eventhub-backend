package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.entity.Notification;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.NotificationRepository;
import com.eventhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public PageResponse<NotificationResponse> list(Integer userId, int page, int pageSize) {
        return PageResponse.from(notifications.findByUserId(userId, AdminPagination.page(page, pageSize))
                .map(NotificationResponse::from));
    }

    public long unreadCount(Integer userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Integer userId, Integer id) {
        AdminPagination.validId(id);
        if (notifications.markRead(id, userId) == 0) {
            throw new AdminException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo");
        }
        events.publishEvent(new Changed(userId));
    }

    @Transactional
    public void markAllRead(Integer userId) {
        if (notifications.markAllRead(userId) > 0) events.publishEvent(new Changed(userId));
    }

    @Transactional
    public void create(User user, String title, String content, String type) {
        if (user.getRole() == Role.STAFF) return;
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(shorten(title));
        notification.setContent(shorten(content));
        notification.setType(type);
        notifications.save(notification);
        events.publishEvent(new Changed(user.getId()));
    }

    @Transactional
    public void notifyEventParticipants(Integer eventId, String title, String content, String type) {
        users.findEventNotificationRecipients(eventId).forEach(user -> create(user, title, content, type));
    }

    private String shorten(String value) {
        if (value == null || value.codePointCount(0, value.length()) <= 255) return value;
        return value.substring(0, value.offsetByCodePoints(0, 252)) + "...";
    }

    public record Changed(Integer userId) {}
}
