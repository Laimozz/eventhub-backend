package com.eventhub.backend;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class DatabaseIntegrationTests {

    private final EntityManager entityManager;
    private final JdbcTemplate jdbc;
    private final Flyway flyway;
    private final UserRepository users;
    private final BookingRepository bookings;

    DatabaseIntegrationTests(EntityManager entityManager, JdbcTemplate jdbc, Flyway flyway,
            UserRepository users, BookingRepository bookings) {
        this.entityManager = entityManager;
        this.jdbc = jdbc;
        this.flyway = flyway;
        this.users = users;
        this.bookings = bookings;
    }

    @Test
    void shouldMigrateAllTablesAndSkipAlreadyAppliedMigration() {
        assertThat(jdbc.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = current_schema() AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """, String.class)).containsExactlyInAnyOrder(
                "users", "venues", "categories", "events", "ticket_types", "event_staffs",
                "event_guests", "user_event_interactions", "bookings", "booking_items",
                "tickets", "payments", "refunds", "notifications", "ai_conversations",
                "ai_messages", "organizer_wallets", "withdrawal_requests", "payout_transactions", "refresh_tokens");
        flyway.validate();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
    }

    @Test
    @Transactional
    void shouldPersistAndReloadAllEntitiesWithRelationshipsAndExactMoney() {
        Fixture fixture = createFixture();
        EventStaff staff = new EventStaff();
        staff.setEvent(fixture.event());
        staff.setStaff(fixture.user());
        staff.setAssignedBy(fixture.user());
        staff.setStatus("TEST");
        persist(staff);

        EventGuest guest = new EventGuest();
        guest.setEvent(fixture.event());
        guest.setName("Test guest");
        persist(guest);

        UserEventInteraction interaction = new UserEventInteraction();
        interaction.setUser(fixture.user());
        interaction.setEvent(fixture.event());
        interaction.setInteractionType("TEST");
        persist(interaction);

        Ticket ticket = new Ticket();
        ticket.setBookingItem(fixture.item());
        ticket.setTicketCode(UUID.randomUUID().toString());
        ticket.setQrToken(UUID.randomUUID().toString());
        ticket.setStatus("TEST");
        persist(ticket);

        Payment payment = createPayment(fixture.booking());
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundTransactionId(UUID.randomUUID().toString());
        refund.setAmount(new BigDecimal("0.30"));
        refund.setStatus("TEST");
        persist(refund);

        Notification notification = new Notification();
        notification.setUser(fixture.user());
        notification.setTitle("Test");
        persist(notification);

        AiConversation conversation = new AiConversation();
        conversation.setUser(fixture.user());
        persist(conversation);
        AiMessage message = new AiMessage();
        message.setConversation(conversation);
        message.setRole("TEST");
        message.setContent("Test message");
        persist(message);

        OrganizerWallet wallet = new OrganizerWallet();
        wallet.setOrganizer(fixture.user());
        wallet.setAvailableBalance(new BigDecimal("1000.30"));
        persist(wallet);

        WithdrawalRequest withdrawal = new WithdrawalRequest();
        withdrawal.setOrganizerWallet(wallet);
        withdrawal.setAmount(new BigDecimal("100.10"));
        withdrawal.setBankName("Test bank");
        withdrawal.setAccountNumber("TEST-ACCOUNT");
        withdrawal.setStatus("TEST");
        withdrawal.setRequestedAt(LocalDateTime.of(2030, 1, 1, 0, 0));
        persist(withdrawal);

        PayoutTransaction payout = new PayoutTransaction();
        payout.setWithdrawalRequest(withdrawal);
        payout.setAmount(new BigDecimal("100.10"));
        payout.setStatus("TEST");
        payout.setInitiatedAt(LocalDateTime.of(2030, 1, 1, 0, 0));
        persist(payout);

        entityManager.flush();
        entityManager.clear();

        Booking loadedBooking = bookings.findById(fixture.booking().getId()).orElseThrow();
        assertThat(loadedBooking.getTotalAmount()).isEqualByComparingTo("0.30");
        assertThat(loadedBooking.getUser().getEmail()).isEqualTo(fixture.user().getEmail());
        assertThat(loadedBooking.getEvent().getVenue().getCity()).isEqualTo("Test city");
        assertThat(loadedBooking.getEvent().getCategory().getName()).isEqualTo("Test category");
        assertThat(loadedBooking.getEvent().getOrganizer().getId()).isEqualTo(fixture.user().getId());
        assertThat(loadedBooking.getEvent().getReviewedBy()).isNull();
        Ticket loadedTicket = entityManager.find(Ticket.class, ticket.getId());
        assertThat(loadedTicket.getBookingItem().getTicketType().getPrice()).isEqualByComparingTo("0.10");
        assertThat(loadedTicket.getCheckedInAt()).isNull();
        assertThat(loadedTicket.getCheckedInBy()).isNull();
        assertThat(entityManager.find(EventStaff.class, staff.getId()).getAssignedBy().getId())
                .isEqualTo(fixture.user().getId());
        assertThat(entityManager.find(EventGuest.class, guest.getId()).getEvent().getId())
                .isEqualTo(fixture.event().getId());
        assertThat(entityManager.find(UserEventInteraction.class, interaction.getId()).getUser().getId())
                .isEqualTo(fixture.user().getId());
        Refund loadedRefund = entityManager.find(Refund.class, refund.getId());
        assertThat(loadedRefund.getPayment().getBooking().getId()).isEqualTo(loadedBooking.getId());
        assertThat(loadedRefund.getRefundedAt()).isNull();
        assertThat(loadedRefund.getRetryCount()).isZero();
        assertThat(loadedRefund.getPayment().getPaidAt()).isNull();
        assertThat(entityManager.find(Notification.class, notification.getId()).getRead()).isFalse();
        assertThat(entityManager.find(AiMessage.class, message.getId()).getConversation().getUser().getId())
                .isEqualTo(fixture.user().getId());
        PayoutTransaction loadedPayout = entityManager.find(PayoutTransaction.class, payout.getId());
        assertThat(loadedPayout.getWithdrawalRequest().getOrganizerWallet().getAvailableBalance())
                .isEqualByComparingTo("1000.30");
        assertThat(loadedPayout.getWithdrawalRequest().getProcessedAt()).isNull();
        assertThat(loadedPayout.getWithdrawalRequest().getProcessedBy()).isNull();
        assertThat(loadedPayout.getCompletedAt()).isNull();
        assertThat(loadedPayout.getCreatedAt()).isNotNull();
        assertThat(loadedPayout.getUpdatedAt()).isNotNull();

        // Each independent entity must maintain its own audit callbacks after inheritance is removed.
        Map<String, Integer> auditedRows = Map.of(
                "users", fixture.user().getId(), "bookings", fixture.booking().getId(),
                "events", fixture.event().getId(), "event_guests", guest.getId(),
                "refunds", refund.getId(), "ai_conversations", conversation.getId(),
                "organizer_wallets", wallet.getId(), "withdrawal_requests", withdrawal.getId(),
                "payout_transactions", payout.getId());
        LocalDateTime previous = LocalDateTime.of(2000, 1, 1, 0, 0);
        auditedRows.forEach((table, id) -> jdbc.update(
                "UPDATE " + table + " SET created_at = ?, updated_at = ? WHERE id = ?", previous, previous, id));
        entityManager.clear();
        entityManager.find(User.class, fixture.user().getId()).setFullName("Updated user");
        entityManager.find(Booking.class, fixture.booking().getId()).setStatus("UPDATED");
        entityManager.find(Event.class, fixture.event().getId()).setName("Updated event");
        entityManager.find(EventGuest.class, guest.getId()).setName("Updated guest");
        entityManager.find(Refund.class, refund.getId()).setStatus("UPDATED");
        entityManager.find(AiConversation.class, conversation.getId()).setTitle("Updated conversation");
        entityManager.find(OrganizerWallet.class, wallet.getId()).setAvailableBalance(new BigDecimal("2000.30"));
        entityManager.find(WithdrawalRequest.class, withdrawal.getId()).setStatus("UPDATED");
        entityManager.find(PayoutTransaction.class, payout.getId()).setStatus("UPDATED");
        entityManager.flush();
        auditedRows.forEach((table, id) -> {
            assertThat(jdbc.queryForObject("SELECT created_at FROM " + table + " WHERE id = ?", LocalDateTime.class, id))
                    .as(table + " created_at").isEqualTo(previous);
            assertThat(jdbc.queryForObject("SELECT updated_at FROM " + table + " WHERE id = ?", LocalDateTime.class, id))
                    .as(table + " updated_at").isAfter(previous);
        });
    }

    @Test
    @Transactional
    void shouldMaintainAuditTimestampsOnInsertAndUpdate() {
        User user = createUser();
        entityManager.flush();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime updatedAt = user.getUpdatedAt();
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        user.setFullName("Updated name");
        entityManager.flush();
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
        entityManager.clear();
        assertThat(users.findById(user.getId()).orElseThrow().getFullName()).isEqualTo("Updated name");
    }

    @Test
    @Transactional
    void shouldAllowMultipleTicketTypesAndBookingsWithUniquePairs() {
        Fixture fixture = createFixture();
        TicketType secondType = createTicketType(fixture.event());
        Booking secondBooking = createBooking(fixture.user(), fixture.event());
        createItem(fixture.booking(), secondType);
        createItem(secondBooking, fixture.type());
        entityManager.flush();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM booking_items WHERE bookings_id = ?",
                Integer.class, fixture.booking().getId())).isEqualTo(2);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO booking_items (bookings_id, ticket_types_id, quantity, unit_price)
                VALUES (?, ?, 1, 0.10)
                """, fixture.booking().getId(), fixture.type().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldAllowSeveralStaffPerEventButRejectDuplicateAssignment() {
        Fixture fixture = createFixture();
        User secondStaff = createUser();
        jdbc.update("""
                INSERT INTO event_staffs (events_id, staff_id, assigned_by, status)
                VALUES (?, ?, ?, 'TEST'), (?, ?, ?, 'TEST')
                """, fixture.event().getId(), fixture.user().getId(), fixture.user().getId(),
                fixture.event().getId(), secondStaff.getId(), fixture.user().getId());
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO event_staffs (events_id, staff_id, assigned_by, status)
                VALUES (?, ?, ?, 'TEST')
                """, fixture.event().getId(), fixture.user().getId(), fixture.user().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE ticket_types SET remaining_quantity = -1 WHERE id = ?",
            "UPDATE ticket_types SET reserved_quantity = 1 WHERE id = ?",
            "UPDATE ticket_types SET price = -0.01 WHERE id = ?",
            "UPDATE ticket_types SET sale_end_time = sale_start_time - INTERVAL '1 second' WHERE id = ?"
    })
    @Transactional
    void shouldRejectInvalidInventoryAndTicketTypeValues(String sql) {
        Fixture fixture = createFixture();
        assertThatThrownBy(() -> jdbc.update(sql, fixture.type().getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldRejectDuplicateEmail() {
        User user = createUser();
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO users (email, password, role, status)
                SELECT email, password, role, status FROM users WHERE id = ?
                """, user.getId())).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldRejectUnknownForeignKey() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO ai_messages (ai_conversations_id, content) VALUES (-1, 'Test')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldRejectMultipleWalletsForOneOrganizer() {
        User user = createUser();
        jdbc.update("INSERT INTO organizer_wallets (organizer_id) VALUES (?)", user.getId());
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO organizer_wallets (organizer_id) VALUES (?)", user.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void shouldRejectMultipleRefundsForOnePayment() {
        Fixture fixture = createFixture();
        Payment payment = createPayment(fixture.booking());
        jdbc.update("""
                INSERT INTO refunds (payments_id, refund_transaction_id, amount, status)
                VALUES (?, ?, 0.30, 'TEST')
                """, payment.getId(), UUID.randomUUID().toString());
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO refunds (payments_id, refund_transaction_id, amount, status)
                VALUES (?, ?, 0.30, 'TEST')
                """, payment.getId(), UUID.randomUUID().toString()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ticket_code", "qr_token"})
    @Transactional
    void shouldRejectDuplicateTicketIdentifiers(String column) {
        Fixture fixture = createFixture();
        String code = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO tickets (booking_items_id, ticket_code, qr_token, status)
                VALUES (?, ?, ?, 'TEST')
                """, fixture.item().getId(), code, token);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO tickets (booking_items_id, ticket_code, qr_token, status)
                VALUES (?, ?, ?, 'TEST')
                """, fixture.item().getId(),
                column.equals("ticket_code") ? code : UUID.randomUUID().toString(),
                column.equals("qr_token") ? token : UUID.randomUUID().toString()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldUpgradeV1ToV2WithBooleanRevocationAndPersistedStaffRole() {
        String schema = "auth_migration_" + UUID.randomUUID().toString().replace("-", "");
        try {
            Flyway.configure().dataSource(jdbc.getDataSource()).schemas(schema).defaultSchema(schema)
                    .target("1").load().migrate();
            jdbc.update("INSERT INTO " + schema + ".users (id, email, password, role, status) "
                    + "VALUES (1, 'migration@example.invalid', 'test-hash', 'CUSTOMER', 'ACTIVE')");
            Flyway upgraded = Flyway.configure().dataSource(jdbc.getDataSource()).schemas(schema)
                    .defaultSchema(schema).load();
            assertThat(upgraded.migrate().migrationsExecuted).isEqualTo(1);
            assertThat(upgraded.info().current().getVersion().getVersion()).isEqualTo("2");
            assertThat(upgraded.info().pending()).isEmpty();
            jdbc.update("UPDATE " + schema + ".users SET role = 'STAFF' WHERE id = 1");
            assertThat(jdbc.queryForObject("SELECT role FROM " + schema + ".users WHERE id = 1", String.class))
                    .isEqualTo("STAFF");
            jdbc.update("INSERT INTO " + schema + ".refresh_tokens (users_id, token_hash, expires_at) "
                    + "VALUES (1, ?, (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') + INTERVAL '7 days')", "a".repeat(64));
            assertThat(jdbc.queryForObject("SELECT revoked FROM " + schema + ".refresh_tokens", Boolean.class)).isFalse();
            jdbc.update("UPDATE " + schema + ".refresh_tokens SET revoked = true");
            assertThat(jdbc.queryForObject("SELECT revoked FROM " + schema + ".refresh_tokens", Boolean.class)).isTrue();
            assertThat(jdbc.queryForList("SELECT column_name FROM information_schema.columns "
                    + "WHERE table_schema = ? AND table_name = 'refresh_tokens'", String.class, schema))
                    .contains("revoked").doesNotContain("revoked_at");
            assertThat(jdbc.queryForObject("SELECT is_nullable FROM information_schema.columns "
                    + "WHERE table_schema = ? AND table_name = 'refresh_tokens' AND column_name = 'revoked'",
                    String.class, schema)).isEqualTo("NO");
        } finally {
            // Only the uniquely named schema created by this test is removed.
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    private Fixture createFixture() {
        User user = createUser();
        Venue venue = new Venue();
        venue.setCity("Test city");
        venue.setAddress("Test address");
        venue.setCapacity(100);
        persist(venue);
        Category category = new Category();
        category.setName("Test category");
        persist(category);
        Event event = new Event();
        event.setOrganizer(user);
        event.setVenue(venue);
        event.setCategory(category);
        event.setName("Test event");
        event.setThumbnailImageUrl("https://example.invalid/thumbnail.jpg");
        event.setBannerImageUrl("https://example.invalid/banner.jpg");
        event.setStartTime(LocalDateTime.of(2030, 1, 2, 10, 0));
        event.setEndTime(LocalDateTime.of(2030, 1, 2, 12, 0));
        event.setStatus("TEST");
        persist(event);
        TicketType type = createTicketType(event);
        Booking booking = createBooking(user, event);
        BookingItem item = createItem(booking, type);
        entityManager.flush();
        return new Fixture(user, event, type, booking, item);
    }

    private User createUser() {
        User user = new User();
        user.setEmail(UUID.randomUUID() + "@example.invalid");
        // Intentionally not a usable login credential; authentication is outside this test.
        user.setPassword("{invalid}test-hash");
        user.setRole(Role.CUSTOMER);
        user.setStatus("TEST");
        return users.saveAndFlush(user);
    }

    private TicketType createTicketType(Event event) {
        TicketType type = new TicketType();
        type.setEvent(event);
        type.setName("Test ticket");
        type.setImageUrl("https://example.invalid/ticket.jpg");
        type.setPrice(new BigDecimal("0.10"));
        type.setQuantity(100);
        type.setRemainingQuantity(100);
        type.setSaleStartTime(LocalDateTime.of(2030, 1, 1, 0, 0));
        type.setSaleEndTime(LocalDateTime.of(2030, 1, 2, 0, 0));
        type.setStatus("TEST");
        return persist(type);
    }

    private Booking createBooking(User user, Event event) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setTotalAmount(new BigDecimal("0.10").add(new BigDecimal("0.20")));
        booking.setStatus("TEST");
        booking.setExpiredAt(LocalDateTime.of(2030, 1, 1, 0, 15));
        return bookings.saveAndFlush(booking);
    }

    private BookingItem createItem(Booking booking, TicketType type) {
        BookingItem item = new BookingItem();
        item.setBooking(booking);
        item.setTicketType(type);
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("0.10"));
        return persist(item);
    }

    private Payment createPayment(Booking booking) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setAmount(new BigDecimal("0.30"));
        payment.setStatus("TEST");
        return persist(payment);
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private record Fixture(User user, Event event, TicketType type, Booking booking, BookingItem item) {
    }
}
