package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 从 OCR 结果创建食物的请求 DTO
 * 用于将 OCR 识别的食物信息保存到用户食物库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrFoodRequest {

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 食物类别
     */
    private String foodCategory;

    /**
     * 份量（基准克数）
     */
    private BigDecimal servingSize;

    /**
     * 份量单位
     */
    private String servingUnit;

    /**
     * 能量（千卡）
     */
    private BigDecimal calories;

    /**
     * 蛋白质（克）
     */
    private BigDecimal protein;

    /**
     * 脂肪（克）
     */
    private BigDecimal fat;

    /**
     * 饱和脂肪（克）
     */
    private BigDecimal saturatedFat;

    /**
     * 碳水化合物（克）
     */
    private BigDecimal carbohydrates;

    /**
     * 膳食纤维（克）
     */
    private BigDecimal fiber;

    /**
     * 钠（毫克）
     */
    private BigDecimal sodium;

    /**
     * 糖（克）
     */
    private BigDecimal sugar;

    /**
     * 钙（毫克）
     */
    private BigDecimal calcium;

    /**
     * 备注
     */
    private String remark;

    /**
     * 从 OcrResultDTO.FoodInfo 创建 OcrFoodRequest
     */
    public static OcrFoodRequest fromFoodInfo(OcrResultDTO.FoodInfo foodInfo, String brand, String remark) {
        if (foodInfo == null) {
            return null;
        }

        OcrFoodRequestBuilder builder = OcrFoodRequest.builder()
                .foodName(foodInfo.getFoodName())
                .foodCategory(foodInfo.getFoodCategory())
                .servingSize(foodInfo.getServingSize())
                .servingUnit("g") // 默认单位为克
                .brand(brand)
                .remark(remark);

        // 映射营养成分
        OcrResultDTO.Nutrition nutrition = foodInfo.getNutrition();
        if (nutrition != null) {
            builder.calories(nutrition.getEnergyKcal())
                   .protein(nutrition.getProtein())
                   .fat(nutrition.getFat())
                   .saturatedFat(nutrition.getSaturatedFat())
                   .carbohydrates(nutrition.getCarbohydrate())
                   .fiber(nutrition.getDietaryFiber())
                   .sodium(nutrition.getSodium())
                   .sugar(nutrition.getSugar())
                   .calcium(nutrition.getCalcium());
        }

        return builder.build();
    }
}
