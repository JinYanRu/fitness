package com.fitness.ocr.service;

import com.fitness.ocr.dto.FoodDTO;
import com.fitness.ocr.dto.FoodRequest;
import com.fitness.ocr.dto.OcrFoodRequest;
import com.fitness.ocr.entity.UserFood;
import com.fitness.ocr.repository.UserFoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户食物库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFoodService {

    private final UserFoodRepository userFoodRepository;

    /**
     * 创建食物
     */
    @Transactional
    public FoodDTO createFood(Long userId, FoodRequest request) {
        UserFood food = new UserFood();
        food.setUserId(userId);
        food.setFoodName(request.getFoodName());
        food.setBrand(request.getBrand());
        food.setServingSize(request.getServingSize());
        food.setServingUnit(request.getServingUnit());
        food.setCalories(request.getCalories());
        food.setProtein(request.getProtein());
        food.setFat(request.getFat());
        food.setSaturatedFat(request.getSaturatedFat());
        food.setCarbohydrates(request.getCarbohydrates());
        food.setFiber(request.getFiber());
        food.setSodium(request.getSodium());
        food.setSugar(request.getSugar());
        food.setCalcium(request.getCalcium());
        food.setRemark(request.getRemark());
        food.setSource(request.getSource() != null ? request.getSource() : "manual");

        UserFood saved = userFoodRepository.save(food);
        log.info("用户创建食物: userId={}, foodId={}, foodName={}", userId, saved.getId(), saved.getFoodName());

        return toDTO(saved);
    }

    /**
     * 从 OCR 结果创建食物
     */
    @Transactional
    public FoodDTO createFromOcr(Long userId, OcrFoodRequest request) {
        UserFood food = new UserFood();
        food.setUserId(userId);
        food.setFoodName(request.getFoodName());
        food.setBrand(request.getBrand());
        food.setServingSize(request.getServingSize());
        food.setServingUnit(request.getServingUnit() != null ? request.getServingUnit() : "g");
        food.setCalories(request.getCalories());
        food.setProtein(request.getProtein());
        food.setFat(request.getFat());
        food.setSaturatedFat(request.getSaturatedFat());
        food.setCarbohydrates(request.getCarbohydrates());
        food.setFiber(request.getFiber());
        food.setSodium(request.getSodium());
        food.setSugar(request.getSugar());
        food.setCalcium(request.getCalcium());
        food.setRemark(request.getRemark());
        food.setSource("ocr"); // OCR 来源

        UserFood saved = userFoodRepository.save(food);
        log.info("用户从OCR创建食物: userId={}, foodId={}, foodName={}", userId, saved.getId(), saved.getFoodName());

        return toDTO(saved);
    }

    /**
     * 更新食物
     */
    @Transactional
    public FoodDTO updateFood(Long userId, Long foodId, FoodRequest request) {
        UserFood food = userFoodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("食物不存在"));

        if (!food.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此食物");
        }

        food.setFoodName(request.getFoodName());
        food.setBrand(request.getBrand());
        food.setServingSize(request.getServingSize());
        food.setServingUnit(request.getServingUnit());
        food.setCalories(request.getCalories());
        food.setProtein(request.getProtein());
        food.setFat(request.getFat());
        food.setSaturatedFat(request.getSaturatedFat());
        food.setCarbohydrates(request.getCarbohydrates());
        food.setFiber(request.getFiber());
        food.setSodium(request.getSodium());
        food.setSugar(request.getSugar());
        food.setCalcium(request.getCalcium());
        food.setRemark(request.getRemark());

        UserFood saved = userFoodRepository.save(food);
        log.info("用户更新食物: userId={}, foodId={}", userId, foodId);

        return toDTO(saved);
    }

    /**
     * 获取用户所有食物
     */
    public List<FoodDTO> getUserFoods(Long userId) {
        return userFoodRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户食物
     */
    public List<FoodDTO> searchUserFoods(Long userId, String keyword) {
        return userFoodRepository.searchByUserIdAndKeyword(userId, keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个食物
     */
    public FoodDTO getFoodById(Long userId, Long foodId) {
        UserFood food = userFoodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("食物不存在"));

        if (!food.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问此食物");
        }

        return toDTO(food);
    }

    /**
     * 删除食物
     */
    @Transactional
    public void deleteFood(Long userId, Long foodId) {
        UserFood food = userFoodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("食物不存在"));

        if (!food.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此食物");
        }

        userFoodRepository.deleteById(foodId);
        log.info("用户删除食物: userId={}, foodId={}", userId, foodId);
    }

    /**
     * 转换为 DTO
     */
    private FoodDTO toDTO(UserFood food) {
        return FoodDTO.builder()
                .id(food.getId())
                .foodName(food.getFoodName())
                .brand(food.getBrand())
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
                .remark(food.getRemark())
                .createdAt(food.getCreateTime())
                .updatedAt(food.getUpdateTime())
                .build();
    }
}