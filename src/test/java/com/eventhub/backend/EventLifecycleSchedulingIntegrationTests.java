package com.eventhub.backend;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties", properties = {
        "app.events.lifecycle-enabled=true", "app.events.lifecycle-delay=50ms", "app.booking.expiry-enabled=false" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventLifecycleSchedulingIntegrationTests {
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean Clock clock;
    Integer organizerId, categoryId, venueId;

    @AfterEach
    void clean() {
        if (organizerId == null) return;
        jdbc.update("DELETE FROM events WHERE organizer_id=?", organizerId);
        if (venueId != null) jdbc.update("DELETE FROM venues WHERE id=?", venueId);
        if (categoryId != null) jdbc.update("DELETE FROM categories WHERE id=?", categoryId);
        jdbc.update("DELETE FROM users WHERE id=?", organizerId);
    }

    @Test
    void springScheduledWorkerAdvancesApprovedEventsAndLeavesPendingEventsAlone() throws Exception {
        LocalDateTime current = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        doReturn(current.toInstant(ZoneOffset.UTC)).when(clock).instant();
        organizerId = jdbc.queryForObject("INSERT INTO users(email,password,role,status) VALUES (?,'test-only-hash','ORGANIZER','ACTIVE') RETURNING id",
                Integer.class, UUID.randomUUID() + "@example.invalid");
        categoryId = jdbc.queryForObject("INSERT INTO categories(name) VALUES (?) RETURNING id", Integer.class, UUID.randomUUID().toString());
        venueId = jdbc.queryForObject("INSERT INTO venues(city,address,capacity) VALUES ('Test city','Lifecycle worker test',10) RETURNING id", Integer.class);
        LocalDateTime start = current.plusHours(1), end = current.plusHours(2);
        int approved = event("APPROVED", start, end);
        int pending = event("PENDING_APPROVAL", start, end);
        assertThat(status(approved)).isEqualTo("APPROVED");

        doReturn(start.toInstant(ZoneOffset.UTC)).when(clock).instant();
        awaitStatus(approved, "ONGOING");
        doReturn(end.toInstant(ZoneOffset.UTC)).when(clock).instant();
        awaitStatus(approved, "COMPLETED");
        assertThat(status(pending)).isEqualTo("PENDING_APPROVAL");
    }

    private int event(String status, LocalDateTime start, LocalDateTime end) {
        return jdbc.queryForObject("""
                INSERT INTO events(organizer_id,venues_id,categories_id,name,thumbnail_image_url,banner_image_url,start_time,end_time,status)
                VALUES (?,?,?,'Scheduled event','https://example.invalid/test.png','https://example.invalid/test.png',?,?,?) RETURNING id
                """, Integer.class, organizerId, venueId, categoryId, start, end, status);
    }

    private String status(int eventId) {
        return jdbc.queryForObject("SELECT status FROM events WHERE id=?", String.class, eventId);
    }

    private void awaitStatus(int eventId, String expected) throws InterruptedException {
        for (int attempt = 0; attempt < 100; attempt++) {
            if (expected.equals(status(eventId))) return;
            Thread.sleep(50);
        }
        assertThat(status(eventId)).isEqualTo(expected);
    }
}
