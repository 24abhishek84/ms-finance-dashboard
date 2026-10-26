package com.finance.dashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class SecurityProperties {

    private RateLimit rateLimit = new RateLimit();
    private AccountLockout accountLockout = new AccountLockout();
    private Mfa mfa = new Mfa();
    private Audit audit = new Audit();
    private Alerts alerts = new Alerts();

    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = false;
        private int maxAttempts = 3;
        private int windowMinutes = 60;
    }

    @Getter
    @Setter
    public static class AccountLockout {
        private boolean enabled = false;
        private int maxAttempts = 2;
        private int lockoutMinutes = 30;
    }

    @Getter
    @Setter
    public static class Mfa {
        private boolean enabled = false;
        private String methods = "EMAIL,SMS";
    }

    @Getter
    @Setter
    public static class Audit {
        private boolean enabled = false;
        private String logLevel = "BASIC";
    }

    @Getter
    @Setter
    public static class Alerts {
        private boolean enabled = false;
        private String email;
        private String slackWebhook;
    }
}
