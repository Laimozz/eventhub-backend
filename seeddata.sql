-- EventHub demo data for PostgreSQL, after migrations V1 through V8.
-- Run this entire file in pgAdmin/DBeaver, or:
--   psql -h localhost -U postgres -d eventhub_db -v ON_ERROR_STOP=1 -f seeddata.sql
-- This file only adds data. Existing records are retained; rerunning skips this seed.
-- Images are selected from the target database BEFORE inserting the demo records.
-- At least one existing image URL of at most 255 characters is required.
-- Dates are relative to execution time and stored as UTC, like the application.
--
-- Demo login password for ALL accounts: EventHub@123 (BCrypt, cost 12).
-- ADMIN:     seed.admin@eventhub.local
-- ORGANIZER: seed.organizer1@eventhub.local ... seed.organizer3@eventhub.local
-- STAFF:     seed.staff1@eventhub.local     ... seed.staff4@eventhub.local
-- CUSTOMER:  seed.customer1@eventhub.local  ... seed.customer10@eventhub.local
-- Payment/transfer references and bank accounts below are synthetic demo data.
-- No QUEUED/PROCESSING refunds are seeded, so the worker has no demo API jobs.

BEGIN;
SET LOCAL client_encoding = 'UTF8';
SET LOCAL TIME ZONE 'UTC';

DO $seed$
DECLARE
    seed_now TIMESTAMP := date_trunc('second', CURRENT_TIMESTAMP AT TIME ZONE 'UTC');
    demo_password TEXT := '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.';
    all_images TEXT[];
    thumbnail_images TEXT[];
    banner_images TEXT[];
    zone_images TEXT[];
    ticket_images TEXT[];
    guest_images TEXT[];
    avatar_images TEXT[];
    admin_id INTEGER;
    organizer_ids INTEGER[] := '{}';
    staff_ids INTEGER[] := '{}';
    customer_ids INTEGER[] := '{}';
    category_ids INTEGER[] := '{}';
    venue_ids INTEGER[] := '{}';
    event_ids INTEGER[] := '{}';
    type_ids INTEGER[] := '{}';
    wallet_ids INTEGER[] := '{}';
    row_data RECORD;
    event_row RECORD;
    item_row RECORD;
    user_id INTEGER;
    category_id INTEGER;
    venue_id INTEGER;
    event_id INTEGER;
    type_id INTEGER;
    booking_id INTEGER;
    payment_id INTEGER;
    refund_id INTEGER;
    wallet_id INTEGER;
    withdrawal_id INTEGER;
    conversation_id INTEGER;
    checkin_staff_id INTEGER;
    i INTEGER;
    j INTEGER;
    k INTEGER;
    type_number INTEGER;
    ticket_quantity INTEGER;
    unit_price NUMERIC(19,2);
    total NUMERIC(19,2);
    fee NUMERIC(19,2);
    booked_at TIMESTAMP;
    starts_at TIMESTAMP;
    ends_at TIMESTAMP;
    checked_at TIMESTAMP;
    ticket_status TEXT;
    payment_method TEXT;
    refund_status TEXT;
    refund_method TEXT;
BEGIN
    -- Serialize concurrent runs of this same demo seed.
    PERFORM pg_advisory_xact_lock(20261023, 810);

    IF to_regclass('payment_finances') IS NULL OR to_regclass('refund_evidence') IS NULL THEN
        RAISE EXCEPTION 'Apply migrations V1 through V8 before running seeddata.sql.';
    END IF;

    IF EXISTS (SELECT 1 FROM users WHERE lower(btrim(email)) = 'seed.admin@eventhub.local') THEN
        IF (SELECT count(*) FROM events e JOIN users u ON u.id = e.organizer_id
            WHERE u.email IN ('seed.organizer1@eventhub.local', 'seed.organizer2@eventhub.local',
                              'seed.organizer3@eventhub.local')) = 10 THEN
            RAISE NOTICE 'EventHub demo data already exists; no rows were changed.';
            RETURN;
        END IF;
        RAISE EXCEPTION 'Seed accounts already exist but the demo is incomplete. No data was changed.';
    END IF;

    -- Reuse actual image links in this database; do not invent external URLs.
    SELECT array_agg(url ORDER BY url) INTO all_images
    FROM (
        SELECT btrim(thumbnail_image_url) AS url FROM events
        UNION SELECT btrim(banner_image_url) FROM events
        UNION SELECT btrim(image_zone_url) FROM events
        UNION SELECT btrim(image_url) FROM ticket_types
        UNION SELECT btrim(image_url) FROM event_guests
        UNION SELECT btrim(avatar_url) FROM users
    ) images WHERE url IS NOT NULL AND length(url) BETWEEN 1 AND 255;

    IF coalesce(cardinality(all_images), 0) = 0 THEN
        RAISE EXCEPTION 'No existing image URL found. Add an image to the database before seeding. No demo data was inserted.';
    END IF;

    SELECT array_agg(DISTINCT thumbnail_image_url ORDER BY thumbnail_image_url)
        INTO thumbnail_images FROM events WHERE length(btrim(thumbnail_image_url)) BETWEEN 1 AND 255;
    SELECT array_agg(DISTINCT banner_image_url ORDER BY banner_image_url)
        INTO banner_images FROM events WHERE length(btrim(banner_image_url)) BETWEEN 1 AND 255;
    SELECT array_agg(DISTINCT image_zone_url ORDER BY image_zone_url)
        INTO zone_images FROM events WHERE length(btrim(image_zone_url)) BETWEEN 1 AND 255;
    SELECT array_agg(DISTINCT image_url ORDER BY image_url)
        INTO ticket_images FROM ticket_types WHERE length(btrim(image_url)) BETWEEN 1 AND 255;
    SELECT array_agg(DISTINCT image_url ORDER BY image_url)
        INTO guest_images FROM event_guests WHERE length(btrim(image_url)) BETWEEN 1 AND 255;
    SELECT array_agg(DISTINCT avatar_url ORDER BY avatar_url)
        INTO avatar_images FROM users WHERE length(btrim(avatar_url)) BETWEEN 1 AND 500;
    thumbnail_images := coalesce(thumbnail_images, all_images);
    banner_images := coalesce(banner_images, thumbnail_images);
    zone_images := coalesce(zone_images, banner_images);
    ticket_images := coalesce(ticket_images, thumbnail_images);
    guest_images := coalesce(guest_images, thumbnail_images);
    avatar_images := coalesce(avatar_images, guest_images);

    -- 18 accounts: 1 admin, 3 organizers, 4 staff, 10 customers.
    FOR row_data IN SELECT * FROM (VALUES
        (1, 'seed.admin@eventhub.local', 'Quản trị EventHub Demo', 'ADMIN'),
        (2, 'seed.organizer1@eventhub.local', 'Nguyễn Minh Anh', 'ORGANIZER'),
        (3, 'seed.organizer2@eventhub.local', 'Trần Hoàng Nam', 'ORGANIZER'),
        (4, 'seed.organizer3@eventhub.local', 'Lê Thảo Vy', 'ORGANIZER'),
        (5, 'seed.staff1@eventhub.local', 'Phạm Quốc Huy', 'STAFF'),
        (6, 'seed.staff2@eventhub.local', 'Võ Ngọc Mai', 'STAFF'),
        (7, 'seed.staff3@eventhub.local', 'Đặng Tuấn Kiệt', 'STAFF'),
        (8, 'seed.staff4@eventhub.local', 'Bùi Khánh Linh', 'STAFF'),
        (9, 'seed.customer1@eventhub.local', 'Nguyễn Gia Bảo', 'CUSTOMER'),
        (10, 'seed.customer2@eventhub.local', 'Trần Minh Châu', 'CUSTOMER'),
        (11, 'seed.customer3@eventhub.local', 'Lê Đức Phúc', 'CUSTOMER'),
        (12, 'seed.customer4@eventhub.local', 'Phạm Hà My', 'CUSTOMER'),
        (13, 'seed.customer5@eventhub.local', 'Hoàng Thanh Tùng', 'CUSTOMER'),
        (14, 'seed.customer6@eventhub.local', 'Vũ Bảo Ngọc', 'CUSTOMER'),
        (15, 'seed.customer7@eventhub.local', 'Đỗ Anh Khoa', 'CUSTOMER'),
        (16, 'seed.customer8@eventhub.local', 'Phan Quỳnh Anh', 'CUSTOMER'),
        (17, 'seed.customer9@eventhub.local', 'Đặng Hải Đăng', 'CUSTOMER'),
        (18, 'seed.customer10@eventhub.local', 'Bùi Ngọc Hân', 'CUSTOMER')
    ) AS accounts(n, email, full_name, role) ORDER BY n LOOP
        INSERT INTO users (email, password, full_name, phone, role, status, date_of_birth,
                           gender, avatar_url, created_at, updated_at)
        VALUES (row_data.email, demo_password, row_data.full_name,
                '0908' || lpad(row_data.n::TEXT, 6, '0'), row_data.role, 'ACTIVE',
                DATE '1990-01-01' + row_data.n * 137,
                CASE WHEN row_data.n % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
                avatar_images[1 + (row_data.n - 1) % cardinality(avatar_images)],
                seed_now - INTERVAL '90 days', seed_now - INTERVAL '1 day')
        RETURNING id INTO user_id;
        CASE row_data.role
            WHEN 'ADMIN' THEN admin_id := user_id;
            WHEN 'ORGANIZER' THEN organizer_ids := array_append(organizer_ids, user_id);
            WHEN 'STAFF' THEN staff_ids := array_append(staff_ids, user_id);
            ELSE customer_ids := array_append(customer_ids, user_id);
        END CASE;

        -- Historical, revoked sessions: populate the table without active demo sessions.
        INSERT INTO refresh_tokens (users_id, token_hash, created_at, expires_at, revoked)
        VALUES (user_id, md5('seed-session-a-' || user_id) || md5('seed-session-b-' || user_id),
                seed_now - INTERVAL '30 days', seed_now - INTERVAL '23 days', TRUE);
    END LOOP;

    -- Reuse categories with the same normalized name (V4 unique index).
    FOR row_data IN SELECT * FROM (VALUES
        (1, 'Âm nhạc', 'Live concert, acoustic và các đêm nhạc cộng đồng.'),
        (2, 'Công nghệ', 'Hội thảo công nghệ, AI và phát triển phần mềm.'),
        (3, 'Kinh doanh', 'Khởi nghiệp, kết nối doanh nghiệp và chia sẻ kinh nghiệm.'),
        (4, 'Thể thao', 'Chạy bộ, vận động ngoài trời và hoạt động sức khỏe.'),
        (5, 'Nghệ thuật', 'Triển lãm, sáng tạo và trải nghiệm nghệ thuật.'),
        (6, 'Cộng đồng', 'Hoạt động tình nguyện, đọc sách và kết nối cộng đồng.')
    ) AS categories_seed(n, name, description) ORDER BY n LOOP
        INSERT INTO categories (name, description) VALUES (row_data.name, row_data.description)
        ON CONFLICT DO NOTHING RETURNING id INTO category_id;
        IF category_id IS NULL THEN
            SELECT id INTO STRICT category_id FROM categories WHERE lower(btrim(name)) = lower(btrim(row_data.name));
        END IF;
        category_ids := array_append(category_ids, category_id);
    END LOOP;

    FOR row_data IN SELECT * FROM (VALUES
        (1, 'Hồ Chí Minh', 'Nhà Văn hóa Thanh Niên, 4 Phạm Ngọc Thạch, Quận 1', 1500),
        (2, 'Hà Nội', 'Trung tâm Hội nghị Quốc gia, 57 Phạm Hùng, Nam Từ Liêm', 2000),
        (3, 'Đà Nẵng', 'Cung Hội nghị Quốc tế Ariyana, 107 Võ Nguyên Giáp', 1200),
        (4, 'Hồ Chí Minh', 'Công viên Gia Định, đường Hoàng Minh Giám, Gò Vấp', 3000),
        (5, 'Hà Nội', 'Không gian sáng tạo, 22 Tôn Thất Tùng, Đống Đa', 600),
        (6, 'Đà Nẵng', 'Nhà Văn hóa Lao động, 2 Cách Mạng Tháng Tám', 800)
    ) AS venues_seed(n, city, address, capacity) ORDER BY n LOOP
        SELECT id INTO venue_id FROM venues
        WHERE city = row_data.city AND address = row_data.address AND capacity >= 260 ORDER BY id LIMIT 1;
        IF venue_id IS NULL THEN
            INSERT INTO venues (city, address, capacity)
            VALUES (row_data.city, row_data.address, row_data.capacity) RETURNING id INTO venue_id;
        END IF;
        venue_ids := array_append(venue_ids, venue_id);
    END LOOP;

    -- 10 events. Day offsets are relative to the UTC execution date.
    FOR row_data IN SELECT * FROM (VALUES
        (1, 1, 1, 1, 'Đêm nhạc Thành phố lên đèn', 'Đêm nhạc pop và acoustic cùng các nghệ sĩ trẻ, giao lưu khán giả và trải nghiệm sân khấu trực tiếp.', 'APPROVED', 14, 12, 4, 350000),
        (2, 2, 2, 2, 'Vietnam AI & Developer Day', 'Hội thảo AI ứng dụng, phát triển phần mềm và kết nối cộng đồng lập trình viên.', 'APPROVED', 21, 2, 8, 250000),
        (3, 2, 3, 3, 'Startup Connect Việt Nam', 'Kết nối nhà sáng lập, doanh nghiệp và nhà đầu tư qua tọa đàm và hoạt động networking.', 'APPROVED', 28, 2, 7, 300000),
        (4, 3, 6, 4, 'Ngày hội Sách và Sống xanh', 'Ngày hội miễn phí với đổi sách, tái chế và các hoạt động cộng đồng dành cho gia đình.', 'APPROVED', 10, 1, 8, 0),
        (5, 3, 4, 4, 'Run Together - Chạy vì cộng đồng', 'Hoạt động chạy bộ cộng đồng, tiếp sức và giao lưu với các câu lạc bộ thể thao.', 'ONGOING', 0, 0, 4, 150000),
        (6, 1, 1, 6, 'Acoustic Cuối tuần bên nhau', 'Đêm nhạc acoustic đã diễn ra, có lịch sử bán vé và check-in để xem thống kê doanh thu.', 'COMPLETED', -14, 12, 4, 200000),
        (7, 2, 2, 5, 'Workshop Spring Boot thực chiến', 'Workshop xây dựng API, thiết kế dữ liệu và chia sẻ kinh nghiệm triển khai ứng dụng.', 'COMPLETED', -7, 2, 7, 400000),
        (8, 1, 1, 1, 'Jazz Night - Giai điệu mùa thu', 'Đêm nhạc jazz đang chờ quản trị viên kiểm tra và duyệt nội dung.', 'PENDING_APPROVAL', 35, 12, 3, 280000),
        (9, 3, 6, 3, 'Ngày hội Ẩm thực và Văn hóa', 'Bản đề xuất cần bổ sung thông tin tổ chức trước khi gửi duyệt lại.', 'REJECTED', 40, 2, 8, 100000),
        (10, 3, 5, 6, 'Art & Light - Không gian sắc màu', 'Triển lãm nghệ thuật đã hủy; có dữ liệu mẫu cho quy trình hoàn tiền tự động và thủ công.', 'CANCELED', 30, 3, 6, 180000)
    ) AS events_seed(n, organizer_no, category_no, venue_no, name, description, status, day_offset, start_hour, duration_hours, price)
    ORDER BY n LOOP
        i := row_data.n;
        starts_at := date_trunc('day', seed_now) + row_data.day_offset * INTERVAL '1 day'
                     + row_data.start_hour * INTERVAL '1 hour';
        IF row_data.status = 'ONGOING' THEN starts_at := seed_now - INTERVAL '1 hour'; END IF;
        ends_at := starts_at + row_data.duration_hours * INTERVAL '1 hour';
        INSERT INTO events (organizer_id, reviewed_by, venues_id, categories_id, name, description,
                            thumbnail_image_url, banner_image_url, image_zone_url, start_time, end_time,
                            status, cancel_reason, canceled_at, reject_reason, reviewed_at, created_at, updated_at)
        VALUES (organizer_ids[row_data.organizer_no],
                CASE WHEN row_data.status = 'PENDING_APPROVAL' THEN NULL ELSE admin_id END,
                venue_ids[row_data.venue_no], category_ids[row_data.category_no], row_data.name, row_data.description,
                thumbnail_images[1 + (i - 1) % cardinality(thumbnail_images)],
                banner_images[1 + (i - 1) % cardinality(banner_images)],
                zone_images[1 + (i - 1) % cardinality(zone_images)], starts_at, ends_at, row_data.status,
                CASE WHEN row_data.status = 'CANCELED' THEN 'Địa điểm bảo trì, ban tổ chức không thể triển khai đúng kế hoạch.' END,
                CASE WHEN row_data.status = 'CANCELED' THEN seed_now - INTERVAL '1 day' END,
                CASE WHEN row_data.status = 'REJECTED' THEN 'Cần bổ sung phương án an toàn và xác nhận địa điểm.' END,
                CASE WHEN row_data.status <> 'PENDING_APPROVAL' THEN seed_now - INTERVAL '55 days' END,
                seed_now - INTERVAL '60 days',
                CASE WHEN row_data.status IN ('CANCELED', 'REJECTED') THEN seed_now - INTERVAL '1 day'
                     ELSE seed_now - INTERVAL '55 days' END)
        RETURNING id INTO event_id;
        event_ids := array_append(event_ids, event_id);

        -- Three types per event: 260 tickets in total, within every venue's capacity.
        FOR j IN 1..3 LOOP
            unit_price := row_data.price * CASE j WHEN 1 THEN 1.0 WHEN 2 THEN 2.0 ELSE 0.8 END;
            ticket_quantity := CASE j WHEN 1 THEN 120 WHEN 2 THEN 60 ELSE 80 END;
            INSERT INTO ticket_types (events_id, name, description, image_url, price, quantity,
                                      reserved_quantity, remaining_quantity, sale_start_time, sale_end_time, status)
            VALUES (event_id, CASE j WHEN 1 THEN 'Tiêu chuẩn' WHEN 2 THEN 'VIP' ELSE 'Early Bird' END,
                    CASE j WHEN 1 THEN 'Vé vào cửa và tham gia các hoạt động chính.'
                           WHEN 2 THEN 'Khu vực ưu tiên và quà tặng từ ban tổ chức.'
                           ELSE 'Ưu đãi dành cho khách đăng ký sớm.' END,
                    ticket_images[1 + (i + j - 2) % cardinality(ticket_images)], unit_price, ticket_quantity,
                    0, ticket_quantity, seed_now - INTERVAL '50 days', starts_at - INTERVAL '2 hours',
                    CASE WHEN row_data.status IN ('APPROVED', 'ONGOING', 'COMPLETED') THEN 'ACTIVE' ELSE 'INACTIVE' END)
            RETURNING id INTO type_id;
            type_ids := array_append(type_ids, type_id);
        END LOOP;

        FOR j IN 1..2 LOOP
            INSERT INTO event_staffs (events_id, staff_id, assigned_by, status, created_at)
            VALUES (event_id, staff_ids[1 + (i + j - 2) % 4], organizer_ids[row_data.organizer_no],
                    'ACTIVE', seed_now - INTERVAL '40 days');
            INSERT INTO event_guests (events_id, name, role, description, image_url, created_at, updated_at)
            VALUES (event_id, CASE j WHEN 1 THEN 'Nguyễn Hoàng Minh' ELSE 'Trần Ngọc Anh' END,
                    CASE WHEN row_data.category_no = 1 THEN CASE j WHEN 1 THEN 'Ca sĩ' ELSE 'Nhạc sĩ' END
                         ELSE CASE j WHEN 1 THEN 'Diễn giả' ELSE 'Khách mời' END END,
                    'Khách mời đồng hành và giao lưu trong chương trình.',
                    guest_images[1 + (i + j - 2) % cardinality(guest_images)],
                    seed_now - INTERVAL '60 days', seed_now - INTERVAL '55 days');
        END LOOP;
    END LOOP;

    -- 48 paid bookings: six for each approved/ongoing/completed/canceled event.
    FOR i IN 1..10 LOOP
        IF i IN (8, 9) THEN CONTINUE; END IF;
        SELECT * INTO STRICT event_row FROM events WHERE id = event_ids[i];
        FOR j IN 1..6 LOOP
            user_id := customer_ids[1 + (i + j - 2) % 10];
            booked_at := least(seed_now - (j + 2) * INTERVAL '1 day', event_row.start_time - INTERVAL '3 days');
            type_number := 1 + (j - 1) % 3;
            type_id := type_ids[(i - 1) * 3 + type_number];
            SELECT price INTO unit_price FROM ticket_types WHERE id = type_id;
            ticket_quantity := 1 + (j - 1) % 3;
            INSERT INTO bookings (users_id, events_id, total_amount, status, expired_at, created_at, updated_at)
            VALUES (user_id, event_ids[i], 0, 'PAID', booked_at + INTERVAL '5 minutes',
                    booked_at, booked_at + INTERVAL '2 minutes') RETURNING id INTO booking_id;
            INSERT INTO booking_items (bookings_id, ticket_types_id, quantity, unit_price)
            VALUES (booking_id, type_id, ticket_quantity, unit_price);
            IF j % 3 = 0 THEN
                type_id := type_ids[(i - 1) * 3 + 1];
                INSERT INTO booking_items (bookings_id, ticket_types_id, quantity, unit_price)
                SELECT booking_id, id, 1, price FROM ticket_types WHERE id = type_id;
            END IF;
            SELECT sum(quantity * bi.unit_price) INTO total FROM booking_items bi WHERE bookings_id = booking_id;
            UPDATE bookings SET total_amount = total WHERE id = booking_id;

            payment_method := CASE WHEN total = 0 THEN 'FREE' ELSE 'VNPAY' END;
            INSERT INTO payments (bookings_id, transaction_id, payment_method, amount, status,
                                  paid_at, created_at, gateway_transaction_id, response_code)
            VALUES (booking_id, 'SEED-PAY-' || booking_id, payment_method, total, 'SUCCESS',
                    booked_at + INTERVAL '2 minutes', booked_at,
                    CASE WHEN total > 0 THEN 'SEED-VNPAY-' || booking_id END,
                    CASE WHEN total > 0 THEN '00' END) RETURNING id INTO payment_id;
            fee := round(total * 0.05, 0);
            INSERT INTO payment_finances (payments_id, gross_amount, platform_fee, organizer_amount,
                                          credited, settled, reversed, created_at)
            VALUES (payment_id, total, fee, total - fee, TRUE, i IN (6, 7), FALSE, booked_at + INTERVAL '2 minutes');

            checkin_staff_id := staff_ids[1 + (i - 1) % 4];
            ticket_status := CASE WHEN i = 10 THEN 'CANCELED'
                                  WHEN i IN (6, 7) OR (i = 5 AND j <= 3) THEN 'USED' ELSE 'VALID' END;
            checked_at := CASE WHEN ticket_status = 'USED' THEN event_row.start_time + (j * 3) * INTERVAL '1 minute' END;
            FOR item_row IN SELECT id, quantity FROM booking_items WHERE bookings_id = booking_id ORDER BY id LOOP
                FOR k IN 1..item_row.quantity LOOP
                    INSERT INTO tickets (booking_items_id, ticket_code, qr_token, status, checked_in_by, checked_in_at, created_at)
                    VALUES (item_row.id, 'EH-SEED-' || item_row.id || '-' || k,
                            md5('eventhub-demo-qr-' || item_row.id || '-' || k), ticket_status,
                            CASE WHEN ticket_status = 'USED' THEN checkin_staff_id END, checked_at,
                            booked_at + INTERVAL '2 minutes');
                END LOOP;
            END LOOP;

            INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
            VALUES (user_id, 'Đặt vé thành công', 'Đơn #' || booking_id || ' cho sự kiện ' || event_row.name || ' đã được xác nhận.',
                    'BOOKING_CONFIRMED', j % 2 = 0, booked_at + INTERVAL '2 minutes');
            INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
            VALUES (event_row.organizer_id, 'Có booking được xác nhận', 'Đơn #' || booking_id || ' đã được xác nhận.',
                    'BOOKING_CONFIRMED', j % 2 = 0, booked_at + INTERVAL '2 minutes');

            IF i = 10 THEN
                refund_status := CASE j WHEN 1 THEN 'WAITING' WHEN 2 THEN 'COMPLETED'
                                        WHEN 3 THEN 'COMPLETED' WHEN 4 THEN 'FAILED'
                                        WHEN 5 THEN 'MANUAL_PENDING' ELSE 'WAITING' END;
                refund_method := CASE WHEN j IN (2, 4) THEN 'VNPAY' WHEN j IN (3, 5) THEN 'MANUAL' END;
                INSERT INTO refunds (payments_id, refund_transaction_id, amount, status, retry_count,
                                     method, requested_by, provider_reference, response_code, transaction_status,
                                     last_error, refunded_at, created_at, updated_at)
                VALUES (payment_id, 'refund-' || payment_id, total, refund_status,
                        CASE WHEN j IN (2, 4) THEN 1 ELSE 0 END, refund_method,
                        CASE WHEN refund_method IS NOT NULL THEN admin_id END,
                        CASE WHEN j = 2 THEN 'SEED-REFUND-VNPAY-' || payment_id
                             WHEN j = 3 THEN 'SEED-REFUND-MANUAL-' || payment_id END,
                        CASE WHEN j = 2 THEN '00' WHEN j = 4 THEN '91' END,
                        CASE WHEN j = 2 THEN '00' END,
                        CASE WHEN j = 4 THEN 'Dữ liệu demo: cổng thanh toán từ chối yêu cầu hoàn tiền.' END,
                        CASE WHEN refund_status = 'COMPLETED' THEN seed_now - INTERVAL '12 hours' END,
                        seed_now - INTERVAL '1 day', seed_now - INTERVAL '12 hours') RETURNING id INTO refund_id;
                IF refund_status = 'COMPLETED' THEN
                    UPDATE payment_finances SET reversed = TRUE WHERE payments_id = payment_id;
                    INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
                    VALUES (user_id, 'Hoàn tiền thành công', 'Đã hoàn toàn bộ giao dịch #' || payment_id || '.',
                            'REFUND_COMPLETED', FALSE, seed_now - INTERVAL '12 hours');
                END IF;
                IF j IN (2, 4) THEN
                    INSERT INTO refund_attempts (refunds_id, request_id, command, outcome, response_code,
                                                 transaction_status, created_at, completed_at)
                    VALUES (refund_id, md5('seed-refund-request-' || payment_id), 'refund', refund_status,
                            CASE WHEN j = 2 THEN '00' ELSE '91' END, CASE WHEN j = 2 THEN '00' END,
                            seed_now - INTERVAL '13 hours', seed_now - INTERVAL '12 hours');
                END IF;
                IF j IN (3, 4, 5) THEN
                    INSERT INTO refund_bank_accounts (payment_id, bank_code, account_number, account_holder_name,
                                                       verified_by, verified_at, created_at, updated_at)
                    VALUES (payment_id, 'NCB', '0000' || lpad(user_id::TEXT, 8, '0'),
                            'KHACH HANG DEMO ' || user_id, admin_id, seed_now - INTERVAL '14 hours',
                            seed_now - INTERVAL '14 hours', seed_now - INTERVAL '14 hours');
                END IF;
                IF j = 3 THEN
                    -- Valid one-pixel PNG fixture for the demo evidence download endpoint.
                    INSERT INTO refund_evidence (refunds_id, transaction_code, content_type, content, recorded_by, recorded_at)
                    VALUES (refund_id, 'SEED-REFUND-MANUAL-' || payment_id, 'image/png',
                            decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGNgaGD4DwAChAGA+gVWHQAAAABJRU5ErkJggg==', 'base64'),
                            admin_id, seed_now - INTERVAL '12 hours');
                END IF;
                INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
                VALUES (user_id, 'Sự kiện đã hủy', 'Sự kiện ' || event_row.name || ' đã hủy. Vui lòng theo dõi thông tin hoàn tiền.',
                        'EVENT_CANCELED', FALSE, seed_now - INTERVAL '1 day');
            END IF;
        END LOOP;
    END LOOP;

    -- Nine extra checkout histories for the three paid upcoming events.
    -- PENDING holds one seat; EXPIRED/FAILED have released their seats and issue no tickets.
    FOR i IN 1..3 LOOP
        FOR j IN 1..3 LOOP
            booked_at := CASE WHEN j = 1 THEN seed_now ELSE seed_now - j * INTERVAL '1 day' END;
            type_id := type_ids[(i - 1) * 3 + 1];
            SELECT price INTO unit_price FROM ticket_types WHERE id = type_id;
            INSERT INTO bookings (users_id, events_id, total_amount, status, expired_at, created_at, updated_at)
            VALUES (customer_ids[7 + j], event_ids[i], unit_price,
                    CASE WHEN j = 1 THEN 'PENDING' ELSE 'EXPIRED' END,
                    booked_at + INTERVAL '5 minutes', booked_at,
                    CASE WHEN j = 1 THEN booked_at ELSE booked_at + INTERVAL '8 minutes' END)
            RETURNING id INTO booking_id;
            INSERT INTO booking_items (bookings_id, ticket_types_id, quantity, unit_price)
            VALUES (booking_id, type_id, 1, unit_price);
            INSERT INTO payments (bookings_id, transaction_id, payment_method, amount, status, created_at, response_code)
            VALUES (booking_id, 'SEED-PAY-' || booking_id, 'VNPAY', unit_price,
                    CASE j WHEN 1 THEN 'PENDING' WHEN 2 THEN 'EXPIRED' ELSE 'FAILED' END,
                    booked_at, CASE WHEN j = 3 THEN '24' END);
        END LOOP;
    END LOOP;

    -- Late successful payment: no issued ticket, no wallet credit, waiting for refund.
    type_id := type_ids[1];
    SELECT price INTO unit_price FROM ticket_types WHERE id = type_id;
    booked_at := seed_now - INTERVAL '2 days';
    INSERT INTO bookings (users_id, events_id, total_amount, status, expired_at, created_at, updated_at)
    VALUES (customer_ids[10], event_ids[1], unit_price, 'PAYMENT_REVIEW',
            booked_at + INTERVAL '5 minutes', booked_at, booked_at + INTERVAL '10 minutes') RETURNING id INTO booking_id;
    INSERT INTO booking_items (bookings_id, ticket_types_id, quantity, unit_price) VALUES (booking_id, type_id, 1, unit_price);
    INSERT INTO payments (bookings_id, transaction_id, payment_method, amount, status, paid_at, created_at, gateway_transaction_id, response_code)
    VALUES (booking_id, 'SEED-PAY-' || booking_id, 'VNPAY', unit_price, 'SUCCESS', booked_at + INTERVAL '8 minutes',
            booked_at, 'SEED-VNPAY-' || booking_id, '00') RETURNING id INTO payment_id;
    fee := round(unit_price * 0.05, 0);
    INSERT INTO payment_finances (payments_id, gross_amount, platform_fee, organizer_amount, credited, settled, reversed, created_at)
    VALUES (payment_id, unit_price, fee, unit_price - fee, FALSE, FALSE, FALSE, booked_at + INTERVAL '10 minutes');
    INSERT INTO refunds (payments_id, refund_transaction_id, amount, status, created_at, updated_at)
    VALUES (payment_id, 'refund-' || payment_id, unit_price, 'WAITING', booked_at + INTERVAL '10 minutes', booked_at + INTERVAL '10 minutes');

    -- Inventory is derived from actual items, not independent hard-coded counters.
    -- Canceled tickets stay sold, matching EventService.cancelEvent (no resale).
    UPDATE ticket_types tt SET
        reserved_quantity = (SELECT coalesce(sum(bi.quantity), 0) FROM booking_items bi
                             JOIN bookings b ON b.id = bi.bookings_id WHERE bi.ticket_types_id = tt.id AND b.status = 'PENDING'),
        remaining_quantity = tt.quantity - (SELECT coalesce(sum(bi.quantity), 0) FROM booking_items bi
                             JOIN bookings b ON b.id = bi.bookings_id WHERE bi.ticket_types_id = tt.id AND b.status IN ('PENDING', 'PAID'))
    WHERE tt.id = ANY(type_ids);

    -- Wallet balances follow the exact same 5% fee and settlement flags as the app.
    FOR i IN 1..3 LOOP
        INSERT INTO organizer_wallets (organizer_id, pending_balance, available_balance, created_at, updated_at)
        SELECT organizer_ids[i],
               coalesce(sum(f.organizer_amount) FILTER (WHERE f.credited AND NOT f.settled AND NOT f.reversed), 0),
               coalesce(sum(f.organizer_amount) FILTER (WHERE f.credited AND f.settled AND NOT f.reversed), 0),
               seed_now - INTERVAL '60 days', seed_now
        FROM payment_finances f JOIN payments p ON p.id = f.payments_id
        JOIN bookings b ON b.id = p.bookings_id JOIN events e ON e.id = b.events_id
        WHERE e.organizer_id = organizer_ids[i] RETURNING id INTO wallet_id;
        wallet_ids := array_append(wallet_ids, wallet_id);
    END LOOP;

    -- Six withdrawal requests, funded by the two completed events; two payouts.
    FOR i IN 1..2 LOOP
        FOR j IN 1..3 LOOP
            total := CASE j WHEN 1 THEN 300000 WHEN 2 THEN 200000 ELSE 100000 END;
            INSERT INTO withdrawal_requests (organizer_wallets_id, processed_by, amount, bank_name, account_number,
                                              status, requested_at, processed_at, reject_reason, transaction_code, created_at, updated_at)
            VALUES (wallet_ids[i], CASE WHEN j <> 2 THEN admin_id END, total, 'NCB', '00009900' || lpad(i::TEXT, 4, '0'),
                    CASE j WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'PENDING' ELSE 'REJECTED' END,
                    seed_now - INTERVAL '3 days', CASE WHEN j <> 2 THEN seed_now - INTERVAL '2 days' END,
                    CASE WHEN j = 3 THEN 'Thông tin tài khoản cần được xác minh lại.' END,
                    CASE WHEN j = 1 THEN 'SEED-PAYOUT-' || wallet_ids[i] END,
                    seed_now - INTERVAL '3 days', CASE WHEN j = 2 THEN seed_now - INTERVAL '3 days' ELSE seed_now - INTERVAL '2 days' END)
            RETURNING id INTO withdrawal_id;
            IF j = 1 THEN
                UPDATE organizer_wallets SET available_balance = available_balance - total,
                    total_withdrawn = total_withdrawn + total WHERE id = wallet_ids[i];
                INSERT INTO payout_transactions (withdrawal_requests_id, amount, transaction_code, status,
                                                  initiated_at, completed_at, created_at, updated_at)
                VALUES (withdrawal_id, total, 'SEED-PAYOUT-' || wallet_ids[i], 'COMPLETED', seed_now - INTERVAL '2 days',
                        seed_now - INTERVAL '2 days', seed_now - INTERVAL '2 days', seed_now - INTERVAL '2 days');
            ELSIF j = 2 THEN
                UPDATE organizer_wallets SET available_balance = available_balance - total,
                    withdrawal_pending_balance = withdrawal_pending_balance + total WHERE id = wallet_ids[i];
            END IF;
            IF j <> 2 THEN
                INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
                VALUES (organizer_ids[i], CASE WHEN j = 1 THEN 'Yêu cầu rút tiền đã được duyệt' ELSE 'Yêu cầu rút tiền bị từ chối' END,
                        'Kết quả xử lý yêu cầu rút tiền #' || withdrawal_id || '.',
                        CASE WHEN j = 1 THEN 'WITHDRAWAL_APPROVED' ELSE 'WITHDRAWAL_REJECTED' END, FALSE, seed_now - INTERVAL '2 days');
            END IF;
        END LOOP;
    END LOOP;

    -- 100 interactions spanning every customer and every demo event.
    FOR i IN 1..10 LOOP
        FOR j IN 1..10 LOOP
            INSERT INTO user_event_interactions (users_id, events_id, interaction_type, created_at)
            VALUES (customer_ids[i], event_ids[j], CASE WHEN (i + j) % 4 = 0 THEN 'FAVORITE' ELSE 'VIEW' END,
                    seed_now - (i + j) * INTERVAL '1 hour');
        END LOOP;
    END LOOP;

    -- Five short conversations, four messages each.
    FOR i IN 1..5 LOOP
        booked_at := seed_now - i * INTERVAL '1 day';
        INSERT INTO ai_conversations (users_id, title, created_at, updated_at)
        VALUES (customer_ids[i], 'Gợi ý sự kiện cuối tuần ' || i, booked_at, booked_at + INTERVAL '3 minutes')
        RETURNING id INTO conversation_id;
        INSERT INTO ai_messages (ai_conversations_id, role, content, created_at) VALUES
            (conversation_id, 'user', 'Có sự kiện nào phù hợp để đi cùng bạn bè không?', booked_at),
            (conversation_id, 'assistant', 'Bạn có thể tham khảo các đêm nhạc, hội thảo hoặc ngày hội cộng đồng đang mở bán trên EventHub.', booked_at + INTERVAL '1 minute'),
            (conversation_id, 'user', 'Tôi muốn xem thêm hoạt động miễn phí.', booked_at + INTERVAL '2 minutes'),
            (conversation_id, 'assistant', 'Ngày hội Sách và Sống xanh có vé miễn phí. Bạn hãy mở chi tiết sự kiện để xem thời gian và địa điểm.', booked_at + INTERVAL '3 minutes');
    END LOOP;

    INSERT INTO notifications (users_id, title, content, type, is_read, created_at) VALUES
        (admin_id, 'Có sự kiện mới chờ duyệt', 'Jazz Night - Giai điệu mùa thu đang chờ kiểm tra nội dung.', 'EVENT_PENDING_APPROVAL', FALSE, seed_now - INTERVAL '1 day'),
        (admin_id, 'Sự kiện đã hủy, cần xử lý hoàn tiền', 'Art & Light - Không gian sắc màu có hồ sơ hoàn tiền cần theo dõi.', 'EVENT_CANCELED', FALSE, seed_now - INTERVAL '1 day'),
        (organizer_ids[1], 'Sự kiện đang chờ duyệt', 'Jazz Night - Giai điệu mùa thu đã được gửi duyệt.', 'EVENT_PENDING_APPROVAL', FALSE, seed_now - INTERVAL '1 day'),
        (organizer_ids[3], 'Sự kiện đã hủy', 'Art & Light - Không gian sắc màu đã hủy và ngừng bán vé.', 'EVENT_CANCELED', FALSE, seed_now - INTERVAL '1 day');

    RAISE NOTICE 'EventHub demo inserted: 18 users, 10 events, 30 ticket types, 58 bookings, 112 tickets, 7 refunds.';
END;
$seed$;

COMMIT;
