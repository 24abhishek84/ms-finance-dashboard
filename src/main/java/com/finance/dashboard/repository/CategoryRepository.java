package com.finance.dashboard.repository;

import com.finance.dashboard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);

    List<Category> findByUserIdAndType(Long userId, String type);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.parentCategory IS NULL")
    List<Category> findParentCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.type = :type AND c.parentCategory IS NULL")
    List<Category> findParentCategoriesByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}

