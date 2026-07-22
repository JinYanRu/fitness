package com.fitness.ocr.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fitness.ocr.config.OcrProperties;
import com.fitness.ocr.dto.OcrResultDTO;
import com.fitness.ocr.entity.OcrRecord;
import com.fitness.ocr.repository.OcrRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OCR 服务
 * 对接 RapidOCR 本地服务，实现营养成分识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final OcrProperties ocrProperties;
    private final LlmService llmService;
    private final OcrRecordRepository ocrRecordRepository;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 识别图片中的文字
     *
     * @param base64Image Base64 编码的图片
     * @return OCR 识别结果
     */
    @Transactional
    public OcrResultDTO recognize(String base64Image) {
        long startTime = System.currentTimeMillis();
        OcrRecord record = new OcrRecord();
        record.setStatus(0); // 默认失败

        try {
            // 移除 data:image/xxx;base64, 前缀
            String base64Data = base64Image.contains(",")
                    ? base64Image.split(",")[1]
                    : base64Image;

            // 调用 RapidOCR 服务
            JSONObject result = callRapidOcr(base64Data);

            if (result == null) {
                saveOcrRecord(record, null, "OCR 服务返回空结果", startTime);
                return OcrResultDTO.builder()
                        .success(false)
                        .error("OCR 服务返回空结果")
                        .build();
            }

            // 解析结果
            OcrResultDTO.OcrResultDTOBuilder builder = OcrResultDTO.builder()
                    .success(result.getInteger("code") == 200)
                    .elapseMs(System.currentTimeMillis() - startTime);

            // 获取文本块 - RapidOCR 返回的是 data 字段
            JSONArray dataArray = result.getJSONArray("data");
            if (dataArray == null) {
                // 兼容旧格式
                dataArray = result.getJSONArray("texts");
            }

            if (dataArray != null) {
                List<OcrResultDTO.TextBlock> textBlocks = parseTextBlocks(dataArray);
                builder.texts(textBlocks);

                // 拼接完整文本
                StringBuilder fullTextBuilder = new StringBuilder();
                for (OcrResultDTO.TextBlock block : textBlocks) {
                    if (block.getText() != null) {
                        if (fullTextBuilder.length() > 0) {
                            fullTextBuilder.append("\n");
                        }
                        fullTextBuilder.append(block.getText());
                    }
                }
                String fullText = fullTextBuilder.toString();
                builder.text(fullText);
                record.setFullText(fullText);

                // 使用大模型解析食物信息
                if (!textBlocks.isEmpty()) {
                    OcrResultDTO.FoodInfo foodInfo = llmService.parseNutrition(result);
                    builder.foodInfo(foodInfo);

                    // 将解析结果保存到记录
                    if (foodInfo != null && foodInfo.getNutrition() != null) {
                        saveNutritionToRecord(record, foodInfo);
                    }
                }
            }

            OcrResultDTO dto = builder.build();
            record.setStatus(1); // 成功
            saveOcrRecord(record, dto, null, startTime);

            return dto;

        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            saveOcrRecord(record, null, e.getMessage(), startTime);
            return OcrResultDTO.builder()
                    .success(false)
                    .error(e.getMessage())
                    .elapseMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 将营养信息保存到 OCR 记录
     */
    private void saveNutritionToRecord(OcrRecord record, OcrResultDTO.FoodInfo foodInfo) {
        OcrResultDTO.Nutrition nutrition = foodInfo.getNutrition();
        if (nutrition == null) return;

        // 设置食物名称
        if (foodInfo.getFoodName() != null) {
            // 可以扩展 OcrRecord 添加 foodName 字段
        }

        // 设置营养成分（转换为字符串存储）
        if (nutrition.getEnergyKcal() != null) {
            record.setCalories(nutrition.getEnergyKcal().toString());
        }
        if (nutrition.getProtein() != null) {
            record.setProtein(nutrition.getProtein().toString());
        }
        if (nutrition.getFat() != null) {
            record.setFat(nutrition.getFat().toString());
        }
        if (nutrition.getSaturatedFat() != null) {
            record.setSaturatedFat(nutrition.getSaturatedFat().toString());
        }
        if (nutrition.getCarbohydrate() != null) {
            record.setCarbohydrates(nutrition.getCarbohydrate().toString());
        }
        if (nutrition.getDietaryFiber() != null) {
            record.setFiber(nutrition.getDietaryFiber().toString());
        }
        if (nutrition.getSodium() != null) {
            record.setSodium(nutrition.getSodium().toString());
        }
        if (nutrition.getSugar() != null) {
            record.setSugar(nutrition.getSugar().toString());
        }
        if (nutrition.getCalcium() != null) {
            record.setCalcium(nutrition.getCalcium().toString());
        }
    }

    /**
     * 保存 OCR 识别记录
     */
    private void saveOcrRecord(OcrRecord record, OcrResultDTO result, String errorMsg, long startTime) {
        record.setElapseMs(System.currentTimeMillis() - startTime);
        if (errorMsg != null) {
            record.setErrorMsg(errorMsg);
            record.setStatus(0);
        } else if (result != null && Boolean.TRUE.equals(result.getSuccess())) {
            record.setStatus(1);
        }

        try {
            ocrRecordRepository.save(record);
            log.info("OCR 记录已保存: id={}, status={}, elapseMs={}",
                    record.getId(), record.getStatus(), record.getElapseMs());
        } catch (Exception e) {
            log.error("保存 OCR 记录失败", e);
        }
    }

    /**
     * 调用 RapidOCR 服务
     */
    private JSONObject callRapidOcr(String base64Data) throws IOException {
        String url = ocrProperties.getRapidUrl() + "/ocr/base64";

        log.info("调用 RapidOCR: url={}, base64Length={}", url, base64Data.length());

        // RapidOCR API /ocr/base64 端点期望 application/x-www-form-urlencoded 格式
        RequestBody body = new FormBody.Builder()
                .add("image", base64Data)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            log.info("RapidOCR 响应: code={}, body={}", response.code(),
                     responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

            if (!response.isSuccessful()) {
                log.error("RapidOCR 服务返回错误: code={}, body={}", response.code(), responseBody);
                throw new RuntimeException("OCR 服务错误: " + response.code() + ", 响应: " + responseBody);
            }

            log.debug("RapidOCR 返回: {}", responseBody);

            return JSONObject.parseObject(responseBody);
        }
    }

    /**
     * 解析文本块
     */
    private List<OcrResultDTO.TextBlock> parseTextBlocks(JSONArray textsArray) {
        List<OcrResultDTO.TextBlock> blocks = new ArrayList<>();

        for (int i = 0; i < textsArray.size(); i++) {
            JSONObject item = textsArray.getJSONObject(i);
            OcrResultDTO.TextBlock block = new OcrResultDTO.TextBlock();
            block.setText(item.getString("text"));

            // 解析 box
            JSONArray boxArray = item.getJSONArray("box");
            if (boxArray != null) {
                List<List<Integer>> box = new ArrayList<>();
                for (int j = 0; j < boxArray.size(); j++) {
                    JSONArray point = boxArray.getJSONArray(j);
                    box.add(List.of(point.getInteger(0), point.getInteger(1)));
                }
                block.setBox(box);
            }

            // 解析 score/confidence
            Double score = item.getDouble("confidence");
            if (score == null) {
                score = item.getDouble("score");
            }
            block.setScore(score);

            blocks.add(block);
        }

        return blocks;
    }
}