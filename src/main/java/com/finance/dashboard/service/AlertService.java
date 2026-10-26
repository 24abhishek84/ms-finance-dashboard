package com.finance.dashboard.service;

import com.finance.dashboard.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SecurityProperties securityProperties;

    public void alertSecurityTeam(String subject, String message) {
        if (!securityProperties.getAlerts().isEnabled()) {
            return;
        }

        log.warn("SECURITY ALERT: {} - {}", subject, message);

        // Email alert
        if (securityProperties.getAlerts().getEmail() != null) {
            sendEmailAlert(
                securityProperties.getAlerts().getEmail(),
                subject,
                message
            );
        }

        // Slack alert
        if (securityProperties.getAlerts().getSlackWebhook() != null) {
            sendSlackAlert(subject, message);
        }
    }

    public void alertUser(String email, String message) {
        if (!securityProperties.getAlerts().isEnabled()) {
            return;
        }

        log.info("User alert sent to: {} - {}", email, message);

        // TODO: Implement email notification to user
        // emailService.sendUserAlert(email, message);
    }

    private void sendEmailAlert(String to, String subject, String message) {
        // TODO: Implement email service integration
        log.info("Email alert to {} - Subject: {}, Message: {}", to, subject, message);
    }

    private void sendSlackAlert(String subject, String message) {
        // TODO: Implement Slack webhook integration
        log.info("Slack alert - Subject: {}, Message: {}", subject, message);
    }
}
