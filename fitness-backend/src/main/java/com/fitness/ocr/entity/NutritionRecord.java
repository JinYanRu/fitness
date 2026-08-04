package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养记录实体
 * 记录用户每日饮食摄入
 */
@Data
@Entity
@Table(name = "nutrition_record")
public class NutritionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64)
    private String userId;

    /**
     * 记录日期
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /**
     * 用餐类型: breakfast, lunch, dinner, snack, workout
     */
    @Column(name = "meal_type", length = 32)
    private String mealType;

    /**
     * 食物名称
     */
    @Column(name = "food_name", length = 128, nullable = false)
    private String foodName;

    /**
     * 品牌
     */
    @Column(name = "brand", length = 64)
    private String brand;

    /**
     * 食用份量
     */
    @Column(name = "serving_amount", precision = 10, scale = 2)
    private BigDecimal servingAmount;

    /**
     * 份量单位
     */
    @Column(name = "serving_unit", length = 16)
    private String servingUnit;

    /**
     * 快捷单位数量（用快捷单位记录时，如 2包 中的 2）
     * 为空表示按基准单位（克）记录
     */
    @Column(name = "display_amount", precision = 10, scale = 2)
    private BigDecimal displayAmount;

    /**
     * 快捷单位名（如 包/个/片）
     * 为空表示按基准单位（克）记录
     */
    @Column(name = "display_unit", length = 16)
    private String displayUnit;

    /**
     * 能量 (kcal)
     */
    @Column(name = "calories", precision = 10, scale = 2)
    private BigDecimal calories;

    /**
     * 蛋白质 (g)
     */
    @Column(name = "protein", precision = 10, scale = 2)
    private BigDecimal protein;

    /**
     * 脂肪 (g)
     */
    @Column(name = "fat", precision = 10, scale = 2)
    private BigDecimal fat;

    /**
     * 饱和脂肪 (g)
     */
    @Column(name = "saturated_fat", precision = 10, scale = 2)
    private BigDecimal saturatedFat;

    /**
     * 碳水化合物 (g)
     */
    @Column(name = "carbohydrates", precision = 10, scale = 2)
    private BigDecimal carbohydrates;

    /**
     * 膳食纤维 (g)
     */
    @Column(name = "fiber", precision = 10, scale = 2)
    private BigDecimal fiber;

    /**
     * 钠 (mg)
     */
    @Column(name = "sodium", precision = 10, scale = 2)
    private BigDecimal sodium;

    /**
     * 糖 (g)
     */
    @Column(name = "sugar", precision = 10, scale = 2)
    private BigDecimal sugar;

    /**
     * 钙 (mg)
     */
    @Column(name = "calcium", precision = 10, scale = 2)
    private BigDecimal calcium;

    /**
     * 备注
     */
    @Column(name = "remark", length = 256)
    private String remark;

    /**
     * 原始OCR文本
     */
    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 是否已吃：false=导入待确认，true=正常已吃
     */
    @Column(name = "eaten", nullable = false)
    private Boolean eaten = true;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
