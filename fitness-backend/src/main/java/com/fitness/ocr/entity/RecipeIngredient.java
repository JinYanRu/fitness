package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜谱原料实体
 */
@Data
@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜谱ID
     */
    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    /**
     * 食物类型: common/user
     */
    @Column(name = "food_type", length = 32)
    private String foodType;

    /**
     * 食物ID（关联 common_food 或 user_food）
     */
    @Column(name = "food_id")
    private Long foodId;

    /**
     * 食物名称（冗余存储）
     */
    @Column(name = "food_name", length = 128)
    private String foodName;

    /**
     * 用量
     */
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 单位
     */
    @Column(name = "unit", length = 16)
    private String unit;

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
     * 碳水化合物 (g)
     */
    @Column(name = "carbohydrates", precision = 10, scale = 2)
    private BigDecimal carbohydrates;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}