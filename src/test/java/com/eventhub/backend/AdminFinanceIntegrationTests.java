package com.eventhub.backend;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.*;
import com.eventhub.backend.security.*;
import com.eventhub.backend.service.AdminWithdrawalService;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties")
class AdminFinanceIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired RefreshTokenRepository tokens;
    @Autowired JwtService jwt;
    @Autowired AdminWithdrawalService withdrawals;
    @MockitoSpyBean NotificationRepository notifications;
    User admin, organizer;
    Cookie access, organizerAccess;
    int walletId, requestId;

    @BeforeEach void prepare() {
        admin = user(Role.ADMIN); organizer = user(Role.ORGANIZER);
        access = session(admin); organizerAccess = session(organizer);
        walletId = jdbc.queryForObject("""
                INSERT INTO organizer_wallets(organizer_id,pending_balance,available_balance,withdrawal_pending_balance,total_withdrawn)
                VALUES (?,10,100,300,20) RETURNING id
                """, Integer.class, organizer.getId());
        requestId = request(walletId, 100);
    }
    @AfterEach void clean() {
        jdbc.update("DELETE FROM payout_transactions WHERE withdrawal_requests_id IN (SELECT id FROM withdrawal_requests WHERE organizer_wallets_id=?)", walletId);
        jdbc.update("DELETE FROM withdrawal_requests WHERE organizer_wallets_id=?", walletId);
        jdbc.update("DELETE FROM organizer_wallets WHERE id=?", walletId);
        jdbc.update("DELETE FROM notifications WHERE users_id IN (?,?)", admin.getId(), organizer.getId());
        jdbc.update("DELETE FROM refresh_tokens WHERE users_id IN (?,?)", admin.getId(), organizer.getId());
        jdbc.update("DELETE FROM users WHERE id IN (?,?)", admin.getId(), organizer.getId());
    }
    int request(int wallet, int amount) {
        return jdbc.queryForObject("""
                INSERT INTO withdrawal_requests(organizer_wallets_id,amount,bank_name,account_number,status,requested_at)
                VALUES (?,?,'Test Bank','0123456789','PENDING',CURRENT_TIMESTAMP AT TIME ZONE 'UTC') RETURNING id
                """, Integer.class, wallet, amount);
    }
    User user(Role role) {
        User u = new User(); u.setEmail(UUID.randomUUID() + "@example.invalid"); u.setFullName("Finance test");
        u.setPassword("test-only-invalid-hash"); u.setRole(role); u.setStatus("ACTIVE"); return users.saveAndFlush(u);
    }
    Cookie session(User u) {
        RefreshToken t = new RefreshToken(); t.setUser(u); t.setTokenHash(jwt.hashRefreshToken(jwt.createRefreshToken()));
        t.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1)); tokens.saveAndFlush(t);
        return new Cookie(AuthCookies.ACCESS, jwt.createAccessToken(u.getId(), t.getId(), u.getRole()));
    }
    String path(String action) { return "/api/admin/withdrawal-requests/" + requestId + "/" + action; }
    BigDecimal balance(String column) { return jdbc.queryForObject("SELECT " + column + " FROM organizer_wallets WHERE id=?", BigDecimal.class, walletId); }

    @Test void protectsFinanceEndpointsAndCsrf() throws Exception {
        for (Role role : List.of(Role.CUSTOMER, Role.STAFF, Role.ORGANIZER)) {
            User forbidden = role == Role.ORGANIZER ? organizer : user(role);
            try {
                Cookie cookie = role == Role.ORGANIZER ? organizerAccess : session(forbidden);
                var calls = List.of(get("/api/admin/statistics"), get("/api/admin/withdrawal-requests"),
                        get("/api/admin/withdrawal-requests/" + requestId), get("/api/admin/refund-events"),
                        get("/api/admin/refund-events/1"), post(path("approve")).content("{\"transactionCode\":\"TEST\"}"),
                        post(path("reject")).content("{\"reason\":\"Test\"}"));
                for (var call : calls) {
                    mvc.perform(call.contentType(MediaType.APPLICATION_JSON).header("X-CSRF-Protection", "1"))
                            .andExpect(status().isUnauthorized());
                    mvc.perform(call.cookie(cookie)).andExpect(status().isForbidden());
                }
            } finally {
                if (role != Role.ORGANIZER) {
                    jdbc.update("DELETE FROM refresh_tokens WHERE users_id=?", forbidden.getId());
                    users.deleteById(forbidden.getId());
                }
            }
        }
        mvc.perform(post(path("approve")).cookie(access).contentType(MediaType.APPLICATION_JSON)
                .content("{\"transactionCode\":\"TEST\"}")).andExpect(status().isForbidden());
    }
    @Test void listsFiltersPaginatesAndRestrictsBankDetailsToDetail() throws Exception {
        mvc.perform(get("/api/admin/withdrawal-requests").cookie(access).param("status", "PENDING"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].accountNumber").isEmpty());
        mvc.perform(get("/api/admin/withdrawal-requests/" + requestId).cookie(access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accountNumber").value("0123456789"))
                .andExpect(jsonPath("$.organizer.id").value(organizer.getId()));
        mvc.perform(get("/api/admin/withdrawal-requests").cookie(access).param("status", "REJECTED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get("/api/admin/withdrawal-requests").cookie(access).param("pageSize", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/withdrawal-requests").cookie(access).param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
    @Test void approvesRecordedTransferExactlyOnce() throws Exception {
        mvc.perform(post(path("approve")).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"transactionCode\":\" TEST-" + requestId + " \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.processedBy").value(admin.getId())).andExpect(jsonPath("$.payouts[0].status").value("COMPLETED"));
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("200");
        assertThat(balance("total_withdrawn")).isEqualByComparingTo("120");
        assertThat(balance("available_balance")).isEqualByComparingTo("100");
        mvc.perform(post(path("approve")).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"transactionCode\":\"TEST\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post(path("reject")).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"No\"}"))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM payout_transactions WHERE withdrawal_requests_id=?", Integer.class, requestId)).isEqualTo(1);
    }
    @Test void rejectsAndReleasesOnlyHeldBalance() throws Exception {
        mvc.perform(post(path("reject")).cookie(access).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\" Sai thông tin ngân hàng \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rejectReason").value("Sai thông tin ngân hàng"));
        assertThat(balance("available_balance")).isEqualByComparingTo("200");
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("200");
        assertThat(balance("total_withdrawn")).isEqualByComparingTo("20");
        assertThatThrownBy(() -> withdrawals.decide(admin.getId(), requestId, null, "Again"))
                .isInstanceOf(com.eventhub.backend.exception.AdminException.class);
        assertThat(balance("available_balance")).isEqualByComparingTo("200");
    }
    @Test void validatesInputsMissingIdsAndInsufficientReservation() throws Exception {
        for (String action : List.of("approve", "reject")) {
            String key = action.equals("approve") ? "transactionCode" : "reason";
            for (String value : List.of("", "   ", "x".repeat(256)))
                mvc.perform(post(path(action)).cookie(access).header("X-CSRF-Protection", "1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"" + key + "\":\"" + value + "\"}"))
                        .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/admin/withdrawal-requests/2147483647").cookie(access)).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/withdrawal-requests/0").cookie(access)).andExpect(status().isBadRequest());
        jdbc.update("UPDATE organizer_wallets SET withdrawal_pending_balance=50 WHERE id=?", walletId);
        assertThatThrownBy(() -> withdrawals.decide(admin.getId(), requestId, "TEST", null))
                .isInstanceOf(com.eventhub.backend.exception.AdminException.class);
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("50");
        assertThat(balance("total_withdrawn")).isEqualByComparingTo("20");
    }
    @Test void notificationFailureRollsBackRequestWalletAndPayout() {
        doThrow(new IllegalStateException("Test notification failure")).when(notifications).saveAndFlush(any(Notification.class));
        assertThatThrownBy(() -> withdrawals.decide(admin.getId(), requestId, "ROLLBACK-" + requestId, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("300");
        assertThat(jdbc.queryForObject("SELECT status FROM withdrawal_requests WHERE id=?", String.class, requestId)).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM payout_transactions WHERE withdrawal_requests_id=?", Integer.class, requestId)).isZero();
    }
    @Test void simultaneousDecisionsOnSameRequestHaveOnlyOneWinner() throws Exception {
        race(requestId, requestId);
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("200");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE users_id=?", Integer.class, organizer.getId())).isEqualTo(1);
    }
    @Test void simultaneousRequestsShareWalletLockAndCannotOverspend() throws Exception {
        int second = request(walletId, 250);
        race(requestId, second);
        assertThat(balance("withdrawal_pending_balance").signum()).isGreaterThanOrEqualTo(0);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM withdrawal_requests WHERE organizer_wallets_id=? AND status='APPROVED'", Integer.class, walletId)).isEqualTo(1);
    }
    void race(int first, int second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            var futures = List.of(first, second).stream().map(id -> pool.submit(() -> {
                ready.countDown(); start.await(10, TimeUnit.SECONDS);
                try { withdrawals.decide(admin.getId(), id, "RACE-" + id, null); return true; }
                catch (com.eventhub.backend.exception.AdminException expected) { return false; }
            })).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(futures.get(0).get(20, TimeUnit.SECONDS), futures.get(1).get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally { start.countDown(); pool.shutdownNow(); }
    }
    @Test void preventsReuseOfTransferReferenceAndRequiresPayoutReconciliation() {
        withdrawals.decide(admin.getId(), requestId, "UNIQUE-" + requestId, null);
        int next = request(walletId, 100);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO payout_transactions(withdrawal_requests_id,amount,transaction_code,status,initiated_at)
                VALUES (?,100,?,'COMPLETED',CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
                """, next, "UNIQUE-" + requestId))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> withdrawals.decide(admin.getId(), next, "UNIQUE-" + requestId, null))
                .isInstanceOf(com.eventhub.backend.exception.AdminException.class);
        jdbc.update("""
                INSERT INTO payout_transactions(withdrawal_requests_id,amount,status,initiated_at)
                VALUES (?,100,'PROCESSING',CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
                """, next);
        assertThatThrownBy(() -> withdrawals.decide(admin.getId(), next, null, "No"))
                .isInstanceOf(com.eventhub.backend.exception.AdminException.class);
        assertThat(balance("withdrawal_pending_balance")).isEqualByComparingTo("200");
    }
    @Test void overviewAggregatesDatabaseBalancesAndDoesNotInventRevenue() throws Exception {
        BigDecimal available = jdbc.queryForObject("SELECT sum(available_balance) FROM organizer_wallets", BigDecimal.class);
        mvc.perform(get("/api/admin/statistics").cookie(access)).andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(available.doubleValue()))
                .andExpect(jsonPath("$.grossRevenue").isEmpty()).andExpect(jsonPath("$.netRevenue").isEmpty());
        mvc.perform(get("/api/admin/refund-events/2147483647").cookie(access)).andExpect(status().isNotFound());
    }

    @Test void readsCanceledEventPaymentAndPartialRefundWithoutChangingFinancialData() throws Exception {
        int category = jdbc.queryForObject("INSERT INTO categories(name) VALUES (?) RETURNING id", Integer.class, "Finance " + UUID.randomUUID());
        int venue = jdbc.queryForObject("INSERT INTO venues(city,address,capacity) VALUES ('Test','Test',100) RETURNING id", Integer.class);
        int event = jdbc.queryForObject("""
                INSERT INTO events(organizer_id,venues_id,categories_id,name,thumbnail_image_url,banner_image_url,start_time,end_time,status)
                VALUES (?,?,?,'Canceled test','https://example.invalid/a','https://example.invalid/b',
                CURRENT_TIMESTAMP + interval '10 days',CURRENT_TIMESTAMP + interval '11 days','PENDING_CANCELLATION') RETURNING id
                """, Integer.class, organizer.getId(), venue, category);
        try {
            mvc.perform(get("/api/admin/refund-events/" + event).cookie(access)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EVENT_NOT_CANCELED"));
            jdbc.update("UPDATE events SET status='CANCELED' WHERE id=?", event);
            int booking = jdbc.queryForObject("""
                    INSERT INTO bookings(users_id,events_id,total_amount,status,expired_at)
                    VALUES (?,?,100,'TEST',CURRENT_TIMESTAMP) RETURNING id
                    """, Integer.class, organizer.getId(), event);
            int payment = jdbc.queryForObject("""
                    INSERT INTO payments(bookings_id,transaction_id,amount,status,paid_at)
                    VALUES (?,?,100,'PROVIDER_TEST_STATUS',CURRENT_TIMESTAMP) RETURNING id
                    """, Integer.class, booking, UUID.randomUUID().toString());
            jdbc.update("""
                    INSERT INTO refunds(payments_id,refund_transaction_id,amount,status,retry_count)
                    VALUES (?,?,50,'PROVIDER_TEST_PARTIAL',2)
                    """, payment, UUID.randomUUID().toString());
            mvc.perform(get("/api/admin/refund-events").cookie(access)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].id").value(event));
            for (int i = 0; i < 2; i++)
                mvc.perform(get("/api/admin/refund-events/" + event).cookie(access)).andExpect(status().isOk())
                        .andExpect(jsonPath("$.canRefund").value(false)).andExpect(jsonPath("$.payments.items[0].paidAmount").value(100))
                        .andExpect(jsonPath("$.payments.items[0].refundAmount").value(50))
                        .andExpect(jsonPath("$.payments.items[0].refundStatus").value("PROVIDER_TEST_PARTIAL"));
            assertThat(jdbc.queryForObject("SELECT count(*) FROM refunds WHERE payments_id=?", Integer.class, payment)).isEqualTo(1);
            assertThat(balance("pending_balance")).isEqualByComparingTo("10");
            jdbc.update("DELETE FROM refunds WHERE payments_id=?", payment);
            jdbc.update("DELETE FROM payments WHERE id=?", payment);
            jdbc.update("DELETE FROM bookings WHERE id=?", booking);
        } finally {
            jdbc.update("DELETE FROM refunds WHERE payments_id IN (SELECT p.id FROM payments p JOIN bookings b ON p.bookings_id=b.id WHERE b.events_id=?)", event);
            jdbc.update("DELETE FROM payments WHERE bookings_id IN (SELECT id FROM bookings WHERE events_id=?)", event);
            jdbc.update("DELETE FROM bookings WHERE events_id=?", event);
            jdbc.update("DELETE FROM events WHERE id=?", event);
            jdbc.update("DELETE FROM venues WHERE id=?", venue);
            jdbc.update("DELETE FROM categories WHERE id=?", category);
        }
    }
}
