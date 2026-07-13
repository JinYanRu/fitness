package com.fitness.ocr.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * OCR 识别请求
 */
@Data
public class OcrRequest {

    /**
     * Base64 编码的图片数据
     */
    @NotBlank(message = "图片数据不能为空")
    private String imageBase64;

    /**
     * 是否需要解析营养成分
     */
    private Boolean parseNutrition = true;
}