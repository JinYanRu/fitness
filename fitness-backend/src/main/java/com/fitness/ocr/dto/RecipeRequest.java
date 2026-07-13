package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建/更新菜谱请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequest {

    @NotBlank(message = "菜谱名称不能为空")
    private String name;

    private Integer servings;

    private BigDecimal totalWeight;

    private String remark;

    private List<IngredientRequest> ingredients;

    /**
     * 原料请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientRequest {
        private String foodType;

        private Long foodId;

        private String foodName;

        @NotNull(message = "用量不能为空")
        private BigDecimal amount;

        private String unit;

        private BigDecimal calories;

        private BigDecimal protein;

        private BigDecimal fat;

        private BigDecimal carbohydrates;
    }
}