package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategoryRequestDTO;
import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.event.producer.CategoryEventProducer;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.finance.dashboard.event.CategoryEvent;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryEventProducer eventProducer;

    public Category createCategory(Long userId, CategoryRequestDTO request) {
        log.debug("Creating category for user: {} with name: {}", userId, request.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for userId: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        Category.CategoryBuilder builder = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(Category.CategoryType.valueOf(request.getType()))
                .color(request.getColor())
                .user(user);

        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> {
                        log.error("Parent category not found for categoryId: {}", request.getParentCategoryId());
                        return new IllegalArgumentException("Parent category not found");
                    });
            builder.parentCategory(parentCategory);
        }

        Category category = builder.build();
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {} for user: {}", savedCategory.getId(), userId);

        // Publish category created event
        CategoryEvent event = CategoryEvent.fromCategory(userId, savedCategory,
                CategoryEvent.EventType.CATEGORY_CREATED);
        eventProducer.publishCategoryEvent(event);
        log.debug("Category created event published for categoryId: {}", savedCategory.getId());

        return savedCategory;
    }

    @Cacheable(value = "userCategories", key = "#userId")
    public List<Category> getUserCategories(Long userId) {
        log.debug("Fetching categories for user: {}", userId);
        List<Category> categories = categoryRepository.findParentCategoriesByUserId(userId);
        log.debug("Retrieved {} categories for user: {}", categories.size(), userId);
        return categories;
    }

    public List<Category> getUserCategoriesByType(Long userId, String type) {
        log.debug("Fetching categories for user: {} with type: {}", userId, type);
        List<Category> categories = categoryRepository.findParentCategoriesByUserIdAndType(userId, type);
        log.debug("Retrieved {} categories of type {} for user: {}", categories.size(), type, userId);
        return categories;
    }

    public Category updateCategory(Long categoryId, CategoryRequestDTO request) {
        log.debug("Updating category: {} with name: {}", categoryId, request.getName());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found for categoryId: {}", categoryId);
                    return new IllegalArgumentException("Category not found");
                });

        Long userId = category.getUser().getId();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor());

        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> {
                        log.error("Parent category not found for categoryId: {}", request.getParentCategoryId());
                        return new IllegalArgumentException("Parent category not found");
                    });
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully - categoryId: {} for user: {}", categoryId, userId);

        // Publish category updated event
        CategoryEvent event = CategoryEvent.fromCategory(userId, updatedCategory,
                CategoryEvent.EventType.CATEGORY_UPDATED);
        eventProducer.publishCategoryEvent(event);
        log.debug("Category updated event published for categoryId: {}", categoryId);

        return updatedCategory;
    }

    @CacheEvict(value = "userCategories", allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.debug("Deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found for categoryId: {}", categoryId);
                    return new IllegalArgumentException("Category not found");
                });

        Long userId = category.getUser().getId();
        categoryRepository.deleteById(categoryId);
        log.info("Category deleted successfully - categoryId: {} for user: {}", categoryId, userId);

        // Publish category deleted event
        CategoryEvent event = CategoryEvent.fromCategory(userId, category, CategoryEvent.EventType.CATEGORY_DELETED);
        eventProducer.publishCategoryEvent(event);
        log.debug("Category deleted event published for categoryId: {}", categoryId);
    }

}
