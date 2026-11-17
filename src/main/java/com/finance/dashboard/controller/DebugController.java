package com.finance.dashboard.controller;

import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo(Authentication authentication) {
        Map<String, Object> info = new HashMap<>();
        Long userId = (Long) authentication.getPrincipal();

        info.put("userId", userId);
        info.put("totalUsers", userRepository.count());
        info.put("totalCategories", categoryRepository.count());
        info.put("totalTransactions", transactionRepository.count());
        info.put("categoriesForUser", categoryRepository.findByUserId(userId).size());
        info.put("transactionsForUser", transactionRepository.findByUserId(userId).size());

        return ResponseEntity.ok(info);
    }
}
