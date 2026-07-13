package com.fitness.ocr.service;

import com.fitness.ocr.dto.FoodDTO;
import com.fitness.ocr.entity.CommonFood;
import com.fitness.ocr.repository.CommonFoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共食物库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonFoodService {

    private final CommonFoodRepository commonFoodRepository;

    /**
     * 获取所有食物
     */
    public List<FoodDTO> getAllFoods() {
        return commonFoodRepository.findByIsActiveOrderByFoodName(1)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索食物
     */
    public List<FoodDTO> searchFoods(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllFoods();
        }
        return commonFoodRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按分类搜索
     */
    public List<FoodDTO> searchByCategory(String category, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return commonFoodRepository.findByCategoryAndIsActiveOrderByFoodName(category, 1)
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return commonFoodRepository.searchByCategoryAndKeyword(category, keyword.trim())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个食物
     */
    public FoodDTO getFoodById(Long foodId) {
        CommonFood food = commonFoodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("食物不存在"));

        if (food.getIsActive() != 1) {
            throw new RuntimeException("食物已下架");
        }

        return toDTO(food);
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return commonFoodRepository.findAllCategories();
    }

    /**
     * 转换为 DTO
     */
    private FoodDTO toDTO(CommonFood food) {
        return FoodDTO.builder()
                .id(food.getId())
                .foodName(food.getFoodName())
                .brand(food.getBrand())
                .category(food.getCategory())
                .servingSize(food.getServingSize())
                .servingUnit(food.getServingUnit())
                .calories(food.getCalories())
                .protein(food.getProtein())
                .fat(food.getFat())
                .saturatedFat(food.getSaturatedFat())
                .carbohydrates(food.getCarbohydrates())
                .fiber(food.getFiber())
                .sodium(food.getSodium())
                .sugar(food.getSugar())
                .calcium(food.getCalcium())
                .source(food.getSource())
                .createdAt(food.getCreateTime())
                .updatedAt(food.getUpdateTime())
                .build();
    }
}