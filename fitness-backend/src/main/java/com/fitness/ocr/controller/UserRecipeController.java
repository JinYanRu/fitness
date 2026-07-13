package com.fitness.ocr.controller;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.service.UserRecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户菜谱控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class UserRecipeController {

    private final UserRecipeService userRecipeService;

    /**
     * 创建菜谱
     */
    @PostMapping
    public Result<RecipeDTO> createRecipe(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody RecipeRequest request) {
        log.info("创建菜谱: userId={}, name={}", userId, request.getName());
        RecipeDTO recipe = userRecipeService.createRecipe(userId, request);
        return Result.success(recipe);
    }

    /**
     * 更新菜谱
     */
    @PutMapping("/{id}")
    public Result<RecipeDTO> updateRecipe(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        log.info("更新菜谱: userId={}, recipeId={}", userId, id);
        RecipeDTO recipe = userRecipeService.updateRecipe(userId, id, request);
        return Result.success(recipe);
    }

    /**
     * 获取用户所有菜谱
     */
    @GetMapping
    public Result<List<RecipeDTO>> getUserRecipes(@RequestAttribute("userId") Long userId) {
        List<RecipeDTO> recipes = userRecipeService.getUserRecipes(userId);
        return Result.success(recipes);
    }

    /**
     * 搜索用户菜谱
     */
    @GetMapping("/search")
    public Result<List<RecipeDTO>> searchUserRecipes(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String keyword) {
        List<RecipeDTO> recipes = userRecipeService.searchUserRecipes(userId, keyword);
        return Result.success(recipes);
    }

    /**
     * 获取单个菜谱
     */
    @GetMapping("/{id}")
    public Result<RecipeDTO> getRecipeById(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        RecipeDTO recipe = userRecipeService.getRecipeById(userId, id);
        return Result.success(recipe);
    }

    /**
     * 删除菜谱
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecipe(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        log.info("删除菜谱: userId={}, recipeId={}", userId, id);
        userRecipeService.deleteRecipe(userId, id);
        return Result.success();
    }
}