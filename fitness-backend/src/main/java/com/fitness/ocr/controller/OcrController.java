package com.fitness.ocr.controller;

import com.fitness.ocr.dto.OcrRequest;
import com.fitness.ocr.dto.OcrResultDTO;
import com.fitness.ocr.dto.Result;
import com.fitness.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * OCR 识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    /**
     * OCR 识别接口
     *
     * @param request OCR 请求
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<OcrResultDTO> recognize(@Valid @RequestBody OcrRequest request) {
        log.info("收到 OCR 识别请求");

        try {
            OcrResultDTO result = ocrService.recognize(request.getImageBase64());

            if (Boolean.TRUE.equals(result.getSuccess())) {
                return Result.success(result);
            } else {
                return Result.error(400, result.getError());
            }

        } catch (Exception e) {
            log.error("OCR 识别异常", e);
            return Result.error("OCR 识别失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}