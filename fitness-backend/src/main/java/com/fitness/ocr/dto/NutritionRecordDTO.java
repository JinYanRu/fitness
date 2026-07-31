package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养记录 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionRecordDTO {

    private Long id;

    private String userId;

    private LocalDate recordDate;

    private String mealType;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 是否已吃：false=导入待确认，true=正常已吃 */
    private Boolean eaten;
}
