package com.finance.dashboard.service;

import com.finance.dashboard.config.SecurityProperties;
import com.finance.dashboard.entity.LoginAttempt;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.AccountLockedException;
import com.finance.dashboard.exception.RateLimitExceededException;
import com.finance.dashboard.repository.LoginAttemptRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;
    private final AlertService alertService;

    public void validateLoginAttempt(String email, String ipAddress) {
        // Rate limiting check (IP-based)
        if (securityProperties.getRateLimit().isEnabled()) {
            checkRateLimit(ipAddress);
        }

        // Account lockout check (User-based)
        if (securityProperties.getAccountLockout().isEnabled()) {
            checkAccountLockout(email);
        }
    }

    private void checkRateLimit(String ipAddress) {
        LocalDateTime since = LocalDateTime.now()
            .minusMinutes(securityProperties.getRateLimit().getWindowMinutes());

        long failedAttempts = loginAttemptRepository.countByIpAddressAndSuccessFalseAndAttemptTimeGreaterThan(ipAddress, since);

        if (failedAttempts >= securityProperties.getRateLimit().getMaxAttempts()) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            alertService.alertSecurityTeam(
                "Rate limit exceeded",
                "Multiple login failures from IP: " + ipAddress + " (" + failedAttempts + " attempts)"
            );
            throw new RateLimitExceededException(
                "Too many login attempts. Please try again later."
            );
        }
    }

    private void checkAccountLockout(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.isLocked()) {
            if (user.getLockedUntil() != null &&
                LocalDateTime.now().isBefore(user.getLockedUntil())) {
                log.warn("Login attempt on locked account: {}", email);
                throw new AccountLockedException(
                    "Account is temporarily locked. Please try again later or contact support."
                );
            } else if (user.getLockedUntil() != null) {
                // Auto-unlock after lockout duration expires
                user.setLocked(false);
                user.setLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }
    }

    public void recordLoginAttempt(String email, String ipAddress, boolean success,
                                   String userAgent, String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
            .email(email)
            .ipAddress(ipAddress)
            .success(success)
            .attemptTime(LocalDateTime.now())
            .userAgent(userAgent)
            .failureReason(failureReason)
            .build();

        loginAttemptRepository.save(attempt);

        if (securityProperties.getAudit().isEnabled()) {
            log.info("Login attempt - Email: {}, IP: {}, Success: {}",
                email, ipAddress, success);
        }

        // Handle failed attempts
        if (!success) {
            handleFailedLoginAttempt(email, ipAddress, failureReason);
        } else {
            // Clear failed attempts on success
            clearFailedAttempts(email);
        }
    }

    private void handleFailedLoginAttempt(String email, String ipAddress, String reason) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account if max attempts exceeded
            if (securityProperties.getAccountLockout().isEnabled() &&
                user.getFailedLoginAttempts() >= securityProperties.getAccountLockout().getMaxAttempts()) {

                user.setLocked(true);
                user.setLockedUntil(LocalDateTime.now()
                    .plusMinutes(securityProperties.getAccountLockout().getLockoutMinutes()));

                log.warn("Account locked due to failed login attempts: {}", email);

                if (securityProperties.getAlerts().isEnabled()) {
                    alertService.alertUser(email,
                        "Your account has been locked due to multiple failed login attempts. " +
                        "Please try again after 30 minutes or contact support.");
                    alertService.alertSecurityTeam(
                        "Account locked",
                        "Account locked for user: " + email + " from IP: " + ipAddress
                    );
                }
            }

            userRepository.save(user);
        }
    }

    private void clearFailedAttempts(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void recordSuccessfulLogin(String email, String ipAddress) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setLastLoginIp(ipAddress);
            user.setLastLoginTime(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }
}
