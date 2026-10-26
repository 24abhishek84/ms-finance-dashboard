-- Create LoginAttempt table for tracking and auditing login attempts
CREATE TABLE IF NOT EXISTS login_attempt (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(500),
    geo_location VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_login_email_time ON login_attempt(email, attempt_time DESC);
CREATE INDEX IF NOT EXISTS idx_login_ip_time ON login_attempt(ip_address, attempt_time DESC);
CREATE INDEX IF NOT EXISTS idx_login_success ON login_attempt(success);
