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

    /** 快捷单位数量（用快捷单位记录时，如 2包 中的 2） */
    private BigDecimal displayAmount;

    /** 快捷单位名（如 包/个/片）；为空表示按基准单位（克）记录 */
    private String displayUnit;

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
