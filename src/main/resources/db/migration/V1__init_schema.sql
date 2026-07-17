-- ============================================================
-- FruitLink Enterprise ERP — Initial Schema
-- Migration: V1__init_schema.sql
-- Covers sections 13.1 – 13.5 from the architecture document
-- All monetary values stored in paise (integer), UUID PKs
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- 13.1  Identity, Roles & Access
-- ============================================================

CREATE TABLE role (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,  -- Super Admin, Shop Admin, Salesman, Driver, Vendor
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE permission (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module  TEXT NOT NULL,
    action  TEXT NOT NULL CHECK (action IN ('read','write','delete','override')),
    UNIQUE (module, action)
);

CREATE TABLE role_permission (
    role_id       UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE app_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id         UUID NOT NULL REFERENCES role(id),
    full_name       TEXT NOT NULL,
    phone           TEXT NOT NULL UNIQUE,
    email           TEXT UNIQUE,
    auth_provider   TEXT NOT NULL DEFAULT 'password' CHECK (auth_provider IN ('password','otp')),
    password_hash   TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 13.2  Shops, Catalog & Inventory
-- ============================================================

CREATE TABLE route (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT NOT NULL,
    assigned_vehicle_id UUID,
    assigned_driver_id  UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE shop (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  TEXT NOT NULL,
    contact_phone         TEXT NOT NULL,
    address               TEXT,
    gstin                 TEXT,
    status                TEXT NOT NULL DEFAULT 'pending_kyc'
                              CHECK (status IN ('pending_kyc','active','inactive','critical_followup')),
    credit_limit          BIGINT NOT NULL DEFAULT 0,   -- paise
    assigned_salesman_id  UUID REFERENCES app_user(id),
    route_id              UUID REFERENCES route(id),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE shop_kyc_document (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id        UUID NOT NULL REFERENCES shop(id) ON DELETE CASCADE,
    document_type  TEXT NOT NULL,
    file_url       TEXT NOT NULL,   -- S3 key
    review_status  TEXT NOT NULL DEFAULT 'pending'
                       CHECK (review_status IN ('pending','approved','rejected')),
    reviewed_by    UUID REFERENCES app_user(id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sku (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           TEXT NOT NULL UNIQUE,
    name           TEXT NOT NULL,
    category       TEXT,
    hsn_code       TEXT,
    unit           TEXT NOT NULL DEFAULT 'kg',
    current_price  BIGINT NOT NULL DEFAULT 0,   -- paise per unit
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE vendor (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           TEXT NOT NULL,
    contact_phone  TEXT NOT NULL,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_batch (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id           UUID NOT NULL REFERENCES sku(id),
    vendor_id        UUID REFERENCES vendor(id),
    received_weight  NUMERIC(12,3) NOT NULL,
    expiry_estimate  DATE,                        -- drives FEFO allocation
    status           TEXT NOT NULL DEFAULT 'available'
                         CHECK (status IN ('available','depleted','spoiled')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_movement (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id      UUID NOT NULL REFERENCES inventory_batch(id),
    change_qty    NUMERIC(12,3) NOT NULL,   -- signed: negative = deduction
    reason        TEXT NOT NULL,
    reference_id  UUID,                    -- order_id, po_id, etc.
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE spoilage_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id    UUID NOT NULL REFERENCES inventory_batch(id),
    quantity    NUMERIC(12,3) NOT NULL,
    reason      TEXT,
    logged_by   UUID REFERENCES app_user(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 13.3  Vendors, Orders & Fulfillment
-- ============================================================

CREATE TABLE purchase_order (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id       UUID NOT NULL REFERENCES vendor(id),
    status          TEXT NOT NULL DEFAULT 'draft'
                        CHECK (status IN ('draft','sent','confirmed','received')),
    generated_by    TEXT NOT NULL DEFAULT 'manual'
                        CHECK (generated_by IN ('system_auto','manual')),
    created_by      UUID REFERENCES app_user(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_item (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_id       UUID NOT NULL REFERENCES purchase_order(id) ON DELETE CASCADE,
    sku_id      UUID NOT NULL REFERENCES sku(id),
    quantity    NUMERIC(12,3) NOT NULL,
    unit_cost   BIGINT NOT NULL DEFAULT 0   -- paise
);

CREATE TABLE orders (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id       UUID NOT NULL REFERENCES shop(id),
    status        TEXT NOT NULL DEFAULT 'pending'
                      CHECK (status IN ('pending','confirmed','packed','dispatched','delivered','cancelled')),
    payment_mode  TEXT NOT NULL DEFAULT 'credit'
                      CHECK (payment_mode IN ('credit','cash','upi')),
    source        TEXT NOT NULL DEFAULT 'salesman'
                      CHECK (source IN ('salesman','self_serve','repeat')),
    total_value   BIGINT NOT NULL DEFAULT 0,  -- paise
    created_by    UUID REFERENCES app_user(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_item (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id             UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sku_id               UUID NOT NULL REFERENCES sku(id),
    ordered_qty          NUMERIC(12,3) NOT NULL,
    packed_qty           NUMERIC(12,3),           -- from catch-weight capture
    unit_price_at_order  BIGINT NOT NULL,          -- paise, locked at order time
    weight_variance_flag BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE delivery_manifest (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id       UUID REFERENCES route(id),
    driver_id      UUID REFERENCES app_user(id),
    vehicle_id     UUID,
    dispatch_date  DATE NOT NULL,
    status         TEXT NOT NULL DEFAULT 'pending'
                       CHECK (status IN ('pending','in_transit','completed')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE manifest_stop (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manifest_id   UUID NOT NULL REFERENCES delivery_manifest(id) ON DELETE CASCADE,
    shop_id       UUID NOT NULL REFERENCES shop(id),
    order_id      UUID REFERENCES orders(id),
    sequence      INT NOT NULL,
    eta           TIMESTAMPTZ,
    status        TEXT NOT NULL DEFAULT 'pending'
                      CHECK (status IN ('pending','arrived','completed','skipped'))
);

CREATE TABLE proof_of_delivery (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    manifest_stop_id    UUID NOT NULL UNIQUE REFERENCES manifest_stop(id),
    confirmation_code   TEXT,
    photo_url           TEXT,   -- S3 key
    crates_delivered    INT NOT NULL DEFAULT 0,
    crates_reclaimed    INT NOT NULL DEFAULT 0,
    confirmed_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE geofence_check_in (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id              UUID NOT NULL REFERENCES shop(id),
    salesman_id          UUID NOT NULL REFERENCES app_user(id),
    coordinates          TEXT NOT NULL,   -- lat,lng
    distance_from_shop_m NUMERIC(10,2),
    result               TEXT NOT NULL CHECK (result IN ('inside','outside')),
    checked_in_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 13.4  Ledger, Tax, Returns & Assets
-- ============================================================

CREATE TABLE ledger_entry (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id      UUID NOT NULL REFERENCES shop(id),
    entry_type   TEXT NOT NULL
                     CHECK (entry_type IN ('invoice','payment','credit_note','deposit_fee')),
    amount       BIGINT NOT NULL,   -- signed paise: positive = debit, negative = credit
    reference_id UUID,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE invoice (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      UUID NOT NULL UNIQUE REFERENCES orders(id),
    gstin         TEXT,
    hsn_summary   JSONB,
    cgst          BIGINT NOT NULL DEFAULT 0,   -- paise
    sgst          BIGINT NOT NULL DEFAULT 0,
    igst          BIGINT NOT NULL DEFAULT 0,
    net_value     BIGINT NOT NULL DEFAULT 0,
    total_value   BIGINT NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE return_claim (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id      UUID NOT NULL REFERENCES shop(id),
    order_id     UUID REFERENCES orders(id),
    reason       TEXT NOT NULL,
    photo_url    TEXT,
    status       TEXT NOT NULL DEFAULT 'pending'
                     CHECK (status IN ('pending','approved','rejected')),
    reviewed_by  UUID REFERENCES app_user(id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE credit_note (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    return_claim_id  UUID NOT NULL UNIQUE REFERENCES return_claim(id),
    amount           BIGINT NOT NULL,   -- paise
    gst_adjustment   BIGINT NOT NULL DEFAULT 0,
    issued_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE crate_transaction (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id               UUID NOT NULL REFERENCES shop(id),
    delivered_count       INT NOT NULL DEFAULT 0,
    reclaimed_count       INT NOT NULL DEFAULT 0,
    deposit_fee_applied   BIGINT NOT NULL DEFAULT 0,   -- paise
    manifest_stop_id      UUID REFERENCES manifest_stop(id),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 13.5  Notifications, Settings & Audit
-- ============================================================

CREATE TABLE notification (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trigger_type     TEXT NOT NULL,
    target_user_id   UUID REFERENCES app_user(id),
    target_group     TEXT,
    channel          TEXT NOT NULL CHECK (channel IN ('push','sms','email','in_app')),
    payload          JSONB,
    delivery_status  TEXT NOT NULL DEFAULT 'pending'
                         CHECK (delivery_status IN ('pending','sent','failed')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE global_setting (
    key         TEXT PRIMARY KEY,
    value       JSONB NOT NULL,
    updated_by  UUID REFERENCES app_user(id),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID REFERENCES app_user(id),
    action       TEXT NOT NULL,
    module       TEXT NOT NULL,
    before_state JSONB,
    after_state  JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
    -- insert-only: no updates, no deletes ever
);

CREATE TABLE helpdesk_ticket (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    raised_by    UUID NOT NULL REFERENCES app_user(id),
    subject      TEXT NOT NULL,
    description  TEXT,
    status       TEXT NOT NULL DEFAULT 'open'
                     CHECK (status IN ('open','in_progress','resolved','closed')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Indexes for common query patterns
-- ============================================================

CREATE INDEX idx_app_user_phone       ON app_user(phone);
CREATE INDEX idx_shop_status          ON shop(status);
CREATE INDEX idx_orders_shop_id       ON orders(shop_id);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_inventory_batch_sku  ON inventory_batch(sku_id, expiry_estimate);
CREATE INDEX idx_ledger_shop_id       ON ledger_entry(shop_id, created_at DESC);
CREATE INDEX idx_audit_log_actor      ON audit_log(actor_id, created_at DESC);
CREATE INDEX idx_manifest_stop_seq    ON manifest_stop(manifest_id, sequence);

-- ============================================================
-- Seed Roles
-- ============================================================

INSERT INTO role (name) VALUES
    ('Super Admin'),
    ('Shop Admin'),
    ('Salesman'),
    ('Driver'),
    ('Vendor')
ON CONFLICT (name) DO NOTHING;
