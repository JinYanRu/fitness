package com.fitness.ocr.controller;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.service.CommonFoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共食物库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/food/common")
@RequiredArgsConstructor
public class CommonFoodController {

    private final CommonFoodService commonFoodService;

    /**
     * 获取所有食物
     */
    @GetMapping
    public Result<List<FoodDTO>> getAllFoods() {
        List<FoodDTO> foods = commonFoodService.getAllFoods();
        return Result.success(foods);
    }

    /**
     * 搜索食物
     */
    @GetMapping("/search")
    public Result<List<FoodDTO>> searchFoods(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {

        List<FoodDTO> foods;
        if (category != null && !category.isEmpty()) {
            foods = commonFoodService.searchByCategory(category, keyword);
        } else {
            foods = commonFoodService.searchFoods(keyword);
        }

        return Result.success(foods);
    }

    /**
     * 获取单个食物
     */
    @GetMapping("/{id}")
    public Result<FoodDTO> getFoodById(@PathVariable Long id) {
        FoodDTO food = commonFoodService.getFoodById(id);
        return Result.success(food);
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        List<String> categories = commonFoodService.getAllCategories();
        return Result.success(categories);
    }
}