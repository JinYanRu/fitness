package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * OCR 识别结果 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultDTO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 识别的完整文本
     */
    private String text;

    /**
     * 文本块列表（含位置信息）
     */
    private List<TextBlock> texts;

    /**
     * 食物信息（解析后的结构化数据）
     */
    private FoodInfo foodInfo;

    /**
     * 处理耗时（毫秒）
     */
    private Long elapseMs;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 文本块
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextBlock {
        /**
         * 文本内容
         */
        private String text;

        /**
         * 边界框坐标 [[x1,y1], [x2,y2], [x3,y3], [x4,y4]]
         */
        private List<List<Integer>> box;

        /**
         * 置信度
         */
        private Double score;
    }

    /**
     * 食物信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodInfo {
        /**
         * 食物名称
         */
        private String foodName;

        /**
         * 食物类型/类别
         */
        private String foodCategory;

        /**
         * 每多少克（营养成分表的基准份量）
         */
        private BigDecimal servingSize;

        /**
         * 营养成分表
         */
        private Nutrition nutrition;
    }

    /**
     * 营养成分（数值用 BigDecimal 存储，不带单位）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nutrition {
        /**
         * 能量（千焦）
         */
        private BigDecimal energyKj;

        /**
         * 能量（千卡）
         */
        private BigDecimal energyKcal;

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
         * 反式脂肪（克）
         */
        private BigDecimal transFat;

        /**
         * 碳水化合物（克）
         */
        private BigDecimal carbohydrate;

        /**
         * 糖（克）
         */
        private BigDecimal sugar;

        /**
         * 膳食纤维（克）
         */
        private BigDecimal dietaryFiber;

        /**
         * 钠（毫克）
         */
        private BigDecimal sodium;

        /**
         * 钙（毫克）
         */
        private BigDecimal calcium;
    }

    /**
     * 营养数据（旧格式，保持向后兼容）
     * @deprecated 使用 FoodInfo 替代
     */
    @Deprecated
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionData {
        private String calories;       // 能量/热量
        private String protein;        // 蛋白质
        private String fat;            // 脂肪
        private String saturatedFat;   // 饱和脂肪
        private String carbohydrates;  // 碳水化合物
        private String fiber;          // 膳食纤维
        private String sodium;         // 钠
        private String sugar;          // 糖
        private String calcium;        // 钙
    }
}