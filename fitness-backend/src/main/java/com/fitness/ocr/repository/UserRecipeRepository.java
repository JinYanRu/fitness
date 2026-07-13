package com.fitness.ocr.repository;

import com.fitness.ocr.entity.UserRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户菜谱 Repository
 */
@Repository
public interface UserRecipeRepository extends JpaRepository<UserRecipe, Long> {

    /**
     * 查询用户的所有菜谱
     */
    List<UserRecipe> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 搜索用户菜谱
     */
    @Query("SELECT r FROM UserRecipe r WHERE r.userId = :userId AND r.name LIKE %:keyword%")
    List<UserRecipe> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 统计用户菜谱数量
     */
    long countByUserId(Long userId);
}