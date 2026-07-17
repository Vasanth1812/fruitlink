-- Seed default global settings
INSERT INTO global_setting (key, value) VALUES
('tax.default_rate', '{"rate": 18.0}'::jsonb),
('order.cutoff_time', '{"time": "23:00"}'::jsonb),
('app.maintenance_mode', '{"enabled": false}'::jsonb)
ON CONFLICT (key) DO NOTHING;
