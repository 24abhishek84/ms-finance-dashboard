# Kafka Event Integration Guide for CategoryService

This guide provides step-by-step instructions to implement Kafka event publishing for the CategoryService. Follow the steps below to add event-driven architecture to your application.

## Prerequisites

✅ Kafka dependency is already included in `build.gradle`:
```gradle
implementation 'org.springframework.kafka:spring-kafka'
```

✅ Kafka configuration is already in `application.yml`:
```yaml
spring:
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
```

## Step 1: Create CategoryEvent Class

Create file: `src/main/java/com/finance/dashboard/event/CategoryEvent.java`

```java
package com.finance.dashboard.event;

import com.finance.dashboard.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EventType {
        CATEGORY_CREATED,
        CATEGORY_UPDATED,
        CATEGORY_DELETED
    }

    private String eventId;
    private EventType eventType;
    private Long categoryId;
    private Long userId;
    private String categoryName;
    private String categoryType;
    private String color;
    private String description;
    private Long parentCategoryId;
    private LocalDateTime timestamp;
    private String source;

    // Factory method to create event from Category entity
    public static CategoryEvent fromCategory(Long userId, Category category, EventType eventType) {
        return CategoryEvent.builder()
            .eventId(java.util.UUID.randomUUID().toString())
            .eventType(eventType)
            .categoryId(category.getId())
            .userId(userId)
            .categoryName(category.getName())
            .categoryType(category.getType().toString())
            .color(category.getColor())
            .description(category.getDescription())
            .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
            .timestamp(LocalDateTime.now())
            .source("CategoryService")
            .build();
    }
}
```

## Step 2: Create CategoryEventProducer

Create file: `src/main/java/com/finance/dashboard/event/producer/CategoryEventProducer.java`

```java
package com.finance.dashboard.event.producer;

import com.finance.dashboard.event.CategoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventProducer {

    private final KafkaTemplate<String, CategoryEvent> kafkaTemplate;

    @Value("${kafka.topics.category:category-events}")
    private String categoryTopic;

    public void publishCategoryEvent(CategoryEvent event) {
        try {
            log.info("Publishing category event: eventId={}, type={}, categoryId={}",
                event.getEventId(), event.getEventType(), event.getCategoryId());

            kafkaTemplate.send(categoryTopic, event.getEventId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish category event: eventId={}", event.getEventId(), ex);
                    } else {
                        log.info("Successfully published category event: eventId={}, partition={}, offset={}",
                            event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });
        } catch (Exception e) {
            log.error("Exception occurred while publishing category event", e);
            throw new RuntimeException("Failed to publish category event", e);
        }
    }
}
```

## Step 3: Create Kafka Producer Configuration

Create file: `src/main/java/com/finance/dashboard/config/KafkaProducerConfig.java`

```java
package com.finance.dashboard.config;

import com.finance.dashboard.event.CategoryEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, CategoryEvent> categoryEventKafkaTemplate() {
        return new KafkaTemplate<>(categoryEventProducerFactory());
    }

    private ProducerFactory<String, CategoryEvent> categoryEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
```

## Step 4: Update application.yml

Add the Kafka topic configuration to `src/main/resources/application.yml`:

```yaml
kafka:
  topics:
    category: category-events
```

Insert it after the `spring.kafka` section:

```yaml
spring:
  # ... existing config ...
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

kafka:
  topics:
    category: category-events

# ... rest of config ...
```

## Step 5: Update CategoryService

Update file: `src/main/java/com/finance/dashboard/service/CategoryService.java`

Add imports at the top:
```java
import com.finance.dashboard.event.CategoryEvent;
import com.finance.dashboard.event.producer.CategoryEventProducer;
import org.springframework.cache.annotation.CacheEvict;
```

Inject the producer:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryEventProducer eventProducer;  // ADD THIS LINE
```

Update the `createCategory()` method:
```java
public Category createCategory(Long userId, CategoryRequestDTO request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Category.CategoryBuilder builder = Category.builder()
        .name(request.getName())
        .description(request.getDescription())
        .type(Category.CategoryType.valueOf(request.getType()))
        .color(request.getColor())
        .user(user);

    if (request.getParentCategoryId() != null) {
        Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
        builder.parentCategory(parentCategory);
    }

    Category category = builder.build();
    Category savedCategory = categoryRepository.save(category);

    // Publish category created event
    CategoryEvent event = CategoryEvent.fromCategory(userId, savedCategory, CategoryEvent.EventType.CATEGORY_CREATED);
    eventProducer.publishCategoryEvent(event);

    return savedCategory;
}
```

Update the `updateCategory()` method:
```java
@CacheEvict(value = "userCategories", allEntries = true)
public Category updateCategory(Long categoryId, CategoryRequestDTO request) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    Long userId = category.getUser().getId();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setColor(request.getColor());

    if (request.getParentCategoryId() != null) {
        Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
        category.setParentCategory(parentCategory);
    } else {
        category.setParentCategory(null);
    }

    Category updatedCategory = categoryRepository.save(category);

    // Publish category updated event
    CategoryEvent event = CategoryEvent.fromCategory(userId, updatedCategory, CategoryEvent.EventType.CATEGORY_UPDATED);
    eventProducer.publishCategoryEvent(event);

    return updatedCategory;
}
```

Update the `deleteCategory()` method:
```java
@CacheEvict(value = "userCategories", allEntries = true)
public void deleteCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("Category not found"));

    Long userId = category.getUser().getId();
    categoryRepository.deleteById(categoryId);

    // Publish category deleted event
    CategoryEvent event = CategoryEvent.fromCategory(userId, category, CategoryEvent.EventType.CATEGORY_DELETED);
    eventProducer.publishCategoryEvent(event);
}
```

## Step 6: Write Unit Tests

### Test CategoryEvent Factory Method

Create file: `src/test/java/com/finance/dashboard/event/CategoryEventTest.java`

```java
package com.finance.dashboard.event;

import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryEventTest {

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("John")
            .build();

        testCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(Category.CategoryType.EXPENSE)
            .color("#FF6B6B")
            .description("Food and groceries")
            .user(testUser)
            .build();
    }

    @Test
    @DisplayName("Should create CATEGORY_CREATED event from category")
    void testFromCategoryCreatedEvent() {
        // Act
        CategoryEvent event = CategoryEvent.fromCategory(1L, testCategory, CategoryEvent.EventType.CATEGORY_CREATED);

        // Assert
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(CategoryEvent.EventType.CATEGORY_CREATED, event.getEventType());
        assertEquals(1L, event.getCategoryId());
        assertEquals(1L, event.getUserId());
        assertEquals("Groceries", event.getCategoryName());
        assertEquals("EXPENSE", event.getCategoryType());
    }

    @Test
    @DisplayName("Should generate unique event IDs")
    void testEventIdsAreUnique() {
        // Act
        CategoryEvent event1 = CategoryEvent.fromCategory(1L, testCategory, CategoryEvent.EventType.CATEGORY_CREATED);
        CategoryEvent event2 = CategoryEvent.fromCategory(1L, testCategory, CategoryEvent.EventType.CATEGORY_CREATED);

        // Assert
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }
}
```

### Test CategoryEventProducer

Create file: `src/test/java/com/finance/dashboard/event/producer/CategoryEventProducerTest.java`

```java
package com.finance.dashboard.event.producer;

import com.finance.dashboard.event.CategoryEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryEventProducerTest {

    @Mock
    private KafkaTemplate<String, CategoryEvent> kafkaTemplate;

    @InjectMocks
    private CategoryEventProducer categoryEventProducer;

    @Test
    @DisplayName("Should publish category event successfully")
    void testPublishCategoryEvent() {
        // Arrange
        CategoryEvent event = CategoryEvent.builder()
            .eventId("test-event-123")
            .eventType(CategoryEvent.EventType.CATEGORY_CREATED)
            .categoryId(1L)
            .userId(1L)
            .categoryName("Groceries")
            .categoryType("EXPENSE")
            .color("#FF6B6B")
            .description("Food and groceries")
            .timestamp(LocalDateTime.now())
            .source("CategoryService")
            .build();

        ReflectionTestUtils.setField(categoryEventProducer, "categoryTopic", "category-events");

        CompletableFuture<SendResult<String, CategoryEvent>> future = CompletableFuture.completedFuture(
            new SendResult<>(null, null)
        );
        when(kafkaTemplate.send(anyString(), anyString(), any(CategoryEvent.class)))
            .thenReturn(future);

        // Act
        categoryEventProducer.publishCategoryEvent(event);

        // Assert
        verify(kafkaTemplate, times(1)).send("category-events", event.getEventId(), event);
    }

    @Test
    @DisplayName("Should throw exception when Kafka send fails synchronously")
    void testPublishCategoryEventThrowsException() {
        // Arrange
        CategoryEvent event = CategoryEvent.builder()
            .eventId("test-event-789")
            .eventType(CategoryEvent.EventType.CATEGORY_DELETED)
            .categoryId(3L)
            .userId(1L)
            .categoryName("Travel")
            .categoryType("EXPENSE")
            .build();

        ReflectionTestUtils.setField(categoryEventProducer, "categoryTopic", "category-events");

        when(kafkaTemplate.send(anyString(), anyString(), any(CategoryEvent.class)))
            .thenThrow(new RuntimeException("Kafka connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            categoryEventProducer.publishCategoryEvent(event);
        });
    }
}
```

### Update CategoryServiceTest

Update file: `src/test/java/com/finance/dashboard/service/CategoryServiceTest.java`

Add imports:
```java
import com.finance.dashboard.event.CategoryEvent;
import com.finance.dashboard.event.producer.CategoryEventProducer;
import org.mockito.ArgumentCaptor;
```

Add mock and inject:
```java
@Mock
private CategoryEventProducer eventProducer;  // ADD THIS

@InjectMocks
private CategoryService categoryService;
```

Add test for event publishing on create:
```java
@Test
@DisplayName("Should publish CATEGORY_CREATED event when creating category")
void testCreateCategoryPublishesEvent() {
    // Arrange
    CategoryRequestDTO request = new CategoryRequestDTO();
    request.setName("Groceries");
    request.setType("EXPENSE");
    request.setColor("#FF6B6B");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    // Act
    Category result = categoryService.createCategory(1L, request);

    // Assert
    ArgumentCaptor<CategoryEvent> eventCaptor = ArgumentCaptor.forClass(CategoryEvent.class);
    verify(eventProducer, times(1)).publishCategoryEvent(eventCaptor.capture());

    CategoryEvent publishedEvent = eventCaptor.getValue();
    assertEquals(CategoryEvent.EventType.CATEGORY_CREATED, publishedEvent.getEventType());
    assertEquals(1L, publishedEvent.getCategoryId());
    assertEquals(1L, publishedEvent.getUserId());
}
```

## Step 7: Verify Implementation

Run tests to ensure everything works:
```bash
./gradlew test
```

Expected output:
```
BUILD SUCCESSFUL
22 tests completed
```

## Event Payload Example

When a category is created, updated, or deleted, an event like this is published:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "CATEGORY_CREATED",
  "categoryId": 1,
  "userId": 1,
  "categoryName": "Groceries",
  "categoryType": "EXPENSE",
  "color": "#FF6B6B",
  "description": "Food and groceries",
  "parentCategoryId": null,
  "timestamp": "2024-11-11T10:30:00",
  "source": "CategoryService"
}
```

## Next Steps

After implementing the basic event publishing:

1. **Create a Consumer**: Listen to `category-events` topic in another service
2. **Add Event Sourcing**: Store events in a separate table for audit trails
3. **Implement Dead Letter Queue**: Handle failed event deliveries
4. **Add Metrics**: Monitor event publishing using Spring Boot Actuator
5. **Add Retry Logic**: Implement exponential backoff for failed events

## Troubleshooting

### Kafka Connection Failed
- Ensure Kafka broker is running on `localhost:9092`
- Check docker-compose logs if using Docker

### Event Not Published
- Verify `categoryTopic` property is set in `application.yml`
- Check application logs for errors
- Ensure `KafkaTemplate` bean is properly created

### Serialization Errors
- Verify `CategoryEvent` has Lombok annotations
- Check that all fields have proper getters/setters
- Ensure JSON serializer is in classpath

