package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 创建/更新营养记录请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecordRequest {

    @NotBlank(message = "食物名称不能为空")
    private String foodName;

    private String brand;

    private BigDecimal servingAmount;

    private String servingUnit;

    private BigDecimal calories;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal saturatedFat;

    private BigDecimal carbohydrates;

    private BigDecimal fiber;

    private BigDecimal sodium;

    private BigDecimal sugar;

    private BigDecimal calcium;

    private String remark;

    private String ocrText;

    private String mealType;

    private String recordDate;
}
