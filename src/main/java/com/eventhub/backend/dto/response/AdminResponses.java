package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public final class AdminResponses {
    private AdminResponses() {}
    public record PageResponse<T>(List<T> items, int page, int pageSize, long totalElements, int totalPages) {
        public static <T> PageResponse<T> from(Page<T> result) {
            return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                    result.getTotalElements(), result.getTotalPages());
        }
    }
    public record AdminUserResponse(Integer id, String fullName, String email, String phone, Role role, String status) {
        public static AdminUserResponse from(User user) {
            return new AdminUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                    user.getRole(), user.getStatus());
        }
    }
    public record OrganizerResponse(Integer id, String fullName, String email) {
        public static OrganizerResponse from(User user) {
            return new OrganizerResponse(user.getId(), user.getFullName(), user.getEmail());
        }
    }
    public record PendingEventResponse(Integer id, String name, String thumbnailImageUrl, String categoryName,
            OrganizerResponse organizer, LocalDateTime createdAt, EventStatus status) {
        public static PendingEventResponse from(Event event) {
            return new PendingEventResponse(event.getId(), event.getName(), event.getThumbnailImageUrl(),
                    event.getCategory().getName(), OrganizerResponse.from(event.getOrganizer()),
                    event.getCreatedAt(), event.getStatus());
        }
    }
    public record ReviewDetailResponse(EventResponse event, OrganizerResponse organizer, long version) {}
    public record ReviewResponse(Integer eventId, EventStatus status, Integer reviewedBy,
            LocalDateTime reviewedAt, String reason, long version) {}
    public record NotificationResponse(Integer id, String title, String content, String type,
            LocalDateTime createdAt, boolean read) {
        public static NotificationResponse from(Notification notification) {
            return new NotificationResponse(notification.getId(), notification.getTitle(), notification.getContent(),
                    notification.getType(), notification.getCreatedAt(), Boolean.TRUE.equals(notification.getRead()));
        }
    }
}
