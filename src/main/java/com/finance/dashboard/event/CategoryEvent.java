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
