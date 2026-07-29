package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 食物 DTO（通用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDTO {

    private Long id;

    private String foodName;

    private String brand;

    private String category;

    private BigDecimal servingSize;

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

    private String source;

    private String remark;

    /**
     * 可选计量单位列表
     */
    private List<FoodUnitDTO> units;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}