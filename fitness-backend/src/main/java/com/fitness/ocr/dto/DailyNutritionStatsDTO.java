package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 每日营养统计 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyNutritionStatsDTO {

    /**
     * 记录数量
     */
    private Integer recordCount;

    /**
     * 总热量 (kcal)
     */
    private BigDecimal totalCalories;

    /**
     * 总蛋白质 (g)
     */
    private BigDecimal totalProtein;

    /**
     * 总脂肪 (g)
     */
    private BigDecimal totalFat;

    /**
     * 总饱和脂肪 (g)
     */
    private BigDecimal totalSaturatedFat;

    /**
     * 总碳水化合物 (g)
     */
    private BigDecimal totalCarbohydrates;

    /**
     * 总膳食纤维 (g)
     */
    private BigDecimal totalFiber;

    /**
     * 总钠 (mg)
     */
    private BigDecimal totalSodium;

    /**
     * 总糖 (g)
     */
    private BigDecimal totalSugar;

    /**
     * 总钙 (mg)
     */
    private BigDecimal totalCalcium;

    /**
     * 日期
     */
    private String date;
}
