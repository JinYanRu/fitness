package com.fitness.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型服务配置属性
 * 对接火山引擎豆包大模型
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm.service")
public class LlmProperties {

    /**
     * 大模型 API 地址
     */
    private String apiUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
}
