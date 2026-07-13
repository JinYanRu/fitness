package com.fitness.ocr.repository;

import com.fitness.ocr.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜谱原料 Repository
 */
@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /**
     * 查询菜谱的所有原料
     */
    List<RecipeIngredient> findByRecipeIdOrderByCreateTime(Long recipeId);

    /**
     * 删除菜谱的所有原料
     */
    void deleteByRecipeId(Long recipeId);

    /**
     * 删除单个原料
     */
    void deleteByIdAndRecipeId(Long id, Long recipeId);
}