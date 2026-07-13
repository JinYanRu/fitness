package com.fitness.ocr.controller;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.service.UserFoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户食物库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/food/user")
@RequiredArgsConstructor
public class UserFoodController {

    private final UserFoodService userFoodService;

    /**
     * 创建食物
     */
    @PostMapping
    public Result<FoodDTO> createFood(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody FoodRequest request) {
        log.info("创建用户食物: userId={}, foodName={}", userId, request.getFoodName());
        FoodDTO food = userFoodService.createFood(userId, request);
        return Result.success(food);
    }

    /**
     * 从 OCR 结果创建食物
     */
    @PostMapping("/from-ocr")
    public Result<FoodDTO> createFromOcr(
            @RequestAttribute("userId") Long userId,
            @RequestBody OcrFoodRequest request) {
        log.info("从OCR创建用户食物: userId={}, foodName={}", userId, request.getFoodName());
        FoodDTO food = userFoodService.createFromOcr(userId, request);
        return Result.success(food);
    }

    /**
     * 更新食物
     */
    @PutMapping("/{id}")
    public Result<FoodDTO> updateFood(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody FoodRequest request) {
        log.info("更新用户食物: userId={}, foodId={}", userId, id);
        FoodDTO food = userFoodService.updateFood(userId, id, request);
        return Result.success(food);
    }

    /**
     * 获取用户所有食物
     */
    @GetMapping
    public Result<List<FoodDTO>> getUserFoods(@RequestAttribute("userId") Long userId) {
        List<FoodDTO> foods = userFoodService.getUserFoods(userId);
        return Result.success(foods);
    }

    /**
     * 搜索用户食物
     */
    @GetMapping("/search")
    public Result<List<FoodDTO>> searchUserFoods(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String keyword) {
        List<FoodDTO> foods = userFoodService.searchUserFoods(userId, keyword);
        return Result.success(foods);
    }

    /**
     * 获取单个食物
     */
    @GetMapping("/{id}")
    public Result<FoodDTO> getFoodById(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        FoodDTO food = userFoodService.getFoodById(userId, id);
        return Result.success(food);
    }

    /**
     * 删除食物
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFood(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        log.info("删除用户食物: userId={}, foodId={}", userId, id);
        userFoodService.deleteFood(userId, id);
        return Result.success();
    }
}