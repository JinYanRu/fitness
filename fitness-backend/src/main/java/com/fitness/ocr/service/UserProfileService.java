package com.fitness.ocr.service;

import com.fitness.ocr.dto.UpdateProfileRequest;
import com.fitness.ocr.dto.UserProfileDTO;
import com.fitness.ocr.entity.User;
import com.fitness.ocr.entity.UserProfile;
import com.fitness.ocr.repository.UserRepository;
import com.fitness.ocr.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 用户档案服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * 获取用户档案
     *
     * @param userId 用户ID
     * @return 用户档案
     */
    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 如果档案不存在，创建默认档案
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return userProfileRepository.save(newProfile);
                });

        return toDTO(user, profile);
    }

    /**
     * 更新用户档案
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 更新后的档案
     */
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户基本信息
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userRepository.save(user);

        // 获取或创建档案
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(userId);
                    return newProfile;
                });

        // 更新档案信息
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            profile.setBirthday(LocalDate.parse(request.getBirthday()));
        }
        if (request.getHeight() != null) {
            profile.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            profile.setWeight(request.getWeight());
        }
        if (request.getTargetWeight() != null) {
            profile.setTargetWeight(request.getTargetWeight());
        }
        if (request.getGoal() != null) {
            profile.setGoal(request.getGoal());
        }
        if (request.getActivityLevel() != null) {
            profile.setActivityLevel(request.getActivityLevel());
        }

        // 计算营养目标
        calculateNutritionTargets(profile);

        UserProfile saved = userProfileRepository.save(profile);
        log.info("用户档案更新成功: userId={}", userId);

        return toDTO(user, saved);
    }

    /**
     * 计算营养目标
     * 使用 Mifflin-St Jeor 公式计算 BMR 和 TDEE
     */
    private void calculateNutritionTargets(UserProfile profile) {
        // 只有在有足够数据时才计算
        if (profile.getGender() == null || profile.getBirthday() == null
                || profile.getHeight() == null || profile.getWeight() == null) {
            return;
        }

        int age = (int) ChronoUnit.YEARS.between(profile.getBirthday(), LocalDate.now());
        BigDecimal weight = profile.getWeight();
        BigDecimal height = profile.getHeight();

        // 计算 BMR (Mifflin-St Jeor 公式)
        BigDecimal bmr;
        if (profile.getGender() == 1) { // 男性
            // BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 + 5
            bmr = BigDecimal.TEN.multiply(weight)
                    .add(new BigDecimal("6.25").multiply(height))
                    .subtract(new BigDecimal(5 * age))
                    .add(new BigDecimal("5"));
        } else { // 女性
            // BMR = 10 × 体重(kg) + 6.25 × 身高(cm) - 5 × 年龄 - 161
            bmr = BigDecimal.TEN.multiply(weight)
                    .add(new BigDecimal("6.25").multiply(height))
                    .subtract(new BigDecimal(5 * age))
                    .subtract(new BigDecimal("161"));
        }

        profile.setBmr(bmr.setScale(0, RoundingMode.HALF_UP));

        // 计算 TDEE (根据活动水平)
        BigDecimal activityMultiplier = getActivityMultiplier(profile.getActivityLevel());
        BigDecimal tdee = bmr.multiply(activityMultiplier).setScale(0, RoundingMode.HALF_UP);
        profile.setTdee(tdee);

        // 根据目标计算目标热量
        BigDecimal targetCalories;
        String goal = profile.getGoal() != null ? profile.getGoal() : "maintain";
        switch (goal) {
            case "muscle_gain":
                targetCalories = tdee.multiply(new BigDecimal("1.1")); // +10%
                break;
            case "fat_loss":
                targetCalories = tdee.multiply(new BigDecimal("0.8")); // -20%
                break;
            default:
                targetCalories = tdee;
        }
        profile.setTargetCalories(targetCalories.setScale(0, RoundingMode.HALF_UP).intValue());

        // 计算三大营养素目标
        BigDecimal proteinRatio, fatRatio, carbsRatio;
        switch (goal) {
            case "muscle_gain":
                proteinRatio = new BigDecimal("0.30");
                fatRatio = new BigDecimal("0.25");
                carbsRatio = new BigDecimal("0.45");
                break;
            case "fat_loss":
                proteinRatio = new BigDecimal("0.35");
                fatRatio = new BigDecimal("0.25");
                carbsRatio = new BigDecimal("0.40");
                break;
            default:
                proteinRatio = new BigDecimal("0.25");
                fatRatio = new BigDecimal("0.30");
                carbsRatio = new BigDecimal("0.45");
        }

        // 蛋白质: g = kcal * ratio / 4
        profile.setTargetProtein(new BigDecimal(profile.getTargetCalories())
                .multiply(proteinRatio)
                .divide(new BigDecimal("4"), 1, RoundingMode.HALF_UP));

        // 脂肪: g = kcal * ratio / 9
        profile.setTargetFat(new BigDecimal(profile.getTargetCalories())
                .multiply(fatRatio)
                .divide(new BigDecimal("9"), 1, RoundingMode.HALF_UP));

        // 碳水: g = kcal * ratio / 4
        profile.setTargetCarbs(new BigDecimal(profile.getTargetCalories())
                .multiply(carbsRatio)
                .divide(new BigDecimal("4"), 1, RoundingMode.HALF_UP));
    }

    /**
     * 获取活动系数
     */
    private BigDecimal getActivityMultiplier(String activityLevel) {
        if (activityLevel == null) {
            return new BigDecimal("1.2");
        }
        switch (activityLevel) {
            case "sedentary":
                return new BigDecimal("1.2");
            case "light":
                return new BigDecimal("1.375");
            case "moderate":
                return new BigDecimal("1.55");
            case "high":
                return new BigDecimal("1.725");
            default:
                return new BigDecimal("1.2");
        }
    }

    /**
     * 转换为 DTO
     */
    private UserProfileDTO toDTO(User user, UserProfile profile) {
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(profile.getId())
                .userId(user.getId())
                .gender(profile.getGender())
                .birthday(profile.getBirthday())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .targetWeight(profile.getTargetWeight())
                .goal(profile.getGoal())
                .activityLevel(profile.getActivityLevel())
                .bmr(profile.getBmr())
                .tdee(profile.getTdee())
                .targetCalories(profile.getTargetCalories())
                .targetProtein(profile.getTargetProtein())
                .targetFat(profile.getTargetFat())
                .targetCarbs(profile.getTargetCarbs())
                .createdAt(profile.getCreateTime())
                .updatedAt(profile.getUpdateTime())
                .build();

        return dto;
    }
}