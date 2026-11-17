package com.finance.dashboard.config;

import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.Transaction;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.TransactionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initializing test data...");
                seedTestData();
                log.info("Test data initialization completed!");
            }
        };
    }

    public void seedTestData() {
        User testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .build();

        if (userRepository.findByEmail("test@example.com").isEmpty()) {
            testUser = userRepository.save(testUser);
            List<Category> parentCategories = createParentCategories(testUser);
            createTransactions(testUser, parentCategories);
        } else {
            testUser = userRepository.findByEmail("test@example.com").get();
            List<Category> parentCategories = createParentCategories(testUser);
            createTransactions(testUser, parentCategories);
        }

        // Clear all caches
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );
        log.info("Cache cleared after seeding data");
    }

    public void seedTestDataForUser(User user) {
        List<Category> parentCategories = createParentCategories(user);
        createTransactions(user, parentCategories);

        // Clear all caches
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );
        log.info("Cache cleared after seeding data for user: {}", user.getEmail());
    }

    private List<Category> createParentCategories(User user) {
        List<Category> parentCategories = new ArrayList<>();

        // Food & Dining
        Category food = categoryRepository.save(Category.builder()
                .name("Food & Dining")
                .description("All food and dining expenses")
                .type(Category.CategoryType.EXPENSE)
                .color("#FF6B6B")
                .user(user)
                .build());

        List<Category> foodSubcats = List.of(
            Category.builder().name("Groceries").type(Category.CategoryType.EXPENSE).color("#FF8787").user(user).parentCategory(food).build(),
            Category.builder().name("Restaurants").type(Category.CategoryType.EXPENSE).color("#FFB0B0").user(user).parentCategory(food).build(),
            Category.builder().name("Coffee").type(Category.CategoryType.EXPENSE).color("#FFD9D9").user(user).parentCategory(food).build()
        );
        categoryRepository.saveAll(foodSubcats);
        parentCategories.add(food);

        // Transportation
        Category transport = categoryRepository.save(Category.builder()
                .name("Transportation")
                .description("All travel and commuting expenses")
                .type(Category.CategoryType.EXPENSE)
                .color("#4ECDC4")
                .user(user)
                .build());

        List<Category> transportSubcats = List.of(
            Category.builder().name("Gas").type(Category.CategoryType.EXPENSE).color("#7FE5E0").user(user).parentCategory(transport).build(),
            Category.builder().name("Uber/Lyft").type(Category.CategoryType.EXPENSE).color("#A8F0EB").user(user).parentCategory(transport).build(),
            Category.builder().name("Parking").type(Category.CategoryType.EXPENSE).color("#D1F9F6").user(user).parentCategory(transport).build()
        );
        categoryRepository.saveAll(transportSubcats);
        parentCategories.add(transport);

        // Entertainment
        Category entertainment = categoryRepository.save(Category.builder()
                .name("Entertainment")
                .description("Movies, games, hobbies")
                .type(Category.CategoryType.EXPENSE)
                .color("#95E1D3")
                .user(user)
                .build());

        List<Category> entertainmentSubcats = List.of(
            Category.builder().name("Movies").type(Category.CategoryType.EXPENSE).color("#B3EFE5").user(user).parentCategory(entertainment).build(),
            Category.builder().name("Gaming").type(Category.CategoryType.EXPENSE).color("#D1F9F1").user(user).parentCategory(entertainment).build(),
            Category.builder().name("Subscriptions").type(Category.CategoryType.EXPENSE).color("#E9FCF9").user(user).parentCategory(entertainment).build()
        );
        categoryRepository.saveAll(entertainmentSubcats);
        parentCategories.add(entertainment);

        // Utilities
        Category utilities = categoryRepository.save(Category.builder()
                .name("Utilities")
                .description("Electric, water, internet")
                .type(Category.CategoryType.EXPENSE)
                .color("#A8D8EA")
                .user(user)
                .build());

        List<Category> utilitiesSubcats = List.of(
            Category.builder().name("Electricity").type(Category.CategoryType.EXPENSE).color("#C6E8F5").user(user).parentCategory(utilities).build(),
            Category.builder().name("Water").type(Category.CategoryType.EXPENSE).color("#DDF4FB").user(user).parentCategory(utilities).build(),
            Category.builder().name("Internet").type(Category.CategoryType.EXPENSE).color("#EEF9FE").user(user).parentCategory(utilities).build()
        );
        categoryRepository.saveAll(utilitiesSubcats);
        parentCategories.add(utilities);

        // Health & Fitness
        Category health = categoryRepository.save(Category.builder()
                .name("Health & Fitness")
                .description("Medical and gym expenses")
                .type(Category.CategoryType.EXPENSE)
                .color("#F38181")
                .user(user)
                .build());

        List<Category> healthSubcats = List.of(
            Category.builder().name("Gym Membership").type(Category.CategoryType.EXPENSE).color("#F7B4B4").user(user).parentCategory(health).build(),
            Category.builder().name("Medical").type(Category.CategoryType.EXPENSE).color("#FBD9D9").user(user).parentCategory(health).build()
        );
        categoryRepository.saveAll(healthSubcats);
        parentCategories.add(health);

        // Income
        Category income = categoryRepository.save(Category.builder()
                .name("Income")
                .description("Salary and other income")
                .type(Category.CategoryType.INCOME)
                .color("#90EE90")
                .user(user)
                .build());
        parentCategories.add(income);

        return parentCategories;
    }

    private void createTransactions(User user, List<Category> parentCategories) {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusMonths(2);

        // Sample transaction data
        Object[][] transactionData = {
            // Food & Dining (7 transactions)
            {"Whole Foods", 85.50, 0, 0},
            {"Trader Joe's", 62.30, 0, 1},
            {"Starbucks", 5.50, 0, 2},
            {"McDonald's", 12.99, 0, 1},
            {"Olive Garden", 45.75, 0, 1},
            {"Kroger", 120.45, 0, 0},
            {"Dunkin Donuts", 8.75, 0, 2},

            // Transportation (5 transactions)
            {"Shell Gas", 55.00, 1, 0},
            {"Uber", 18.50, 1, 1},
            {"Parking", 10.00, 1, 2},
            {"Costco Gas", 60.50, 1, 0},
            {"Lyft", 22.00, 1, 1},

            // Entertainment (5 transactions)
            {"AMC Cinemas", 15.00, 2, 0},
            {"PlayStation Store", 59.99, 2, 1},
            {"Netflix", 15.99, 2, 2},
            {"Disney+", 10.99, 2, 2},
            {"Cinemark", 18.50, 2, 0},

            // Utilities (3 transactions)
            {"Electric Bill", 125.00, 3, 0},
            {"Water Bill", 45.00, 3, 1},
            {"Comcast Internet", 89.99, 3, 2},

            // Health & Fitness (3 transactions)
            {"Gold's Gym", 55.00, 4, 0},
            {"CVS Pharmacy", 35.50, 4, 1},
            {"Walgreens", 28.75, 4, 1},

            // Income (2 transactions)
            {"Monthly Salary", -3500.00, 5, 0},
            {"Freelance Work", -500.00, 5, 0}
        };

        int dayOffset = 0;
        for (Object[] data : transactionData) {
            String description = (String) data[0];
            BigDecimal amount = BigDecimal.valueOf((Double) data[1]);
            int parentCatIndex = (Integer) data[2];
            int subCatIndex = (Integer) data[3];

            Category parentCat = parentCategories.get(parentCatIndex);
            Category category;

            if (parentCat.getSubCategories() != null && !parentCat.getSubCategories().isEmpty()) {
                category = parentCat.getSubCategories().get(subCatIndex);
            } else {
                category = parentCat;
            }

            Transaction transaction = Transaction.builder()
                    .description(description)
                    .amount(amount)
                    .transactionDate(baseDate.plusDays(dayOffset))
                    .notes("Test transaction")
                    .user(user)
                    .category(category)
                    .build();

            transactions.add(transaction);
            dayOffset += 2;
        }

        transactionRepository.saveAll(transactions);
        log.info("Created {} test transactions", transactions.size());
    }
}
