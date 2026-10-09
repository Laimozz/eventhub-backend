package com.eventhub.backend;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.*;
import com.eventhub.backend.repository.*;
import com.eventhub.backend.security.*;
import com.eventhub.backend.service.AdminEventReviewService;
import jakarta.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties")
class AdminIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired AdminEventReviewService reviews;
    @Autowired EventRepository events;
    @Autowired PlatformTransactionManager transactions;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean NotificationRepository notifications;
    @Autowired tools.jackson.databind.ObjectMapper json;
    User admin;
    User organizer;
    Cookie access;
    Cookie organizerAccess;
    int categoryId;
    int venueId;
    int eventId;

    @BeforeEach void prepare() {
        admin = user(Role.ADMIN);
        organizer = user(Role.ORGANIZER);
        access = session(admin);
        organizerAccess = session(organizer);
        categoryId = jdbc.queryForObject("INSERT INTO categories(name,description) VALUES (?,?) RETURNING id",
                Integer.class, "Admin test " + UUID.randomUUID(), "Test");
        venueId = jdbc.queryForObject("INSERT INTO venues(city,address,capacity) VALUES ('City','Test',100) RETURNING id", Integer.class);
        eventId = jdbc.queryForObject("""
                INSERT INTO events(organizer_id,venues_id,categories_id,name,thumbnail_image_url,banner_image_url,
                start_time,end_time,status) VALUES (?,?,?,'Review event','https://example.invalid/a','https://example.invalid/b',
                CURRENT_TIMESTAMP + interval '10 days', CURRENT_TIMESTAMP + interval '11 days','PENDING_APPROVAL') RETURNING id
                """, Integer.class, organizer.getId(), venueId, categoryId);
        jdbc.update("""
                INSERT INTO ticket_types(events_id,name,image_url,price,quantity,reserved_quantity,remaining_quantity,
                sale_start_time,sale_end_time,status) VALUES (?,'Ticket','https://example.invalid/t',100,10,0,10,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + interval '9 days','INACTIVE')
                """, eventId);
    }
    @AfterEach void clean() {
        jdbc.update("DELETE FROM notifications WHERE users_id IN (?,?)", admin.getId(), organizer.getId());
        jdbc.update("DELETE FROM ticket_types WHERE events_id=?", eventId);
        jdbc.update("DELETE FROM events WHERE id=?", eventId);
        jdbc.update("DELETE FROM venues WHERE id=?", venueId);
        jdbc.update("DELETE FROM categories WHERE id=?", categoryId);
        jdbc.update("DELETE FROM refresh_tokens WHERE users_id IN (?,?)", admin.getId(), organizer.getId());
        jdbc.update("DELETE FROM users WHERE id IN (?,?)", admin.getId(), organizer.getId());
    }
    User user(Role role) {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.invalid");
        user.setFullName("Admin test");
        user.setPassword("invalid-test-hash");
        user.setStatus("ACTIVE");
        user.setRole(role);
        return users.saveAndFlush(user);
    }
    Cookie session(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(jwt.hashRefreshToken(jwt.createRefreshToken()));
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        tokens.saveAndFlush(token);
        return new Cookie(AuthCookies.ACCESS, jwt.createAccessToken(user.getId(), token.getId(), user.getRole()));
    }

    @Test void protectsEveryAdminEndpoint() throws Exception {
        var requests = java.util.List.of(
            get("/api/admin/users"), post("/api/admin/users").content("{}"),
            patch("/api/admin/users/1/status").content("{\"status\":\"LOCKED\"}"),
            get("/api/admin/event-categories"), post("/api/admin/event-categories").content("{}"),
            put("/api/admin/event-categories/1").content("{}"), delete("/api/admin/event-categories/1"),
            get("/api/admin/events/pending"), get("/api/admin/events/pending/1"),
            post("/api/admin/events/1/approve").content("{\"version\":0}"),
            post("/api/admin/events/1/reject").content("{\"version\":0,\"reason\":\"Test\"}"));
        for (var request : requests) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).header("X-CSRF-Protection", "1"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(request.cookie(organizerAccess)).andExpect(status().isForbidden());
        }
    }
    @Test void managesUsersAndBlocksSelfLockAndExistingSessions() throws Exception {
        mvc.perform(get("/api/admin/users").cookie(access).param("keyword", organizer.getEmail()).param("role","ORGANIZER"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(patch("/api/admin/users/" + admin.getId() + "/status").cookie(access)
                .header("X-CSRF-Protection","1").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CANNOT_LOCK_SELF"));
        mvc.perform(patch("/api/admin/users/" + organizer.getId() + "/status").cookie(access)
                .header("X-CSRF-Protection","1").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"LOCKED\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/events/mine").cookie(organizerAccess)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/users").cookie(access).param("pageSize","101")).andExpect(status().isBadRequest());
    }
    @Test void validatesCategoriesAndProtectsReferencedData() throws Exception {
        mvc.perform(delete("/api/admin/event-categories/" + categoryId).cookie(access).header("X-CSRF-Protection","1"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CATEGORY_IN_USE"));
        mvc.perform(put("/api/admin/event-categories/" + categoryId).cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \",\"description\":\"test\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/event-categories/" + categoryId).cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated " + categoryId + "\",\"description\":\"test\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/admin/event-categories").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" updated " + categoryId + " \",\"description\":\"test\"}"))
                .andExpect(status().isConflict());
    }
    @Test void approvesOnlyOnceAndActivatesTickets() throws Exception {
        mvc.perform(get("/api/admin/events/pending/" + eventId).cookie(access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.event.ticketTypes[0].name").value("Ticket"))
                .andExpect(jsonPath("$.version").value(0));
        mvc.perform(post("/api/admin/events/" + eventId + "/approve").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(post("/api/admin/events/" + eventId + "/approve").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0}")).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT status FROM ticket_types WHERE events_id=?", String.class,eventId)).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id=?", Integer.class,organizer.getId())).isEqualTo(1);
    }
    @Test void rejectsAndExposesReasonOnlyToOwner() throws Exception {
        mvc.perform(post("/api/admin/events/" + eventId + "/reject").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0,\"reason\":\"   \"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/events/" + eventId + "/reject").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0,\"reason\":\" Bổ sung địa điểm \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reason").value("Bổ sung địa điểm"));
        mvc.perform(get("/api/events/" + eventId).cookie(organizerAccess))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rejectReason").value("Bổ sung địa điểm"));
        mvc.perform(get("/api/notifications").cookie(organizerAccess)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].content").value("Bổ sung địa điểm"));
        assertThat(jdbc.queryForObject("SELECT status FROM ticket_types WHERE events_id=?", String.class,eventId)).isEqualTo("INACTIVE");
    }
    @Test void organizerLockBumpsVersionEvenWithoutScalarChanges() {
        new TransactionTemplate(transactions).executeWithoutResult(status ->
                events.findOwnedForUpdate(eventId, organizer.getId()).orElseThrow());
        assertThatThrownBy(() -> reviews.decide(admin.getId(), eventId, 0, null))
                .isInstanceOf(com.eventhub.backend.exception.AdminException.class)
                .hasMessageContaining("thay đổi");
    }
    @Test void createsHashedUserAndValidatesDuplicateRoleAndPassword() throws Exception {
        String email = UUID.randomUUID() + "@example.invalid";
        String password = "Admin-test-password";
        var payload = new java.util.HashMap<String, Object>(java.util.Map.of(
                "fullName","Test user","email",email,"phone","0900000000","password",password,"role","CUSTOMER"));
        try {
            mvc.perform(post("/api/admin/users").cookie(access).header("X-CSRF-Protection","1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.password").doesNotExist());
            User created = users.findByNormalizedEmail(email).orElseThrow();
            assertThat(passwords.matches(password,created.getPassword())).isTrue();
            payload.put("email", " " + email.toUpperCase(java.util.Locale.ROOT) + " ");
            mvc.perform(post("/api/admin/users").cookie(access).header("X-CSRF-Protection","1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
            payload.put("role","ADMIN");
            mvc.perform(post("/api/admin/users").cookie(access).header("X-CSRF-Protection","1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.role").exists());
            payload.put("role","CUSTOMER"); payload.put("password","ệ".repeat(25));
            mvc.perform(post("/api/admin/users").cookie(access).header("X-CSRF-Protection","1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        } finally { jdbc.update("DELETE FROM users WHERE email=?",email); }
    }
    @Test void createsUpdatesAndDeletesUnusedCategory() throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("name","New " + UUID.randomUUID(),"description","Test"));
        var result = mvc.perform(post("/api/admin/event-categories").cookie(access).header("X-CSRF-Protection","1")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andReturn();
        int id = json.readTree(result.getResponse().getContentAsString()).get("id").asInt();
        try {
            mvc.perform(put("/api/admin/event-categories/" + id).cookie(access).header("X-CSRF-Protection","1")
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
            mvc.perform(delete("/api/admin/event-categories/" + id).cookie(access).header("X-CSRF-Protection","1"))
                    .andExpect(status().isNoContent());
            assertThat(jdbc.queryForObject("SELECT count(*) FROM categories WHERE id=?",Integer.class,id)).isZero();
        } finally { jdbc.update("DELETE FROM categories WHERE id=?",id); }
    }
    @Test void onlyPendingEventsAreListedAndNotificationsAreIsolated() throws Exception {
        reviews.decide(admin.getId(),eventId,0,"Please clarify");
        mvc.perform(get("/api/admin/events/pending").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == " + eventId + ")]").isEmpty());
        User other = user(Role.ORGANIZER);
        try {
            mvc.perform(get("/api/notifications").cookie(session(other))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty());
        } finally {
            jdbc.update("DELETE FROM refresh_tokens WHERE users_id=?",other.getId());
            users.deleteById(other.getId());
        }
    }
    @Test void failedNotificationRollsBackDecisionAndTickets() {
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("test failure"))
                .when(notifications).saveAndFlush(org.mockito.ArgumentMatchers.any(Notification.class));
        assertThatThrownBy(() -> reviews.decide(admin.getId(),eventId,0,null))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("SELECT status FROM events WHERE id=?",String.class,eventId)).isEqualTo("PENDING_APPROVAL");
        assertThat(jdbc.queryForObject("SELECT status FROM ticket_types WHERE events_id=?",String.class,eventId)).isEqualTo("INACTIVE");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id=?",Integer.class,organizer.getId())).isZero();
    }
    @Test void competingDecisionsProduceOneNotification() throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> decision = () -> {
                ready.countDown(); start.await(5, TimeUnit.SECONDS);
                try { reviews.decide(admin.getId(),eventId,0,null); return true; }
                catch (com.eventhub.backend.exception.AdminException conflict) { return false; }
            };
            var first = executor.submit(decision);
            var second = executor.submit(decision);
            assertThat(ready.await(5,TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(java.util.List.of(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true,false);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id=?",Integer.class,organizer.getId())).isEqualTo(1);
    }
}
