package com.fitness.ocr.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fitness.ocr.config.LlmProperties;
import com.fitness.ocr.dto.OcrResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 大模型服务
 * 对接火山引擎豆包大模型，解析营养成分数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final LlmProperties llmProperties;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 解析 OCR 结果中的营养成分
     *
     * @param ocrJson OCR 识别的 JSON 结果
     * @return 解析后的食物信息
     */
    public OcrResultDTO.FoodInfo parseNutrition(JSONObject ocrJson) {
        try {
            // 构建提示词
            String prompt = buildPrompt(ocrJson);

            log.info("调用大模型解析营养成分...");

            // 调用大模型 API
            String response = callLlmApi(prompt);

            if (response == null || response.isEmpty()) {
                log.error("大模型返回空结果");
                return new OcrResultDTO.FoodInfo();
            }

            log.debug("大模型原始返回: {}", response);

            // 解析大模型返回的 JSON
            OcrResultDTO.FoodInfo foodInfo = parseFoodInfoFromResponse(response);

            log.info("解析成功: {}", foodInfo);
            return foodInfo;

        } catch (Exception e) {
            log.error("大模型解析失败", e);
            return new OcrResultDTO.FoodInfo();
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(JSONObject ocrJson) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("分析这个营养成分表数据，给我返回一个JSON，除了JSON，什么都不要返回。\n\n");
        prompt.append("OCR识别的数据如下：\n");
        prompt.append("```json\n");
        prompt.append(ocrJson.toJSONString());
        prompt.append("\n```\n\n");

        prompt.append("返回的JSON格式如下：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"id\": \"\",\n");
        prompt.append("  \"name\": \"\",\n");
        prompt.append("  \"brand\": \"\",\n");
        prompt.append("  \"category\": \"\",\n");
        prompt.append("  \"servingSize\": {\n");
        prompt.append("    \"amount\": 100,\n");
        prompt.append("    \"unit\": \"g\"\n");
        prompt.append("  },\n");
        prompt.append("  \"nutrition\": {\n");
        prompt.append("    \"energy\": {\n");
        prompt.append("      \"kj\": 0,\n");
        prompt.append("      \"kcal\": 0\n");
        prompt.append("    },\n");
        prompt.append("    \"protein\": 0,\n");
        prompt.append("    \"fat\": 0,\n");
        prompt.append("    \"saturatedFat\": 0,\n");
        prompt.append("    \"transFat\": 0,\n");
        prompt.append("    \"carbohydrate\": 0,\n");
        prompt.append("    \"sugar\": 0,\n");
        prompt.append("    \"dietaryFiber\": 0,\n");
        prompt.append("    \"sodium\": 0,\n");
        prompt.append("    \"calcium\": 0\n");
        prompt.append("  }\n");
        prompt.append("}\n");
        prompt.append("```\n\n");

        prompt.append("要求：\n");
        prompt.append("1. name 字段：根据营养成分猜测这是什么食品，如果无法确定就留空\n");
        prompt.append("2. 数值请提取真实数据，如果OCR中没有某项数据，该项填0\n");
        prompt.append("3. 能量单位转换：如果只有千焦(kJ)，请计算千卡(kcal)，公式：kcal = kJ / 4.184\n");
        prompt.append("4. 只返回JSON，不要有任何其他文字说明\n");
        prompt.append("5. 确保返回的是有效的JSON格式\n");

        return prompt.toString();
    }

    /**
     * 调用大模型 API
     */
    private String callLlmApi(String prompt) throws IOException {
        JSONObject requestBody = new JSONObject();

        // 设置模型
        requestBody.put("model", llmProperties.getModel());

        // 关闭深度思考,直接返回结果
        JSONObject thinkingConfig = new JSONObject();
        thinkingConfig.put("type", "disabled");
        requestBody.put("thinking", thinkingConfig);

        // 构建输入
        JSONArray inputArray = new JSONArray();
        JSONObject userInput = new JSONObject();
        JSONArray contentArray = new JSONArray();

        // 添加文本内容
        JSONObject textContent = new JSONObject();
        textContent.put("type", "input_text");
        textContent.put("text", prompt);
        contentArray.add(textContent);

        userInput.put("role", "user");
        userInput.put("content", contentArray);
        inputArray.add(userInput);

        requestBody.put("input", inputArray);

        log.debug("请求大模型: {}", requestBody.toJSONString());

        // 发送请求
        Request request = new Request.Builder()
                .url(llmProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + llmProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json; charset=utf-8")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无错误信息";
                log.error("大模型服务返回错误: {} - {}", response.code(), errorBody);
                throw new RuntimeException("大模型服务错误: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("大模型原始响应: {}", responseBody);

            // 解析响应
            JSONObject responseJson = JSONObject.parseObject(responseBody);

            // 火山引擎返回的响应格式: 可能是数组形式 [{"type":"reasoning",...}, {"type":"message",...}]
            // 也可能是对象形式 {"output":...} 或 {"choices":[...]}
            String outputText = null;

            // 尝试从 message 中提取
            if (responseJson.containsKey("output")) {
                // 直接返回 output 字段(文本内容)
                outputText = responseJson.getString("output");
            } else if (responseJson.containsKey("choices")) {
                // OpenAI 格式兼容
                JSONArray choices = responseJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    if (message != null && message.containsKey("content")) {
                        outputText = message.getString("content");
                    }
                }
            } else {
                // 检查是否是数组格式(火山引擎新格式)
                // 响应体可能是数组 [{"type":"reasoning",...}, {"type":"message","content":[{"type":"output_text","text":"..."}]}]
                if (responseBody.trim().startsWith("[")) {
                    log.debug("检测到数组格式的响应");
                    JSONArray outputs = JSONArray.parseArray(responseBody);
                    if (outputs != null) {
                        for (int i = 0; i < outputs.size(); i++) {
                            JSONObject item = outputs.getJSONObject(i);
                            String type = item.getString("type");

                            // 查找 message 类型
                            if ("message".equals(type)) {
                                JSONArray messageContentArray = item.getJSONArray("content");
                                if (messageContentArray != null) {
                                    for (int j = 0; j < messageContentArray.size(); j++) {
                                        JSONObject contentItem = messageContentArray.getJSONObject(j);
                                        if ("output_text".equals(contentItem.getString("type"))) {
                                            outputText = contentItem.getString("text");
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    // 对象格式: 从 outputs 或 data 字段提取
                    JSONArray outputs = null;
                    if (responseJson.containsKey("outputs")) {
                        outputs = responseJson.getJSONArray("outputs");
                    } else if (responseJson.containsKey("data")) {
                        outputs = responseJson.getJSONArray("data");
                    }

                    if (outputs != null) {
                        for (int i = 0; i < outputs.size(); i++) {
                            JSONObject item = outputs.getJSONObject(i);
                            String type = item.getString("type");

                            // 查找 message 类型
                            if ("message".equals(type)) {
                                JSONArray messageContentArray = item.getJSONArray("content");
                                if (messageContentArray != null) {
                                    for (int j = 0; j < messageContentArray.size(); j++) {
                                        JSONObject contentItem = messageContentArray.getJSONObject(j);
                                        if ("output_text".equals(contentItem.getString("type"))) {
                                            outputText = contentItem.getString("text");
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (outputText == null || outputText.isEmpty()) {
                log.error("无法从大模型响应中提取文本内容: {}", responseBody);
                return null;
            }

            log.info("提取的文本内容: {}", outputText);
            return outputText;
        }
    }

    /**
     * 从大模型响应中解析食物信息
     */
    private OcrResultDTO.FoodInfo parseFoodInfoFromResponse(String response) {
        try {
            String jsonStr = response.trim();

            // 处理火山引擎返回的嵌套数组格式
            // [{"type":"message","content":[{"type":"output_text","text":"```json{...}```"}]}]
            if (jsonStr.startsWith("[")) {
                JSONArray responseArray = JSONArray.parseArray(jsonStr);
                if (responseArray != null && !responseArray.isEmpty()) {
                    // 找到 message 类型的元素
                    for (int i = 0; i < responseArray.size(); i++) {
                        JSONObject item = responseArray.getJSONObject(i);
                        if ("message".equals(item.getString("type"))) {
                            JSONArray contentArray = item.getJSONArray("content");
                            if (contentArray != null && !contentArray.isEmpty()) {
                                for (int j = 0; j < contentArray.size(); j++) {
                                    JSONObject contentItem = contentArray.getJSONObject(j);
                                    if ("output_text".equals(contentItem.getString("type"))) {
                                        jsonStr = contentItem.getString("text");
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

            // 去除可能的 markdown 代码块标记
            jsonStr = jsonStr.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            } else if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            // 解析 JSON
            JSONObject result = JSONObject.parseObject(jsonStr);

            OcrResultDTO.FoodInfo foodInfo = new OcrResultDTO.FoodInfo();

            // 解析食物基本信息
            foodInfo.setFoodName(result.getString("name"));
            foodInfo.setFoodCategory(result.getString("category"));

            // 解析份量
            JSONObject servingSizeJson = result.getJSONObject("servingSize");
            if (servingSizeJson != null) {
                BigDecimal amount = servingSizeJson.getBigDecimal("amount");
                if (amount != null) {
                    foodInfo.setServingSize(amount);
                }
            }

            // 解析营养成分
            JSONObject nutritionJson = result.getJSONObject("nutrition");
            if (nutritionJson != null) {
                OcrResultDTO.Nutrition nutrition = new OcrResultDTO.Nutrition();

                // 能量
                JSONObject energy = nutritionJson.getJSONObject("energy");
                if (energy != null) {
                    nutrition.setEnergyKj(energy.getBigDecimal("kj"));
                    nutrition.setEnergyKcal(energy.getBigDecimal("kcal"));
                }

                // 其他营养成分
                nutrition.setProtein(nutritionJson.getBigDecimal("protein"));
                nutrition.setFat(nutritionJson.getBigDecimal("fat"));
                nutrition.setSaturatedFat(nutritionJson.getBigDecimal("saturatedFat"));
                nutrition.setTransFat(nutritionJson.getBigDecimal("transFat"));
                nutrition.setCarbohydrate(nutritionJson.getBigDecimal("carbohydrate"));
                nutrition.setSugar(nutritionJson.getBigDecimal("sugar"));
                nutrition.setDietaryFiber(nutritionJson.getBigDecimal("dietaryFiber"));
                nutrition.setSodium(nutritionJson.getBigDecimal("sodium"));
                nutrition.setCalcium(nutritionJson.getBigDecimal("calcium"));

                foodInfo.setNutrition(nutrition);
            }

            return foodInfo;

        } catch (Exception e) {
            log.error("解析大模型返回的 JSON 失败: {}", response, e);
            return new OcrResultDTO.FoodInfo();
        }
    }
}
