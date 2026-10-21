package com.eventhub.backend;

import com.eventhub.backend.entity.RefreshToken;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.RefreshTokenRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.security.JwtService;
import com.eventhub.backend.service.NotificationService;
import com.eventhub.backend.service.NotificationStreamService;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties", properties = "app.booking.expiry-enabled=false")
class NotificationIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired JwtService jwt;
    @Autowired NotificationService notifications;
    @Autowired NotificationStreamService streams;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @Autowired org.springframework.core.env.Environment environment;
    private final List<User> testUsers = new ArrayList<>();

    @AfterEach void cleanup() {
        streams.close();
        for (User user : testUsers) {
            jdbc.update("DELETE FROM notifications WHERE users_id=?", user.getId());
            jdbc.update("DELETE FROM refresh_tokens WHERE users_id=?", user.getId());
            users.deleteById(user.getId());
        }
    }

    @ParameterizedTest @EnumSource(value = Role.class, names = {"ADMIN", "ORGANIZER", "CUSTOMER"})
    void enabledRolesCanReadOnlyTheirOwnNotificationsAndMarkThemRead(Role role) throws Exception {
        User owner = user(role);
        User other = user(role);
        Cookie access = session(owner);
        stream(access);
        notifications.create(owner, "First", "Own content", "TEST");
        notifications.create(owner, "Latest", "Own latest content", "TEST");
        notifications.create(other, "Private", "Other user's content", "TEST");
        int id = jdbc.queryForObject("SELECT max(id) FROM notifications WHERE users_id=?", Integer.class, owner.getId());
        int foreignId = jdbc.queryForObject("SELECT max(id) FROM notifications WHERE users_id=?", Integer.class, other.getId());

        mvc.perform(get("/api/notifications").cookie(access).param("pageSize", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items[0].id").value(id)).andExpect(jsonPath("$.items[0].read").value(false));
        mvc.perform(get("/api/notifications/unread-count").cookie(access)).andExpect(content().string("2"));
        mvc.perform(patch("/api/notifications/{id}/read", foreignId).cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isNotFound());
        for (int repeat = 0; repeat < 2; repeat++) {
            mvc.perform(patch("/api/notifications/{id}/read", id).cookie(access).header("X-CSRF-Protection", "1"))
                    .andExpect(status().isNoContent());
        }
        mvc.perform(get("/api/notifications/unread-count").cookie(access)).andExpect(content().string("1"));
        mvc.perform(patch("/api/notifications/read-all").cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/unread-count").cookie(access)).andExpect(content().string("0"));
        assertThat(notifications.unreadCount(other.getId())).isEqualTo(1);
        mvc.perform(get("/api/notifications").cookie(access).param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/notifications").cookie(access).param("pageSize", "101")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/notifications/{id}/read", id).cookie(access)).andExpect(status().isForbidden());
    }

    @Test void anonymousCannotUseAnyNotificationEndpoint() throws Exception {
        for (var request : List.of(get("/api/notifications"), get("/api/notifications/unread-count"),
                get("/api/notifications/stream"), patch("/api/notifications/1/read"), patch("/api/notifications/read-all"))) {
            mvc.perform(request.header("X-CSRF-Protection", "1")).andExpect(status().isUnauthorized());
        }
    }

    @Test void staffCannotUseNotificationEndpointsOrReceiveNewNotifications() throws Exception {
        User staff = user(Role.STAFF);
        Cookie access = session(staff);
        for (var request : List.of(get("/api/notifications"), get("/api/notifications/unread-count"),
                get("/api/notifications/stream"), patch("/api/notifications/1/read"), patch("/api/notifications/read-all"))) {
            mvc.perform(request.cookie(access).header("X-CSRF-Protection", "1")).andExpect(status().isForbidden());
        }
        notifications.create(staff, "Disabled", "Staff notifications are disabled", "TEST");
        assertThat(notifications.unreadCount(staff.getId())).isZero();
    }

    @Test void sseNotifiesAllOwnersTabsOnlyAfterCommitAndNeverOnRollback() throws Exception {
        User owner = user(Role.CUSTOMER);
        User other = user(Role.ORGANIZER);
        Cookie access = session(owner);
        MvcResult first = stream(access);
        MvcResult second = stream(access);
        MvcResult foreign = stream(session(other));
        var transaction = new TransactionTemplate(transactions);
        transaction.executeWithoutResult(status -> {
            notifications.create(owner, "Committed", "Visible after commit", "TEST");
            assertThat(body(first)).doesNotContain("notifications-changed");
        });
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(body(first)).contains("event:notifications-changed");
            assertThat(body(second)).contains("event:notifications-changed");
        });
        assertThat(body(foreign)).doesNotContain("notifications-changed");
        int delivered = changes(first);
        transaction.executeWithoutResult(status -> {
            notifications.create(owner, "Rolled back", "Must never be sent", "TEST");
            status.setRollbackOnly();
        });
        assertThat(notifications.unreadCount(owner.getId())).isEqualTo(1);
        // A subsequent committed change acts as a barrier for the executor.
        notifications.markAllRead(owner.getId());
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(changes(first)).isEqualTo(delivered + 1));
        assertThat(body(foreign)).doesNotContain("notifications-changed");
        streams.close();
        mvc.perform(asyncDispatch(first)).andExpect(status().isOk());
    }

    @Test void revokedSessionStopsReceivingSse() throws Exception {
        User owner = user(Role.ORGANIZER);
        Cookie access = session(owner);
        MvcResult result = stream(access);
        jdbc.update("UPDATE refresh_tokens SET revoked=true WHERE users_id=?", owner.getId());
        notifications.create(owner, "Private", "After logout", "TEST");
        result.getAsyncResult(5_000);
        assertThat(body(result)).doesNotContain("notifications-changed");
        mvc.perform(get("/api/notifications/stream").cookie(access)).andExpect(status().isUnauthorized());
    }

    @Test void actualHttpConnectionReceivesSseAfterDatabaseCommit() throws Exception {
        User owner = user(Role.CUSTOMER);
        Cookie access = session(owner);
        int port = environment.getRequiredProperty("local.server.port", Integer.class);
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/api/notifications/stream"))
                .header("Cookie", AuthCookies.ACCESS + "=" + access.getValue()).timeout(Duration.ofSeconds(10)).build();
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("content-type").orElseThrow()).startsWith("text/event-stream");
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(response.body(), java.nio.charset.StandardCharsets.UTF_8))) {
                assertThat(reader.readLine()).isEqualTo("event:connected");
                var received = java.util.concurrent.CompletableFuture.supplyAsync(() -> reader.lines()
                        .filter(line -> line.equals("event:notifications-changed")).findFirst().orElseThrow());
                notifications.create(owner, "Live notification", "Delivered over HTTP", "TEST");
                assertThat(received.get(5, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("event:notifications-changed");
                assertThat(notifications.unreadCount(owner.getId())).isEqualTo(1);
            }
        }
    }

    @Test void eventRecipientsIncludeOnlyPaidCustomersOnceAndExcludeStaff() {
        User organizer = user(Role.ORGANIZER);
        User customer = user(Role.CUSTOMER);
        User unpaid = user(Role.CUSTOMER);
        User staff = user(Role.STAFF);
        User inactiveStaff = user(Role.STAFF);
        int category = jdbc.queryForObject("INSERT INTO categories(name) VALUES (?) RETURNING id", Integer.class, UUID.randomUUID().toString());
        int venue = jdbc.queryForObject("INSERT INTO venues(city,address,capacity) VALUES ('City','Address',10) RETURNING id", Integer.class);
        int event = jdbc.queryForObject("""
                INSERT INTO events(organizer_id,venues_id,categories_id,name,thumbnail_image_url,banner_image_url,start_time,end_time,status)
                VALUES (?,?,?,'Test','','',CURRENT_TIMESTAMP + interval '1 day',CURRENT_TIMESTAMP + interval '2 days','APPROVED') RETURNING id
                """, Integer.class, organizer.getId(), venue, category);
        try {
            for (User booked : List.of(customer, customer, unpaid, staff)) {
                jdbc.update("INSERT INTO bookings(users_id,events_id,total_amount,status,expired_at) VALUES (?,?,0,?,CURRENT_TIMESTAMP)",
                        booked.getId(), event, booked == unpaid ? "PENDING" : "PAID");
            }
            jdbc.update("INSERT INTO event_staffs(events_id,staff_id,assigned_by,status) VALUES (?,?,?,'ACTIVE'),(?,?,?,'INACTIVE')",
                    event, staff.getId(), organizer.getId(), event, inactiveStaff.getId(), organizer.getId());
            notifications.notifyEventParticipants(event, "Event updated", "Details changed", "EVENT_UPDATED");
            assertThat(notifications.unreadCount(customer.getId())).isEqualTo(1);
            assertThat(notifications.unreadCount(staff.getId())).isZero();
            assertThat(notifications.unreadCount(unpaid.getId())).isZero();
            assertThat(notifications.unreadCount(inactiveStaff.getId())).isZero();
            assertThat(notifications.unreadCount(organizer.getId())).isZero();
        } finally {
            jdbc.update("DELETE FROM event_staffs WHERE events_id=?", event);
            jdbc.update("DELETE FROM bookings WHERE events_id=?", event);
            jdbc.update("DELETE FROM events WHERE id=?", event);
            jdbc.update("DELETE FROM venues WHERE id=?", venue);
            jdbc.update("DELETE FROM categories WHERE id=?", category);
        }
    }

    private MvcResult stream(Cookie access) throws Exception {
        return mvc.perform(get("/api/notifications/stream").cookie(access)).andExpect(status().isOk())
                .andExpect(request().asyncStarted()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(header().string("X-Accel-Buffering", "no")).andReturn();
    }

    private String body(MvcResult result) {
        return new String(result.getResponse().getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private int changes(MvcResult result) {
        return body(result).split("event:notifications-changed", -1).length - 1;
    }

    private User user(Role role) {
        var user = new User();
        user.setEmail(UUID.randomUUID() + "@example.invalid");
        user.setFullName("Notification test");
        user.setPassword("invalid-test-hash");
        user.setRole(role);
        user.setStatus("ACTIVE");
        testUsers.add(users.saveAndFlush(user));
        return user;
    }

    private Cookie session(User user) {
        var token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(jwt.hashRefreshToken(jwt.createRefreshToken()));
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        tokens.saveAndFlush(token);
        return new Cookie(AuthCookies.ACCESS, jwt.createAccessToken(user.getId(), token.getId(), user.getRole()));
    }
}
