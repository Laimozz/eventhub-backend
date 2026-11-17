-- Keep existing conversation data and allow complete AI answers.
ALTER TABLE ai_messages ALTER COLUMN content TYPE TEXT;
ALTER TABLE ai_conversations ADD COLUMN owner_role VARCHAR(20);
UPDATE ai_conversations c SET owner_role = u.role FROM users u WHERE u.id = c.users_id;
ALTER TABLE ai_conversations ALTER COLUMN owner_role SET NOT NULL;
ALTER TABLE ai_conversations ADD CONSTRAINT ck_ai_conversation_role
    CHECK (owner_role IN ('CUSTOMER', 'ORGANIZER', 'STAFF', 'ADMIN'));
CREATE INDEX idx_ai_conversations_owner_updated ON ai_conversations (users_id, owner_role, updated_at DESC, id DESC);
CREATE INDEX idx_ai_messages_conversation_order ON ai_messages (ai_conversations_id, id);
