package com.fitness.ocr.controller;

import com.fitness.ocr.dto.OcrResultDTO;
import com.fitness.ocr.dto.Result;
import com.fitness.ocr.service.LlmService;
import com.fitness.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
