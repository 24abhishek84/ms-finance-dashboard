# Financial Dashboard - Full Stack Setup Guide

## Project Structure

```
financial-dashboard/
├── backend/                          # Spring Boot Microservice
│   ├── src/main/java/com/java/learning/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   ├── config/
│   │   └── FinancialDashboardApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   └── build.gradle
│
└── frontend/                         # React + TypeScript + GraphQL
    ├── src/
    │   ├── components/
    │   ├── pages/
    │   ├── graphql/
    │   │   ├── queries.ts
    │   │   └── mutations.ts
    │   ├── types/
    │   └── App.tsx
    ├── package.json
    └── tsconfig.json
```

---

## PART 1: SPRING BOOT BACKEND

### 1.1 Update build.gradle

Add these dependencies to your existing `build.gradle`:

```gradle
dependencies {
    // Existing dependencies...
    
    // Kafka
    implementation 'org.springframework.kafka:spring-kafka'
    
    // Caching
    implementation 'com.github.ben-manes.caffeine:caffeine'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    
    // Serialization
    implementation 'org.apache.avro:avro:1.11.3'
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Flyway (migrations)
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
    
    // Crypto & JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
}
```

### 1.2 Application Configuration (application.yml)

```yaml
spring:
  application:
    name: financial-dashboard-ms
  
  datasource:
    url: jdbc:postgresql://localhost:5432/financial_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      bootstrap-servers: localhost:9092
      group-id: financial-dashboard-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
  
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m

server:
  port: 8080
  servlet:
    context-path: /api

# Security
app:
  jwt:
    secret: your-secret-key-min-256-bits-long-for-HS256
    expiration: 86400000  # 24 hours in milliseconds
```

### 1.3 Entity Classes

#### User.java
```java
package com.java.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Transaction> transactions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Category> categories;
}
```

#### Category.java
```java
package com.java.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type; // INCOME, EXPENSE
    
    @Column(nullable = false, length = 7)
    @Builder.Default
    private String color = "#FF5733";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Transaction> transactions;
    
    public enum CategoryType {
        INCOME, EXPENSE
    }
}
```

#### Transaction.java
```java
package com.java.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDate transactionDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

### 1.4 DTOs

#### UserRequestDTO.java
```java
package com.java.learning.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;
}
```

#### UserResponseDTO.java
```java
package com.java.learning.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean active;
    private LocalDateTime createdAt;
}
```

#### CategoryRequestDTO.java
```java
package com.java.learning.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @NotNull(message = "Category type is required")
    private String type; // INCOME or EXPENSE
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format")
    private String color;
}
```

#### TransactionRequestDTO.java
```java
package com.java.learning.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    
    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Amount exceeds maximum")
    private BigDecimal amount;
    
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in future")
    private LocalDate transactionDate;
    
    @Size(max = 500)
    private String notes;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
```

#### TransactionResponseDTO.java
```java
package com.java.learning.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String notes;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 1.5 Repository Interfaces

#### UserRepository.java
```java
package com.java.learning.repository;

import com.java.learning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### CategoryRepository.java
```java
package com.java.learning.repository;

import com.java.learning.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    List<Category> findByUserIdAndType(Long userId, String type);
}
```

#### TransactionRepository.java
```java
package com.java.learning.repository;

import com.java.learning.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId")
    List<Transaction> findByUserIdAndCategoryId(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId
    );
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.category.type = :categoryType")
    BigDecimal sumByUserIdAndCategoryType(
        @Param("userId") Long userId,
        @Param("categoryType") String categoryType
    );
}
```

### 1.6 Service Classes

#### UserService.java
```java
package com.java.learning.service;

import com.java.learning.dto.UserRequestDTO;
import com.java.learning.dto.UserResponseDTO;
import com.java.learning.entity.User;
import com.java.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UserResponseDTO registerUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .active(true)
            .build();
        
        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }
    
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::mapToResponseDTO);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .active(user.getActive())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
```

#### CategoryService.java
```java
package com.java.learning.service;

import com.java.learning.dto.CategoryRequestDTO;
import com.java.learning.entity.Category;
import com.java.learning.entity.User;
import com.java.learning.repository.CategoryRepository;
import com.java.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    public Category createCategory(Long userId, CategoryRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Category category = Category.builder()
            .name(request.getName())
            .description(request.getDescription())
            .type(Category.CategoryType.valueOf(request.getType()))
            .color(request.getColor())
            .user(user)
            .build();
        
        return categoryRepository.save(category);
    }
    
    @Cacheable(value = "userCategories", key = "#userId")
    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findByUserId(userId);
    }
    
    public List<Category> getUserCategoriesByType(Long userId, String type) {
        return categoryRepository.findByUserIdAndType(userId, type);
    }
    
    public Category updateCategory(Long categoryId, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());
        
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
```

#### TransactionService.java
```java
package com.java.learning.service;

import com.java.learning.dto.TransactionRequestDTO;
import com.java.learning.dto.TransactionResponseDTO;
import com.java.learning.entity.Category;
import com.java.learning.entity.Transaction;
import com.java.learning.entity.User;
import com.java.learning.repository.CategoryRepository;
import com.java.learning.repository.TransactionRepository;
import com.java.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        Transaction transaction = Transaction.builder()
            .description(request.getDescription())
            .amount(request.getAmount())
            .transactionDate(request.getTransactionDate())
            .notes(request.getNotes())
            .user(user)
            .category(category)
            .build();
        
        Transaction saved = transactionRepository.save(transaction);
        return mapToResponseDTO(saved);
    }
    
    @Cacheable(value = "userTransactions", key = "#userId")
    public List<TransactionResponseDTO> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public List<TransactionResponseDTO> getTransactionsByDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    public TransactionResponseDTO updateTransaction(
            Long transactionId, TransactionRequestDTO request) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());
        transaction.setCategory(category);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }
    
    public void deleteTransaction(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }
    
    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
            .id(transaction.getId())
            .description(transaction.getDescription())
            .amount(transaction.getAmount())
            .transactionDate(transaction.getTransactionDate())
            .notes(transaction.getNotes())
            .userId(transaction.getUser().getId())
            .categoryId(transaction.getCategory().getId())
            .categoryName(transaction.getCategory().getName())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(transaction.getUpdatedAt())
            .build();
    }
}
```

### 1.7 JWT & Security Configuration

#### JwtTokenProvider.java
```java
package com.java.learning.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;
    
    public String generateToken(Long userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key)
            .compact();
    }
    
    public Long getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return Long.parseLong(claims.getSubject());
    }
    
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### SecurityConfig.java
```java
package com.java.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 1.8 Controllers

#### AuthController.java
```java
package com.java.learning.controller;

import com.java.learning.config.JwtTokenProvider;
import com.java.learning.dto.UserRequestDTO;
import com.java.learning.dto.UserResponseDTO;
import com.java.learning.entity.User;
import com.java.learning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO user = userService.registerUser(request);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", user);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!userService.validatePassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        return ResponseEntity.ok(jwtTokenProvider.validateToken(jwt));
    }
}

class LoginRequest {
    public String email;
    public String password;
}
```

#### CategoryController.java
```java
package com.java.learning.controller;

import com.java.learning.dto.CategoryRequestDTO;
import com.java.learning.entity.Category;
import com.java.learning.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CategoryRequestDTO request) {
        Category category = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getUserCategories(
            @RequestHeader("X-User-Id") Long userId) {
        List<Category> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> getCategoriesByType(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String type) {
        List<Category> categories = categoryService.getUserCategoriesByType(userId, type);
        return ResponseEntity.ok(categories);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO request) {
        Category category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### TransactionController.java
```java
package com.java.learning.controller;

import com.java.learning.dto.TransactionRequestDTO;
import com.java.learning.dto.TransactionResponseDTO;
import com.java.learning.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO transaction = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getUserTransactions(
            @RequestHeader("X-User-Id") Long userId) {
        List<TransactionResponseDTO> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<TransactionResponseDTO> transactions = 
            transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO transaction = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(transaction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 1.9 Flyway Database Migration

Create file: `src/main/resources/db/migration/V1__Initial_Schema.sql`

```sql
-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories Table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#FF5733',
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Transactions Table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    notes TEXT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
```

---

## PART 2: REACT + TYPESCRIPT + GRAPHQL FRONTEND

### 2.1 Project Setup

```bash
# Create React project
npx create-react-app financial-dashboard --template typescript

# Navigate to project
cd financial-dashboard

# Install dependencies
npm install @apollo/client graphql
npm install recharts
npm install axios
npm install react-router-dom
npm install -D @types/react-router-dom
```

### 2.2 package.json Configuration

```json
{
  "name": "financial-dashboard",
  "version": "0.1.0",
  "private": true,
  "dependencies": {
    "@apollo/client": "^3.8.0",
    "graphql": "^16.8.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.16.0",
    "recharts": "^2.10.0",
    "axios": "^1.6.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "typescript": "^5.2.0"
  },
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject"
  }
}
```

### 2.3 TypeScript Types

#### types/index.ts
```typescript
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  type: 'INCOME' | 'EXPENSE';
  color: string;
  userId: number;
}

export interface Transaction {
  id: number;
  description: string;
  amount: number;
  transactionDate: string;
  notes?: string;
  userId: number;
  categoryId: number;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardStats {
  totalIncome: number;
  totalExpense: number;
  balance: number;
  transactionCount: number;
}
```

### 2.4 Apollo Client Setup

#### client/apolloClient.ts
```typescript
import { ApolloClient, InMemoryCache, HttpLink, ApolloLink } from '@apollo/client';

const httpLink = new HttpLink({
  uri: 'http://localhost:8080/api/graphql',
  credentials: 'include',
});

const authLink = new ApolloLink((operation, forward) => {
  const token = localStorage.getItem('authToken');
  operation.setContext({
    headers: {
      authorization: token ? `Bearer ${token}` : '',
      'X-User-Id': localStorage.getItem('userId') || '',
    },
  });
  return forward(operation);
});

export const apolloClient = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache(),
});
```

### 2.5 GraphQL Queries & Mutations

#### graphql/queries.ts
```typescript
import { gql } from '@apollo/client';

export const GET_TRANSACTIONS = gql`
  query GetTransactions($userId: ID!) {
    transactions(userId: $userId) {
      id
      description
      amount
      transactionDate
      notes
      categoryId
      categoryName
      createdAt
    }
  }
`;

export const GET_TRANSACTIONS_BY_DATE = gql`
  query GetTransactionsByDate($userId: ID!, $startDate: String!, $endDate: String!) {
    transactionsByDateRange(userId: $userId, startDate: $startDate, endDate: $endDate) {
      id
      description
      amount
      transactionDate
      notes
      categoryId
      categoryName
      createdAt
    }
  }
`;

export const GET_CATEGORIES = gql`
  query GetCategories($userId: ID!) {
    categories(userId: $userId) {
      id
      name
      description
      type
      color
    }
  }
`;

export const GET_DASHBOARD_STATS = gql`
  query GetDashboardStats($userId: ID!) {
    dashboardStats(userId: $userId) {
      totalIncome
      totalExpense
      balance
      transactionCount
    }
  }
`;
```

#### graphql/mutations.ts
```typescript
import { gql } from '@apollo/client';

export const CREATE_TRANSACTION = gql`
  mutation CreateTransaction($input: TransactionInput!) {
    createTransaction(input: $input) {
      id
      description
      amount
      transactionDate
      categoryId
      categoryName
    }
  }
`;

export const UPDATE_TRANSACTION = gql`
  mutation UpdateTransaction($id: ID!, $input: TransactionInput!) {
    updateTransaction(id: $id, input: $input) {
      id
      description
      amount
      transactionDate
      categoryId
    }
  }
`;

export const DELETE_TRANSACTION = gql`
  mutation DeleteTransaction($id: ID!) {
    deleteTransaction(id: $id)
  }
`;

export const CREATE_CATEGORY = gql`
  mutation CreateCategory($input: CategoryInput!) {
    createCategory(input: $input) {
      id
      name
      type
      color
    }
  }
`;
```

### 2.6 Authentication Context

#### context/AuthContext.tsx
```typescript
import React, { createContext, useState, useContext, ReactNode } from 'react';
import axios from 'axios';

interface AuthContextType {
  user: any | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, firstName: string, lastName: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));

  const login = async (email: string, password: string) => {
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        email,
        password,
      });
      
      const { token, userId, firstName } = response.data;
      setToken(token);
      setUser({ id: userId, email, firstName });
      localStorage.setItem('authToken', token);
      localStorage.setItem('userId', userId);
    } catch (error) {
      throw new Error('Login failed');
    }
  };

  const register = async (email: string, password: string, firstName: string, lastName: string) => {
    try {
      await axios.post('http://localhost:8080/api/auth/register', {
        email,
        password,
        firstName,
        lastName,
      });
      // Auto-login after registration
      await login(email, password);
    } catch (error) {
      throw new Error('Registration failed');
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
```

### 2.7 Components

#### components/LoginForm.tsx
```typescript
import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div className="login-form">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Login</button>
      </form>
      {error && <p className="error">{error}</p>}
    </div>
  );
};
```

#### components/Dashboard.tsx
```typescript
import React, { useState } from 'react';
import { useQuery } from '@apollo/client';
import { GET_TRANSACTIONS, GET_CATEGORIES } from '../graphql/queries';
import { useAuth } from '../context/AuthContext';
import { TransactionChart } from './TransactionChart';
import { TransactionList } from './TransactionList';
import { CategoryFilter } from './CategoryFilter';

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState({ start: '', end: '' });

  const { data: transactionsData, loading: transLoading } = useQuery(GET_TRANSACTIONS, {
    variables: { userId: user?.id },
  });

  const { data: categoriesData } = useQuery(GET_CATEGORIES, {
    variables: { userId: user?.id },
  });

  if (transLoading) return <div>Loading...</div>;

  const transactions = transactionsData?.transactions || [];
  const categories = categoriesData?.categories || [];

  return (
    <div className="dashboard">
      <h1>Financial Dashboard</h1>
      
      <div className="filters">
        <CategoryFilter 
          categories={categories}
          selected={selectedCategory}
          onChange={setSelectedCategory}
        />
        <div className="date-range">
          <input
            type="date"
            value={dateRange.start}
            onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
            placeholder="Start Date"
          />
          <input
            type="date"
            value={dateRange.end}
            onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
            placeholder="End Date"
          />
        </div>
      </div>

      <TransactionChart transactions={transactions} />
      <TransactionList 
        transactions={transactions}
        selectedCategory={selectedCategory}
      />
    </div>
  );
};
```

#### components/TransactionChart.tsx
```typescript
import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Transaction } from '../types';

interface Props {
  transactions: Transaction[];
}

export const TransactionChart: React.FC<Props> = ({ transactions }) => {
  // Group transactions by category
  const data = transactions.reduce((acc: any, trans) => {
    const existing = acc.find((item: any) => item.category === trans.categoryName);
    if (existing) {
      existing.amount += trans.amount;
    } else {
      acc.push({ category: trans.categoryName, amount: trans.amount });
    }
    return acc;
  }, []);

  return (
    <div className="chart-container">
      <h2>Transactions by Category</h2>
      <ResponsiveContainer width="100%" height={400}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="category" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="amount" fill="#8884d8" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};
```

#### components/TransactionList.tsx
```typescript
import React from 'react';
import { Transaction } from '../types';

interface Props {
  transactions: Transaction[];
  selectedCategory: string | null;
}

export const TransactionList: React.FC<Props> = ({ transactions, selectedCategory }) => {
  const filtered = selectedCategory
    ? transactions.filter((t) => t.categoryId === parseInt(selectedCategory))
    : transactions;

  return (
    <div className="transaction-list">
      <h2>Transactions</h2>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Description</th>
            <th>Category</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((trans) => (
            <tr key={trans.id}>
              <td>{trans.transactionDate}</td>
              <td>{trans.description}</td>
              <td>{trans.categoryName}</td>
              <td>${trans.amount.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

#### components/CategoryFilter.tsx
```typescript
import React from 'react';
import { Category } from '../types';

interface Props {
  categories: Category[];
  selected: string | null;
  onChange: (categoryId: string | null) => void;
}

export const CategoryFilter: React.FC<Props> = ({ categories, selected, onChange }) => {
  return (
    <div className="category-filter">
      <label>Filter by Category:</label>
      <select value={selected || ''} onChange={(e) => onChange(e.target.value || null)}>
        <option value="">All Categories</option>
        {categories.map((cat) => (
          <option key={cat.id} value={cat.id}>
            {cat.name}
          </option>
        ))}
      </select>
    </div>
  );
};
```

### 2.8 App.tsx

```typescript
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ApolloProvider } from '@apollo/client';
import { AuthProvider, useAuth } from './context/AuthContext';
import { apolloClient } from './client/apolloClient';
import { LoginForm } from './components/LoginForm';
import { Dashboard } from './components/Dashboard';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

function App() {
  return (
    <ApolloProvider client={apolloClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginForm />} />
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              } 
            />
            <Route path="/" element={<Navigate to="/dashboard" />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ApolloProvider>
  );
}

export default App;
```

---

## Setup Instructions

### Backend Setup:
1. Add dependencies to `build.gradle`
2. Create entities in `src/main/java/com/java/learning/entity/`
3. Create DTOs in `src/main/java/com/java/learning/dto/`
4. Create repositories in `src/main/java/com/java/learning/repository/`
5. Create services in `src/main/java/com/java/learning/service/`
6. Create controllers in `src/main/java/com/java/learning/controller/`
7. Add Flyway migration SQL file
8. Create `application.yml` with database config
9. Run: `./gradlew build` then `./gradlew bootRun`

### Frontend Setup:
1. Create React project with TypeScript
2. Install Apollo Client, Recharts, Axios
3. Create types in `src/types/`
4. Set up Apollo Client in `src/client/`
5. Create GraphQL queries/mutations in `src/graphql/`
6. Create Context for authentication
7. Create components
8. Update `App.tsx`
9. Run: `npm start`

### PostgreSQL Setup:
```sql
CREATE DATABASE financial_db;
```

### Kafka Setup (Optional for now):
```bash
docker run -d \
  --name kafka \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:latest
```

---

## Next Steps:
1. Implement GraphQL resolvers in Spring backend
2. Add more sophisticated filtering/aggregation queries
3. Add transaction export functionality (CSV/PDF)
4. Implement budget tracking and alerts
5. Add real-time notifications via WebSocket or Server-Sent Events
6. Implement recurring transactions
7. Add multi-currency support
