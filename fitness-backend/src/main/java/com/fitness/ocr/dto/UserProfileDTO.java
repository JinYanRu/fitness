package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long id;

    private Long userId;

    private Integer gender;

    private String genderText;

    private LocalDate birthday;

    private Integer age;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal targetWeight;

    private String goal;

    private String goalText;

    private String activityLevel;

    private String activityLevelText;

    private BigDecimal bmr;

    private BigDecimal tdee;

    private Integer targetCalories;

    private BigDecimal targetProtein;

    private BigDecimal targetFat;

    private BigDecimal targetCarbs;

    private BigDecimal proteinMultiplier;

    private BigDecimal fatMultiplier;

    private BigDecimal carbsMultiplier;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 获取性别文本
     */
    public String getGenderText() {
        if (gender == null) return null;
        return gender == 1 ? "男" : "女";
    }

    /**
     * 获取目标文本
     */
    public String getGoalText() {
        if (goal == null) return null;
        switch (goal) {
            case "muscle_gain": return "增肌";
            case "fat_loss": return "减脂";
            case "maintain": return "维持";
            default: return goal;
        }
    }

    /**
     * 获取活动水平文本
     */
    public String getActivityLevelText() {
        if (activityLevel == null) return null;
        switch (activityLevel) {
            case "sedentary": return "久坐";
            case "light": return "轻度活动";
            case "moderate": return "中度活动";
            case "high": return "高强度活动";
            default: return activityLevel;
        }
    }

    /**
     * 计算年龄
     */
    public Integer getAge() {
        if (birthday == null) return null;
        return LocalDate.now().getYear() - birthday.getYear();
    }
}