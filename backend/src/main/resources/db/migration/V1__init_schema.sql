-- ========== Người dùng & phân quyền ==========
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(20),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'LOCKED'))
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    PRIMARY KEY (user_id, role_id)
);

-- ========== Tài xế ==========
CREATE TABLE drivers (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    vehicle_type         VARCHAR(30),
    license_plate        VARCHAR(20),
    service_area         VARCHAR(100),
    max_load_kg          NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (max_load_kg >= 0),
    max_active_shipments INT           NOT NULL DEFAULT 10 CHECK (max_active_shipments > 0),
    status               VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT chk_drivers_status CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE'))
);

-- ========== Vận đơn ==========
CREATE TABLE shipments (
    id                BIGSERIAL PRIMARY KEY,
    tracking_code     VARCHAR(30)    NOT NULL UNIQUE,
    sender_name       VARCHAR(150)   NOT NULL,
    sender_phone      VARCHAR(20)    NOT NULL,
    pickup_address    VARCHAR(500)   NOT NULL,
    receiver_name     VARCHAR(150)   NOT NULL,
    receiver_phone    VARCHAR(20)    NOT NULL,
    delivery_address  VARCHAR(500)   NOT NULL,
    weight_kg         NUMERIC(10, 2) NOT NULL CHECK (weight_kg > 0),
    cod_amount        NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (cod_amount >= 0),
    shipping_fee      NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0),
    status            VARCHAR(30)    NOT NULL DEFAULT 'CREATED',
    current_driver_id BIGINT REFERENCES drivers (id),
    version           BIGINT         NOT NULL DEFAULT 0,   -- cho @Version (optimistic lock)
    created_by        BIGINT REFERENCES users (id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT chk_shipments_status CHECK (status IN (
        'CREATED', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED',
        'DELIVERY_FAILED', 'RETURNING', 'RETURNED', 'CANCELLED'))
);

CREATE INDEX idx_shipments_status ON shipments (status);
CREATE INDEX idx_shipments_driver ON shipments (current_driver_id);
CREATE INDEX idx_shipments_created_at ON shipments (created_at);

-- Lịch sử trạng thái: chỉ thêm, không sửa/xóa
CREATE TABLE shipment_status_history (
    id          BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT      NOT NULL REFERENCES shipments (id),
    from_status VARCHAR(30),
    to_status   VARCHAR(30) NOT NULL,
    changed_by  BIGINT REFERENCES users (id),
    reason      VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_status_history_shipment ON shipment_status_history (shipment_id, created_at);

-- ========== Gán đơn / tendering ==========
CREATE TABLE assignments (
    id           BIGSERIAL PRIMARY KEY,
    shipment_id  BIGINT      NOT NULL REFERENCES shipments (id),
    driver_id    BIGINT      NOT NULL REFERENCES drivers (id),
    status       VARCHAR(20) NOT NULL DEFAULT 'OFFERED',
    offered_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at TIMESTAMPTZ,
    reason       VARCHAR(500),
    CONSTRAINT chk_assignments_status CHECK (status IN (
        'OFFERED', 'ACCEPTED', 'REJECTED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_assignments_driver ON assignments (driver_id, status);

-- Mỗi vận đơn chỉ có tối đa một lượt ĐÃ NHẬN: chặn hai tài xế cùng nhận ở tầng database
CREATE UNIQUE INDEX ux_assignments_one_accepted
    ON assignments (shipment_id) WHERE status = 'ACCEPTED';

-- ========== Xác nhận giao hàng ==========
CREATE TABLE proof_of_delivery (
    id            BIGSERIAL PRIMARY KEY,
    shipment_id   BIGINT       NOT NULL UNIQUE REFERENCES shipments (id),
    driver_id     BIGINT       NOT NULL REFERENCES drivers (id),
    image_url     VARCHAR(500) NOT NULL,
    receiver_name VARCHAR(150),
    note          VARCHAR(500),
    delivered_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ========== Đối soát COD ==========
CREATE TABLE cod_settlements (
    id                BIGSERIAL PRIMARY KEY,
    driver_id         BIGINT         NOT NULL REFERENCES drivers (id),
    settlement_date   DATE           NOT NULL,
    expected_amount   NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- tổng COD tài xế phải thu
    submitted_amount  NUMERIC(15, 2) NOT NULL DEFAULT 0,   -- tổng tiền tài xế đã nộp
    status            VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
    note              VARCHAR(500),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_cod_driver_date UNIQUE (driver_id, settlement_date),
    CONSTRAINT chk_cod_status CHECK (status IN ('OPEN', 'MATCHED', 'DISCREPANCY'))
);

-- ========== Hạ tầng hỗ trợ ==========
-- Chống xử lý trùng khi app retry (mạng yếu)
CREATE TABLE idempotency_keys (
    user_id         BIGINT       NOT NULL REFERENCES users (id),
    idempotency_key VARCHAR(100) NOT NULL,
    endpoint        VARCHAR(200) NOT NULL,
    response_status INT,
    response_body   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, idempotency_key)
);

-- Token thiết bị cho thông báo đẩy (FCM)
CREATE TABLE device_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    token      VARCHAR(500) NOT NULL UNIQUE,
    platform   VARCHAR(20),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
