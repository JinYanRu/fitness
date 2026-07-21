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

    /**
     * 食物信息 JSON 模板（营养成分表解析和食谱解析共用）
     */
    private static final String FOOD_INFO_JSON_TEMPLATE = "{\n" +
            "  \"id\": \"\",\n" +
            "  \"name\": \"\",\n" +
            "  \"brand\": \"\",\n" +
            "  \"category\": \"\",\n" +
            "  \"servingSize\": {\n" +
            "    \"amount\": 100,\n" +
            "    \"unit\": \"g\"\n" +
            "  },\n" +
            "  \"nutrition\": {\n" +
            "    \"energy\": {\n" +
            "      \"kj\": 0,\n" +
            "      \"kcal\": 0\n" +
            "    },\n" +
            "    \"protein\": 0,\n" +
            "    \"fat\": 0,\n" +
            "    \"saturatedFat\": 0,\n" +
            "    \"transFat\": 0,\n" +
            "    \"carbohydrate\": 0,\n" +
            "    \"sugar\": 0,\n" +
            "    \"dietaryFiber\": 0,\n" +
            "    \"sodium\": 0,\n" +
            "    \"calcium\": 0\n" +
            "  }\n" +
            "}";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 解析 OCR 结果中的营养成分（不开启深度思考）
     *
     * @param ocrJson OCR 识别的 JSON 结果
     * @return 解析后的食物信息
     */
    public OcrResultDTO.FoodInfo parseNutrition(JSONObject ocrJson) {
        try {
            // 构建提示词
            String prompt = buildNutritionPrompt(ocrJson);

            log.info("调用大模型解析营养成分（不开启深度思考）...");

            // 调用大模型 API，不开启深度思考
            String response = callLlmApi(prompt, false);

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
     * 解析食谱文本，计算总营养成分（开启深度思考）
     *
     * @param recipeText 食谱文本
     * @return 解析后的食物信息（包含总营养）
     */
    public OcrResultDTO.FoodInfo parseRecipe(String recipeText) {
        try {
            // 构建食谱解析提示词
            String prompt = buildRecipePrompt(recipeText);

            log.info("调用大模型解析食谱（开启深度思考）...");

            // 调用大模型 API，开启深度思考
            String response = callLlmApi(prompt, true);

            if (response == null || response.isEmpty()) {
                log.error("大模型返回空结果");
                return new OcrResultDTO.FoodInfo();
            }

            log.debug("大模型原始返回: {}", response);

            // 解析大模型返回的 JSON（复用同一个解析方法）
            OcrResultDTO.FoodInfo foodInfo = parseFoodInfoFromResponse(response);

            log.info("食谱解析成功: {}", foodInfo);
            return foodInfo;

        } catch (Exception e) {
            log.error("大模型解析食谱失败", e);
            return new OcrResultDTO.FoodInfo();
        }
    }

    /**
     * 构建营养成分表解析提示词
     */
    private String buildNutritionPrompt(JSONObject ocrJson) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("分析这个营养成分表数据，给我返回一个JSON，除了JSON，什么都不要返回。\n\n");
        prompt.append("OCR识别的数据如下：\n");
        prompt.append("```json\n");
        prompt.append(ocrJson.toJSONString());
        prompt.append("\n```\n\n");

        prompt.append("返回的JSON格式如下：\n");
        prompt.append("```json\n");
        prompt.append(FOOD_INFO_JSON_TEMPLATE);
        prompt.append("\n```\n\n");

        prompt.append("要求：\n");
        prompt.append("1. name 字段：根据营养成分猜测这是什么食品，如果无法确定就留空\n");
        prompt.append("2. 数值请提取真实数据，如果OCR中没有某项数据，该项填0\n");
        prompt.append("3. 能量单位转换：如果只有千焦(kJ)，请计算千卡(kcal)，公式：kcal = kJ / 4.184\n");
        prompt.append("4. 只返回JSON，不要有任何其他文字说明\n");
        prompt.append("5. 确保返回的是有效的JSON格式\n");

        return prompt.toString();
    }

    /**
     * 构建食谱解析提示词
     */
    private String buildRecipePrompt(String recipeText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是营养分析专家。严格按照以下步骤计算食谱的总营养成分。\n\n");
        prompt.append("食谱内容：\n");
        prompt.append(recipeText);
        prompt.append("\n\n");

        prompt.append("【第一步：列出每个食材及其重量，计算营养】\n");
        prompt.append("使用公式：食材营养 = (食材重量g / 100) × 每100g营养参考值\n\n");

        prompt.append("【食材营养参考表（每100g）】\n");
        prompt.append("| 食材 | 热量kcal | 蛋白质g | 脂肪g | 碳水g | 钠mg |\n");
        prompt.append("|------|----------|---------|-------|-------|------|\n");
        prompt.append("| 鸡胸肉 | 120 | 23 | 2 | 0 | 0 |\n");
        prompt.append("| 鸡腿肉 | 180 | 20 | 8 | 0 | 0 |\n");
        prompt.append("| 牛瘦肉 | 150 | 22 | 5 | 0 | 0 |\n");
        prompt.append("| 猪瘦肉 | 143 | 20 | 6 | 0 | 0 |\n");
        prompt.append("| 木薯淀粉/淀粉 | 346 | 1 | 0 | 85 | 0 |\n");
        prompt.append("| 玉米淀粉 | 346 | 1 | 0 | 85 | 0 |\n");
        prompt.append("| 白糖/糖 | 400 | 0 | 0 | 100 | 0 |\n");
        prompt.append("| 盐 | 0 | 0 | 0 | 0 | 38800 |\n");
        prompt.append("| 味精 | 0 | 0 | 0 | 0 | 12000 |\n");
        prompt.append("| 食用油 | 900 | 0 | 100 | 0 | 0 |\n");
        prompt.append("| 鸡蛋/全蛋 | 144 | 13 | 11 | 1 | 0 |\n");
        prompt.append("| 蛋清 | 18 | 11 | 0.2 | 0 | 0 |\n\n");

        prompt.append("【第二步：汇总计算】\n");
        prompt.append("总热量 = 所有食材热量相加\n");
        prompt.append("总蛋白质 = 所有食材蛋白质相加\n");
        prompt.append("总脂肪 = 所有食材脂肪相加\n");
        prompt.append("总碳水 = 所有食材碳水相加\n");
        prompt.append("总钠 = 所有食材钠相加\n\n");

        prompt.append("【第三步：热量校验 - 必须通过】\n");
        prompt.append("校验公式：热量(kcal) = 蛋白质 × 4 + 脂肪 × 9 + 碳水 × 4\n");
        prompt.append("要求：校验热量与汇总热量的差距必须 ≤ 10%\n");
        prompt.append("如果差距 > 10%，说明计算错误，必须重新计算！\n\n");

        prompt.append("【返回JSON格式】\n");
        prompt.append("```json\n");
        prompt.append(FOOD_INFO_JSON_TEMPLATE);
        prompt.append("\n```\n\n");

        prompt.append("【输出要求】\n");
        prompt.append("1. name：食谱名称\n");
        prompt.append("2. servingSize.amount：成品重量（如果食谱给出就用给定的，否则用食材总重量）\n");
        prompt.append("3. nutrition：填写第二步汇总的结果\n");
        prompt.append("4. 只返回JSON，不要返回计算过程\n");
        prompt.append("5. 确保JSON格式正确\n");

        return prompt.toString();
    }

    /**
     * 调用大模型 API
     *
     * @param prompt        提示词
     * @param enableThinking 是否开启深度思考
     */
    private String callLlmApi(String prompt, boolean enableThinking) throws IOException {
        JSONObject requestBody = new JSONObject();

        // 设置模型
        requestBody.put("model", llmProperties.getModel());

        // 根据场景决定是否开启深度思考
        if (enableThinking) {
            // AI识别场景：开启深度思考，提高食谱计算准确性
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("type", "enabled");
            requestBody.put("thinking", thinkingConfig);
        }else{
            // AI识别场景：开启深度思考，提高食谱计算准确性
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("type", "disabled");
            requestBody.put("thinking", thinkingConfig);
        }
        // 拍照添加场景：不开启深度思考，直接返回结果

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
