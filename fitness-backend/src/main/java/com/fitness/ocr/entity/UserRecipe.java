package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户菜谱实体
 */
@Data
@Entity
@Table(name = "user_recipe")
public class UserRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 菜谱名称
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * 份数
     */
    @Column(name = "servings")
    private Integer servings = 1;

    /**
     * 总重量 (g)
     */
    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight;

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
     * 备注/做法
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