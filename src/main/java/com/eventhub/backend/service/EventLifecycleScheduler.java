package com.eventhub.backend.service;

import com.eventhub.backend.repository.EventRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.events.lifecycle-enabled", havingValue = "true", matchIfMissing = true)
public class EventLifecycleScheduler {
    private final EventRepository events;
    private final EventLifecycleService lifecycle;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${app.events.lifecycle-delay:30s}", initialDelayString = "${app.events.lifecycle-delay:30s}")
    public void advance() {
        for (Integer eventId : events.findLifecycleDueIds(LocalDateTime.now(clock), PageRequest.of(0, 100))) {
            try {
                lifecycle.advanceEvent(eventId);
            } catch (RuntimeException exception) {
                log.warn("Could not advance event {} ({})", eventId, exception.getClass().getSimpleName());
            }
        }
    }
}
