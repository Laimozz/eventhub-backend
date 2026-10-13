package com.eventhub.backend;

import com.eventhub.backend.dto.request.CreateBookingRequest;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.*;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.security.JwtService;
import com.eventhub.backend.service.BookingService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties", properties = {
        "app.booking.expiry-enabled=false", "app.booking.ipn-grace-period=120s", "app.vnpay.tmn-code=TESTCODE",
        "app.vnpay.hash-secret=booking-test-only-secret",
        "app.vnpay.return-url=http://localhost:8080/api/payments/vnpay/return"})
class BookingIntegrationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private BookingService service;
    @Autowired private BookingRepository bookings;
    @MockitoSpyBean private Clock clock;
    @Autowired private UserRepository users;
    @Autowired private CategoryRepository categories;
    @Autowired private VenueRepository venues;
    @Autowired private EventRepository events;
    @Autowired private TicketTypeRepository types;
    @Autowired private RefreshTokenRepository sessions;
    @Autowired private JwtService jwt;
    @MockitoSpyBean private TicketRepository tickets;

    private final List<User> testUsers = new ArrayList<>();
    private User customer;
    private User other;
    private User organizer;
    private Category category;
    private Venue venue;
    private Event event;
    private TicketType type;
    private Cookie access;

    @BeforeEach
    void prepare() {
        customer = user(Role.CUSTOMER);
        other = user(Role.CUSTOMER);
        organizer = user(Role.ORGANIZER);
        category = new Category();
        category.setName("Booking test " + UUID.randomUUID());
        categories.saveAndFlush(category);
        venue = new Venue();
        venue.setCity("Test city");
        venue.setAddress("Booking test venue " + UUID.randomUUID());
        venue.setCapacity(100);
        venues.saveAndFlush(venue);
        event = new Event();
        event.setOrganizer(organizer);
        event.setCategory(category);
        event.setVenue(venue);
        event.setName("Booking test event");
        event.setThumbnailImageUrl("https://example.invalid/thumbnail.png");
        event.setBannerImageUrl("https://example.invalid/banner.png");
        event.setStartTime(now().plusDays(1));
        event.setEndTime(now().plusDays(1).plusHours(2));
        event.setStatus(EventStatus.APPROVED);
        events.saveAndFlush(event);
        type = ticketType(new BigDecimal("150000.50"), 5);
        access = cookie(customer);
    }

    @AfterEach
    void cleanup() {
        if (event != null && event.getId() != null) {
            Integer id = event.getId();
            jdbc.update("DELETE FROM tickets WHERE booking_items_id IN (SELECT i.id FROM booking_items i JOIN bookings b ON b.id = i.bookings_id WHERE b.events_id = ?)", id);
            jdbc.update("DELETE FROM payments WHERE bookings_id IN (SELECT id FROM bookings WHERE events_id = ?)", id);
            jdbc.update("DELETE FROM booking_items WHERE bookings_id IN (SELECT id FROM bookings WHERE events_id = ?)", id);
            jdbc.update("DELETE FROM bookings WHERE events_id = ?", id);
            jdbc.update("DELETE FROM ticket_types WHERE events_id = ?", id);
            jdbc.update("DELETE FROM events WHERE id = ?", id);
        }
        if (venue != null && venue.getId() != null) venues.deleteById(venue.getId());
        if (category != null && category.getId() != null) categories.deleteById(category.getId());
        for (User user : testUsers) {
            jdbc.update("DELETE FROM refresh_tokens WHERE users_id = ?", user.getId());
            users.deleteById(user.getId());
        }
    }

    @Test
    void createsBookingUsingServerPriceAndReservesInventory() throws Exception {
        var response = create(2);
        assertThat(response.get("status").asText()).isEqualTo("PENDING");
        assertThat(response.get("totalAmount").decimalValue()).isEqualByComparingTo("300001.00");
        assertThat(response.get("currency").asText()).isEqualTo("VND");
        assertInventory(3, 2);
        assertThat(response.at("/items/0/tickets").size()).isZero();
        mvc.perform(get("/api/bookings/events/{id}/ticket-types", event.getId()).cookie(access))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].remainingQuantity").value(3))
                .andExpect(jsonPath("$[0].bookable").value(true));
    }

    @Test
    void enforcesAuthenticationCustomerRoleAndCsrf() throws Exception {
        mvc.perform(get("/api/bookings")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bookings").cookie(cookie(organizer))).andExpect(status().isForbidden());
        mvc.perform(post("/api/bookings").cookie(access).contentType(MediaType.APPLICATION_JSON).content(body(1)))
                .andExpect(status().isForbidden());
        assertInventory(5, 0);
    }

    @Test
    void validatesQuantityDuplicateTypesAndMissingItems() throws Exception {
        for (String body : List.of(body(0), body(-1), body(101),
                "{\"eventId\":" + event.getId() + ",\"items\":[]}",
                "{\"eventId\":" + event.getId() + ",\"items\":[null]}",
                "{\"eventId\":" + event.getId() + ",\"items\":[{\"ticketTypeId\":" + type.getId()
                        + ",\"quantity\":1},{\"ticketTypeId\":" + type.getId() + ",\"quantity\":1}]}")) {
            createRequest(access, body).andExpect(status().isBadRequest());
        }
        assertInventory(5, 0);
    }

    @Test
    void refusesUnavailableEventsClosedSalesForeignTypesAndInsufficientStock() throws Exception {
        createRequest(access, body(6)).andExpect(status().isConflict());
        jdbc.update("UPDATE events SET status = 'PENDING_APPROVAL' WHERE id = ?", event.getId());
        createRequest(access, body(1)).andExpect(status().isConflict());
        jdbc.update("UPDATE events SET status = 'APPROVED' WHERE id = ?", event.getId());
        jdbc.update("UPDATE ticket_types SET sale_end_time = ? WHERE id = ?", now().minusMinutes(1), type.getId());
        createRequest(access, body(1)).andExpect(status().isConflict());
        createRequest(access, body(1).replace("\"ticketTypeId\":" + type.getId(), "\"ticketTypeId\":2147483647"))
                .andExpect(status().isBadRequest());
        assertInventory(5, 0);
    }

    @Test
    void historyAndDetailArePaginatedAndRestrictedToOwner() throws Exception {
        int first = create(1).get("id").asInt();
        int second = create(1).get("id").asInt();
        var otherCookie = cookie(other);
        createRequest(otherCookie, body(1)).andExpect(status().isCreated());
        mvc.perform(get("/api/bookings").cookie(access).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items[0].id").value(second)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/bookings/{id}", first).cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.event.address").value(venue.getAddress()));
        mvc.perform(get("/api/bookings/{id}", first).cookie(otherCookie)).andExpect(status().isNotFound());
        mvc.perform(post("/api/bookings/{id}/payments/vnpay", first).cookie(otherCookie).header("X-CSRF-Protection", "1"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/bookings").cookie(access).param("size", "51")).andExpect(status().isBadRequest());
    }

    @Test
    void checkoutReusesPendingAttemptAndKeepsOriginalPrice() throws Exception {
        int id = create(2).get("id").asInt();
        jdbc.update("UPDATE ticket_types SET price = 999999 WHERE id = ?", type.getId());
        var payment = pay(id);
        var repeat = pay(id);
        assertThat(repeat.get("transactionId").asText()).isEqualTo(payment.get("transactionId").asText());
        assertThat(repeat.get("paymentUrl").asText()).isEqualTo(payment.get("paymentUrl").asText());
        var params = decodeUrl(payment.get("paymentUrl").asText());
        String suppliedHash = params.getFirst("vnp_SecureHash");
        assertThat(signed(params).getFirst("vnp_SecureHash")).isEqualTo(suppliedHash);
        assertThat(params.getFirst("vnp_Amount")).isEqualTo("30000100");
        assertThat(params.getFirst("vnp_CurrCode")).isEqualTo("VND");
        var expiry = LocalDateTime.parse(payment.get("expiredAt").asText());
        assertThat(params.getFirst("vnp_ExpireDate")).isEqualTo(formatVnpDate(expiry));
        assertThat(count("payments", "bookings_id", id)).isEqualTo(1);
        assertInventory(3, 2);
    }

    @Test
    void ipnConfirmsPaymentAndIssuesUniqueTicketsOnlyOnce() throws Exception {
        int id = create(2).get("id").asInt();
        var callback = callback(pay(id), "00");
        ipn(callback).andExpect(jsonPath("$.RspCode").value("00"));
        ipn(callback).andExpect(jsonPath("$.RspCode").value("02"));
        assertInventory(3, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(2))
                .andExpect(jsonPath("$.payments[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.payments[0].gatewayTransactionId").exists());
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT t.qr_token) FROM tickets t JOIN booking_items i ON t.booking_items_id = i.id JOIN bookings b ON b.id = i.bookings_id WHERE b.id = ?",
                Integer.class, id)).isEqualTo(2);
        mvc.perform(post("/api/bookings/{id}/payments/vnpay", id).cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void browserReturnDoesNotConfirmPaymentAndIgnoresExpiredCookie() throws Exception {
        int id = create(1).get("id").asInt();
        var callback = callback(pay(id), "00");
        mvc.perform(get("/api/payments/vnpay/return").params(callback)
                        .cookie(new Cookie(AuthCookies.ACCESS, "expired-invalid-cookie")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.bookingStatus").value("PENDING"));
        assertInventory(4, 1);
        ipn(callback).andExpect(jsonPath("$.RspCode").value("00"));
    }

    @Test
    void rejectsTamperingWrongAmountUnknownReferenceAndDuplicateParameters() throws Exception {
        int id = create(1).get("id").asInt();
        var payment = pay(id);
        var tampered = callback(payment, "00");
        tampered.set("vnp_Amount", "1");
        ipn(tampered).andExpect(jsonPath("$.RspCode").value("97"));
        var wrongAmount = callback(payment, "00");
        wrongAmount.set("vnp_Amount", "1");
        ipn(signed(wrongAmount)).andExpect(jsonPath("$.RspCode").value("04"));
        var unknown = callback(payment, "00");
        unknown.set("vnp_TxnRef", UUID.randomUUID().toString());
        ipn(signed(unknown)).andExpect(jsonPath("$.RspCode").value("01"));
        var duplicate = callback(payment, "00");
        duplicate.add("vnp_Amount", duplicate.getFirst("vnp_Amount"));
        ipn(duplicate).andExpect(jsonPath("$.RspCode").value("97"));
        var wrongMerchant = callback(payment, "00");
        wrongMerchant.set("vnp_TmnCode", "WRONGTMC");
        ipn(signed(wrongMerchant)).andExpect(jsonPath("$.RspCode").value("97"));
        assertInventory(4, 1);
    }

    @Test
    void failedPaymentCanBeRetriedBeforeHoldExpires() throws Exception {
        int id = create(1).get("id").asInt();
        var first = pay(id);
        ipn(callback(first, "24")).andExpect(jsonPath("$.RspCode").value("00"));
        assertInventory(4, 1);
        var retry = pay(id);
        assertThat(retry.get("transactionId").asText()).isNotEqualTo(first.get("transactionId").asText());
        ipn(callback(retry, "00")).andExpect(jsonPath("$.RspCode").value("00"));
        assertInventory(4, 0);
    }

    @Test
    void requiresBothSuccessCodesAndValidGatewayDate() throws Exception {
        int id = create(1).get("id").asInt();
        var payment = pay(id);
        var invalidDate = callback(payment, "00");
        invalidDate.set("vnp_PayDate", "20260230120000");
        ipn(signed(invalidDate)).andExpect(jsonPath("$.RspCode").value("99"));
        var mismatched = callback(payment, "00");
        mismatched.set("vnp_TransactionStatus", "02");
        ipn(signed(mismatched)).andExpect(jsonPath("$.RspCode").value("00"));
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.payments[0].status").value("FAILED"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(0));
        assertInventory(4, 1);
    }

    @Test
    void rejectsUnsupportedGatewayAmountsWithoutPersistingAttempt() throws Exception {
        jdbc.update("UPDATE ticket_types SET price = 10000000000 WHERE id = ?", type.getId());
        int id = create(1).get("id").asInt();
        mvc.perform(post("/api/bookings/{id}/payments/vnpay", id).cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isBadRequest());
        assertThat(count("payments", "bookings_id", id)).isZero();
        assertInventory(4, 1);
    }

    @Test
    void expiryReleasesSeatsExactlyOnceAndRejectsCheckout() throws Exception {
        int id = create(2).get("id").asInt();
        pay(id);
        expire(id);
        service.expireBookings(event.getId());
        assertInventory(5, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.payments[0].status").value("EXPIRED"));
        mvc.perform(post("/api/bookings/{id}/payments/vnpay", id).cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void lateSuccessIsRecordedForReviewWithoutIssuingReleasedSeats() throws Exception {
        int id = create(2).get("id").asInt();
        var callback = callback(pay(id), "00");
        expire(id);
        create(5);
        ipn(callback).andExpect(jsonPath("$.RspCode").value("00"));
        ipn(callback).andExpect(jsonPath("$.RspCode").value("02"));
        assertInventory(0, 5);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("PAYMENT_REVIEW"))
                .andExpect(jsonPath("$.payments[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(0));
    }

    @Test
    void pendingPaymentKeepsSeatsDuringGraceAndPreventsNewPayment() throws Exception {
        int id = create(2).get("id").asInt();
        var payment = pay(id);
        var deadline = freezeAfterDeadline(id, 30);
        var current = LocalDateTime.now(clock);
        assertThat(bookings.findExpiredEventIds(current, current.minusSeconds(120), PageRequest.of(0, 100)))
                .doesNotContain(event.getId());
        service.expireBookings(event.getId());
        assertInventory(3, 2);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("PAYMENT_CONFIRMING"))
                .andExpect(result -> assertThat(LocalDateTime.parse(json.readTree(result.getResponse().getContentAsString())
                        .get("paymentConfirmationDeadline").asText())).isEqualTo(deadline.plusSeconds(120)))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(0));
        mvc.perform(get("/api/bookings").cookie(access)).andExpect(jsonPath("$.items[0].status").value("PAYMENT_CONFIRMING"));
        mvc.perform(get("/api/payments/vnpay/return").params(callback(payment, "00")))
                .andExpect(jsonPath("$.bookingStatus").value("PAYMENT_CONFIRMING"));
        mvc.perform(post("/api/bookings/{id}/payments/vnpay", id).cookie(access).header("X-CSRF-Protection", "1"))
                .andExpect(status().isConflict());
        createRequest(access, body(4)).andExpect(status().isConflict());
        create(3); // Creating another booking must not reclaim this pending payment's seats.
        assertInventory(0, 5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 119})
    void onTimePaymentWinsAgainstExpiryThroughoutGrace(int elapsedSeconds) throws Exception {
        int id = create(2).get("id").asInt();
        var data = callback(pay(id), "00");
        var deadline = freezeAfterDeadline(id, elapsedSeconds);
        data.set("vnp_PayDate", formatVnpDate(deadline));
        signed(data);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var expiry = executor.submit(() -> { start.await(); service.expireBookings(event.getId()); return true; });
            var payment = executor.submit(() -> { start.await(); return service.processIpn(data).get("RspCode"); });
            start.countDown();
            assertThat(payment.get(15, TimeUnit.SECONDS)).isEqualTo("00");
            assertThat(expiry.get(15, TimeUnit.SECONDS)).isTrue();
        }
        ipn(data).andExpect(jsonPath("$.RspCode").value("02"));
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(2));
        assertInventory(3, 0);
    }

    @Test
    void graceDeadlineIsEnforcedEvenBeforeExpiryJobRuns() throws Exception {
        int id = create(2).get("id").asInt();
        var data = callback(pay(id), "00");
        var deadline = freezeAfterDeadline(id, 120);
        data.set("vnp_PayDate", formatVnpDate(deadline));
        var current = LocalDateTime.now(clock);
        assertThat(bookings.findExpiredEventIds(current, current.minusSeconds(120), PageRequest.of(0, 100)))
                .contains(event.getId());
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("EXPIRED"));
        ipn(signed(data)).andExpect(jsonPath("$.RspCode").value("00"));
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("PAYMENT_REVIEW"))
                .andExpect(jsonPath("$.payments[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(0));
        service.expireBookings(event.getId());
        assertInventory(5, 0);
    }

    @Test
    void graceDoesNotAcceptPaymentMadeAfterOriginalDeadline() throws Exception {
        int id = create(2).get("id").asInt();
        var data = callback(pay(id), "00");
        var deadline = freezeAfterDeadline(id, 30);
        data.set("vnp_PayDate", formatVnpDate(deadline.plusSeconds(1)));
        ipn(signed(data)).andExpect(jsonPath("$.RspCode").value("00"));
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("PAYMENT_REVIEW"))
                .andExpect(jsonPath("$.items[0].tickets.length()").value(0));
        assertInventory(5, 0);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bookingsWithoutPendingPaymentExpireAtOriginalDeadline(boolean failedPayment) throws Exception {
        int id = create(2).get("id").asInt();
        if (failedPayment) ipn(callback(pay(id), "24")).andExpect(jsonPath("$.RspCode").value("00"));
        freezeAfterDeadline(id, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("EXPIRED"));
        create(5); // The new-booking cleanup path follows the same expiry policy as the scheduler.
        assertInventory(0, 5);
        assertThat(jdbc.queryForObject("SELECT status FROM bookings WHERE id = ?", String.class, id)).isEqualTo("EXPIRED");
    }

    @Test
    void failureDuringGraceReleasesSeatsImmediately() throws Exception {
        int id = create(2).get("id").asInt();
        var data = callback(pay(id), "24");
        freezeAfterDeadline(id, 30);
        ipn(data).andExpect(jsonPath("$.RspCode").value("00"));
        assertInventory(5, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    void expiryJobReleasesPendingPaymentAtExactGraceDeadlineOnlyOnce() throws Exception {
        int id = create(2).get("id").asInt();
        pay(id);
        freezeAfterDeadline(id, 120);
        service.expireBookings(event.getId());
        service.expireBookings(event.getId());
        assertInventory(5, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.payments[0].status").value("EXPIRED"));
    }

    @Test
    void successDuringEventCancellationRequiresReviewAndReleasesHold() throws Exception {
        int id = create(2).get("id").asInt();
        var callback = callback(pay(id), "00");
        jdbc.update("UPDATE events SET status = 'PENDING_CANCELLATION' WHERE id = ?", event.getId());
        ipn(callback).andExpect(jsonPath("$.RspCode").value("00"));
        assertInventory(5, 0);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("PAYMENT_REVIEW"));
    }

    @Test
    void freeTicketsAreIssuedWithoutCallingGateway() throws Exception {
        jdbc.update("UPDATE ticket_types SET price = 0 WHERE id = ?", type.getId());
        var booking = create(2);
        assertThat(booking.get("status").asText()).isEqualTo("PAID");
        assertThat(booking.at("/payments/0/paymentMethod").asText()).isEqualTo("FREE");
        assertThat(booking.at("/items/0/tickets").size()).isEqualTo(2);
        assertInventory(3, 0);
    }

    @Test
    void simultaneousBookingsCannotOversellLastSeats() throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var request = new CreateBookingRequest(event.getId(), List.of(new CreateBookingRequest.Item(type.getId(), 4)));
            var results = List.of(customer, other).stream().map(user -> executor.submit(() -> {
                start.await();
                try {
                    service.create(user.getId(), request);
                    return 201;
                } catch (ResponseStatusException exception) {
                    return exception.getStatusCode().value();
                }
            })).toList();
            start.countDown();
            assertThat(List.of(results.get(0).get(15, TimeUnit.SECONDS), results.get(1).get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
        assertInventory(1, 4);
    }

    @Test
    void simultaneousIpnsIssueTicketsOnce() throws Exception {
        var callback = callback(pay(create(2).get("id").asInt()), "00");
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { start.await(); return service.processIpn(callback).get("RspCode"); });
            var second = executor.submit(() -> { start.await(); return service.processIpn(callback).get("RspCode"); });
            start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("00", "02");
        }
        assertInventory(3, 0);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM tickets t JOIN booking_items i ON t.booking_items_id = i.id JOIN bookings b ON b.id = i.bookings_id WHERE b.events_id = ?",
                Integer.class, event.getId())).isEqualTo(2);
    }

    @Test
    void ticketPersistenceFailureRollsBackPaymentAndCanBeRetried() throws Exception {
        int id = create(2).get("id").asInt();
        var callback = callback(pay(id), "00");
        doThrow(new DataIntegrityViolationException("test ticket persistence failure")).when(tickets).saveAll(any());
        ipn(callback).andExpect(jsonPath("$.RspCode").value("99"));
        assertInventory(3, 2);
        mvc.perform(get("/api/bookings/{id}", id).cookie(access)).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.payments[0].status").value("PENDING"));
        reset(tickets);
        ipn(callback).andExpect(jsonPath("$.RspCode").value("00"));
        assertInventory(3, 0);
    }

    @Test
    void electronicTicketsArePaginatedAndOnlyVisibleToOwner() throws Exception {
        int id = paidBooking(2);
        mvc.perform(get("/api/tickets").cookie(access).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.items[0].qrToken").isString())
                .andExpect(jsonPath("$.items[0].canCheckIn").value(true))
                .andExpect(jsonPath("$.items[0].bookingId").value(id));
        mvc.perform(get("/api/tickets").cookie(cookie(other))).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/tickets").cookie(cookie(other)).param("bookingId", String.valueOf(id)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/tickets").cookie(access).param("status", "USED"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/tickets").cookie(access).param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tickets").cookie(access).param("size", "51")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tickets").cookie(access).param("status", "UNKNOWN")).andExpect(status().isBadRequest());
    }

    @Test
    void electronicTicketsRequireCustomerAuthentication() throws Exception {
        mvc.perform(get("/api/tickets")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tickets").cookie(cookie(organizer))).andExpect(status().isForbidden());
        mvc.perform(get("/api/tickets").cookie(cookie(user(Role.STAFF)))).andExpect(status().isForbidden());
    }

    @Test
    void usedTicketsHideQrInCustomerListAndBookingDetail() throws Exception {
        int id = paidBooking(1);
        String token = qrToken(id);
        // Existing ticket usage is represented as fixture data; this scope has no check-in endpoint.
        jdbc.update("UPDATE tickets SET status = 'USED', checked_in_at = ? WHERE qr_token = ?", now(), token);
        mvc.perform(get("/api/tickets").cookie(access).param("status", "USED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].status").value("USED"))
                .andExpect(jsonPath("$.items[0].qrToken").isEmpty()).andExpect(jsonPath("$.items[0].canCheckIn").value(false));
        mvc.perform(get("/api/tickets").cookie(access).param("status", "VALID"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/bookings/{id}", id).cookie(access))
                .andExpect(jsonPath("$.items[0].tickets[0].qrToken").isEmpty());
    }

    @Test
    void unavailableEventsAndUnconfirmedBookingsHideCustomerQr() throws Exception {
        int id = paidBooking(1);
        for (String eventStatus : List.of("CANCELED", "PENDING_CANCELLATION", "COMPLETED", "REJECTED")) {
            jdbc.update("UPDATE events SET status = ? WHERE id = ?", eventStatus, event.getId());
            mvc.perform(get("/api/tickets").cookie(access)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].qrToken").isEmpty())
                    .andExpect(jsonPath("$.items[0].canCheckIn").value(false));
        }
        jdbc.update("UPDATE events SET status = 'APPROVED', start_time = ?, end_time = ? WHERE id = ?", now().minusHours(2), now().minusSeconds(1), event.getId());
        mvc.perform(get("/api/tickets").cookie(access)).andExpect(jsonPath("$.items[0].qrToken").isEmpty());
        jdbc.update("UPDATE events SET end_time = ? WHERE id = ?", now().plusDays(2), event.getId());
        jdbc.update("UPDATE bookings SET status = 'PAYMENT_REVIEW' WHERE id = ?", id);
        mvc.perform(get("/api/tickets").cookie(access)).andExpect(jsonPath("$.items[0].qrToken").isEmpty());
    }

    private int paidBooking(int quantity) throws Exception {
        int id = create(quantity).get("id").asInt();
        ipn(callback(pay(id), "00")).andExpect(jsonPath("$.RspCode").value("00"));
        return id;
    }

    private String qrToken(int id) {
        return jdbc.queryForObject("SELECT t.qr_token FROM tickets t JOIN booking_items i ON t.booking_items_id = i.id WHERE i.bookings_id = ? ORDER BY t.id LIMIT 1", String.class, id);
    }

    private User user(Role role) {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.invalid");
        user.setPassword("test-only-invalid-hash");
        user.setRole(role);
        user.setStatus("ACTIVE");
        users.saveAndFlush(user);
        testUsers.add(user);
        return user;
    }

    private Cookie cookie(User user) {
        RefreshToken session = new RefreshToken();
        session.setUser(user);
        session.setTokenHash(jwt.hashRefreshToken(jwt.createRefreshToken()));
        session.setExpiresAt(now().plusDays(1));
        sessions.saveAndFlush(session);
        return new Cookie(AuthCookies.ACCESS, jwt.createAccessToken(user.getId(), session.getId(), user.getRole()));
    }

    private TicketType ticketType(BigDecimal price, int quantity) {
        TicketType type = new TicketType();
        type.setEvent(event);
        type.setName("Standard");
        type.setImageUrl("https://example.invalid/ticket.png");
        type.setPrice(price);
        type.setQuantity(quantity);
        type.setRemainingQuantity(quantity);
        type.setReservedQuantity(0);
        type.setSaleStartTime(now().minusHours(1));
        type.setSaleEndTime(now().plusHours(2));
        type.setStatus("ACTIVE");
        return types.saveAndFlush(type);
    }

    private String body(int quantity) {
        return "{\"eventId\":" + event.getId() + ",\"items\":[{\"ticketTypeId\":" + type.getId()
                + ",\"quantity\":" + quantity + "}],\"totalAmount\":1,\"userId\":" + other.getId() + ",\"status\":\"PAID\"}";
    }

    private ResultActions createRequest(Cookie cookie, String body) throws Exception {
        return mvc.perform(post("/api/bookings").cookie(cookie).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private JsonNode create(int quantity) throws Exception {
        return json.readTree(createRequest(access, body(quantity)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode pay(int id) throws Exception {
        return json.readTree(mvc.perform(post("/api/bookings/{id}/payments/vnpay", id)
                        .cookie(access).header("X-CSRF-Protection", "1")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private MultiValueMap<String, String> callback(JsonNode payment, String response) throws Exception {
        var data = new LinkedMultiValueMap<String, String>();
        data.set("vnp_TmnCode", "TESTCODE");
        data.set("vnp_TxnRef", payment.get("transactionId").asText());
        data.set("vnp_Amount", decodeUrl(payment.get("paymentUrl").asText()).getFirst("vnp_Amount"));
        data.set("vnp_ResponseCode", response);
        data.set("vnp_TransactionStatus", "00".equals(response) ? "00" : "02");
        data.set("vnp_TransactionNo", "1" + String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits() % 100000000000000L)));
        data.set("vnp_PayDate", formatVnpDate(now()));
        data.set("vnp_OrderInfo", "Thanh toan booking " + payment.get("bookingId").asInt());
        return signed(data);
    }

    private MultiValueMap<String, String> signed(MultiValueMap<String, String> input) throws Exception {
        input.remove("vnp_SecureHash");
        var sorted = new TreeMap<>(input.toSingleValueMap());
        String text = sorted.entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec("booking-test-only-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        input.set("vnp_SecureHash", HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8))));
        return input;
    }

    private MultiValueMap<String, String> decodeUrl(String url) {
        var values = new LinkedMultiValueMap<String, String>();
        for (String pair : url.substring(url.indexOf('?') + 1).split("&")) {
            String[] parts = pair.split("=", 2);
            values.add(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
        }
        return values;
    }

    private ResultActions ipn(MultiValueMap<String, String> data) throws Exception {
        return mvc.perform(get("/api/payments/vnpay/ipn").params(data)).andExpect(status().isOk());
    }

    private void expire(int id) {
        jdbc.update("UPDATE bookings SET expired_at = ? WHERE id = ?", now().minusSeconds(121), id);
        service.expireBookings(event.getId());
    }

    private LocalDateTime freezeAfterDeadline(int id, int elapsedSeconds) {
        var current = now().withNano(0);
        var deadline = current.minusSeconds(elapsedSeconds);
        // Shift the fixture's timeline without sleeping or expiring the authenticated test session.
        jdbc.update("UPDATE bookings SET expired_at = ? WHERE id = ?", deadline, id);
        jdbc.update("UPDATE payments SET created_at = ? WHERE bookings_id = ?", deadline.minusSeconds(60), id);
        doReturn(current.toInstant(ZoneOffset.UTC)).when(clock).instant();
        return deadline;
    }

    private void assertInventory(int remaining, int reserved) {
        var inventory = jdbc.queryForMap("SELECT remaining_quantity, reserved_quantity FROM ticket_types WHERE id = ?", type.getId());
        assertThat(inventory.get("remaining_quantity")).isEqualTo(remaining);
        assertThat(inventory.get("reserved_quantity")).isEqualTo(reserved);
    }

    private int count(String table, String column, int id) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, id);
    }

    private LocalDateTime now() { return LocalDateTime.now(ZoneOffset.UTC); }
    private String formatVnpDate(LocalDateTime utc) { return utc.plusHours(7).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); }
}
