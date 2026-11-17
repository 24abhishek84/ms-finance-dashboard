package com.finance.dashboard.controller;

import com.finance.dashboard.dto.CategoryExpenseDTO;
import com.finance.dashboard.dto.DashboardExpenseDTO;
import com.finance.dashboard.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics service is running");
    }

    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryExpenseDTO>> getExpensesByCategory(
            Authentication authentication,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching expenses by category for user: {} from {} to {}", userId, startDate, endDate);

        List<CategoryExpenseDTO> expenses = analyticsService.getExpensesByCategory(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/category/{categoryId}/breakdown")
    public ResponseEntity<List<CategoryExpenseDTO>> getSubCategoryBreakdown(
            Authentication authentication,
            @PathVariable Long categoryId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching sub-category breakdown for user: {}, categoryId: {} from {} to {}",
                 userId, categoryId, startDate, endDate);

        List<CategoryExpenseDTO> breakdown = analyticsService.getSubCategoryBreakdown(userId, categoryId, startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<DashboardExpenseDTO> getYearlyExpenses(
            Authentication authentication,
            @PathVariable int year) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching yearly expenses for user: {} for year: {}", userId, year);

        DashboardExpenseDTO yearlyExpenses = analyticsService.getYearlyExpenses(userId, year);
        return ResponseEntity.ok(yearlyExpenses);
    }

    @GetMapping("/yearly/current")
    public ResponseEntity<DashboardExpenseDTO> getCurrentYearExpenses(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching current year expenses for user: {}", userId);

        DashboardExpenseDTO yearlyExpenses = analyticsService.getCurrentYearExpenses(userId);
        return ResponseEntity.ok(yearlyExpenses);
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<DashboardExpenseDTO> getMonthlyExpenses(
            Authentication authentication,
            @PathVariable int year,
            @PathVariable int month) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching monthly expenses for user: {} for {}-{}", userId, year, month);

        DashboardExpenseDTO monthlyExpenses = analyticsService.getMonthlyExpenses(userId, year, month);
        return ResponseEntity.ok(monthlyExpenses);
    }

    @GetMapping("/monthly/current")
    public ResponseEntity<DashboardExpenseDTO> getCurrentMonthExpenses(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching current month expenses for user: {}", userId);

        DashboardExpenseDTO monthlyExpenses = analyticsService.getCurrentMonthExpenses(userId);
        return ResponseEntity.ok(monthlyExpenses);
    }

    @GetMapping("/weekly")
    public ResponseEntity<DashboardExpenseDTO> getWeeklyExpenses(
            Authentication authentication,
            @RequestParam LocalDate startDate) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching weekly expenses for user: {} starting from {}", userId, startDate);

        DashboardExpenseDTO weeklyExpenses = analyticsService.getWeeklyExpenses(userId, startDate);
        return ResponseEntity.ok(weeklyExpenses);
    }

    @GetMapping("/weekly/current")
    public ResponseEntity<DashboardExpenseDTO> getCurrentWeekExpenses(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("Fetching current week expenses for user: {}", userId);

        DashboardExpenseDTO weeklyExpenses = analyticsService.getCurrentWeekExpenses(userId);
        return ResponseEntity.ok(weeklyExpenses);
    }
}
