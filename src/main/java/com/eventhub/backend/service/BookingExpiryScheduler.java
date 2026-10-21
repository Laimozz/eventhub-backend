package com.eventhub.backend.service;

import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.config.ApplicationProperties;
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
@ConditionalOnProperty(name = "app.booking.expiry-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {
    private final BookingRepository bookings;
    private final BookingService service;
    private final Clock clock;
    private final ApplicationProperties properties;

    @Scheduled(fixedDelayString = "${app.booking.expiry-delay:30s}", initialDelayString = "${app.booking.expiry-delay:30s}")
    public void expire() {
        var now = LocalDateTime.now(clock);
        for (Integer eventId : bookings.findExpiredEventIds(now, now.minus(properties.getBooking().getIpnGracePeriod()), PageRequest.of(0, 100))) {
            try {
                // Each event commits independently; multiple application instances share the DB lock.
                service.expireBookings(eventId);
            } catch (RuntimeException exception) {
                log.error("Could not release expired bookings for event {} ({})", eventId, exception.getClass().getSimpleName());
            }
        }
    }
}
