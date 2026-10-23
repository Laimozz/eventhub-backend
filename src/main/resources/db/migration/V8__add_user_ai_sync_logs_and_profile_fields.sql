-- =================================================================================
-- FILE: V8__add_user_ai_sync_logs_and_profile_fields.sql
-- DỰ ÁN: EVENTHUB - NỀN TẢNG BÁN VÉ SỰ KIỆN
-- MỤC ĐÍCH:
--   1. Bổ sung trường city (Tỉnh/Thành phố) và preferred_categories (Sở thích ban đầu) vào bảng users.
--   2. Tạo bảng hộp thư user_ai_sync_logs để lưu vết thêm/sửa user cho AI Service.
-- =================================================================================

-- 1. Bổ sung cột city và preferred_categories vào bảng users
ALTER TABLE users ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_categories VARCHAR(255);

-- 2. Bảng hộp thư đồng bộ User cho AI (Outbox Pattern)
CREATE TABLE IF NOT EXISTS user_ai_sync_logs (
    users_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(20) NOT NULL,                 -- 'CREATE_USER' hoặc 'UPDATE_USER'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',    -- 'PENDING' hoặc 'PROCESSED'
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_user_ai_sync_logs_status
ON user_ai_sync_logs (status) WHERE status = 'PENDING';
