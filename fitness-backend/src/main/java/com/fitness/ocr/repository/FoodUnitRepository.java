package com.fitness.ocr.repository;

import com.fitness.ocr.entity.FoodUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 食物计量单位 Repository
 */
@Repository
public interface FoodUnitRepository extends JpaRepository<FoodUnit, Long> {

    /**
     * 根据食物ID和类型查询所有单位
     */
    List<FoodUnit> findByFoodIdAndFoodType(Long foodId, String foodType);

    /**
     * 根据食物ID和类型查询默认单位
     */
    FoodUnit findFirstByFoodIdAndFoodTypeAndIsDefaultTrue(Long foodId, String foodType);

    /**
     * 根据食物ID、类型和单位名称查询
     */
    FoodUnit findByFoodIdAndFoodTypeAndUnitName(Long foodId, String foodType, String unitName);

    /**
     * 删除食物的所有单位
     */
    void deleteByFoodIdAndFoodType(Long foodId, String foodType);

    /**
     * 批量删除多个食物的单位
     */
    void deleteByFoodIdInAndFoodType(List<Long> foodIds, String foodType);
}