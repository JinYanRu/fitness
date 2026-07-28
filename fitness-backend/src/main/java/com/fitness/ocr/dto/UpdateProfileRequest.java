package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新用户档案请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 身高 (cm)
     */
    private BigDecimal height;

    /**
     * 当前体重 (kg)
     */
    private BigDecimal weight;

    /**
     * 目标体重 (kg)
     */
    private BigDecimal targetWeight;

    /**
     * 目标：muscle_gain/fat_loss/maintain
     */
    private String goal;

    /**
     * 活动水平：sedentary/light/moderate/high
     */
    private String activityLevel;

    /**
     * 蛋白质倍率 (g/kg体重)
     */
    private BigDecimal proteinMultiplier;

    /**
     * 脂肪倍率 (g/kg体重)
     */
    private BigDecimal fatMultiplier;

    /**
     * 碳水倍率 (g/kg体重)
     */
    private BigDecimal carbsMultiplier;
}