package com.eventhub.automation.utils;

import com.eventhub.automation.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to manage database snapshots and automated rollback / cleanup
 * after test runs, ensuring test isolation and preserving pristine database
 * state.
 */
public class DatabaseCleaner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

    // Hard baseline thresholds from seed data + volume data
    private static final int BASELINE_MAX_USER_ID = 119;
    private static final int BASELINE_MAX_CATEGORY_ID = 12;
    private static final int BASELINE_MAX_EVENT_ID = 90;
    private static final int BASELINE_MAX_TICKET_TYPE_ID = 270;
    private static final int BASELINE_MAX_BOOKING_ID = 179;
    private static final int BASELINE_MAX_TICKET_ID = 304;
    private static final int BASELINE_MAX_WITHDRAWAL_ID = 26;
    private static final int BASELINE_MAX_EVENT_STAFF_ID = 80;

    public record Snapshot(
            int maxUserId,
            int maxCategoryId,
            int maxEventId,
            int maxTicketTypeId,
            int maxBookingId,
            int maxTicketId,
            int maxPaymentId,
            int maxRefundId,
            int maxWithdrawalId,
            int maxEventStaffId,
            int maxNotificationId) {
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ConfigurationManager.getDbUrl(),
                ConfigurationManager.getDbUsername(),
                ConfigurationManager.getDbPassword());
    }

    /**
     * Captures a snapshot of current database state (max IDs).
     */
    public static Snapshot takeSnapshot() {
        if (!ConfigurationManager.isDbRollbackEnabled()) {
            return null;
        }

        try (Connection conn = getConnection()) {
            return new Snapshot(
                    getMaxId(conn, "users"),
                    getMaxId(conn, "categories"),
                    getMaxId(conn, "events"),
                    getMaxId(conn, "ticket_types"),
                    getMaxId(conn, "bookings"),
                    getMaxId(conn, "tickets"),
                    getMaxId(conn, "payments"),
                    getMaxId(conn, "refunds"),
                    getMaxId(conn, "withdrawal_requests"),
                    getMaxId(conn, "event_staffs"),
                    getMaxId(conn, "notifications"));
        } catch (Exception e) {
            logger.warn("Could not take database snapshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Rolls back database to the snapshot state, deleting any records created
     * since the snapshot was taken, and restoring altered baseline records.
     */
    public static void rollbackToSnapshot(Snapshot snapshot) {
        if (!ConfigurationManager.isDbRollbackEnabled() || snapshot == null) {
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete tickets created since snapshot or referencing new bookings/events
                executeUpdate(conn, """
                        DELETE FROM tickets
                        WHERE id > ?
                           OR booking_items_id IN (
                               SELECT id FROM booking_items
                               WHERE bookings_id > ?
                                  OR ticket_types_id IN (SELECT id FROM ticket_types WHERE events_id > ?)
                           )
                        """, snapshot.maxTicketId(), snapshot.maxBookingId(), snapshot.maxEventId());

                // 2. Delete refunds and payment child tables
                executeUpdate(conn, """
                        DELETE FROM refunds
                        WHERE id > ?
                           OR payments_id IN (SELECT id FROM payments WHERE id > ? OR bookings_id > ?)
                        """, snapshot.maxRefundId(), snapshot.maxPaymentId(), snapshot.maxBookingId());

                executeUpdate(conn, """
                        DELETE FROM payment_finances
                        WHERE payments_id IN (SELECT id FROM payments WHERE id > ? OR bookings_id > ?)
                        """, snapshot.maxPaymentId(), snapshot.maxBookingId());

                executeUpdate(conn, "DELETE FROM payments WHERE id > ? OR bookings_id > ?",
                        snapshot.maxPaymentId(), snapshot.maxBookingId());

                // 3. Delete booking items and bookings
                executeUpdate(conn, """
                        DELETE FROM booking_items
                        WHERE bookings_id > ?
                           OR ticket_types_id IN (SELECT id FROM ticket_types WHERE events_id > ?)
                        """, snapshot.maxBookingId(), snapshot.maxEventId());

                executeUpdate(conn, "DELETE FROM bookings WHERE id > ? OR users_id > ?",
                        snapshot.maxBookingId(), snapshot.maxUserId());

                // 4. Delete ticket types
                executeUpdate(conn, "DELETE FROM ticket_types WHERE id > ? OR events_id > ?",
                        snapshot.maxTicketTypeId(), snapshot.maxEventId());

                // 5. Delete event guests, event staffs, user event interactions
                executeUpdate(conn, "DELETE FROM event_guests WHERE events_id > ?", snapshot.maxEventId());
                executeUpdate(conn, """
                        DELETE FROM event_staffs
                        WHERE id > ?
                           OR events_id > ?
                           OR staff_id > ?
                           OR assigned_by > ?
                        """, snapshot.maxEventStaffId(), snapshot.maxEventId(), snapshot.maxUserId(),
                        snapshot.maxUserId());

                executeUpdate(conn, "DELETE FROM user_event_interactions WHERE users_id > ? OR events_id > ?",
                        snapshot.maxUserId(), snapshot.maxEventId());

                // 6. Delete events
                executeUpdate(conn, "DELETE FROM events WHERE id > ? OR organizer_id > ?",
                        snapshot.maxEventId(), snapshot.maxUserId());

                // 7. Delete categories (created since snapshot or matching test patterns)
                executeUpdate(conn, """
                        DELETE FROM categories
                        WHERE id > ?
                           OR name LIKE 'Danh mục Auto%'
                           OR name LIKE 'Test%'
                           OR name = 'Tự do'
                        """, snapshot.maxCategoryId());

                // 8. Delete payout transactions & withdrawal requests
                executeUpdate(conn, """
                        DELETE FROM payout_transactions
                        WHERE withdrawal_requests_id IN (SELECT id FROM withdrawal_requests WHERE id > ?)
                        """, snapshot.maxWithdrawalId());

                executeUpdate(conn, "DELETE FROM withdrawal_requests WHERE id > ?", snapshot.maxWithdrawalId());

                // 9. Delete organizer wallets for new test organizers
                executeUpdate(conn, "DELETE FROM organizer_wallets WHERE organizer_id > ?", snapshot.maxUserId());

                // 10. Delete refresh tokens & notifications
                executeUpdate(conn, "DELETE FROM refresh_tokens WHERE users_id > ?", snapshot.maxUserId());
                executeUpdate(conn, "DELETE FROM notifications WHERE id > ? OR users_id > ?",
                        snapshot.maxNotificationId(), snapshot.maxUserId());

                // 11. Delete AI logs & conversations for new users
                executeUpdate(conn, """
                        DELETE FROM ai_messages
                        WHERE ai_conversations_id IN (SELECT id FROM ai_conversations WHERE users_id > ?)
                        """, snapshot.maxUserId());
                executeUpdate(conn, "DELETE FROM ai_conversations WHERE users_id > ?", snapshot.maxUserId());
                executeUpdate(conn, "DELETE FROM user_ai_sync_logs WHERE users_id > ?", snapshot.maxUserId());

                // 12. Delete new users created during test (safely preserve dev user
                // nguyennam61yb@gmail.com)
                executeUpdate(conn, """
                        DELETE FROM users
                        WHERE (id > ? OR email LIKE 'auto_%' OR email LIKE 'testuser%')
                          AND email <> 'nguyennam61yb@gmail.com'
                        """, snapshot.maxUserId());

                // 13. Restore baseline customer profile (vol.customer1 & seed.customer1)
                executeUpdate(conn, """
                        UPDATE users
                        SET full_name = 'Khách Hàng Test Số 1',
                            phone = '0933000001',
                            password = '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.',
                            status = 'ACTIVE'
                        WHERE email = 'vol.customer1@eventhub.test'
                        """);

                executeUpdate(conn, """
                        UPDATE users
                        SET full_name = 'Nguyễn Gia Bảo',
                            phone = '0908000009',
                            password = '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.',
                            status = 'ACTIVE'
                        WHERE email = 'seed.customer1@eventhub.local'
                        """);

                // 13b. Restore default test password (EventHub@123) for all seeded test users
                executeUpdate(conn, """
                        UPDATE users
                        SET password = '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.'
                        WHERE email IN (
                            'vol.customer1@eventhub.test',
                            'seed.customer1@eventhub.local',
                            'demo20.customer1@example.test',
                            'vol.organizer1@eventhub.test',
                            'seed.organizer1@eventhub.local',
                            'demo20.organizer1@example.test',
                            'vol.staff1@eventhub.test',
                            'seed.staff1@eventhub.local',
                            'demo20.staff1@example.test',
                            'seed.admin@eventhub.local',
                            'demo20.admin@example.test'
                        )
                        """);

                // 14. Restore locked accounts state
                executeUpdate(conn, """
                        UPDATE users
                        SET status = 'LOCKED'
                        WHERE email IN (
                            'vol.customer36@eventhub.test',
                            'vol.customer37@eventhub.test',
                            'vol.customer38@eventhub.test',
                            'vol.customer39@eventhub.test',
                            'vol.customer40@eventhub.test'
                        ) AND status <> 'LOCKED'
                        """);

                // 15. Restore active accounts state
                executeUpdate(conn, """
                        UPDATE users
                        SET status = 'ACTIVE'
                        WHERE email IN (
                            'seed.customer1@eventhub.local',
                            'seed.organizer1@eventhub.local',
                            'seed.staff1@eventhub.local',
                            'vol.customer1@eventhub.test',
                            'vol.organizer1@eventhub.test',
                            'vol.staff1@eventhub.test',
                            'seed.admin@eventhub.local'
                        ) AND status <> 'ACTIVE'
                        """);

                // 16. Restore pending approval events state if any were approved or rejected during test
                executeUpdate(conn, """
                        UPDATE events
                        SET status = 'PENDING_APPROVAL',
                            reviewed_by = NULL,
                            reviewed_at = NULL,
                            reject_reason = NULL
                        WHERE id IN (48, 81, 82, 83, 84)
                          AND status <> 'PENDING_APPROVAL'
                        """);

                // 17. Restore pending withdrawal requests state if any were approved or rejected during test
                executeUpdate(conn, """
                        UPDATE withdrawal_requests
                        SET status = 'PENDING',
                            processed_by = NULL,
                            processed_at = NULL,
                            reject_reason = NULL,
                            transaction_code = NULL
                        WHERE id IN (2, 5, 7, 10, 13, 16, 19, 22, 25)
                          AND status <> 'PENDING'
                        """);

                conn.commit();
                logger.info("Database rollback to snapshot completed successfully.");
            } catch (Exception e) {
                conn.rollback();
                logger.error("Error during database rollback: {}", e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.warn("Could not execute database rollback: {}", e.getMessage());
        }
    }

    /**
     * Comprehensive cleanup of all test-generated data across the entire database,
     * resetting database back to pristine baseline seed data.
     */
    public static final String DEFAULT_PASSWORD_BCRYPT = "$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.";

    public static void resetUserPasswords() {
        if (!ConfigurationManager.isDbRollbackEnabled()) {
            return;
        }
        try (Connection conn = getConnection()) {
            executeUpdate(conn, """
                    UPDATE users
                    SET password = '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.'
                    WHERE email IN (
                        'vol.customer1@eventhub.test',
                        'seed.customer1@eventhub.local',
                        'demo20.customer1@example.test',
                        'vol.organizer1@eventhub.test',
                        'seed.organizer1@eventhub.local',
                        'demo20.organizer1@example.test',
                        'vol.staff1@eventhub.test',
                        'seed.staff1@eventhub.local',
                        'demo20.staff1@example.test',
                        'seed.admin@eventhub.local',
                        'demo20.admin@example.test'
                    )
                    """);
        } catch (Exception e) {
            logger.warn("Could not reset user passwords: {}", e.getMessage());
        }
    }

    public static void cleanAllTestData() {
        Snapshot baselineSnapshot = new Snapshot(
                BASELINE_MAX_USER_ID,
                BASELINE_MAX_CATEGORY_ID,
                BASELINE_MAX_EVENT_ID,
                BASELINE_MAX_TICKET_TYPE_ID,
                BASELINE_MAX_BOOKING_ID,
                BASELINE_MAX_TICKET_ID,
                0, // max payment id
                0, // max refund id
                BASELINE_MAX_WITHDRAWAL_ID,
                BASELINE_MAX_EVENT_STAFF_ID,
                0 // max notification id
        );
        rollbackToSnapshot(baselineSnapshot);
    }

    private static int getMaxId(Connection conn, String table) {
        String sql = "SELECT COALESCE(MAX(id), 0) FROM " + table;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("Could not query max id for {}: {}", table, e.getMessage());
        }
        return 0;
    }

    private static void executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        System.out.println("Running manual EventHub Database Cleanup...");
        cleanAllTestData();
        System.out.println("Database cleanup finished.");
    }
}
