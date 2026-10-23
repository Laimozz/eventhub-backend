package com.eventhub.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chuyên trách xử lý ghi nhận các hộp thư Outbox cho AI:
 * 1. Hộp thư sự kiện (event_ai_sync_logs): ghi nhận thêm/sửa sự kiện để AI tiêu thụ.
 * 2. Hộp thư hành vi (user_event_interactions): tiếp nhận luồng stream tương tác từ người dùng.
 * 3. Hộp thư phản hồi (ai_recommendation_logs): đánh giá độ chính xác các gợi ý của AI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiOutboxService {

    private final JdbcTemplate jdbc;

    // Tạm thời bật chế độ bảo trì hệ thống AI
    private static final boolean AI_MAINTENANCE_MODE = true;

    /**
     * Ghi nhận sự kiện vào hộp thư Outbox `event_ai_sync_logs`.
     * Gọi bên trong các method có @Transactional khi tạo, sửa, hoặc duyệt sự kiện.
     */
    @Transactional
    public void logEventOutbox(Integer eventId, String actionType) {
        if (AI_MAINTENANCE_MODE || eventId == null) return;
        try {
            String sql = """
                INSERT INTO event_ai_sync_logs (event_id, action_type, status, edit_count, updated_at)
                VALUES (?, ?, 'PENDING', 1, CURRENT_TIMESTAMP)
                ON CONFLICT (event_id)
                DO UPDATE SET
                    action_type = EXCLUDED.action_type,
                    status = 'PENDING',
                    edit_count = event_ai_sync_logs.edit_count + 1,
                    updated_at = CURRENT_TIMESTAMP
            """;
            jdbc.update(sql, eventId, actionType != null ? actionType.toUpperCase() : "UPDATE");
        } catch (Exception e) {
            log.warn("Lỗi khi ghi sự kiện #{} vào outbox event_ai_sync_logs: {}", eventId, e.getMessage());
        }
    }

    /**
     * Tiếp nhận luồng stream hành vi user (VIEW, CLICK, SEARCH_CLICK, dwell_seconds) và ghi vào hộp thư.
     */
    @Transactional
    public void logUserInteraction(Integer userId, Integer eventId, String actionType, int dwellSeconds, Integer recLogId) {
        if (AI_MAINTENANCE_MODE || eventId == null) return;

        // 1. Ghi vào hộp thư user_event_interactions nếu người dùng đã đăng nhập
        if (userId != null && userId > 0) {
            try {
                String sql = """
                    INSERT INTO user_event_interactions (users_id, events_id, interaction_type, dwell_seconds, created_at)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
                jdbc.update(sql, userId, eventId, actionType != null ? actionType.toUpperCase() : "VIEW", dwellSeconds);
            } catch (Exception e) {
                log.warn("Lỗi khi ghi nhận hành vi user vào hộp thư: {}", e.getMessage());
            }
        }

        // 2. Nếu có recLogId (bấm vào từ 6 card gợi ý AI): Cập nhật vị trí bấm (hit_rank) vào hộp thư đánh giá
        if (recLogId != null && recLogId > 0) {
            try {
                String sqlFeedback = """
                    UPDATE ai_recommendation_logs
                    SET clicked_event_id = ?,
                        hit_rank = array_position(recommended_ids, ?),
                        clicked_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND ? = ANY(recommended_ids)
                """;
                jdbc.update(sqlFeedback, eventId, eventId, recLogId, eventId);
            } catch (Exception e) {
                log.warn("Lỗi khi cập nhật feedback ai_recommendation_logs: {}", e.getMessage());
            }
        }
    }

    /**
     * Ghi nhận hành vi ĐẶT VÉ (BOOK) thành công vào hộp thư.
     * Gọi bên trong @Transactional của BookingService.
     */
    @Transactional
    public void logBookingInteraction(Integer userId, Integer eventId) {
        if (AI_MAINTENANCE_MODE || userId == null || eventId == null) return;
        try {
            // Ghi vào hộp thư tương tác
            String sqlBook = """
                INSERT INTO user_event_interactions (users_id, events_id, interaction_type, dwell_seconds, created_at)
                VALUES (?, ?, 'BOOK', 0, CURRENT_TIMESTAMP)
            """;
            jdbc.update(sqlBook, userId, eventId);

            // Đánh dấu is_booked = true nếu trước đó user đã nhận phiên gợi ý từ AI
            String sqlMarkBooked = """
                UPDATE ai_recommendation_logs
                SET is_booked = true
                WHERE users_id = ? AND ? = ANY(recommended_ids)
                  AND created_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
            """;
            jdbc.update(sqlMarkBooked, userId, eventId);
        } catch (Exception e) {
            log.warn("Lỗi khi ghi nhận hành vi BOOK vào hộp thư: {}", e.getMessage());
        }
    }

    /**
     * Ghi nhận người dùng mới hoặc cập nhật hồ sơ vào hộp thư Outbox `user_ai_sync_logs`.
     * Gọi bên trong @Transactional của AuthService (đăng ký) hoặc UserService (cập nhật thông tin).
     */
    @Transactional
    public void logUserOutbox(Integer userId, String actionType) {
        if (AI_MAINTENANCE_MODE || userId == null) return;
        try {
            String sql = """
                INSERT INTO user_ai_sync_logs (users_id, action_type, status, updated_at)
                VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP)
                ON CONFLICT (users_id)
                DO UPDATE SET
                    action_type = EXCLUDED.action_type,
                    status = 'PENDING',
                    updated_at = CURRENT_TIMESTAMP
            """;
            jdbc.update(sql, userId, actionType != null ? actionType.toUpperCase() : "UPDATE_USER");
        } catch (Exception e) {
            log.warn("Lỗi khi ghi user #{} vào outbox user_ai_sync_logs: {}", userId, e.getMessage());
        }
    }
}
