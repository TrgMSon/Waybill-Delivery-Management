-- =====================================================================
-- DỮ LIỆU MẪU (DEMO) - toàn bộ là dữ liệu giả, chỉ nạp ở profile dev/demo
-- Tài khoản demo (mật khẩu chung: Demo@123)
--   admin@demo.tms                    (ADMIN)
--   driver1@demo.tms ... driver4@demo.tms (DRIVER)
-- File repeatable (R__): chạy sau mọi migration V..., và có thể chạy lại an toàn
-- (mỗi câu lệnh bỏ qua dữ liệu đã tồn tại).
-- =====================================================================

-- ========== Người dùng ==========
-- password_hash = BCrypt (cost 10) của "Demo@123"
INSERT INTO users (email, password_hash, full_name, phone, status, role)
VALUES
    ('admin@demo.tms',   '$2b$10$aAImJUkmcFxt1X8QBTgnSej3EOzAWGRoH.KsPqPs2rXKyeK4Ahz0e', 'Quản trị viên Demo', '0900000000', 'ACTIVE', 'ADMIN'),
    ('driver1@demo.tms', '$2b$10$aAImJUkmcFxt1X8QBTgnSej3EOzAWGRoH.KsPqPs2rXKyeK4Ahz0e', 'Nguyễn Văn An',     '0900000001', 'ACTIVE', 'DRIVER'),
    ('driver2@demo.tms', '$2b$10$aAImJUkmcFxt1X8QBTgnSej3EOzAWGRoH.KsPqPs2rXKyeK4Ahz0e', 'Trần Minh Khoa',    '0900000002', 'ACTIVE', 'DRIVER'),
    ('driver3@demo.tms', '$2b$10$aAImJUkmcFxt1X8QBTgnSej3EOzAWGRoH.KsPqPs2rXKyeK4Ahz0e', 'Lê Thị Hoa',        '0900000003', 'ACTIVE', 'DRIVER'),
    ('driver4@demo.tms', '$2b$10$aAImJUkmcFxt1X8QBTgnSej3EOzAWGRoH.KsPqPs2rXKyeK4Ahz0e', 'Phạm Quốc Bảo',     '0900000004', 'ACTIVE', 'DRIVER')
ON CONFLICT (email) DO NOTHING;

-- ========== Tài xế ==========
INSERT INTO drivers (user_id, vehicle_type, license_plate, service_area, max_load_kg, max_active_shipments, status)
SELECT u.id, v.vehicle_type, v.license_plate, v.service_area, v.max_load_kg, v.max_active_shipments, v.status
FROM (VALUES
    ('driver1@demo.tms', 'MOTORBIKE', '29B1-000.01', 'Cầu Giấy', 30.00,  10, 'BUSY'),
    ('driver2@demo.tms', 'MOTORBIKE', '29B1-000.02', 'Đống Đa',  30.00,  10, 'BUSY'),
    ('driver3@demo.tms', 'TRUCK',     '30F-000.03',  'Hà Đông',  500.00, 15, 'AVAILABLE'),
    ('driver4@demo.tms', 'MOTORBIKE', '29B1-000.04', 'Long Biên', 30.00,  5, 'OFFLINE')
) AS v(email, vehicle_type, license_plate, service_area, max_load_kg, max_active_shipments, status)
JOIN users u ON u.email = v.email
ON CONFLICT (user_id) DO NOTHING;

-- ========== Vận đơn ==========
-- Thời gian tính tương đối so với lúc nạp dữ liệu (now() - khoảng cách)
INSERT INTO shipments (tracking_code, sender_name, sender_phone, pickup_address,
                       receiver_name, receiver_phone, delivery_address,
                       weight_kg, cod_amount, shipping_fee, status,
                       current_driver_id, created_by, created_at, updated_at)
SELECT v.code, v.sender_name, v.sender_phone, v.pickup_address,
       v.receiver_name, v.receiver_phone, v.delivery_address,
       v.weight_kg, v.cod_amount, v.shipping_fee, v.status,
       d.id, adm.id, now() - v.created_ago, now() - v.updated_ago
FROM (VALUES
    ('TMS-DEMO-0001', 'Shop Hoa Sen Demo',         '0900100001', '12 Phố Demo, Cầu Giấy, Hà Nội',          'Nguyễn Thị Lan',  '0900200001', '35 Ngõ Mẫu, Thanh Xuân, Hà Nội',        2.5,  350000, 25000, 'CREATED',         NULL,               interval '3 hours',             interval '3 hours'),
    ('TMS-DEMO-0002', 'Nhà sách Demo',             '0900100002', '88 Phố Thử Nghiệm, Đống Đa, Hà Nội',     'Trần Văn Hùng',   '0900200002', '7 Đường Mẫu, Hai Bà Trưng, Hà Nội',     1.2,  0,      18000, 'CREATED',         NULL,               interval '1 hour',              interval '1 hour'),
    ('TMS-DEMO-0003', 'Cửa hàng Điện máy Demo',    '0900100003', '101 Đường Giả Định, Hà Đông, Hà Nội',    'Lê Hoàng Nam',    '0900200003', '22 Phố Ví Dụ, Cầu Giấy, Hà Nội',        12.0, 1800000, 60000, 'ASSIGNED',       'driver1@demo.tms', interval '5 hours',             interval '20 minutes'),
    ('TMS-DEMO-0004', 'Shop Hoa Sen Demo',         '0900100001', '12 Phố Demo, Cầu Giấy, Hà Nội',          'Phạm Thu Hà',     '0900200004', '15 Ngõ Thử, Đống Đa, Hà Nội',           3.0,  450000, 30000, 'ASSIGNED',        'driver2@demo.tms', interval '6 hours',             interval '5 hours'),
    ('TMS-DEMO-0005', 'Nhà sách Demo',             '0900100002', '88 Phố Thử Nghiệm, Đống Đa, Hà Nội',     'Đỗ Quang Vinh',   '0900200005', '9 Đường Mẫu, Cầu Giấy, Hà Nội',         1.8,  220000, 22000, 'PICKED_UP',       'driver1@demo.tms', interval '8 hours',             interval '2 hours'),
    ('TMS-DEMO-0006', 'Cửa hàng Gốm Demo',         '0900100004', '5 Phố Gốm, Long Biên, Hà Nội',           'Vũ Thị Hương',    '0900200006', '40 Phố Ví Dụ, Cầu Giấy, Hà Nội',        4.0,  0,      35000, 'IN_TRANSIT',      'driver1@demo.tms', interval '9 hours',             interval '1 hour'),
    ('TMS-DEMO-0007', 'Shop Hoa Sen Demo',         '0900100001', '12 Phố Demo, Cầu Giấy, Hà Nội',          'Trần Thị Mai',    '0900200007', '18 Ngõ Thử, Cầu Giấy, Hà Nội',          2.0,  500000, 28000, 'DELIVERED',       'driver1@demo.tms', interval '1 day 6 hours',       interval '1 day'),
    ('TMS-DEMO-0008', 'Cửa hàng Điện máy Demo',    '0900100003', '101 Đường Giả Định, Hà Đông, Hà Nội',    'Bùi Văn Long',    '0900200008', '27 Phố Mẫu, Đống Đa, Hà Nội',           8.0,  1200000, 55000, 'DELIVERED',      'driver2@demo.tms', interval '1 day 7 hours',       interval '1 day'),
    ('TMS-DEMO-0009', 'Nhà sách Demo',             '0900100002', '88 Phố Thử Nghiệm, Đống Đa, Hà Nội',     'Ngô Thanh Tùng',  '0900200009', '63 Đường Ví Dụ, Hà Đông, Hà Nội',       1.5,  320000, 20000, 'DELIVERED',       'driver3@demo.tms', interval '6 hours',             interval '30 minutes'),
    ('TMS-DEMO-0010', 'Cửa hàng Gốm Demo',         '0900100004', '5 Phố Gốm, Long Biên, Hà Nội',           'Hoàng Mỹ Linh',   '0900200010', '71 Phố Mẫu, Hà Đông, Hà Nội',           3.5,  150000, 30000, 'DELIVERY_FAILED', 'driver3@demo.tms', interval '7 hours',             interval '1 hour'),
    ('TMS-DEMO-0011', 'Cửa hàng Điện máy Demo',    '0900100003', '101 Đường Giả Định, Hà Đông, Hà Nội',    'Đặng Văn Phúc',   '0900200011', '4 Ngõ Thử, Hai Bà Trưng, Hà Nội',       5.0,  600000, 40000, 'RETURNING',       'driver2@demo.tms', interval '2 days 8 hours',      interval '1 day 20 hours'),
    ('TMS-DEMO-0012', 'Shop Hoa Sen Demo',         '0900100001', '12 Phố Demo, Cầu Giấy, Hà Nội',          'Lý Thị Ngọc',     '0900200012', '50 Phố Giả Định, Thanh Xuân, Hà Nội',   2.2,  280000, 25000, 'RETURNED',        'driver1@demo.tms', interval '4 days',              interval '3 days 12 hours'),
    ('TMS-DEMO-0013', 'Nhà sách Demo',             '0900100002', '88 Phố Thử Nghiệm, Đống Đa, Hà Nội',     'Mai Văn Thắng',   '0900200013', '31 Đường Mẫu, Long Biên, Hà Nội',       1.0,  90000,  18000, 'CANCELLED',       NULL,               interval '2 days',              interval '1 day 12 hours')
) AS v(code, sender_name, sender_phone, pickup_address,
       receiver_name, receiver_phone, delivery_address,
       weight_kg, cod_amount, shipping_fee, status,
       driver_email, created_ago, updated_ago)
CROSS JOIN (SELECT id FROM users WHERE email = 'admin@demo.tms') AS adm
LEFT JOIN users du   ON du.email = v.driver_email
LEFT JOIN drivers d  ON d.user_id = du.id
ON CONFLICT (tracking_code) DO NOTHING;

-- ========== Lịch sử trạng thái ==========
-- Chỉ chèn cho vận đơn chưa có lịch sử nào (tránh trùng khi chạy lại)
INSERT INTO shipment_status_history (shipment_id, from_status, to_status, changed_by, reason, created_at)
SELECT s.id, v.from_status, v.to_status, u.id, v.reason, now() - v.ago
FROM (VALUES
    ('TMS-DEMO-0001', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '3 hours'),

    ('TMS-DEMO-0002', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '1 hour'),

    -- 0003: bị tài xế 3 từ chối, rồi giao lại cho tài xế 1
    ('TMS-DEMO-0003', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '5 hours'),
    ('TMS-DEMO-0003', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '4 hours 30 minutes'),
    ('TMS-DEMO-0003', 'ASSIGNED',        'CREATED',         'driver3@demo.tms', 'Không đủ thời gian lấy hàng trong ngày', interval '3 hours'),
    ('TMS-DEMO-0003', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '20 minutes'),

    ('TMS-DEMO-0004', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '6 hours'),
    ('TMS-DEMO-0004', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '5 hours'),

    ('TMS-DEMO-0005', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '8 hours'),
    ('TMS-DEMO-0005', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '7 hours'),
    ('TMS-DEMO-0005', 'ASSIGNED',        'PICKED_UP',       'driver1@demo.tms', NULL, interval '2 hours'),

    ('TMS-DEMO-0006', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '9 hours'),
    ('TMS-DEMO-0006', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '8 hours'),
    ('TMS-DEMO-0006', 'ASSIGNED',        'PICKED_UP',       'driver1@demo.tms', NULL, interval '4 hours'),
    ('TMS-DEMO-0006', 'PICKED_UP',       'IN_TRANSIT',      'driver1@demo.tms', NULL, interval '1 hour'),

    ('TMS-DEMO-0007', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '1 day 6 hours'),
    ('TMS-DEMO-0007', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '1 day 5 hours'),
    ('TMS-DEMO-0007', 'ASSIGNED',        'PICKED_UP',       'driver1@demo.tms', NULL, interval '1 day 4 hours'),
    ('TMS-DEMO-0007', 'PICKED_UP',       'IN_TRANSIT',      'driver1@demo.tms', NULL, interval '1 day 3 hours'),
    ('TMS-DEMO-0007', 'IN_TRANSIT',      'DELIVERED',       'driver1@demo.tms', NULL, interval '1 day'),

    ('TMS-DEMO-0008', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '1 day 7 hours'),
    ('TMS-DEMO-0008', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '1 day 6 hours'),
    ('TMS-DEMO-0008', 'ASSIGNED',        'PICKED_UP',       'driver2@demo.tms', NULL, interval '1 day 5 hours'),
    ('TMS-DEMO-0008', 'PICKED_UP',       'IN_TRANSIT',      'driver2@demo.tms', NULL, interval '1 day 4 hours'),
    ('TMS-DEMO-0008', 'IN_TRANSIT',      'DELIVERED',       'driver2@demo.tms', NULL, interval '1 day'),

    ('TMS-DEMO-0009', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '6 hours'),
    ('TMS-DEMO-0009', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '5 hours'),
    ('TMS-DEMO-0009', 'ASSIGNED',        'PICKED_UP',       'driver3@demo.tms', NULL, interval '3 hours'),
    ('TMS-DEMO-0009', 'PICKED_UP',       'IN_TRANSIT',      'driver3@demo.tms', NULL, interval '2 hours'),
    ('TMS-DEMO-0009', 'IN_TRANSIT',      'DELIVERED',       'driver3@demo.tms', NULL, interval '30 minutes'),

    ('TMS-DEMO-0010', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '7 hours'),
    ('TMS-DEMO-0010', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '6 hours'),
    ('TMS-DEMO-0010', 'ASSIGNED',        'PICKED_UP',       'driver3@demo.tms', NULL, interval '4 hours'),
    ('TMS-DEMO-0010', 'PICKED_UP',       'IN_TRANSIT',      'driver3@demo.tms', NULL, interval '3 hours'),
    ('TMS-DEMO-0010', 'IN_TRANSIT',      'DELIVERY_FAILED', 'driver3@demo.tms', 'Người nhận không nghe máy', interval '1 hour'),

    ('TMS-DEMO-0011', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '2 days 8 hours'),
    ('TMS-DEMO-0011', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '2 days 7 hours'),
    ('TMS-DEMO-0011', 'ASSIGNED',        'PICKED_UP',       'driver2@demo.tms', NULL, interval '2 days 5 hours'),
    ('TMS-DEMO-0011', 'PICKED_UP',       'IN_TRANSIT',      'driver2@demo.tms', NULL, interval '2 days 4 hours'),
    ('TMS-DEMO-0011', 'IN_TRANSIT',      'DELIVERY_FAILED', 'driver2@demo.tms', 'Người nhận từ chối nhận hàng', interval '2 days'),
    ('TMS-DEMO-0011', 'DELIVERY_FAILED', 'RETURNING',       'driver2@demo.tms', NULL, interval '1 day 20 hours'),

    ('TMS-DEMO-0012', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '4 days'),
    ('TMS-DEMO-0012', 'CREATED',         'ASSIGNED',        'admin@demo.tms',   NULL, interval '3 days 23 hours'),
    ('TMS-DEMO-0012', 'ASSIGNED',        'PICKED_UP',       'driver1@demo.tms', NULL, interval '3 days 22 hours'),
    ('TMS-DEMO-0012', 'PICKED_UP',       'IN_TRANSIT',      'driver1@demo.tms', NULL, interval '3 days 21 hours'),
    ('TMS-DEMO-0012', 'IN_TRANSIT',      'DELIVERY_FAILED', 'driver1@demo.tms', 'Sai địa chỉ giao hàng', interval '3 days 18 hours'),
    ('TMS-DEMO-0012', 'DELIVERY_FAILED', 'RETURNING',       'driver1@demo.tms', NULL, interval '3 days 17 hours'),
    ('TMS-DEMO-0012', 'RETURNING',       'RETURNED',        'admin@demo.tms',   'Đã hoàn về cho người gửi', interval '3 days 12 hours'),

    ('TMS-DEMO-0013', NULL,              'CREATED',         'admin@demo.tms',   NULL, interval '2 days'),
    ('TMS-DEMO-0013', 'CREATED',         'CANCELLED',       'admin@demo.tms',   'Người gửi yêu cầu hủy đơn', interval '1 day 12 hours')
) AS v(code, from_status, to_status, actor_email, reason, ago)
JOIN shipments s ON s.tracking_code = v.code
JOIN users u     ON u.email = v.actor_email
WHERE NOT EXISTS (SELECT 1 FROM shipment_status_history h WHERE h.shipment_id = s.id);

-- ========== Gán đơn ==========
-- Mỗi vận đơn có tối đa 1 lượt ACCEPTED (khớp unique index ux_assignments_one_accepted)
-- expires_at = offered_at + ttl (ở đây ttl = 2 giờ, đủ cho mọi lượt đã phản hồi trong dữ liệu mẫu)
INSERT INTO assignments (shipment_id, driver_id, status, offered_at, expires_at, responded_at, reason)
SELECT s.id, d.id, v.status, now() - v.offered_ago, now() - v.offered_ago + v.ttl, now() - v.responded_ago, v.reason
FROM (VALUES
    ('TMS-DEMO-0003', 'driver3@demo.tms', 'REJECTED', interval '4 hours 30 minutes', interval '2 hours', interval '3 hours', 'Không đủ thời gian lấy hàng trong ngày'),
    ('TMS-DEMO-0003', 'driver1@demo.tms', 'OFFERED', interval '20 minutes', interval '2 hours', NULL, NULL),
    ('TMS-DEMO-0004', 'driver2@demo.tms', 'ACCEPTED', interval '5 hours', interval '2 hours', interval '4 hours 30 minutes', NULL),
    ('TMS-DEMO-0005', 'driver1@demo.tms', 'ACCEPTED', interval '7 hours', interval '2 hours', interval '6 hours 30 minutes', NULL),
    ('TMS-DEMO-0006', 'driver1@demo.tms', 'ACCEPTED', interval '8 hours', interval '2 hours', interval '7 hours 30 minutes', NULL),
    ('TMS-DEMO-0007', 'driver1@demo.tms', 'ACCEPTED', interval '1 day 5 hours', interval '2 hours', interval '1 day 4 hours 30 minutes', NULL),
    ('TMS-DEMO-0008', 'driver2@demo.tms', 'ACCEPTED', interval '1 day 6 hours', interval '2 hours', interval '1 day 5 hours 30 minutes', NULL),
    ('TMS-DEMO-0009', 'driver3@demo.tms', 'ACCEPTED', interval '5 hours', interval '2 hours', interval '4 hours 30 minutes', NULL),
    ('TMS-DEMO-0010', 'driver3@demo.tms', 'ACCEPTED', interval '6 hours', interval '2 hours', interval '5 hours 30 minutes', NULL),
    ('TMS-DEMO-0011', 'driver2@demo.tms', 'ACCEPTED', interval '2 days 7 hours', interval '2 hours', interval '2 days 6 hours', NULL),
    ('TMS-DEMO-0012', 'driver1@demo.tms', 'ACCEPTED', interval '3 days 23 hours', interval '2 hours', interval '3 days 22 hours 30 minutes', NULL)
) AS v(code, driver_email, status, offered_ago, ttl, responded_ago, reason)
JOIN shipments s ON s.tracking_code = v.code
JOIN users u     ON u.email = v.driver_email
JOIN drivers d   ON d.user_id = u.id
WHERE NOT EXISTS (SELECT 1 FROM assignments a WHERE a.shipment_id = s.id);

-- ========== Xác nhận giao hàng (cho các đơn DELIVERED) ==========
-- image_url chỉ là địa chỉ giả để minh họa, chưa có ảnh thật
INSERT INTO proof_of_delivery (shipment_id, driver_id, image_url, receiver_name, note, delivered_at)
SELECT s.id, d.id, v.image_url, v.receiver_name, v.note, now() - v.ago
FROM (VALUES
    ('TMS-DEMO-0007', 'driver1@demo.tms', 'https://example.com/demo/pod/TMS-DEMO-0007.jpg', 'Trần Thị Mai',   'Giao tận tay, đã thu COD', interval '1 day'),
    ('TMS-DEMO-0008', 'driver2@demo.tms', 'https://example.com/demo/pod/TMS-DEMO-0008.jpg', 'Bùi Văn Long',   'Người nhà nhận hộ',        interval '1 day'),
    ('TMS-DEMO-0009', 'driver3@demo.tms', 'https://example.com/demo/pod/TMS-DEMO-0009.jpg', 'Ngô Thanh Tùng', 'Giao tận tay, đã thu COD', interval '30 minutes')
) AS v(code, driver_email, image_url, receiver_name, note, ago)
JOIN shipments s ON s.tracking_code = v.code
JOIN users u     ON u.email = v.driver_email
JOIN drivers d   ON d.user_id = u.id
ON CONFLICT (shipment_id) DO NOTHING;

-- ========== Đối soát COD ==========
-- expected_amount = tổng COD các đơn DELIVERED của tài xế trong ngày
INSERT INTO cod_settlements (driver_id, settlement_date, expected_amount, submitted_amount, status, note)
SELECT d.id, v.settlement_date, v.expected_amount, v.submitted_amount, v.status, v.note
FROM (VALUES
    ('driver1@demo.tms', current_date - 1, 500000,  500000,  'MATCHED',     'Khớp số liệu'),
    ('driver2@demo.tms', current_date - 1, 1200000, 1150000, 'DISCREPANCY', 'Thiếu 50.000 đ, chờ tài xế giải trình'),
    ('driver3@demo.tms', current_date,     320000,  0,       'OPEN',        NULL)
) AS v(driver_email, settlement_date, expected_amount, submitted_amount, status, note)
JOIN users u   ON u.email = v.driver_email
JOIN drivers d ON d.user_id = u.id
ON CONFLICT (driver_id, settlement_date) DO NOTHING;
