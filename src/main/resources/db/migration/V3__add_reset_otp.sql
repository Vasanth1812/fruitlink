-- Add reset OTP fields to app_user table
ALTER TABLE app_user
ADD COLUMN reset_otp TEXT,
ADD COLUMN reset_otp_expiry TIMESTAMPTZ;
