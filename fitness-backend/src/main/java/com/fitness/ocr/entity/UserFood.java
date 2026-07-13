package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户私有食物库实体
 */
@Data
@Entity
@Table(name = "user_food")
public class UserFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 食物名称
     */
    @Column(name = "food_name", nullable = false, length = 128)
    private String foodName;

    /**
     * 品牌
     */
    @Column(name = "brand", length = 64)
    private String brand;

    /**
     * 份量
     */
    @Column(name = "serving_size", precision = 10, scale = 2)
    private BigDecimal servingSize;

    /**
     * 份量单位
     */
    @Column(name = "serving_unit", length = 16)
    private String servingUnit;

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
     * 数据来源: ocr/manual
     */
    @Column(name = "source", length = 32)
    private String source = "ocr";

    /**
     * 备注（OCR 原始文本可能很长）
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

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

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}