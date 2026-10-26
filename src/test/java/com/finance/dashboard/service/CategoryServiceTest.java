package com.finance.dashboard.service;

import com.finance.dashboard.dto.CategoryRequestDTO;
import com.finance.dashboard.entity.Category;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.CategoryRepository;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("John")
            .password("password123")
            .build();

        testCategory = Category.builder()
            .id(1L)
            .name("Groceries")
            .type(Category.CategoryType.EXPENSE)
            .color("#FF6B6B")
            .user(testUser)
            .build();
    }

    @Test
    @DisplayName("Should create category successfully")
    void testCreateCategory() {
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
        assertNotNull(result);
        assertEquals("Groceries", result.getName());
        assertEquals(Category.CategoryType.EXPENSE, result.getType());
        
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateCategoryUserNotFound() {
        // Arrange
        CategoryRequestDTO request = new CategoryRequestDTO();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(999L, request);
        });
    }

    @Test
    @DisplayName("Should update category successfully")
    void testUpdateCategory() {
        // Arrange
        long categoryId = 1L;
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Groceries");
        request.setType("EXPENSE");
        request.setColor("#FF6B6B");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        Category result = categoryService.updateCategory(categoryId, request);

        // Assert
        verify(categoryRepository, times(1)).save(any(Category.class));
        assertNotNull(result);
        assertEquals("Groceries", result.getName());
    }
}
