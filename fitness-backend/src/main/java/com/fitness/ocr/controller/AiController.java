package com.fitness.ocr.controller;

import com.fitness.ocr.dto.DailyNutritionStatsDTO;
import com.fitness.ocr.dto.DietAnalysisResultDTO;
import com.fitness.ocr.dto.NutritionRecordDTO;
import com.fitness.ocr.dto.OcrResultDTO;
import com.fitness.ocr.dto.Result;
import com.fitness.ocr.dto.UserProfileDTO;
import com.fitness.ocr.service.LlmService;
import com.fitness.ocr.service.NutritionService;
import com.fitness.ocr.service.OcrService;
import com.fitness.ocr.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 识别控制器
 * 提供食谱解析等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final LlmService llmService;
    private final OcrService ocrService;
    private final UserProfileService userProfileService;
    private final NutritionService nutritionService;

    /**
     * AI 分析每日饮食
     * 综合用户身体数据（身高/体重/性别/年龄/目标/活动水平）和指定日期的饮食记录，
     * 分析当日饮食是否合理，给出评分、各项营养分析和改进建议
     *
     * @param userId  用户ID
     * @param request 分析请求（date 可选，默认今天）
     * @return 饮食分析结果
     */
    @PostMapping("/analyze-diet")
    public Result<DietAnalysisResultDTO> analyzeDiet(
            @RequestAttribute("userId") Long userId,
            @RequestBody(required = false) DietAnalysisRequest request) {
        // 解析日期，默认今天
        LocalDate date;
        if (request != null && request.getDate() != null && !request.getDate().trim().isEmpty()) {
            try {
                date = LocalDate.parse(request.getDate());
            } catch (Exception e) {
                return Result.error("日期格式错误，应为 yyyy-MM-dd");
            }
        } else {
            date = LocalDate.now();
        }
        log.info("AI 分析每日饮食: userId={}, date={}", userId, date);

        try {
            // 1. 获取用户档案并校验身体信息是否齐全
            UserProfileDTO profile = userProfileService.getProfile(userId);
            if (profile.getGender() == null || profile.getBirthday() == null
                    || profile.getHeight() == null || profile.getWeight() == null) {
                return Result.error("请先在「我的」页面完善性别、生日、身高、体重等身体信息");
            }

            // 2. 获取当日已确认（已吃）的饮食记录和统计
            List<NutritionRecordDTO> allRecords = nutritionService.getUserRecordsByDate(userId, date);
            List<NutritionRecordDTO> records = allRecords.stream()
                    .filter(r -> r.getEaten() == null || Boolean.TRUE.equals(r.getEaten()))
                    .collect(Collectors.toList());

            if (records.isEmpty()) {
                return Result.error(date + " 暂无已确认的饮食记录，请先记录并确认今日饮食");
            }

            DailyNutritionStatsDTO stats = nutritionService.getStatsByDate(userId, date);

            // 3. 调用大模型分析
            DietAnalysisResultDTO result = llmService.analyzeDailyDiet(profile, records, stats);

            if (result == null) {
                return Result.error("AI 分析失败，请稍后重试");
            }

            return Result.success(result);

        } catch (Exception e) {
            log.error("AI 分析每日饮食异常", e);
            return Result.error("AI 分析失败: " + e.getMessage());
        }
    }

    /**
     * 每日饮食分析请求
     */
    @lombok.Data
    public static class DietAnalysisRequest {
        /**
         * 分析日期 yyyy-MM-dd（可选，默认今天）
         */
        private String date;
    }

    /**
     * AI 解析食谱文本
     * 根据输入的食谱文本，计算总营养成分
     *
     * @param request 食谱解析请求
     * @return 解析后的食物信息
     */
    @PostMapping("/parse-recipe")
    public Result<OcrResultDTO.FoodInfo> parseRecipe(@Valid @RequestBody RecipeRequest request) {
        log.info("收到 AI 食谱解析请求: {}", request.getContent().substring(0, Math.min(50, request.getContent().length())));

        try {
            OcrResultDTO.FoodInfo foodInfo = llmService.parseRecipe(request.getContent());

            if (foodInfo.getFoodName() != null || foodInfo.getNutrition() != null) {
                return Result.success(foodInfo);
            } else {
                return Result.error("AI 解析失败，请检查输入内容");
            }

        } catch (Exception e) {
            log.error("AI 食谱解析异常", e);
            return Result.error("AI 解析失败: " + e.getMessage());
        }
    }

    /**
     * AI 解析食谱图片
     * 先 OCR 提取文字，再 AI 分析食谱
     *
     * @param request 图片解析请求
     * @return 解析后的食物信息
     */
    @PostMapping("/parse-recipe-image")
    public Result<OcrResultDTO.FoodInfo> parseRecipeImage(@Valid @RequestBody RecipeImageRequest request) {
        log.info("收到 AI 食谱图片解析请求");

        try {
            // 1. 先调用 OCR 提取文字
            OcrResultDTO ocrResult = ocrService.recognize(request.getImageBase64());

            if (!Boolean.TRUE.equals(ocrResult.getSuccess())) {
                return Result.error("OCR 识别失败: " + ocrResult.getError());
            }

            String extractedText = ocrResult.getText();
            if (extractedText == null || extractedText.trim().isEmpty()) {
                // 如果 OCR 没识别到文字，但有补充文本，则使用补充文本
                if (request.getSupplement() != null && !request.getSupplement().trim().isEmpty()) {
                    extractedText = request.getSupplement();
                } else {
                    return Result.error("图片中未识别到文字");
                }
            }

            // 2. 如果有补充文本，合并到 OCR 结果中
            String recipeText = extractedText;
            if (request.getSupplement() != null && !request.getSupplement().trim().isEmpty()) {
                recipeText = "【OCR识别内容】\n" + extractedText + "\n\n【用户补充】\n" + request.getSupplement();
            }

            log.info("合并后的食谱文本: {}", recipeText.substring(0, Math.min(100, recipeText.length())));

            // 3. 调用 AI 分析食谱
            OcrResultDTO.FoodInfo foodInfo = llmService.parseRecipe(recipeText);

            if (foodInfo.getFoodName() != null || foodInfo.getNutrition() != null) {
                return Result.success(foodInfo);
            } else {
                return Result.error("AI 解析失败，请检查图片内容");
            }

        } catch (Exception e) {
            log.error("AI 食谱图片解析异常", e);
            return Result.error("AI 解析失败: " + e.getMessage());
        }
    }

    /**
     * 食谱解析请求（文本）
     */
    @lombok.Data
    public static class RecipeRequest {
        /**
         * 食谱文本内容
         */
        @jakarta.validation.constraints.NotBlank(message = "食谱内容不能为空")
        private String content;
    }

    /**
     * 食谱解析请求（图片）
     */
    @lombok.Data
    public static class RecipeImageRequest {
        /**
         * Base64 编码的图片数据
         */
        @jakarta.validation.constraints.NotBlank(message = "图片数据不能为空")
        private String imageBase64;

        /**
         * 补充说明文本（可选）
         */
        private String supplement;
    }

    /**
     * AI 根据食物名称填充营养成分
     * 根据输入的食物名称，返回该食物的营养成分参考值
     *
     * @param request 食物名称请求
     * @return 食物的营养成分信息
     */
    @PostMapping("/fill-nutrition")
    public Result<OcrResultDTO.FoodInfo> fillNutrition(@Valid @RequestBody FoodNameRequest request) {
        log.info("收到 AI 营养填充请求: {}", request.getFoodName());

        try {
            OcrResultDTO.FoodInfo foodInfo = llmService.fillNutritionByFoodName(request.getFoodName());

            if (foodInfo.getFoodName() != null && foodInfo.getNutrition() != null) {
                return Result.success(foodInfo);
            } else {
                return Result.error("AI 填充失败，请检查输入内容");
            }

        } catch (Exception e) {
            log.error("AI 营养填充异常", e);
            return Result.error("AI 填充失败: " + e.getMessage());
        }
    }

    /**
     * 食物名称请求
     */
    @lombok.Data
    public static class FoodNameRequest {
        /**
         * 食物名称
         */
        @jakarta.validation.constraints.NotBlank(message = "食物名称不能为空")
        private String foodName;
    }
}
