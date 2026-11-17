package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategoryExpenseDTO;
import com.finance.dashboard.dto.DashboardExpenseDTO;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    @Cacheable(value = "expensesByCategory", key = "#userId + '-' + #startDate + '-' + #endDate")
    public List<CategoryExpenseDTO> getExpensesByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching expenses by category for user: {} from {} to {}", userId, startDate, endDate);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        List<Transaction> expenses = transactions.stream()
            .filter(t -> t.getCategory().getType().toString().equals("EXPENSE"))
            .toList();

        Map<Long, CategoryExpenseDTO> categoryMap = new LinkedHashMap<>();

        for (Transaction transaction : expenses) {
            Long categoryId = transaction.getCategory().getId();
            categoryMap.computeIfAbsent(categoryId, k -> CategoryExpenseDTO.builder()
                    .categoryId(categoryId)
                    .categoryName(transaction.getCategory().getName())
                    .categoryColor(transaction.getCategory().getColor())
                    .totalAmount(BigDecimal.ZERO)
                    .transactionCount(0)
                    .build());

            CategoryExpenseDTO dto = categoryMap.get(categoryId);
            dto.setTotalAmount(dto.getTotalAmount().add(transaction.getAmount()));
            dto.setTransactionCount(dto.getTransactionCount() + 1);
        }

        List<CategoryExpenseDTO> result = new ArrayList<>(categoryMap.values());
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        log.debug("Retrieved {} categories with expenses for user: {}", result.size(), userId);
        return result;
    }

    @Cacheable(value = "subCategoryBreakdown", key = "#userId + '-' + #parentCategoryId + '-' + #startDate + '-' + #endDate")
    public List<CategoryExpenseDTO> getSubCategoryBreakdown(Long userId, Long parentCategoryId,
                                                           LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching sub-category breakdown for user: {}, parentCategoryId: {} from {} to {}",
                  userId, parentCategoryId, startDate, endDate);

        List<Transaction> transactions = transactionRepository
            .findByUserIdAndCategoryParentCategoryIdAndTransactionDateBetween(userId, parentCategoryId, startDate, endDate);

        Map<Long, CategoryExpenseDTO> categoryMap = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            Long categoryId = transaction.getCategory().getId();
            categoryMap.computeIfAbsent(categoryId, k -> CategoryExpenseDTO.builder()
                    .categoryId(categoryId)
                    .categoryName(transaction.getCategory().getName())
                    .categoryColor(transaction.getCategory().getColor())
                    .totalAmount(BigDecimal.ZERO)
                    .transactionCount(0)
                    .build());

            CategoryExpenseDTO dto = categoryMap.get(categoryId);
            dto.setTotalAmount(dto.getTotalAmount().add(transaction.getAmount()));
            dto.setTransactionCount(dto.getTransactionCount() + 1);
        }

        List<CategoryExpenseDTO> result = new ArrayList<>(categoryMap.values());
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));

        log.debug("Retrieved {} sub-categories with expenses for user: {}", result.size(), userId);
        return result;
    }

    @Cacheable(value = "yearlyExpenses", key = "#userId + '-' + #year")
    public DashboardExpenseDTO getYearlyExpenses(Long userId, int year) {
        log.debug("Fetching yearly expenses for user: {} for year: {}", userId, year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<CategoryExpenseDTO> categoryExpenses = getExpensesByCategory(userId, startDate, endDate);
        BigDecimal totalAmount = categoryExpenses.stream()
            .map(CategoryExpenseDTO::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = categoryExpenses.stream()
            .mapToInt(CategoryExpenseDTO::getTransactionCount)
            .sum();

        DashboardExpenseDTO result = DashboardExpenseDTO.builder()
            .period(String.valueOf(year))
            .totalAmount(totalAmount)
            .transactionCount(totalTransactions)
            .expensesByCategory(categoryExpenses)
            .build();

        log.debug("Yearly expenses retrieved for user: {} - Total: {}", userId, totalAmount);
        return result;
    }

    @Cacheable(value = "monthlyExpenses", key = "#userId + '-' + #year + '-' + #month")
    public DashboardExpenseDTO getMonthlyExpenses(Long userId, int year, int month) {
        log.debug("Fetching monthly expenses for user: {} for {}-{}", userId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<CategoryExpenseDTO> categoryExpenses = getExpensesByCategory(userId, startDate, endDate);
        BigDecimal totalAmount = categoryExpenses.stream()
            .map(CategoryExpenseDTO::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = categoryExpenses.stream()
            .mapToInt(CategoryExpenseDTO::getTransactionCount)
            .sum();

        String period = YearMonth.of(year, month).toString();

        DashboardExpenseDTO result = DashboardExpenseDTO.builder()
            .period(period)
            .totalAmount(totalAmount)
            .transactionCount(totalTransactions)
            .expensesByCategory(categoryExpenses)
            .build();

        log.debug("Monthly expenses retrieved for user: {} - Total: {}", userId, totalAmount);
        return result;
    }

    @Cacheable(value = "weeklyExpenses", key = "#userId + '-' + #startDate")
    public DashboardExpenseDTO getWeeklyExpenses(Long userId, LocalDate startDate) {
        log.debug("Fetching weekly expenses for user: {} starting from {}", userId, startDate);

        LocalDate endDate = startDate.plusDays(6);

        List<CategoryExpenseDTO> categoryExpenses = getExpensesByCategory(userId, startDate, endDate);
        BigDecimal totalAmount = categoryExpenses.stream()
            .map(CategoryExpenseDTO::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = categoryExpenses.stream()
            .mapToInt(CategoryExpenseDTO::getTransactionCount)
            .sum();

        String period = startDate + " to " + endDate;

        DashboardExpenseDTO result = DashboardExpenseDTO.builder()
            .period(period)
            .totalAmount(totalAmount)
            .transactionCount(totalTransactions)
            .expensesByCategory(categoryExpenses)
            .build();

        log.debug("Weekly expenses retrieved for user: {} - Total: {}", userId, totalAmount);
        return result;
    }

    public DashboardExpenseDTO getCurrentMonthExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        return getMonthlyExpenses(userId, now.getYear(), now.getMonthValue());
    }

    public DashboardExpenseDTO getCurrentYearExpenses(Long userId) {
        return getYearlyExpenses(userId, LocalDate.now().getYear());
    }

    public DashboardExpenseDTO getCurrentWeekExpenses(Long userId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return getWeeklyExpenses(userId, startOfWeek);
    }
}
