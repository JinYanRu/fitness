package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜谱 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {

    private Long id;

    private Long userId;

    private String name;

    private Integer servings;

    private BigDecimal totalWeight;

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

    private List<IngredientDTO> ingredients;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 原料 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDTO {
        private Long id;

        private String foodType;

        private Long foodId;

        private String foodName;

        private BigDecimal amount;

        private String unit;

        private BigDecimal calories;

        private BigDecimal protein;

        private BigDecimal fat;

        private BigDecimal carbohydrates;
    }
}