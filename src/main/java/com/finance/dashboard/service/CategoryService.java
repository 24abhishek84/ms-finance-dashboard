package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategoryRequestDTO;
import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.UserRepository;
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
        return categoryRepository.save(category);
    }
    
    @Cacheable(value = "userCategories", key = "#userId")
    public List<Category> getUserCategories(Long userId) {
        return categoryRepository.findParentCategoriesByUserId(userId);
    }

    public List<Category> getUserCategoriesByType(Long userId, String type) {
        return categoryRepository.findParentCategoriesByUserIdAndType(userId, type);
    }
    
    public Category updateCategory(Long categoryId, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

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

        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
