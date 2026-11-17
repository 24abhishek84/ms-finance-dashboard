package com.finance.dashboard.controller;

import com.finance.dashboard.config.DataInitializer;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/seed")
@RequiredArgsConstructor
public class SeedDataController {

    private final DataInitializer dataInitializer;
    private final UserRepository userRepository;

    @PostMapping("/init-test-data")
    public ResponseEntity<String> initializeTestData(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            var user = userRepository.findById(userId).orElseThrow();
            dataInitializer.seedTestDataForUser(user);
            return ResponseEntity.ok("Test data initialized successfully for user: " + user.getEmail());
        } catch (Exception e) {
            log.error("Error seeding test data", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}

