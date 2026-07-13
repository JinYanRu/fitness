package com.fitness.ocr.service;

import com.fitness.ocr.dto.RecipeDTO;
import com.fitness.ocr.dto.RecipeRequest;
import com.fitness.ocr.entity.RecipeIngredient;
import com.fitness.ocr.entity.UserRecipe;
import com.fitness.ocr.repository.RecipeIngredientRepository;
import com.fitness.ocr.repository.UserRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户菜谱服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecipeService {

    private final UserRecipeRepository userRecipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    /**
     * 创建菜谱
     */
    @Transactional
    public RecipeDTO createRecipe(Long userId, RecipeRequest request) {
        // 创建菜谱
        UserRecipe recipe = new UserRecipe();
        recipe.setUserId(userId);
        recipe.setName(request.getName());
        recipe.setServings(request.getServings() != null ? request.getServings() : 1);
        recipe.setTotalWeight(request.getTotalWeight());
        recipe.setRemark(request.getRemark());

        // 计算营养成分总和
        calculateTotalNutrition(recipe, request.getIngredients());

        UserRecipe saved = userRecipeRepository.save(recipe);

        // 保存原料
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            saveIngredients(saved.getId(), request.getIngredients());
        }

        log.info("用户创建菜谱: userId={}, recipeId={}, name={}", userId, saved.getId(), saved.getName());

        return toDTO(saved);
    }

    /**
     * 更新菜谱
     */
    @Transactional
    public RecipeDTO updateRecipe(Long userId, Long recipeId, RecipeRequest request) {
        UserRecipe recipe = userRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("菜谱不存在"));

        if (!recipe.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此菜谱");
        }

        recipe.setName(request.getName());
        recipe.setServings(request.getServings() != null ? request.getServings() : 1);
        recipe.setTotalWeight(request.getTotalWeight());
        recipe.setRemark(request.getRemark());

        // 重新计算营养成分
        calculateTotalNutrition(recipe, request.getIngredients());

        // 删除旧原料，保存新原料
        recipeIngredientRepository.deleteByRecipeId(recipeId);
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            saveIngredients(recipeId, request.getIngredients());
        }

        UserRecipe saved = userRecipeRepository.save(recipe);
        log.info("用户更新菜谱: userId={}, recipeId={}", userId, recipeId);

        return toDTO(saved);
    }

    /**
     * 计算总营养成分
     */
    private void calculateTotalNutrition(UserRecipe recipe, List<RecipeRequest.IngredientRequest> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalSaturatedFat = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        BigDecimal totalSodium = BigDecimal.ZERO;
        BigDecimal totalSugar = BigDecimal.ZERO;
        BigDecimal totalCalcium = BigDecimal.ZERO;

        for (RecipeRequest.IngredientRequest ingredient : ingredients) {
            if (ingredient.getCalories() != null) totalCalories = totalCalories.add(ingredient.getCalories());
            if (ingredient.getProtein() != null) totalProtein = totalProtein.add(ingredient.getProtein());
            if (ingredient.getFat() != null) totalFat = totalFat.add(ingredient.getFat());
            if (ingredient.getCarbohydrates() != null) totalCarbs = totalCarbs.add(ingredient.getCarbohydrates());
        }

        recipe.setCalories(totalCalories);
        recipe.setProtein(totalProtein);
        recipe.setFat(totalFat);
        recipe.setSaturatedFat(totalSaturatedFat);
        recipe.setCarbohydrates(totalCarbs);
        recipe.setFiber(totalFiber);
        recipe.setSodium(totalSodium);
        recipe.setSugar(totalSugar);
        recipe.setCalcium(totalCalcium);
    }

    /**
     * 保存原料
     */
    private void saveIngredients(Long recipeId, List<RecipeRequest.IngredientRequest> ingredients) {
        for (RecipeRequest.IngredientRequest ingredient : ingredients) {
            RecipeIngredient entity = new RecipeIngredient();
            entity.setRecipeId(recipeId);
            entity.setFoodType(ingredient.getFoodType());
            entity.setFoodId(ingredient.getFoodId());
            entity.setFoodName(ingredient.getFoodName());
            entity.setAmount(ingredient.getAmount());
            entity.setUnit(ingredient.getUnit());
            entity.setCalories(ingredient.getCalories());
            entity.setProtein(ingredient.getProtein());
            entity.setFat(ingredient.getFat());
            entity.setCarbohydrates(ingredient.getCarbohydrates());

            recipeIngredientRepository.save(entity);
        }
    }

    /**
     * 获取用户所有菜谱
     */
    public List<RecipeDTO> getUserRecipes(Long userId) {
        return userRecipeRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户菜谱
     */
    public List<RecipeDTO> searchUserRecipes(Long userId, String keyword) {
        return userRecipeRepository.searchByUserIdAndKeyword(userId, keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个菜谱
     */
    public RecipeDTO getRecipeById(Long userId, Long recipeId) {
        UserRecipe recipe = userRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("菜谱不存在"));

        if (!recipe.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此菜谱");
        }

        return toDTOWithIngredients(recipe);
    }

    /**
     * 删除菜谱
     */
    @Transactional
    public void deleteRecipe(Long userId, Long recipeId) {
        UserRecipe recipe = userRecipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("菜谱不存在"));

        if (!recipe.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此菜谱");
        }

        // 先删除原料
        recipeIngredientRepository.deleteByRecipeId(recipeId);
        // 再删除菜谱
        userRecipeRepository.deleteById(recipeId);

        log.info("用户删除菜谱: userId={}, recipeId={}", userId, recipeId);
    }

    /**
     * 转换为 DTO（不含原料）
     */
    private RecipeDTO toDTO(UserRecipe recipe) {
        return RecipeDTO.builder()
                .id(recipe.getId())
                .userId(recipe.getUserId())
                .name(recipe.getName())
                .servings(recipe.getServings())
                .totalWeight(recipe.getTotalWeight())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .fat(recipe.getFat())
                .saturatedFat(recipe.getSaturatedFat())
                .carbohydrates(recipe.getCarbohydrates())
                .fiber(recipe.getFiber())
                .sodium(recipe.getSodium())
                .sugar(recipe.getSugar())
                .calcium(recipe.getCalcium())
                .remark(recipe.getRemark())
                .createdAt(recipe.getCreateTime())
                .updatedAt(recipe.getUpdateTime())
                .build();
    }

    /**
     * 转换为 DTO（含原料）
     */
    private RecipeDTO toDTOWithIngredients(UserRecipe recipe) {
        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipeIdOrderByCreateTime(recipe.getId());

        List<RecipeDTO.IngredientDTO> ingredientDTOs = ingredients.stream()
                .map(i -> RecipeDTO.IngredientDTO.builder()
                        .id(i.getId())
                        .foodType(i.getFoodType())
                        .foodId(i.getFoodId())
                        .foodName(i.getFoodName())
                        .amount(i.getAmount())
                        .unit(i.getUnit())
                        .calories(i.getCalories())
                        .protein(i.getProtein())
                        .fat(i.getFat())
                        .carbohydrates(i.getCarbohydrates())
                        .build())
                .collect(Collectors.toList());

        return RecipeDTO.builder()
                .id(recipe.getId())
                .userId(recipe.getUserId())
                .name(recipe.getName())
                .servings(recipe.getServings())
                .totalWeight(recipe.getTotalWeight())
                .calories(recipe.getCalories())
                .protein(recipe.getProtein())
                .fat(recipe.getFat())
                .saturatedFat(recipe.getSaturatedFat())
                .carbohydrates(recipe.getCarbohydrates())
                .fiber(recipe.getFiber())
                .sodium(recipe.getSodium())
                .sugar(recipe.getSugar())
                .calcium(recipe.getCalcium())
                .remark(recipe.getRemark())
                .ingredients(ingredientDTOs)
                .createdAt(recipe.getCreateTime())
                .updatedAt(recipe.getUpdateTime())
                .build();
    }
}