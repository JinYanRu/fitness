package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 食物计量单位实体
 * 支持一个食物有多个计量单位（如：牛奶可以按g、包、杯计量）
 */
@Data
@Entity
@Table(name = "food_unit", indexes = {
    @Index(name = "idx_food", columnList = "food_id, food_type")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_food_unit", columnNames = {"food_id", "food_type", "unit_name"})
})
public class FoodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 食物ID
     */
    @Column(nullable = false)
    private Long foodId;

    /**
     * 食物类型: user(用户食物库) / common(公共食物库)
     */
    @Column(nullable = false, length = 16)
    private String foodType = "user";

    /**
     * 单位名称（如：包、片、个、杯、碗）
     */
    @Column(nullable = false, length = 20)
    private String unitName;

    /**
     * 单位对应基准单位的量
     * 例如：1包=250g，则 unitValue=250
     * 基准单位通常是食物的 servingUnit（如 g 或 ml）
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitValue;

    /**
     * 是否为默认单位
     */
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
