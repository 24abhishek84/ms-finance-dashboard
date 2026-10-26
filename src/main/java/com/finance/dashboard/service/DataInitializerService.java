package com.finance.dashboard.service;

import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataInitializerService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void initializeSampleDataForUser(User user) {
        List<Category> expenseCategories = createExpenseCategories(user);
        List<Category> incomeCategories = createIncomeCategories(user);

        categoryRepository.saveAll(expenseCategories);
        categoryRepository.saveAll(incomeCategories);

        List<Transaction> sampleTransactions = createSampleTransactions(user, expenseCategories, incomeCategories);
        transactionRepository.saveAll(sampleTransactions);
    }

    private List<Category> createExpenseCategories(User user) {
        List<Category> categories = new ArrayList<>();

        // Groceries parent category
        Category groceries = Category.builder()
                .name("Groceries")
                .type(Category.CategoryType.EXPENSE)
                .color("#FF6B6B")
                .user(user)
                .build();
        Category groceriesSaved = categoryRepository.save(groceries);

        Category coles = Category.builder()
                .name("Coles")
                .type(Category.CategoryType.EXPENSE)
                .color("#FFB3B3")
                .user(user)
                .parentCategory(groceriesSaved)
                .build();

        Category woolworths = Category.builder()
                .name("Woolworths")
                .type(Category.CategoryType.EXPENSE)
                .color("#FFB3B3")
                .user(user)
                .parentCategory(groceriesSaved)
                .build();

        Category sriKumaran = Category.builder()
                .name("Sri Kumaran")
                .type(Category.CategoryType.EXPENSE)
                .color("#FFB3B3")
                .user(user)
                .parentCategory(groceriesSaved)
                .build();

        categories.addAll(List.of(groceriesSaved, coles, woolworths, sriKumaran));

        // Fees parent category
        Category fees = Category.builder()
                .name("Fees")
                .type(Category.CategoryType.EXPENSE)
                .color("#4ECDC4")
                .user(user)
                .build();
        Category feesSaved = categoryRepository.save(fees);

        Category directDebit = Category.builder()
                .name("Direct Debit")
                .type(Category.CategoryType.EXPENSE)
                .color("#9EEDDF")
                .user(user)
                .parentCategory(feesSaved)
                .build();

        Category bankFees = Category.builder()
                .name("Bank Fees")
                .type(Category.CategoryType.EXPENSE)
                .color("#9EEDDF")
                .user(user)
                .parentCategory(feesSaved)
                .build();

        categories.addAll(List.of(feesSaved, directDebit, bankFees));

        // Fuel parent category
        Category fuel = Category.builder()
                .name("Fuel")
                .type(Category.CategoryType.EXPENSE)
                .color("#FFE66D")
                .user(user)
                .build();
        Category fuelSaved = categoryRepository.save(fuel);

        Category petrol = Category.builder()
                .name("Petrol Station")
                .type(Category.CategoryType.EXPENSE)
                .color("#FFF4B3")
                .user(user)
                .parentCategory(fuelSaved)
                .build();

        categories.addAll(List.of(fuelSaved, petrol));

        // Utilities
        Category utilities = Category.builder()
                .name("Utilities")
                .type(Category.CategoryType.EXPENSE)
                .color("#95E1D3")
                .user(user)
                .build();
        Category utilitiesSaved = categoryRepository.save(utilities);

        Category electricity = Category.builder()
                .name("Electricity")
                .type(Category.CategoryType.EXPENSE)
                .color("#C8F3EE")
                .user(user)
                .parentCategory(utilitiesSaved)
                .build();

        Category water = Category.builder()
                .name("Water")
                .type(Category.CategoryType.EXPENSE)
                .color("#C8F3EE")
                .user(user)
                .parentCategory(utilitiesSaved)
                .build();

        categories.addAll(List.of(utilitiesSaved, electricity, water));

        return categories;
    }

    private List<Category> createIncomeCategories(User user) {
        List<Category> categories = new ArrayList<>();

        // Salary parent category
        Category salary = Category.builder()
                .name("Income")
                .type(Category.CategoryType.INCOME)
                .color("#90EE90")
                .user(user)
                .build();
        Category salarySaved = categoryRepository.save(salary);

        Category monthlyPaycheck = Category.builder()
                .name("Salary")
                .type(Category.CategoryType.INCOME)
                .color("#C8E6C9")
                .user(user)
                .parentCategory(salarySaved)
                .build();

        Category bonus = Category.builder()
                .name("Bonus")
                .type(Category.CategoryType.INCOME)
                .color("#C8E6C9")
                .user(user)
                .parentCategory(salarySaved)
                .build();

        Category freelance = Category.builder()
                .name("Freelance")
                .type(Category.CategoryType.INCOME)
                .color("#C8E6C9")
                .user(user)
                .parentCategory(salarySaved)
                .build();

        categories.addAll(List.of(salarySaved, monthlyPaycheck, bonus, freelance));

        return categories;
    }

    private List<Transaction> createSampleTransactions(User user, List<Category> expenses, List<Category> incomes) {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Sample expense transactions
        transactions.add(Transaction.builder()
                .description("Weekly groceries at Coles")
                .amount(new BigDecimal("85.50"))
                .transactionDate(today.minusDays(2))
                .notes("Weekly shopping")
                .user(user)
                .category(expenses.stream().filter(c -> "Coles".equals(c.getName())).findFirst().orElse(expenses.get(0)))
                .build());

        transactions.add(Transaction.builder()
                .description("Woolworths groceries")
                .amount(new BigDecimal("62.30"))
                .transactionDate(today.minusDays(5))
                .notes("Milk, bread, eggs")
                .user(user)
                .category(expenses.stream().filter(c -> "Woolworths".equals(c.getName())).findFirst().orElse(expenses.get(0)))
                .build());

        transactions.add(Transaction.builder()
                .description("Fuel")
                .amount(new BigDecimal("55.00"))
                .transactionDate(today.minusDays(3))
                .notes("Full tank")
                .user(user)
                .category(expenses.stream().filter(c -> "Petrol Station".equals(c.getName())).findFirst().orElse(expenses.get(0)))
                .build());

        transactions.add(Transaction.builder()
                .description("Direct Debit - Insurance")
                .amount(new BigDecimal("120.00"))
                .transactionDate(today.minusDays(7))
                .notes("Monthly insurance payment")
                .user(user)
                .category(expenses.stream().filter(c -> "Direct Debit".equals(c.getName())).findFirst().orElse(expenses.get(0)))
                .build());

        // Sample income transactions
        transactions.add(Transaction.builder()
                .description("Monthly Salary")
                .amount(new BigDecimal("5000.00"))
                .transactionDate(today.minusDays(1))
                .notes("Monthly paycheck")
                .user(user)
                .category(incomes.stream().filter(c -> "Salary".equals(c.getName())).findFirst().orElse(incomes.get(0)))
                .build());

        transactions.add(Transaction.builder()
                .description("Freelance Project - Web Design")
                .amount(new BigDecimal("500.00"))
                .transactionDate(today.minusDays(10))
                .notes("Completed web design project")
                .user(user)
                .category(incomes.stream().filter(c -> "Freelance".equals(c.getName())).findFirst().orElse(incomes.get(0)))
                .build());

        return transactions;
    }
}
