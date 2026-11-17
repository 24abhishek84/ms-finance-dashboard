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
