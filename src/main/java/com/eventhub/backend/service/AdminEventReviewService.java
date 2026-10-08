package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.dto.response.EventResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Notification;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventReviewService {
    private final EventRepository events;
    private final TicketTypeRepository tickets;
    private final EventGuestRepository guests;
    private final UserRepository users;
    private final NotificationRepository notifications;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<PendingEventResponse> list(int page, int pageSize) {
        return PageResponse.from(events.findByStatus(EventStatus.PENDING_APPROVAL,
                AdminPagination.page(page, pageSize)).map(PendingEventResponse::from));
    }

    public ReviewDetailResponse detail(Integer id) {
        // Share the event lock used by Organizer edits, so all parts describe one revision.
        Event event = pending(id);
        return new ReviewDetailResponse(EventResponse.from(event, tickets.findByEventIdOrderByIdAsc(id),
                guests.findByEventIdOrderByIdAsc(id), LocalDateTime.now(clock)),
                OrganizerResponse.from(event.getOrganizer()), event.getVersion());
    }

    public ReviewResponse decide(Integer adminId, Integer id, long version, String reason) {
        Event event = pending(id);
        if (event.getVersion() != version) {
            throw new AdminException(HttpStatus.CONFLICT, "EVENT_VERSION_CONFLICT", "Nội dung đã thay đổi. Vui lòng tải lại.");
        }
        boolean approved = reason == null;
        event.setStatus(approved ? EventStatus.APPROVED : EventStatus.REJECTED);
        event.setReviewedBy(users.getReferenceById(adminId));
        event.setReviewedAt(LocalDateTime.now(clock));
        event.setRejectReason(reason);
        tickets.findForUpdate(id).forEach(ticket -> ticket.setStatus(approved ? "ACTIVE" : "INACTIVE"));
        events.saveAndFlush(event);
        Notification notification = new Notification();
        notification.setUser(event.getOrganizer());
        notification.setTitle("Sự kiện #" + id + (approved ? " đã được duyệt" : " bị từ chối"));
        notification.setType(approved ? "EVENT_APPROVED" : "EVENT_REJECTED");
        // Existing notification content is VARCHAR(255); preserve the complete rejection reason.
        notification.setContent(approved ? "Sự kiện #" + id + " đã được phê duyệt." : reason);
        notifications.saveAndFlush(notification);
        return new ReviewResponse(id, event.getStatus(), adminId, event.getReviewedAt(), reason, event.getVersion());
    }

    private Event pending(Integer id) {
        AdminPagination.validId(id);
        Event event = events.findForReview(id).orElseThrow(() ->
                new AdminException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Không tìm thấy sự kiện"));
        if (event.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw new AdminException(HttpStatus.CONFLICT, "EVENT_NOT_PENDING", "Sự kiện không còn chờ duyệt");
        }
        return event;
    }
}
