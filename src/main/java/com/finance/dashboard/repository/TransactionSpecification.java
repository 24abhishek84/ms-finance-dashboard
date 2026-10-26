package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionSpecification {

    public static Specification<Transaction> byUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Transaction> byDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> cb.between(root.get("transactionDate"), startDate, endDate);
    }

    public static Specification<Transaction> byAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, cb) -> cb.between(root.get("amount"), minAmount, maxAmount);
    }

    public static Specification<Transaction> byCategoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> byDescription(String description) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Transaction> byMinAmount(BigDecimal amount) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), amount);
    }

    public static Specification<Transaction> byMaxAmount(BigDecimal amount) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), amount);
    }

}
