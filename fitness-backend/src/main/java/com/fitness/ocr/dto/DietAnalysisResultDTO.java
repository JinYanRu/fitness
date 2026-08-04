package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 每日饮食分析结果 DTO
 * 综合用户身体数据和当日饮食记录，由大模型生成
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietAnalysisResultDTO {

    /**
     * 总评分 (0-100)
     */
    private Integer score;

    /**
     * 总体评价
     */
    private String summary;

    /**
     * 各项营养分析（热量/蛋白质/脂肪/碳水）
     */
    private List<AnalysisItem> items;

    /**
     * 改进建议列表
     */
    private List<String> suggestions;

    /**
     * 单项营养分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisItem {

        /**
         * 名称（热量/蛋白质/脂肪/碳水）
         */
        private String name;

        /**
         * 图标 emoji
         */
        private String icon;

        /**
         * 摄入量
         */
        private BigDecimal intake;

        /**
         * 目标量
         */
        private BigDecimal target;

        /**
         * 单位（kcal/g）
         */
        private String unit;

        /**
         * 状态：达标/偏高/偏低/合理
         */
        private String status;

        /**
         * 点评
         */
        private String comment;
    }
}
