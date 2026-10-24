package com.eventhub.backend.service;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.repository.EventRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLifecycleService {
    private final EventRepository events;
    private final Clock clock;

    @Transactional
    public void advanceEvent(Integer eventId) {
        // Serialize with bookings, cancellation and review before changing the lifecycle.
        events.findForReview(eventId).ifPresent(event -> {
            EventStatus next = statusAt(event, LocalDateTime.now(clock));
            if (event.getStatus() != next) event.setStatus(next);
        });
    }

    public static EventStatus statusAt(Event event, LocalDateTime now) {
        EventStatus status = event.getStatus();
        if (status != EventStatus.APPROVED && status != EventStatus.ONGOING) return status;
        if (!now.isBefore(event.getEndTime())) return EventStatus.COMPLETED;
        if (!now.isBefore(event.getStartTime())) return EventStatus.ONGOING;
        return status;
    }
}
