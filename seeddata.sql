-- EventHub local demo, PostgreSQL + Flyway V16. The single DO statement is atomic.
-- Run against the migrated DB with psql -v ON_ERROR_STOP=1 -f seeddata.sql.
-- Demo password: EventHub@123. The four entry points are demo20.admin@example.test,
-- demo20.organizer1@example.test, demo20.staff1@example.test and demo20.customer1@example.test.
-- All dates are relative to first import (UTC). Existing application records are retained.

DO $seed$
DECLARE
    seed_now TIMESTAMP := date_trunc('second', CURRENT_TIMESTAMP AT TIME ZONE 'UTC');
    demo_password TEXT := '$2a$12$hweuGtSsNY/7tUFke/kHwuThHy/H/5qu9mC8o6U5Ds6gqFui..J9.';
    -- Unsplash photo IDs, ordered to match the event catalog below.
    photo_ids TEXT[] := ARRAY[
        '1470229722913-7c0e2dbbafd3', '1540575467063-178a50c2df87', '1511795409834-ef04bbd61622',
        '1507842217343-583bb7270b66', '1552674605-db6ffd4facb5', '1516280440614-37939bbacd81',
        '1519389950473-47ba0277781c', '1540039155733-5bb30b53aa14', '1414235077428-338989a2e8c0',
        '1513364776144-60967b0f800f', '1518770660439-4636190af475', '1500530855697-b586d89ba3ee',
        '1524758631624-e2822e304c36', '1500534623283-312aade485b7', '1455390582262-044cdead277a',
        '1472396961693-142e6e269027', '1519681393784-d120267933ba', '1500534314209-a25ddb2bd429',
        '1506157786151-b8491531f063', '1540039155733-5bb30b53aa14'];
    portraits TEXT[] := ARRAY['1500648767791-00dcc994a43e', '1534528741775-53994a69daeb',
                              '1506794778202-cad84cf45f1d', '1524504388940-b1c1722653e1'];
    photo_base CONSTANT TEXT := 'https://images.unsplash.com/photo-';
    event_photo TEXT;
    recommended INTEGER[];
    -- A compact, self-contained seating plan for the concert venues (VARCHAR(255)).
    seating_plan CONSTANT TEXT := 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 180 90"><path fill="ivory" d="M0 0h180v90H0z"/><g text-anchor="middle"><text x="90" y="20">SAN KHAU</text><text x="90" y="48">VIP</text><text x="90" y="76">GA</text></g></svg>';
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
BEGIN
    -- Serialize concurrent runs of this same demo seed.
    PERFORM pg_advisory_xact_lock(20261023, 810);

    IF EXISTS (SELECT FROM users WHERE email = 'demo20.admin@example.test' AND role = 'ADMIN') THEN
        RAISE NOTICE 'EventHub demo20 already imported.';
        RETURN;
    END IF;

    -- 20 accounts, including one locked customer for account administration.
    FOR row_data IN SELECT * FROM (VALUES
        (1, 'demo20.admin@example.test', 'Quản trị EventHub Demo', 'ADMIN'),
        (2, 'demo20.organizer1@example.test', 'Nguyễn Minh Anh', 'ORGANIZER'),
        (3, 'demo20.organizer2@example.test', 'Trần Hoàng Nam', 'ORGANIZER'),
        (4, 'demo20.organizer3@example.test', 'Lê Thảo Vy', 'ORGANIZER'),
        (5, 'demo20.staff1@example.test', 'Phạm Quốc Huy', 'STAFF'),
        (6, 'demo20.staff2@example.test', 'Võ Ngọc Mai', 'STAFF'),
        (7, 'demo20.staff3@example.test', 'Đặng Tuấn Kiệt', 'STAFF'),
        (8, 'demo20.staff4@example.test', 'Bùi Khánh Linh', 'STAFF'),
        (9, 'demo20.customer1@example.test', 'Nguyễn Gia Bảo', 'CUSTOMER'),
        (10, 'demo20.customer2@example.test', 'Trần Minh Châu', 'CUSTOMER'),
        (11, 'demo20.customer3@example.test', 'Lê Đức Phúc', 'CUSTOMER'),
        (12, 'demo20.customer4@example.test', 'Phạm Hà My', 'CUSTOMER'),
        (13, 'demo20.customer5@example.test', 'Hoàng Thanh Tùng', 'CUSTOMER'),
        (14, 'demo20.customer6@example.test', 'Vũ Bảo Ngọc', 'CUSTOMER'),
        (15, 'demo20.customer7@example.test', 'Đỗ Anh Khoa', 'CUSTOMER'),
        (16, 'demo20.customer8@example.test', 'Phan Quỳnh Anh', 'CUSTOMER'),
        (17, 'demo20.customer9@example.test', 'Đặng Hải Đăng', 'CUSTOMER'),
        (18, 'demo20.customer10@example.test', 'Bùi Ngọc Hân', 'CUSTOMER'),
        (19, 'demo20.customer11@example.test', 'Ngô Thanh Hà', 'CUSTOMER'),
        (20, 'demo20.customer12@example.test', 'Lý Hoài Phong', 'CUSTOMER')
    ) AS accounts(n, email, full_name, role) ORDER BY n LOOP
        INSERT INTO users (email, password, full_name, phone, role, status, date_of_birth,
                           gender, avatar_url, city, preferred_categories, created_at, updated_at)
        VALUES (row_data.email, demo_password, row_data.full_name,
                '0908' || lpad(row_data.n::TEXT, 6, '0'), row_data.role, CASE WHEN row_data.n = 20 THEN 'LOCKED' ELSE 'ACTIVE' END,
                DATE '1990-01-01' + row_data.n * 137,
                CASE WHEN row_data.n % 2 = 0 THEN 'FEMALE' ELSE 'MALE' END,
                photo_base || portraits[1 + (row_data.n - 1) % 4] || '?auto=format&fit=crop&w=200&q=75',
                (ARRAY['Hồ Chí Minh','Hà Nội','Đà Nẵng'])[1 + (row_data.n - 1) % 3],
                (ARRAY['Âm nhạc, Nghệ thuật','Công nghệ, Kinh doanh','Cộng đồng, Thể thao'])[1 + (row_data.n - 1) % 3],
                seed_now - INTERVAL '90 days', seed_now - INTERVAL '1 day')
        RETURNING id INTO user_id;
        INSERT INTO user_ai_sync_logs (users_id, action_type, status, updated_at)
        VALUES (user_id, 'CREATE_USER', 'PENDING', seed_now);
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

    -- 20 events. Day offsets are relative to the UTC execution date.
    FOR row_data IN SELECT * FROM (VALUES
        (1, 1, 1, 1, 'Đêm nhạc Thành phố lên đèn', 'Đêm nhạc pop và acoustic cùng các nghệ sĩ trẻ, giao lưu khán giả và trải nghiệm sân khấu trực tiếp.', 'APPROVED', 14, 12, 4, 350000),
        (2, 2, 2, 2, 'Vietnam AI & Developer Day', 'Hội thảo AI ứng dụng, phát triển phần mềm và kết nối cộng đồng lập trình viên.', 'APPROVED', 21, 2, 8, 250000),
        (3, 2, 3, 3, 'Startup Connect Việt Nam', 'Kết nối nhà sáng lập, doanh nghiệp và nhà đầu tư qua tọa đàm và hoạt động networking.', 'APPROVED', 28, 2, 7, 300000),
        (4, 3, 6, 4, 'Ngày hội Sách và Sống xanh', 'Ngày hội miễn phí với đổi sách, tái chế và các hoạt động cộng đồng dành cho gia đình.', 'APPROVED', 10, 1, 8, 0),
        (5, 3, 4, 4, 'Run Together - Chạy vì cộng đồng', 'Hoạt động chạy bộ cộng đồng, tiếp sức và giao lưu với các câu lạc bộ thể thao.', 'ONGOING', 0, 0, 4, 150000),
        (6, 1, 1, 6, 'Acoustic Cuối tuần bên nhau', 'Một đêm nhạc mộc với guitar, cajón và những ca khúc do khán giả yêu cầu.', 'COMPLETED', -14, 12, 4, 200000),
        (7, 2, 2, 5, 'Workshop Spring Boot thực chiến', 'Workshop xây dựng API, thiết kế dữ liệu và chia sẻ kinh nghiệm triển khai ứng dụng.', 'COMPLETED', -7, 2, 7, 400000),
        (8, 1, 1, 1, 'Jazz Night - Giai điệu mùa thu', 'Đêm nhạc jazz đang chờ quản trị viên kiểm tra và duyệt nội dung.', 'PENDING_APPROVAL', 35, 12, 3, 280000),
        (9, 3, 6, 3, 'Ngày hội Ẩm thực và Văn hóa', 'Bản đề xuất cần bổ sung thông tin tổ chức trước khi gửi duyệt lại.', 'REJECTED', 40, 2, 8, 100000),
        (10, 3, 5, 6, 'Art & Light - Không gian sắc màu', 'Triển lãm ánh sáng và tranh đa chất liệu. Chương trình đã hủy do địa điểm bảo trì.', 'CANCELED', 30, 3, 6, 180000),
        (11, 2, 2, 2, 'Ngày hội Robot học đường', 'Lắp ráp cảm biến và lập trình robot bám đường theo nhóm.', 'APPROVED', 18, 2, 7, 160000),
        (12, 3, 5, 4, 'Dạo phố cùng máy ảnh', 'Thực hành bố cục và ánh sáng tự nhiên trong buổi đi bộ chụp ảnh.', 'APPROVED', 9, 1, 3, 120000),
        (13, 2, 3, 5, 'Chợ phiên Thiết kế Việt', 'Gặp gỡ các xưởng thiết kế nhỏ và chia sẻ cách xây dựng thương hiệu.', 'APPROVED', 23, 2, 6, 80000),
        (14, 3, 4, 4, 'Yoga dưới tán cây', 'Buổi tập thở và giãn cơ ngoài trời dành cho người mới.', 'APPROVED', 5, 0, 2, 0),
        (15, 1, 5, 5, 'Xưởng viết Những lá thư', 'Viết tản văn và đọc bản thảo cùng nhóm tác giả trẻ.', 'APPROVED', 16, 6, 3, 90000),
        (16, 3, 6, 6, 'Nhật ký thiên nhiên', 'Nhận diện cây bản địa và vẽ sổ tay thực địa cùng tình nguyện viên.', 'COMPLETED', -12, 1, 4, 120000),
        (17, 2, 2, 3, 'Đêm quan sát bầu trời', 'Nhận diện chòm sao và thực hành điều chỉnh kính thiên văn.', 'APPROVED', 32, 12, 3, 110000),
        (18, 3, 4, 6, 'Đi bộ ven sông Hàn', 'Hành trình đi bộ theo nhóm và thử thách ghi lại cảnh quan ven sông.', 'APPROVED', 12, 0, 3, 70000),
        (19, 1, 1, 1, 'Sân khấu Ban nhạc trẻ', 'Các ban nhạc sinh viên trình diễn những sáng tác đầu tay.', 'APPROVED', 27, 11, 4, 210000),
        (20, 1, 1, 6, 'Đêm diễn Giao mùa', 'Đêm nhạc kết hợp nhạc cụ dân tộc và âm thanh điện tử.', 'PENDING_CANCELLATION', 22, 12, 3, 240000)
    ) AS events_seed(n, organizer_no, category_no, venue_no, name, description, status, day_offset, start_hour, duration_hours, price)
    ORDER BY n LOOP
        i := row_data.n;
        event_photo := photo_base || photo_ids[i];
        starts_at := date_trunc('day', seed_now) + row_data.day_offset * INTERVAL '1 day'
                     + row_data.start_hour * INTERVAL '1 hour';
        IF row_data.status = 'ONGOING' THEN starts_at := seed_now - INTERVAL '1 hour'; END IF;
        ends_at := starts_at + row_data.duration_hours * INTERVAL '1 hour';
        INSERT INTO events (version, organizer_id, reviewed_by, venues_id, categories_id, name, description,
                            thumbnail_image_url, banner_image_url, image_zone_url, start_time, end_time,
                            status, cancel_reason, canceled_at, reject_reason, reviewed_at, created_at, updated_at)
        VALUES (0, organizer_ids[row_data.organizer_no],
                CASE WHEN row_data.status = 'PENDING_APPROVAL' THEN NULL ELSE admin_id END,
                venue_ids[row_data.venue_no], category_ids[row_data.category_no], row_data.name, row_data.description,
                event_photo || '?auto=format&fit=crop&w=800&q=75',
                event_photo || '?auto=format&fit=crop&w=1600&q=80',
                CASE WHEN row_data.category_no = 1 THEN seating_plan END, starts_at, ends_at, row_data.status,
                CASE WHEN row_data.status IN ('CANCELED','PENDING_CANCELLATION') THEN 'Địa điểm bảo trì, ban tổ chức không thể triển khai đúng kế hoạch.' END,
                CASE WHEN row_data.status = 'CANCELED' THEN seed_now - INTERVAL '1 day' END,
                CASE WHEN row_data.status = 'REJECTED' THEN 'Cần bổ sung phương án an toàn và xác nhận địa điểm.' END,
                CASE WHEN row_data.status <> 'PENDING_APPROVAL' THEN seed_now - INTERVAL '55 days' END,
                seed_now - INTERVAL '60 days',
                CASE WHEN row_data.status IN ('CANCELED', 'REJECTED') THEN seed_now - INTERVAL '1 day'
                     ELSE seed_now - INTERVAL '55 days' END)
        RETURNING id INTO event_id;
        event_ids := array_append(event_ids, event_id);

        -- Event 19 is a small, fully booked show. All allocations fit the venues.
        FOR j IN 1..3 LOOP
            unit_price := row_data.price * CASE j WHEN 1 THEN 1.0 WHEN 2 THEN 2.0 ELSE 0.8 END;
            ticket_quantity := CASE WHEN i = 19 THEN CASE j WHEN 1 THEN 4 WHEN 2 THEN 4 ELSE 6 END
                                    ELSE CASE j WHEN 1 THEN 120 WHEN 2 THEN 60 ELSE 80 END END;
            INSERT INTO ticket_types (events_id, name, description, image_url, price, quantity,
                                      reserved_quantity, remaining_quantity, sale_start_time, sale_end_time, status)
            VALUES (event_id, CASE j WHEN 1 THEN 'Tiêu chuẩn' WHEN 2 THEN 'VIP' ELSE 'Early Bird' END,
                    CASE j WHEN 1 THEN 'Vé vào cửa và tham gia các hoạt động chính.'
                           WHEN 2 THEN 'Khu vực ưu tiên và quà tặng từ ban tổ chức.'
                           ELSE 'Ưu đãi dành cho khách đăng ký sớm.' END,
                    event_photo || '?auto=format&fit=crop&w=800&q=75', unit_price, ticket_quantity,
                    0, ticket_quantity,
                    CASE WHEN i = 17 THEN seed_now + INTERVAL '3 days' ELSE seed_now - INTERVAL '50 days' END,
                    CASE WHEN i = 18 THEN seed_now - INTERVAL '1 day' ELSE starts_at - INTERVAL '2 hours' END,
                    CASE WHEN row_data.status IN ('APPROVED', 'ONGOING', 'COMPLETED') THEN 'ACTIVE' ELSE 'INACTIVE' END)
            RETURNING id INTO type_id;
            type_ids := array_append(type_ids, type_id);
        END LOOP;

        FOR j IN 1..2 LOOP
            INSERT INTO event_staffs (events_id, staff_id, assigned_by, status, created_at)
            VALUES (event_id, staff_ids[1 + (i + j - 2) % 4], organizer_ids[row_data.organizer_no],
                    CASE WHEN i = 9 AND j = 2 THEN 'LOCKED' ELSE 'ACTIVE' END, seed_now - INTERVAL '40 days');
            INSERT INTO event_guests (events_id, name, role, description, image_url, created_at, updated_at)
            VALUES (event_id,
                    (ARRAY['Nguyễn Hoàng Minh','Trần Ngọc Anh','Đào Mai Chi','Lưu Mạnh Dũng',
                           'Lê Khánh An','Đỗ Thùy Trang','Võ Nhật Quang','Lý Hà My',
                           'Phạm Nam Sơn','Ngô Cẩm Tú','Đinh Anh Vũ','Vũ Hoài Thương'])
                        [1 + (2 * (row_data.category_no - 1) + j - 1) % 12],
                    CASE WHEN j = 2 THEN 'Người dẫn chương trình' ELSE
                        (ARRAY['Nghệ sĩ','Kỹ sư','Nhà sáng lập','Huấn luyện viên','Tác giả','Điều phối viên'])[row_data.category_no] END,
                    CASE WHEN j = 2 THEN 'Dẫn dắt chương trình và phần hỏi đáp với khán giả.' ELSE
                        (ARRAY['Biểu diễn trực tiếp và giao lưu sau chương trình.',
                               'Hướng dẫn thực hành và giải đáp các câu hỏi kỹ thuật.',
                               'Chia sẻ kinh nghiệm đưa sản phẩm đầu tiên ra thị trường.',
                               'Phụ trách khởi động và hướng dẫn kỹ thuật cho người mới.',
                               'Giới thiệu tác phẩm và hướng dẫn buổi thực hành.',
                               'Kết nối các nhóm tình nguyện và phụ trách hoạt động trải nghiệm.'])[row_data.category_no] END,
                    photo_base || portraits[1 + (i + j - 2) % 4] || '?auto=format&fit=crop&w=200&q=75',
                    seed_now - INTERVAL '60 days', seed_now - INTERVAL '55 days');
        END LOOP;
    END LOOP;

    -- Six paid bookings per event with a sales history. Event 17 has yet to open.
    FOR i IN 1..cardinality(event_ids) LOOP
        IF i IN (8, 9, 17) THEN CONTINUE; END IF;
        SELECT * INTO STRICT event_row FROM events WHERE id = event_ids[i];
        FOR j IN 1..6 LOOP
            user_id := customer_ids[1 + (i + j - 2) % 11];
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
            VALUES (payment_id, total, fee, total - fee, TRUE, i IN (6, 7, 16), FALSE, booked_at + INTERVAL '2 minutes');

            checkin_staff_id := staff_ids[1 + (i - 1) % 4];
            ticket_status := CASE WHEN i = 10 THEN 'CANCELED'
                                  WHEN i IN (6, 7, 16) OR (i = 5 AND j <= 3) THEN 'USED' ELSE 'VALID' END;
            checked_at := CASE WHEN ticket_status = 'USED' THEN event_row.start_time + (j * 3) * INTERVAL '1 minute' END;
            FOR item_row IN SELECT id, quantity FROM booking_items WHERE bookings_id = booking_id ORDER BY id LOOP
                FOR k IN 1..item_row.quantity LOOP
                    INSERT INTO tickets (booking_items_id, ticket_code, qr_token, status, checked_in_by, checked_in_at, created_at)
                    VALUES (item_row.id, 'EH-SEED-' || item_row.id || '-' || k,
                            gen_random_uuid()::text, ticket_status,
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
                refund_status := CASE WHEN j IN (2,3) THEN 'COMPLETED' ELSE 'WAITING' END;
                INSERT INTO refunds (payments_id, refund_transaction_id, amount, status, method,
                                     requested_by, provider_reference, refunded_at, created_at, updated_at)
                VALUES (payment_id, 'refund-' || payment_id, total, refund_status,
                        CASE WHEN refund_status = 'COMPLETED' THEN 'INTERNAL' END,
                        CASE WHEN refund_status = 'COMPLETED' THEN admin_id END,
                        CASE WHEN refund_status = 'COMPLETED' THEN 'RF-' || replace(gen_random_uuid()::text, '-', '') END,
                        CASE WHEN refund_status = 'COMPLETED' THEN seed_now - INTERVAL '12 hours' END,
                        seed_now - INTERVAL '1 day',
                        seed_now - CASE WHEN refund_status = 'COMPLETED' THEN INTERVAL '12 hours' ELSE INTERVAL '1 day' END);
                IF refund_status = 'COMPLETED' THEN
                    UPDATE payment_finances SET reversed = TRUE WHERE payments_id = payment_id;
                    INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
                    VALUES (user_id, 'Hoàn tiền thành công', 'Đã hoàn toàn bộ giao dịch #' || payment_id || '.',
                            'REFUND_COMPLETED', FALSE, seed_now - INTERVAL '12 hours');
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
    INSERT INTO refunds (payments_id, refund_transaction_id, amount, status, retry_count, created_at, updated_at)
    VALUES (payment_id, 'refund-' || payment_id, unit_price, 'WAITING', 0, booked_at + INTERVAL '10 minutes', booked_at + INTERVAL '10 minutes');

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
        INSERT INTO organizer_wallets (organizer_id, pending_balance, available_balance, total_withdrawn, withdrawal_pending_balance, created_at, updated_at)
        SELECT organizer_ids[i],
               coalesce(sum(f.organizer_amount) FILTER (WHERE f.credited AND NOT f.settled AND NOT f.reversed), 0),
               coalesce(sum(f.organizer_amount) FILTER (WHERE f.credited AND f.settled AND NOT f.reversed), 0),
               0, 0,
               seed_now - INTERVAL '60 days', seed_now
        FROM payment_finances f JOIN payments p ON p.id = f.payments_id
        JOIN bookings b ON b.id = p.bookings_id JOIN events e ON e.id = b.events_id
        WHERE e.organizer_id = organizer_ids[i] RETURNING id INTO wallet_id;
        wallet_ids := array_append(wallet_ids, wallet_id);
    END LOOP;

    -- Approved, pending, rejected and withdrawn requests for the first two organizers.
    FOR i IN 1..2 LOOP
        FOR j IN 1..4 LOOP
            total := CASE j WHEN 1 THEN 300000 WHEN 2 THEN 200000 ELSE 100000 END;
            INSERT INTO withdrawal_requests (organizer_wallets_id, processed_by, amount, bank_name, account_number,
                                              status, requested_at, processed_at, reject_reason, transaction_code, created_at, updated_at)
            VALUES (wallet_ids[i], CASE WHEN j IN (1,3) THEN admin_id END, total, 'NCB', '00009900' || lpad(i::TEXT, 4, '0'),
                    CASE j WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'PENDING' WHEN 3 THEN 'REJECTED' ELSE 'CANCELLED' END,
                    seed_now - INTERVAL '3 days', CASE WHEN j IN (1,3) THEN seed_now - INTERVAL '2 days' END,
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
            IF j IN (1,3) THEN
                INSERT INTO notifications (users_id, title, content, type, is_read, created_at)
                VALUES (organizer_ids[i], CASE WHEN j = 1 THEN 'Yêu cầu rút tiền đã được duyệt' ELSE 'Yêu cầu rút tiền bị từ chối' END,
                        'Kết quả xử lý yêu cầu rút tiền #' || withdrawal_id || '.',
                        CASE WHEN j = 1 THEN 'WITHDRAWAL_APPROVED' ELSE 'WITHDRAWAL_REJECTED' END, FALSE, seed_now - INTERVAL '2 days');
            END IF;
        END LOOP;
    END LOOP;

    -- Browse and recommendation histories use the same IDs as the catalog and purchases.
    recommended := ARRAY[event_ids[1],event_ids[2],event_ids[3],event_ids[4],event_ids[11],event_ids[12]];
    FOR i IN 1..11 LOOP
        FOR j IN 1..6 LOOP
            INSERT INTO user_event_interactions (users_id, events_id, interaction_type, dwell_seconds, created_at)
            VALUES (customer_ids[i], recommended[j],
                    (ARRAY['VIEW','CLICK','SEARCH_CLICK'])[1 + (i+j) % 3], 8 + i*j, seed_now - (i+j)*INTERVAL '1 hour');
        END LOOP;
        j := 1 + (i-1) % 6;
        SELECT min(created_at) INTO booked_at FROM bookings
        WHERE users_id = customer_ids[i] AND events_id = recommended[j] AND status = 'PAID';
        starts_at := coalesce(booked_at, seed_now - i * INTERVAL '1 hour') - INTERVAL '5 minutes';
        INSERT INTO ai_recommendation_logs (users_id, recommended_ids, clicked_event_id, hit_rank, is_booked, created_at, clicked_at)
        VALUES (customer_ids[i], recommended, recommended[j], j, booked_at IS NOT NULL, starts_at, starts_at + INTERVAL '1 minute');
    END LOOP;
    INSERT INTO ai_recommendation_logs (recommended_ids, created_at) VALUES (recommended, seed_now);
    INSERT INTO user_event_interactions (users_id, events_id, interaction_type, dwell_seconds, created_at)
    SELECT users_id, events_id, 'BOOK', 0, updated_at FROM bookings WHERE events_id = ANY(event_ids) AND status='PAID';

    FOR row_data IN SELECT * FROM (VALUES
        (customer_ids[1], 'CUSTOMER', 'Đi đâu cùng bạn bè?', 'Có hoạt động nào miễn phí không?',
         'Ngày hội Sách và Sống xanh có vé miễn phí. Bạn có thể xem lịch và chọn số lượng vé trong trang sự kiện.'),
        (organizer_ids[1], 'ORGANIZER', 'Theo dõi doanh thu', 'Xem tiền có thể rút ở đâu?',
         'Trong Ví nhà tổ chức, mục Số dư khả dụng hiển thị khoản bạn có thể yêu cầu rút sau khi sự kiện kết thúc.'),
        (staff_ids[1], 'STAFF', 'Check-in tại cổng', 'Khách đưa vé điện tử thì tôi làm gì?',
         'Mở sự kiện được phân công, quét QR hoặc nhập mã vé, rồi kiểm tra kết quả xác nhận.'),
        (admin_id, 'ADMIN', 'Duyệt hoàn tiền', 'Hồ sơ nào cần được duyệt hoàn?',
         'Vào phần quản lý hoàn tiền, chọn sự kiện đã hủy và xem các giao dịch đang chờ xử lý.')
    ) AS chat(owner_id, owner_role, title, question, answer) LOOP
        booked_at := seed_now - INTERVAL '2 hours';
        INSERT INTO ai_conversations (users_id, owner_role, title, created_at, updated_at)
        VALUES (row_data.owner_id, row_data.owner_role, row_data.title, booked_at, booked_at + INTERVAL '1 minute')
        RETURNING id INTO conversation_id;
        INSERT INTO ai_messages (ai_conversations_id, role, content, created_at) VALUES
            (conversation_id, 'user', row_data.question, booked_at),
            (conversation_id, 'assistant', row_data.answer, booked_at + INTERVAL '1 minute');
    END LOOP;

    INSERT INTO notifications (users_id, title, content, type, is_read, created_at) VALUES
        (admin_id, 'Có sự kiện mới chờ duyệt', 'Jazz Night - Giai điệu mùa thu đang chờ kiểm tra nội dung.', 'EVENT_PENDING_APPROVAL', FALSE, seed_now - INTERVAL '1 day'),
        (admin_id, 'Sự kiện đã hủy, cần xử lý hoàn tiền', 'Art & Light - Không gian sắc màu có hồ sơ hoàn tiền cần theo dõi.', 'EVENT_CANCELED', FALSE, seed_now - INTERVAL '1 day'),
        (organizer_ids[1], 'Sự kiện đang chờ duyệt', 'Jazz Night - Giai điệu mùa thu đã được gửi duyệt.', 'EVENT_PENDING_APPROVAL', FALSE, seed_now - INTERVAL '1 day'),
        (organizer_ids[3], 'Sự kiện đã hủy', 'Art & Light - Không gian sắc màu đã hủy và ngừng bán vé.', 'EVENT_CANCELED', FALSE, seed_now - INTERVAL '1 day');

    RAISE NOTICE 'EventHub demo20 imported (20 events, 20 accounts).';
END;
$seed$;
