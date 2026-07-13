package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案实体
 * 存储用户的身体信息和健身目标
 */
@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    /**
     * 性别：1-男，2-女
     */
    @Column(name = "gender")
    private Integer gender;

    /**
     * 生日
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 身高 (cm)
     */
    @Column(name = "height", precision = 5, scale = 2)
    private BigDecimal height;

    /**
     * 当前体重 (kg)
     */
    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    /**
     * 目标体重 (kg)
     */
    @Column(name = "target_weight", precision = 5, scale = 2)
    private BigDecimal targetWeight;

    /**
     * 目标：muscle_gain-增肌，fat_loss-减脂，maintain-维持
     */
    @Column(name = "goal", length = 32)
    private String goal;

    /**
     * 活动水平：sedentary-久坐，light-轻度活动，moderate-中度活动，high-高强度活动
     */
    @Column(name = "activity_level", length = 32)
    private String activityLevel;

    /**
     * 基础代谢率 (BMR)
     */
    @Column(name = "bmr", precision = 10, scale = 2)
    private BigDecimal bmr;

    /**
     * 每日总能量消耗 (TDEE)
     */
    @Column(name = "tdee", precision = 10, scale = 2)
    private BigDecimal tdee;

    /**
     * 目标热量 (kcal)
     */
    @Column(name = "target_calories")
    private Integer targetCalories;

    /**
     * 目标蛋白质 (g)
     */
    @Column(name = "target_protein", precision = 10, scale = 2)
    private BigDecimal targetProtein;

    /**
     * 目标脂肪 (g)
     */
    @Column(name = "target_fat", precision = 10, scale = 2)
    private BigDecimal targetFat;

    /**
     * 目标碳水 (g)
     */
    @Column(name = "target_carbs", precision = 10, scale = 2)
    private BigDecimal targetCarbs;

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
