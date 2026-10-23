-- =================================================================================
-- FILE: V7__add_ai_recommendation_tracking.sql
-- DỰ ÁN: EVENTHUB - NỀN TẢNG BÁN VÉ SỰ KIỆN
-- MỤC ĐÍCH:
--   1. Bổ sung cột dwell_seconds (thời gian xem sự kiện) vào bảng user_event_interactions.
--   2. Tạo bảng hộp thư event_ai_sync_logs để gom các thay đổi (INSERT/UPDATE) của sự kiện,
--      khách mời và hạng vé mà không phải tính lại Embedding nhiều lần khi sửa liên tiếp.
--   3. Tạo bảng ai_recommendation_logs để lưu đúng 6 card mà AI đã hiển thị cho khách
--      tại mỗi phiên truy cập, phục vụ chấm điểm thực tế (HitRate@6, MRR, Conversion).
--   4. Tạo Triggers tự động đẩy sự kiện vào hộp thư khi thêm mới hoặc cập nhật.
-- =================================================================================

-- 1. Thêm cột thời gian xem chi tiết sự kiện (tính bằng giây) vào bảng tương tác sẵn có
ALTER TABLE user_event_interactions
ADD COLUMN IF NOT EXISTS dwell_seconds INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_user_interactions_timeline
ON user_event_interactions (users_id, created_at ASC);


-- 2. Bảng hộp thư đồng bộ sự kiện cho AI (Outbox Pattern)
-- Mỗi event_id là khóa chính để nếu sửa lần 1, lần 2, lần 3 liên tiếp thì tự gộp thành 1 dòng PENDING
CREATE TABLE IF NOT EXISTS event_ai_sync_logs (
    event_id INTEGER PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
    action_type VARCHAR(20) NOT NULL,                 -- 'INSERT' hoặc 'UPDATE'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',    -- 'PENDING' hoặc 'PROCESSED'
    edit_count INTEGER NOT NULL DEFAULT 1,            -- Số lần sửa tích lũy trước khi AI đồng bộ
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_event_ai_sync_logs_status
ON event_ai_sync_logs (status) WHERE status = 'PENDING';


-- 3. Bảng lưu vết phiên gợi ý thực tế (Impression & Click Evaluation Log)
-- Lưu lại chính xác 6 card sự kiện AI đã đưa ra trên màn hình cho khách hàng và kết quả chấm bài
CREATE TABLE IF NOT EXISTS ai_recommendation_logs (
    id SERIAL PRIMARY KEY,
    users_id INTEGER REFERENCES users(id) ON DELETE CASCADE,           -- Nullable cho khách vãng lai chưa login
    recommended_ids INTEGER[] NOT NULL,                                -- Mảng 6 ID sự kiện AI đã hiển thị, VD: '{10, 12, 15, 20, 25, 30}'
    clicked_event_id INTEGER REFERENCES events(id) ON DELETE SET NULL, -- Sự kiện khách thực sự bấm vào trong 6 card
    hit_rank INTEGER,                                                 -- Vị trí card được bấm (1..6), NULL nếu khách ngó lơ cả 6 card
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,                         -- TRUE nếu khách tiến tới mua vé thành công từ gợi ý này
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    clicked_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_ai_rec_logs_user
ON ai_recommendation_logs (users_id, created_at DESC);


-- 4. Trigger tự động ghi nhận vào hộp thư khi bảng events thay đổi nội dung
CREATE OR REPLACE FUNCTION fn_notify_event_for_ai()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO event_ai_sync_logs (event_id, action_type, status, edit_count, updated_at)
    VALUES (NEW.id, TG_OP, 'PENDING', 1, CURRENT_TIMESTAMP)
    ON CONFLICT (event_id)
    DO UPDATE SET
        action_type = EXCLUDED.action_type,
        status = 'PENDING',
        edit_count = event_ai_sync_logs.edit_count + 1,
        updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_events_ai_sync ON events;
CREATE TRIGGER trg_events_ai_sync
AFTER INSERT OR UPDATE OF name, description, categories_id, venues_id, organizer_id, status
ON events
FOR EACH ROW
EXECUTE FUNCTION fn_notify_event_for_ai();


-- 5. Trigger tự động ghi nhận vào hộp thư khi thêm/sửa/xóa khách mời trong bảng event_guests
CREATE OR REPLACE FUNCTION fn_notify_guest_for_ai()
RETURNS TRIGGER AS $$
DECLARE
    target_event_id INTEGER;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        target_event_id := OLD.events_id;
    ELSE
        target_event_id := NEW.events_id;
    END IF;

    INSERT INTO event_ai_sync_logs (event_id, action_type, status, edit_count, updated_at)
    VALUES (target_event_id, 'UPDATE_GUEST', 'PENDING', 1, CURRENT_TIMESTAMP)
    ON CONFLICT (event_id)
    DO UPDATE SET
        action_type = 'UPDATE_GUEST',
        status = 'PENDING',
        edit_count = event_ai_sync_logs.edit_count + 1,
        updated_at = CURRENT_TIMESTAMP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_event_guests_ai_sync ON event_guests;
CREATE TRIGGER trg_event_guests_ai_sync
AFTER INSERT OR UPDATE OR DELETE
ON event_guests
FOR EACH ROW
EXECUTE FUNCTION fn_notify_guest_for_ai();


-- 6. Trigger tự động ghi nhận vào hộp thư khi thêm/sửa/xóa loại vé trong bảng ticket_types
CREATE OR REPLACE FUNCTION fn_notify_ticket_type_for_ai()
RETURNS TRIGGER AS $$
DECLARE
    target_event_id INTEGER;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        target_event_id := OLD.events_id;
    ELSE
        target_event_id := NEW.events_id;
    END IF;

    INSERT INTO event_ai_sync_logs (event_id, action_type, status, edit_count, updated_at)
    VALUES (target_event_id, 'UPDATE_TICKET', 'PENDING', 1, CURRENT_TIMESTAMP)
    ON CONFLICT (event_id)
    DO UPDATE SET
        action_type = 'UPDATE_TICKET',
        status = 'PENDING',
        edit_count = event_ai_sync_logs.edit_count + 1,
        updated_at = CURRENT_TIMESTAMP;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ticket_types_ai_sync ON ticket_types;
CREATE TRIGGER trg_ticket_types_ai_sync
AFTER INSERT OR UPDATE OR DELETE
ON ticket_types
FOR EACH ROW
EXECUTE FUNCTION fn_notify_ticket_type_for_ai();
