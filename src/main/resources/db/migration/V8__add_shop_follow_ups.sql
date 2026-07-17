CREATE TABLE shop_follow_up (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shop(id),
    assigned_to UUID REFERENCES app_user(id),
    reason TEXT NOT NULL,
    remarks TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    next_follow_up TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
