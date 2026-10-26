# Banking-Grade Security Implementation

## Overview

This document outlines the complete security implementation for the Financial Dashboard microservice with separate **DEV** and **PROD** profiles.

---

## Architecture

```
Login Request
    ↓
[1] Rate Limiting (IP-based) - Blocks after N attempts
    ↓
[2] Account Lockout (User-based) - Locks after N failed attempts
    ↓
[3] CAPTCHA (Future) - On 2nd failed attempt
    ↓
[4] MFA (Multi-Factor Auth) - SMS/Email verification (Prod only)
    ↓
[5] Geo-location Check (Future) - Flag unusual locations
    ↓
[6] Audit Logging - Log every attempt
    ↓
[7] Real-time Alerts - Alert team & user on suspicious activity
```

---

## DEV Profile (application.yml)

**Target:** Fast development & testing with relaxed security

```yaml
app.security:
  rate-limit:
    enabled: false        # No rate limiting
    max-attempts: 10      # Generous limits
  account-lockout:
    enabled: false        # Don't lock accounts
    max-attempts: 5
    lockout-minutes: 5
  mfa:
    enabled: false        # No MFA
  audit:
    enabled: false        # Minimal logging
  alerts:
    enabled: false        # No alerts
```

**Run DEV:**
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

---

## PROD Profile (application-prod.yml)

**Target:** Banking-grade security with strict validation

```yaml
app.security:
  rate-limit:
    enabled: true         # Enabled
    max-attempts: 3       # 3 attempts per hour per IP
    window-minutes: 60    # 1 hour window
  account-lockout:
    enabled: true         # Enabled
    max-attempts: 2       # Lock after 2 failed attempts
    lockout-minutes: 30   # Lock for 30 minutes
  mfa:
    enabled: true         # SMS/Email verification
    methods: EMAIL,SMS
  audit:
    enabled: true         # Detailed logging
    log-level: DETAILED
  alerts:
    enabled: true         # Real-time alerts
    email: security-team@bank.com
    slack-webhook: https://hooks.slack.com/...
```

**Run PROD:**
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=prod'
# OR
set SPRING_PROFILES_ACTIVE=prod
.\gradlew.bat bootRun
```

---

## Key Components

### 1. SecurityProperties (Config)
Located: `config/SecurityProperties.java`

Centralized configuration for all security settings loaded from application.yml

### 2. SecurityService (Core Logic)
Located: `service/SecurityService.java`

**Responsibilities:**
- Rate limiting (IP-based)
- Account lockout (User-based)
- Login attempt validation
- Recording login attempts
- Clearing failed attempts on success

**Key Methods:**
```java
validateLoginAttempt(email, ipAddress)          // Pre-login checks
recordLoginAttempt(email, ip, success, ...)    // Post-login logging
recordSuccessfulLogin(email, ipAddress)         // Clear failed attempts
```

### 3. AlertService (Notifications)
Located: `service/AlertService.java`

Sends real-time alerts to:
- Security team (email, Slack)
- Users (suspicious login attempts)

### 4. LoginAttempt Entity (Audit Trail)
Located: `entity/LoginAttempt.java`

Stores every login attempt with:
- Email, IP address, timestamp
- Success/failure reason
- User agent, geo-location
- Created at timestamp

### 5. Updated AuthController
Located: `controller/AuthController.java`

**Login Flow:**
1. Extract client IP & User-Agent
2. Call `securityService.validateLoginAttempt()`
3. If pass, verify credentials
4. On failure, call `recordLoginAttempt()` with reason
5. On success, generate JWT & record successful login

### 6. Updated User Entity
Located: `entity/User.java`

**New security fields:**
```java
boolean locked              // Account locked status
LocalDateTime lockedUntil   // Unlock time
int failedLoginAttempts     // Failed attempt counter
String lastLoginIp          // Last known IP
LocalDateTime lastLoginTime // Last login time
boolean mfaEnabled          // MFA toggle
String mfaSecret            // MFA secret key
```

### 7. Database Migrations
Located: `src/main/resources/db/migration/`

**V2:** Add security fields to User table
**V3:** Create LoginAttempt table with indexes

---

## Security Flow by Profile

### DEV Environment
```
User attempts login (DEV=false means no rate limit, no lockout)
    ↓
No rate limit check
No account lockout check
    ↓
Validate credentials
    ↓
Generate JWT token
    ↓
Record attempt (no alerts)
    ↓
✅ Login success
```

### PROD Environment
```
User attempts login (PROD=true means strict checks)
    ↓
[1] Check rate limit: 3 attempts/hour/IP?
    → If exceeded: Alert + throw RateLimitExceededException
    ↓
[2] Check account lockout: Is account locked?
    → If locked: Alert + throw AccountLockedException
    ↓
[3] Validate credentials
    → If failed: Record attempt + increment failed_login_attempts
    → After 2 failures: Lock account + Alert user & team
    ↓
[4] Generate JWT token
    ↓
[5] Record success: Clear failed attempts, update last_login_ip
    ↓
✅ Login success
```

---

## API Responses

### Success (200)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "message": "Login successful"
}
```

### Rate Limit Exceeded (429)
```json
{
  "timestamp": "2024-10-22T10:30:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Too many login attempts. Please try again later."
}
```

### Account Locked (401)
```json
{
  "timestamp": "2024-10-22T10:30:00",
  "status": 401,
  "error": "Account Locked",
  "message": "Account is temporarily locked. Please try again later or contact support."
}
```

### Invalid Credentials (400)
```json
{
  "timestamp": "2024-10-22T10:30:00",
  "status": 400,
  "error": "Invalid Request",
  "message": "Invalid email or password"
}
```

---

## Database Schema

### User Table (updated)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    is_locked BOOLEAN DEFAULT false,
    locked_until TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    last_login_ip VARCHAR(45),
    last_login_time TIMESTAMP,
    mfa_enabled BOOLEAN DEFAULT false,
    mfa_secret VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### LoginAttempt Table (new)
```sql
CREATE TABLE login_attempt (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(500),
    geo_location VARCHAR(100),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_email_time ON login_attempt(email, attempt_time DESC);
CREATE INDEX idx_login_ip_time ON login_attempt(ip_address, attempt_time DESC);
```

---

## Testing

### 1. Test DEV Environment
```powershell
# Run with DEV profile
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'

# Try multiple failed logins - should NOT lock account
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"wrongpass"}'

# Should succeed on 10th attempt (dev=10 max)
```

### 2. Test PROD Environment
```powershell
# Run with PROD profile
.\gradlew.bat bootRun --args='--spring.profiles.active=prod'

# Try to login - should lock after 2 failed attempts
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"wrongpass"}'

# 1st failure: OK, increment counter
# 2nd failure: LOCKED! Account locked for 30 minutes
# 3rd failure: 401 - Account locked
```

### 3. Check Rate Limiting
```bash
# Simulate 3+ login attempts from same IP in PROD
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"test123"}'
  echo "Attempt $i"
done

# After 3 attempts: 429 - Too many requests
```

---

## Migration Steps

### 1. Build
```powershell
.\gradlew.bat clean build
```

### 2. Run DEV (testing)
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

### 3. Run PROD (after validation)
```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:ALERT_EMAIL = "security@bank.com"
$env:JWT_SECRET = "your-secure-key-at-least-256-bits-long"
.\gradlew.bat bootRun
```

---

## Monitoring & Maintenance

### View Login Attempts (SQL)
```sql
-- All failed attempts in last hour
SELECT * FROM login_attempt 
WHERE success = false 
AND attempt_time > NOW() - INTERVAL '1 hour'
ORDER BY attempt_time DESC;

-- Failed attempts by IP
SELECT ip_address, COUNT(*) as failed_attempts
FROM login_attempt 
WHERE success = false 
AND attempt_time > NOW() - INTERVAL '1 hour'
GROUP BY ip_address
ORDER BY failed_attempts DESC;

-- Check locked users
SELECT email, locked_until, failed_login_attempts
FROM users 
WHERE is_locked = true;

-- Unlock user manually
UPDATE users SET is_locked = false, locked_until = NULL 
WHERE email = 'user@example.com';
```

---

## Environment Variables (PROD)

```bash
# Database
DB_HOST=prod-db.azure.com
DB_PORT=5432
DB_NAME=financial_db
DB_USER=postgres
DB_PASSWORD=secure_password

# Kafka
KAFKA_BOOTSTRAP=kafka-prod:9092

# Security
JWT_SECRET=your-min-256-bits-secure-key-for-hs256
ALERT_EMAIL=security-team@bank.com
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK/URL

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

---

## Future Enhancements

- [ ] Implement MFA (SMS/Email verification)
- [ ] Geo-location checking
- [ ] Device fingerprinting
- [ ] IP whitelisting
- [ ] CAPTCHA integration
- [ ] Progressive delays (exponential backoff)
- [ ] 2FA with TOTP (Google Authenticator)
- [ ] Passwordless authentication (WebAuthn)
- [ ] Security event analytics
- [ ] Automated response system (auto-block malicious IPs)

---

## Support

For security issues: `security@bank.com`
For production deployments: Refer to DevOps team
