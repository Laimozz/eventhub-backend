package com.eventhub.backend;

import com.eventhub.backend.entity.Category;
import com.eventhub.backend.entity.RefreshToken;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.CategoryRepository;
import com.eventhub.backend.repository.EventGuestRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.NotificationRepository;
import com.eventhub.backend.repository.RefreshTokenRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.security.JwtService;
import com.eventhub.backend.service.EventImageService;
import jakarta.servlet.http.Cookie;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties", properties = {
        "app.cloudinary.cloud-name=test-cloud", "app.cloudinary.api-key=test-only-key", "app.cloudinary.api-secret=test-only-secret"})
class EventIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private jakarta.persistence.EntityManager entityManager;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
    @Autowired private RefreshTokenRepository tokens;
    @Autowired private JwtService jwt;
    @MockitoSpyBean private EventGuestRepository guests;
    @MockitoSpyBean private NotificationRepository notifications;
    @MockitoSpyBean private EventImageService images;
    @MockitoSpyBean private EventRepository events;

    private User organizer;
    private User admin;
    private Category category;
    private Cookie access;
    private String venueAddress;
    private Map<String, Long> initialCounts;

    @BeforeEach
    void prepareOrganizerSessionAndCategory() {
        organizer = new User();
        organizer.setEmail(UUID.randomUUID() + "@example.invalid");
        organizer.setPassword("{invalid}test-hash");
        organizer.setRole(Role.ORGANIZER);
        organizer.setStatus("ACTIVE");
        users.saveAndFlush(organizer);
        RefreshToken session = new RefreshToken();
        session.setUser(organizer);
        session.setTokenHash(jwt.hashRefreshToken(jwt.createRefreshToken()));
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        tokens.saveAndFlush(session);
        access = new Cookie(AuthCookies.ACCESS,
                jwt.createAccessToken(organizer.getId(), session.getId(), Role.ORGANIZER));
        category = new Category();
        category.setName("Event test category");
        categories.saveAndFlush(category);
        venueAddress = "Event test venue " + UUID.randomUUID();
        initialCounts = rowCounts();
        doAnswer(invocation -> "https://res.cloudinary.com/test-cloud/image/upload/v1/eventhub/events/"
                + invocation.getArgument(1) + ".png").when(images).upload(any(), anyString());
        doNothing().when(images).delete(anyString());
    }

    @AfterEach
    void cleanOnlyThisTestsData() throws Exception {
        if (admin != null) {
            jdbc.update("DELETE FROM notifications WHERE users_id = ?", admin.getId());
        }
        jdbc.update("DELETE FROM event_guests WHERE events_id IN (SELECT id FROM events WHERE organizer_id = ?)",
                organizer.getId());
        jdbc.update("DELETE FROM ticket_types WHERE events_id IN (SELECT id FROM events WHERE organizer_id = ?)",
                organizer.getId());
        jdbc.update("DELETE FROM events WHERE organizer_id = ?", organizer.getId());
        if (admin != null) jdbc.update("DELETE FROM users WHERE id = ?", admin.getId());
        jdbc.update("DELETE FROM venues WHERE address = ?", venueAddress);
        jdbc.update("DELETE FROM categories WHERE id = ?", category.getId());
        jdbc.update("DELETE FROM refresh_tokens WHERE users_id = ?", organizer.getId());
        jdbc.update("DELETE FROM users WHERE id = ?", organizer.getId());
    }

    @Test
    void shouldCreateCompletePendingEventAndIgnoreClientControlledOwnershipAndLifecycle() throws Exception {
        var body = validRequest();
        body.put("organizerId", -1);
        body.put("status", "APPROVED");
        body.put("reviewedBy", organizer.getId());
        body.put("reviewedAt", "2030-01-01T00:00:00");
        firstTicket(body).put("reservedQuantity", 50);
        firstTicket(body).put("remainingQuantity", 1);
        firstTicket(body).put("status", "ACTIVE");

        var result = create(body).andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizerId").value(organizer.getId()))
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.venue.capacity").value(100))
                .andExpect(jsonPath("$.ticketTypes.length()").value(2))
                .andExpect(jsonPath("$.ticketTypes[0].price").value(150000.50))
                .andExpect(jsonPath("$.ticketTypes[0].reservedQuantity").value(0))
                .andExpect(jsonPath("$.ticketTypes[0].remainingQuantity").value(60))
                .andExpect(jsonPath("$.ticketTypes[0].status").value("INACTIVE"))
                .andExpect(jsonPath("$.guests[0].name").value("Test speaker"))
                .andExpect(jsonPath("$.createdAt").exists()).andReturn();
        var response = json.readTree(result.getResponse().getContentAsString());
        int eventId = response.get("id").asInt();
        var event = jdbc.queryForMap("SELECT * FROM events WHERE id = ?", eventId);
        assertThat(event.get("organizer_id")).isEqualTo(organizer.getId());
        assertThat(event.get("status")).isEqualTo("PENDING_APPROVAL");
        assertThat(jdbc.queryForObject("SELECT start_time FROM events WHERE id = ?", LocalDateTime.class, eventId))
                .isEqualTo(LocalDateTime.parse((String) body.get("startTime")));
        assertThat(jdbc.queryForObject("SELECT end_time FROM events WHERE id = ?", LocalDateTime.class, eventId))
                .isEqualTo(LocalDateTime.parse((String) body.get("endTime")));
        assertThat(jdbc.queryForObject("SELECT created_at FROM events WHERE id = ?", LocalDateTime.class, eventId))
                .isCloseTo(LocalDateTime.parse(response.get("createdAt").asText()), within(1, ChronoUnit.MICROS));
        int ticketTypeId = response.get("ticketTypes").get(0).get("id").asInt();
        assertThat(jdbc.queryForObject("SELECT sale_start_time FROM ticket_types WHERE id = ?",
                LocalDateTime.class, ticketTypeId))
                .isEqualTo(LocalDateTime.parse((String) firstTicket(body).get("saleStartTime")));
        assertThat(jdbc.queryForObject("SELECT sale_end_time FROM ticket_types WHERE id = ?",
                LocalDateTime.class, ticketTypeId))
                .isEqualTo(LocalDateTime.parse((String) firstTicket(body).get("saleEndTime")));
        assertThat(event.get("reviewed_by")).isNull();
        assertThat(event.get("reviewed_at")).isNull();
        assertThat(event.get("cancel_reason")).isNull();
        assertThat(event.get("canceled_at")).isNull();
        assertThat(event.get("reject_reason")).isNull();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ticket_types WHERE events_id = ?", Integer.class,
                eventId)).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM event_guests WHERE events_id = ?", Integer.class,
                eventId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications", Long.class))
                .isEqualTo(initialCounts.get("notifications"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"omitted", "null", "empty"})
    void shouldAllowEventsWithoutGuestsAndFreeTickets(String guestInput) throws Exception {
        var body = validRequest();
        switch (guestInput) {
            case "omitted" -> body.remove("guests");
            case "null" -> body.put("guests", null);
            default -> body.put("guests", List.of());
        }
        firstTicket(body).put("price", 0);
        create(body).andExpect(status().isCreated()).andExpect(jsonPath("$.guests").isEmpty())
                .andExpect(jsonPath("$.ticketTypes[0].price").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "CUSTOMER", "STAFF"})
    void shouldRejectOtherRolesUsingCurrentDatabaseRole(String role) throws Exception {
        jdbc.update("UPDATE users SET role = ? WHERE id = ?", role, organizer.getId());
        create(validRequest()).andExpect(status().isForbidden());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldRequireValidSessionAndCsrfHeader() throws Exception {
        var body = new MockMultipartFile("event", "event.json", "application/json", json.writeValueAsBytes(validRequest()));
        mvc.perform(multipart("/api/events").file(body).header("X-CSRF-Protection", "1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/events").file(body).cookie(new Cookie(AuthCookies.ACCESS, "invalid-token"))
                .header("X-CSRF-Protection", "1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/events").file(body).cookie(access))
                .andExpect(status().isForbidden());
        jdbc.update("UPDATE users SET status = 'INACTIVE' WHERE id = ?", organizer.getId());
        create(validRequest()).andExpect(status().isUnauthorized());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    void shouldRejectInvalidInputWithoutSavingPartialData(String description,
            Consumer<Map<String, Object>> invalidate) throws Exception {
        var body = validRequest();
        invalidate.accept(body);
        create(body).andExpect(status().isBadRequest());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                invalid("blank name", b -> b.put("name", "  ")),
                invalid("oversized description", b -> b.put("description", "x".repeat(256))),
                invalid("missing category", b -> b.remove("categoryId")),
                invalid("missing venue", b -> b.remove("venue")),
                invalid("blank address", b -> venue(b).put("address", "  ")),
                invalid("invalid capacity", b -> venue(b).put("capacity", 0)),
                invalid("missing tickets", b -> b.remove("ticketTypes")),
                invalid("empty tickets", b -> b.put("ticketTypes", List.of())),
                invalid("null ticket", b -> b.put("ticketTypes", java.util.Arrays.asList((Object) null))),
                invalid("negative price", b -> firstTicket(b).put("price", -1)),
                invalid("excess money precision", b -> firstTicket(b).put("price", "0.001")),
                invalid("zero ticket quantity", b -> firstTicket(b).put("quantity", 0)),
                invalid("missing sale time", b -> firstTicket(b).remove("saleStartTime")),
                invalid("missing start time", b -> b.remove("startTime")),
                invalid("invalid time format", b -> b.put("startTime", "invalid-time")),
                invalid("event in the past", b -> b.put("startTime", "2000-01-01T00:00:00")),
                invalid("equal event times", b -> b.put("endTime", b.get("startTime"))),
                invalid("reversed event times", b -> b.put("endTime", "2000-01-01T00:00:00")),
                invalid("reversed sale times", b -> firstTicket(b).put("saleEndTime", "2000-01-01T00:00:00")),
                invalid("sale starts at event start", b -> firstTicket(b).put("saleStartTime", b.get("startTime"))),
                invalid("sale ends at event start", b -> firstTicket(b).put("saleEndTime", b.get("startTime"))),
                invalid("sale after event", b -> firstTicket(b).put("saleEndTime", b.get("endTime"))),
                invalid("ticket capacity exceeded", b -> venue(b).put("capacity", 99)),
                invalid("ticket sum overflow", b -> {
                    venue(b).put("capacity", Integer.MAX_VALUE);
                    firstTicket(b).put("quantity", Integer.MAX_VALUE);
                }),
                invalid("null guest", b -> b.put("guests", java.util.Arrays.asList((Object) null))),
                invalid("oversized guest name", b -> b.put("guests",
                        List.of(Map.of("name", "x".repeat(51), "role", "Speaker")))));
    }

    @Test
    void shouldReturnNotFoundForUnknownCategory() throws Exception {
        var body = validRequest();
        body.put("categoryId", Integer.MAX_VALUE);
        create(body).andExpect(status().isNotFound());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldRejectLegacyJsonCreationWithoutUploadingOrSaving() throws Exception {
        mvc.perform(post("/api/events").cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(validRequest())))
                .andExpect(status().isUnsupportedMediaType());
        verify(images, never()).upload(any(), anyString());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldReturnExistingCategoriesForAuthenticatedUser() throws Exception {
        mvc.perform(get("/api/categories").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(category.getId())))
                .andExpect(jsonPath("$[*].name").value(org.hamcrest.Matchers.hasItem(category.getName())));
        mvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCloudinaryUrlAndStoreItInEventImageColumns() throws Exception {
        String url = "https://res.cloudinary.com/test-cloud/image/upload/v1/eventhub/events/poster.png";
        doAnswer(invocation -> {
            assertThat(jdbc.queryForObject("SELECT count(*) FROM events WHERE organizer_id = ?", Long.class, organizer.getId())).isEqualTo(1);
            return url;
        }).when(images).upload(any(), anyString());
        var body = validRequest();
        body.put("bannerImageUrl", "https://example.invalid/client-controlled.jpg");
        body.put("thumbnailImageUrl", "https://example.invalid/client-controlled.jpg");
        body.put("imageZoneUrl", "https://example.invalid/client-controlled.jpg");
        firstTicket(body).put("imageUrl", "https://example.invalid/client-controlled.jpg");
        var files = validImages();
        files.put("imageZone", pngImage("imageZone"));
        files.put("guestImage0", pngImage("guestImage0"));
        createWithImages(body, files).andExpect(status().isCreated()).andExpect(jsonPath("$.bannerImageUrl").value(url));
        var event = jdbc.queryForMap("SELECT banner_image_url, thumbnail_image_url, image_zone_url FROM events WHERE organizer_id = ?", organizer.getId());
        assertThat(event.values()).containsOnly(url);
        assertThat(jdbc.queryForList("SELECT image_url FROM ticket_types WHERE events_id IN (SELECT id FROM events WHERE organizer_id = ?)", String.class, organizer.getId())).contains(url);
        assertThat(jdbc.queryForObject("SELECT image_url FROM event_guests WHERE events_id IN (SELECT id FROM events WHERE organizer_id = ?)", String.class, organizer.getId())).isEqualTo(url);
        verify(images, never()).delete(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "ADMIN", "STAFF"})
    void shouldRestrictImageUploadToOrganizer(String role) throws Exception {
        jdbc.update("UPDATE users SET role = ? WHERE id = ?", role, organizer.getId());
        mvc.perform(multipart("/api/events").file(pngImage("bannerImage")).cookie(access)
                .header("X-CSRF-Protection", "1")).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidImagesAndRequireSessionAndCsrfForUploads() throws Exception {
        mvc.perform(multipart("/api/events").file(pngImage("bannerImage")).header("X-CSRF-Protection", "1"))
                .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/events").file(pngImage("bannerImage")).cookie(access))
                .andExpect(status().isForbidden());
        var files = validImages();
        files.put("bannerImage", new MockMultipartFile("bannerImage", "fake.png", "image/png", "not an image".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        createWithImages(validRequest(), files).andExpect(status().isBadRequest());
        files.put("bannerImage", new MockMultipartFile("bannerImage", "empty.png", "image/png", new byte[0]));
        createWithImages(validRequest(), files).andExpect(status().isBadRequest());
        verify(images, never()).upload(any(), anyString());
        mvc.perform(multipart("/api/events/images").file(pngImage("file")).cookie(access)
                .header("X-CSRF-Protection", "1")).andExpect(status().isNotFound());
        mvc.perform(multipart("/api/event-images").file(pngImage("file")).cookie(access)
                .header("X-CSRF-Protection", "1")).andExpect(status().isNotFound());
        mvc.perform(get("/api/event-images/unknown.png").cookie(access)).andExpect(status().isNotFound());
    }

    private MockMultipartFile pngImage(String name) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return new MockMultipartFile(name, "../../original.png", "image/png", bytes.toByteArray());
    }

    private Map<String, MockMultipartFile> validImages() throws Exception {
        Map<String, MockMultipartFile> files = new java.util.LinkedHashMap<>();
        for (String name : List.of("bannerImage", "thumbnailImage", "ticketImage0", "ticketImage1")) files.put(name, pngImage(name));
        return files;
    }

    private ResultActions createWithImages(Map<String, Object> body, Map<String, MockMultipartFile> files) throws Exception {
        var request = multipart("/api/events").file(new MockMultipartFile("event", "event.json", "application/json", json.writeValueAsBytes(body)));
        files.values().forEach(request::file);
        return mvc.perform(request.cookie(access).header("X-CSRF-Protection", "1"));
    }

    @Test
    void shouldNotUploadImagesIfEventValidationFails() throws Exception {
        var body = validRequest();
        body.put("endTime", body.get("startTime"));
        createWithImages(body, validImages()).andExpect(status().isBadRequest());
        verify(images, never()).upload(any(), anyString());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldNotUploadImagesIfSavingEventDataFails() throws Exception {
        createAdmin("ACTIVE");
        doThrow(new DataIntegrityViolationException("Simulated notification failure")).when(notifications).save(any());
        createWithImages(validRequest(), validImages()).andExpect(status().isConflict());
        verify(images, never()).upload(any(), anyString());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldRollbackEventAndDeleteAttemptedUploadsWhenOneUploadFails() throws Exception {
        List<String> attempts = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        doAnswer(invocation -> {
            attempts.add(invocation.getArgument(1));
            if (attempts.size() == 2) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Simulated provider failure");
            return "https://res.cloudinary.com/test-cloud/image/upload/v1/eventhub/events/" + invocation.getArgument(1) + ".png";
        }).when(images).upload(any(), anyString());
        doAnswer(invocation -> { deleted.add(invocation.getArgument(0)); return null; }).when(images).delete(anyString());
        createWithImages(validRequest(), validImages()).andExpect(status().isBadGateway());
        assertThat(attempts).hasSize(2);
        assertThat(deleted).containsExactlyElementsOf(attempts);
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldRejectMissingOrUnexpectedImagePartsWithoutUploading() throws Exception {
        for (String name : List.of("bannerImage", "thumbnailImage", "ticketImage0", "ticketImage1")) {
            var missingFile = validImages();
            missingFile.remove(name);
            createWithImages(validRequest(), missingFile).andExpect(status().isBadRequest());
        }
        var files = validImages();
        files.put("guestImage99", pngImage("guestImage99"));
        createWithImages(validRequest(), files).andExpect(status().isBadRequest());
        verify(images, never()).upload(any(), anyString());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldDeleteUploadedImagesIfSavingTheirUrlsFails() throws Exception {
        List<String> attempted = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        doAnswer(invocation -> {
            attempted.add(invocation.getArgument(1));
            return "https://res.cloudinary.com/test-cloud/image/upload/v1/eventhub/events/" + invocation.getArgument(1) + ".png";
        }).when(images).upload(any(), anyString());
        doAnswer(invocation -> { deleted.add(invocation.getArgument(0)); return null; }).when(images).delete(anyString());
        var flushCount = new java.util.concurrent.atomic.AtomicInteger();
        doAnswer(invocation -> {
            if (flushCount.incrementAndGet() == 2) throw new DataIntegrityViolationException("Simulated image URL persistence failure");
            entityManager.flush();
            return null;
        }).when(events).flush();
        createWithImages(validRequest(), validImages()).andExpect(status().isConflict());
        assertThat(attempted).hasSize(4);
        assertThat(deleted).containsExactlyElementsOf(attempted);
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldNotUploadOrDeleteImagesWhenCloudinaryIsNotConfigured() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Not configured"))
                .when(images).verifyConfiguration();
        createWithImages(validRequest(), validImages()).andExpect(status().isServiceUnavailable());
        verify(images, never()).upload(any(), anyString());
        verify(images, never()).delete(anyString());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldNotifyAdminWithAnUnreadMessageAfterCreatingEvent() throws Exception {
        createAdmin("ACTIVE");
        LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
        create(validRequest()).andExpect(status().isCreated());

        var messages = jdbc.queryForList("SELECT * FROM notifications WHERE users_id = ?", admin.getId());
        assertThat(messages).hasSize(1);
        var message = messages.getFirst();
        assertThat(message.get("title")).isEqualTo("Có sự kiện mới chờ duyệt");
        assertThat(message.get("content")).isEqualTo("Sự kiện \"Organizer test event\" vừa được gửi lên chờ duyệt.");
        assertThat(message.get("type")).isEqualTo("EVENT_PENDING_APPROVAL");
        assertThat(message.get("is_read")).isEqualTo(false);
        assertThat(jdbc.queryForObject("SELECT created_at FROM notifications WHERE id = ?",
                LocalDateTime.class, message.get("id")))
                .isBetween(before, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id = ?", Long.class,
                organizer.getId())).isZero();
        assertThat(rowCounts().get("notifications")).isEqualTo(initialCounts.get("notifications") + 1);
    }

    @Test
    void shouldKeepNotificationWithinDatabaseLimitForLongEventName() throws Exception {
        createAdmin("ACTIVE");
        var body = validRequest();
        body.put("name", "x".repeat(255));
        create(body).andExpect(status().isCreated());
        String content = jdbc.queryForObject("SELECT content FROM notifications WHERE users_id = ?",
                String.class, admin.getId());
        assertThat(content).hasSize(255).startsWith("Sự kiện \"").contains("...")
                .endsWith("\" vừa được gửi lên chờ duyệt.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVE", "BLOCKED"})
    void shouldCreateEventWithoutNotifyingInactiveAdmin(String status) throws Exception {
        createAdmin(status);
        create(validRequest()).andExpect(status().isCreated());
        assertThat(rowCounts().get("events")).isEqualTo(initialCounts.get("events") + 1);
        assertThat(rowCounts().get("notifications")).isEqualTo(initialCounts.get("notifications"));
    }

    @Test
    void shouldRollbackEntireEventIfSavingAdminNotificationFails() throws Exception {
        createAdmin("ACTIVE");
        doThrow(new DataIntegrityViolationException("Simulated notification persistence failure"))
                .when(notifications).save(any());
        create(validRequest()).andExpect(status().isConflict());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    @Test
    void shouldRollbackVenueEventAndTicketsIfSavingGuestsFails() throws Exception {
        createAdmin("ACTIVE");
        doThrow(new DataIntegrityViolationException("Simulated guest persistence failure")).when(guests).saveAll(any());
        create(validRequest()).andExpect(status().isConflict());
        assertThat(rowCounts()).isEqualTo(initialCounts);
    }

    private ResultActions create(Map<String, Object> body) throws Exception {
        return createWithImages(body, validImages());
    }

    @Test
    void shouldPaginateSearchAndFilterOrganizerEvents() throws Exception {
        int firstId = createEventId();
        int secondId = createEventId();
        jdbc.update("UPDATE events SET status = 'APPROVED' WHERE id = ?", secondId);
        mvc.perform(get("/api/events/mine").cookie(access).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.statusCounts.PENDING_APPROVAL").value(1))
                .andExpect(jsonPath("$.statusCounts.APPROVED").value(1));
        mvc.perform(get("/api/events/mine").cookie(access).param("status", "PENDING_APPROVAL")
                .param("search", "ORGANIZER test"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(firstId))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/events/mine").cookie(access).param("search", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/events/mine").cookie(access).param("size", "51")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/events/mine").cookie(access).param("status", "DRAFT")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldHideOtherOrganizersEventsForEveryOperation() throws Exception {
        int eventId = createEventId();
        var body = updateRequest(eventId);
        createAdmin("ACTIVE");
        jdbc.update("UPDATE events SET organizer_id = ? WHERE id = ?", admin.getId(), eventId);
        try {
            mvc.perform(get("/api/events/mine").cookie(access)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
            mvc.perform(get("/api/events/{eventId}", eventId).cookie(access)).andExpect(status().isNotFound());
            update(eventId, body, Map.of()).andExpect(status().isNotFound());
            mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Test cancellation\"}"))
                    .andExpect(status().isNotFound());
        } finally {
            jdbc.update("UPDATE events SET organizer_id = ? WHERE id = ?", organizer.getId(), eventId);
        }
    }

    @Test
    void shouldUpdateWithoutReuploadingAndPreserveAllocatedTickets() throws Exception {
        createAdmin("ACTIVE");
        int eventId = createEventId();
        var body = updateRequest(eventId);
        int ticketId = firstTicket(body).get("id") instanceof Number number ? number.intValue() : -1;
        jdbc.update("UPDATE events SET status = 'APPROVED', reviewed_by = ?, reviewed_at = start_time WHERE id = ?", admin.getId(), eventId);
        jdbc.update("UPDATE ticket_types SET remaining_quantity = 50, reserved_quantity = 3, status = 'ACTIVE' WHERE id = ?", ticketId);
        String banner = (String) body.get("bannerImageUrl");
        firstTicket(body).put("quantity", 65);
        ticketInputs(body).get(1).put("quantity", 35);
        body.put("name", "Updated organizer event");
        org.mockito.Mockito.clearInvocations(images);
        update(eventId, body, Map.of()).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated organizer event"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.bannerImageUrl").value(banner))
                .andExpect(jsonPath("$.ticketTypes[0].id").value(ticketId))
                .andExpect(jsonPath("$.ticketTypes[0].remainingQuantity").value(55))
                .andExpect(jsonPath("$.ticketTypes[0].reservedQuantity").value(3))
                .andExpect(jsonPath("$.ticketTypes[0].status").value("INACTIVE"));
        verify(images, never()).upload(any(), anyString());
        assertThat(jdbc.queryForMap("SELECT reviewed_by, reviewed_at FROM events WHERE id = ?", eventId))
                .containsEntry("reviewed_by", null).containsEntry("reviewed_at", null);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id = ?", Integer.class, admin.getId())).isEqualTo(2);
    }

    @Test
    void shouldAddRemoveAndReplaceTicketAndGuestImagesOnUpdate() throws Exception {
        int eventId = createEventId();
        var body = updateRequest(eventId);
        var tickets = ticketInputs(body);
        int removedId = ((Number) tickets.remove(1).get("id")).intValue();
        var newTicket = new HashMap<>(firstTicket(body));
        newTicket.remove("id"); newTicket.put("name", "New ticket"); newTicket.put("quantity", 20);
        tickets.add(newTicket);
        body.put("guests", List.of(Map.of("name", "New guest", "role", "MC")));
        update(eventId, body, Map.of()).andExpect(status().isBadRequest());
        update(eventId, body, Map.of("ticketImage1", pngImage("ticketImage1"), "bannerImage", pngImage("bannerImage"),
                "guestImage0", pngImage("guestImage0"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketTypes.length()").value(2))
                .andExpect(jsonPath("$.ticketTypes[1].name").value("New ticket"))
                .andExpect(jsonPath("$.guests.length()").value(1))
                .andExpect(jsonPath("$.guests[0].name").value("New guest"))
                .andExpect(jsonPath("$.guests[0].imageUrl").isNotEmpty());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM ticket_types WHERE id = ?", Integer.class, removedId)).isZero();
    }

    @Test
    void shouldRejectForeignOrDuplicateChildIdsAndUnsafeInventoryChanges() throws Exception {
        int eventId = createEventId();
        int otherId = createEventId();
        var body = updateRequest(eventId);
        var foreign = updateRequest(otherId);
        int ticketId = ((Number) firstTicket(body).get("id")).intValue();
        firstTicket(body).put("id", firstTicket(foreign).get("id"));
        update(eventId, body, Map.of()).andExpect(status().isBadRequest());
        firstTicket(body).put("id", ticketId);
        ticketInputs(body).get(1).put("id", ticketId);
        update(eventId, body, Map.of()).andExpect(status().isBadRequest());
        body = updateRequest(eventId);
        jdbc.update("UPDATE ticket_types SET remaining_quantity = 50, reserved_quantity = 5 WHERE id = ?", ticketId);
        firstTicket(body).put("quantity", 9);
        update(eventId, body, Map.of()).andExpect(status().isConflict());
        ticketInputs(body).removeFirst();
        update(eventId, body, Map.of()).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT remaining_quantity FROM ticket_types WHERE id = ?", Integer.class, ticketId)).isEqualTo(50);
    }

    @Test
    void shouldRequestCancellationAndBlockFurtherChangesWhileAwaitingAdmin() throws Exception {
        createAdmin("ACTIVE");
        int eventId = createEventId();
        var body = updateRequest(eventId);
        mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"  Venue unavailable  \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_CANCELLATION"))
                .andExpect(jsonPath("$.cancelReason").value("Venue unavailable"))
                .andExpect(jsonPath("$.canceledAt").isEmpty()).andExpect(jsonPath("$.canEdit").value(false))
                .andExpect(jsonPath("$.canCancel").value(false))
                .andExpect(jsonPath("$.ticketTypes[0].status").value("INACTIVE"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE type = 'EVENT_PENDING_CANCELLATION' AND users_id = ?",
                Integer.class, admin.getId())).isEqualTo(1);
        update(eventId, body, Map.of()).andExpect(status().isConflict());
        mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Retry\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRemoveOptionalImagesWithoutUploading() throws Exception {
        var files = validImages();
        files.put("imageZone", pngImage("imageZone"));
        files.put("guestImage0", pngImage("guestImage0"));
        int eventId = json.readTree(createWithImages(validRequest(), files).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()).get("id").asInt();
        var body = updateRequest(eventId);
        body.put("removeImageZone", true);
        firstGuest(body).put("removeImage", true);
        org.mockito.Mockito.clearInvocations(images);
        update(eventId, body, Map.of()).andExpect(status().isOk())
                .andExpect(jsonPath("$.imageZoneUrl").isEmpty()).andExpect(jsonPath("$.guests[0].imageUrl").isEmpty());
        verify(images, never()).upload(any(), anyString());
    }

    @Test
    void shouldRollbackUpdatesAndCleanNewUploadsIfReplacingAnImageFails() throws Exception {
        int eventId = createEventId();
        var body = updateRequest(eventId);
        String originalBanner = (String) body.get("bannerImageUrl");
        body.put("name", "Should roll back");
        body.put("guests", List.of());
        List<String> attempted = new ArrayList<>();
        doAnswer(invocation -> {
            attempted.add(invocation.getArgument(1));
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Upload failed");
        }).when(images).upload(any(), anyString());
        update(eventId, body, Map.of("bannerImage", pngImage("bannerImage"))).andExpect(status().isBadGateway());
        mvc.perform(get("/api/events/{eventId}", eventId).cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organizer test event"))
                .andExpect(jsonPath("$.bannerImageUrl").value(originalBanner))
                .andExpect(jsonPath("$.guests.length()").value(1));
        assertThat(attempted).hasSize(1);
        verify(images).delete(attempted.getFirst());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "ADMIN", "STAFF"})
    void shouldDenyNonOrganizersAllManagementEndpoints(String role) throws Exception {
        int eventId = createEventId();
        var body = updateRequest(eventId);
        organizer.setRole(Role.valueOf(role));
        users.saveAndFlush(organizer);
        mvc.perform(get("/api/events/mine").cookie(access)).andExpect(status().isForbidden());
        mvc.perform(get("/api/events/{eventId}", eventId).cookie(access)).andExpect(status().isForbidden());
        update(eventId, body, Map.of()).andExpect(status().isForbidden());
        mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldValidateCancellationReasonAndCsrf() throws Exception {
        int eventId = createEventId();
        mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access)
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Test\"}"))
                .andExpect(status().isForbidden());
        for (String reason : List.of("  ", "x".repeat(256))) {
            mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(Map.of("reason", reason))))
                    .andExpect(status().isBadRequest());
        }
        assertThat(jdbc.queryForObject("SELECT status FROM events WHERE id = ?", String.class, eventId)).isEqualTo("PENDING_APPROVAL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMPLETED", "CANCELED"})
    void shouldPreventEditingAndCancelingTerminalEvents(String eventStatus) throws Exception {
        int eventId = createEventId();
        var body = updateRequest(eventId);
        jdbc.update("UPDATE events SET status = ? WHERE id = ?", eventStatus, eventId);
        mvc.perform(get("/api/events/{eventId}", eventId).cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(false)).andExpect(jsonPath("$.canCancel").value(false));
        update(eventId, body, Map.of()).andExpect(status().isConflict());
        mvc.perform(post("/api/events/{eventId}/cancel", eventId).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Test\"}"))
                .andExpect(status().isConflict());
    }

    private int createEventId() throws Exception {
        return json.readTree(create(validRequest()).andExpect(status().isCreated()).andReturn()
                .getResponse().getContentAsString()).get("id").asInt();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> updateRequest(int eventId) throws Exception {
        return json.readValue(mvc.perform(get("/api/events/{eventId}", eventId).cookie(access))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> ticketInputs(Map<String, Object> body) {
        return (List<Map<String, Object>>) body.get("ticketTypes");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstGuest(Map<String, Object> body) {
        return ((List<Map<String, Object>>) body.get("guests")).getFirst();
    }

    private ResultActions update(int eventId, Map<String, Object> body, Map<String, MockMultipartFile> files) throws Exception {
        var request = multipart(org.springframework.http.HttpMethod.PUT, "/api/events/{eventId}", eventId)
                .file(new MockMultipartFile("event", "event.json", "application/json", json.writeValueAsBytes(body)));
        files.values().forEach(request::file);
        return mvc.perform(request.cookie(access).header("X-CSRF-Protection", "1"));
    }

    private void createAdmin(String status) {
        admin = new User();
        admin.setEmail(UUID.randomUUID() + "@example.invalid");
        admin.setPassword("{invalid}test-hash");
        admin.setRole(Role.ADMIN);
        admin.setStatus(status);
        users.saveAndFlush(admin);
    }

    private Map<String, Object> validRequest() {
        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC).plusDays(30).withNano(0);
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Organizer test event");
        body.put("description", "Test event description");
        body.put("startTime", start.toString());
        body.put("endTime", start.plusHours(3).toString());
        body.put("categoryId", category.getId());
        body.put("venue", new HashMap<>(Map.of("city", "Test city", "address", venueAddress, "capacity", 100)));
        List<Map<String, Object>> ticketInputs = new ArrayList<>();
        for (int quantity : List.of(60, 40)) {
            ticketInputs.add(new HashMap<>(Map.of("name", "Ticket " + quantity, "quantity", quantity,
                    "price", "150000.50",
                    "saleStartTime", start.minusDays(10).toString(), "saleEndTime", start.minusHours(1).toString())));
        }
        body.put("ticketTypes", ticketInputs);
        body.put("guests", List.of(Map.of("name", "Test speaker", "role", "Speaker")));
        return body;
    }

    private Map<String, Long> rowCounts() {
        Map<String, Long> counts = new HashMap<>();
        for (String table : List.of("events", "venues", "ticket_types", "event_guests", "notifications")) {
            counts.put(table, jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class));
        }
        return counts;
    }

    private static Arguments invalid(String description, Consumer<Map<String, Object>> invalidate) {
        return Arguments.of(description, invalidate);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstTicket(Map<String, Object> body) {
        return ((List<Map<String, Object>>) body.get("ticketTypes")).getFirst();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> venue(Map<String, Object> body) {
        return (Map<String, Object>) body.get("venue");
    }
}
