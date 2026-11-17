package com.finance.dashboard.event;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.finance.dashboard.entity.User;
import com.finance.dashboard.entity.Category;

public class CategoryEventTest {
    
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .email("testuser@example.com")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Groceries")
                .description("Food and groceries")
                .type(Category.CategoryType.INCOME)
                .color("blue")
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Should emit CATEGORY_CREATED event from category creation")
    void testCategoryCreatedEvent() {
        // Act
        CategoryEvent event = CategoryEvent.fromCategory(1L, testCategory, CategoryEvent.EventType.CATEGORY_CREATED);
    
        // Assert
        assertNotNull(event);
        assertNotNull(event.getEventId());
        assertEquals(CategoryEvent.EventType.CATEGORY_CREATED, event.getEventType());
        assertEquals(1L, event.getCategoryId());
        assertEquals(1L, event.getUserId());
        assertEquals("Groceries", event.getCategoryName());
        assertEquals("INCOME", event.getCategoryType());
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
