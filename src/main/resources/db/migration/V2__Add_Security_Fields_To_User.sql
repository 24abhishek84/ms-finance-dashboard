-- Add security fields to User table for banking-grade authentication
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT false;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(45);
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMP;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS mfa_enabled BOOLEAN DEFAULT false;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS mfa_secret VARCHAR(255);

-- Create indexes for security queries
CREATE INDEX IF NOT EXISTS idx_user_locked ON "user"(is_locked);
CREATE INDEX IF NOT EXISTS idx_user_email_locked ON "user"(email, is_locked);
