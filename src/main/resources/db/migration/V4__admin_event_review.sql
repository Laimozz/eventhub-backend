-- Fails safely if existing categories have duplicate normalized names.
CREATE UNIQUE INDEX uk_categories_name_normalized ON categories (LOWER(BTRIM(name)));
ALTER TABLE events ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE events DROP CONSTRAINT ck_events_status;
ALTER TABLE events ADD CONSTRAINT ck_events_status CHECK (status IN (
    'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'ONGOING',
    'COMPLETED', 'PENDING_CANCELLATION', 'CANCELED'
));
