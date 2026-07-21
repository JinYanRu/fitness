package com.fitness.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OCR 服务配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocr.service")
public class OcrProperties {

    /**
     * RapidOCR 服务地址
     */
    private String rapidUrl = "http://111.228.49.250:10265";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
}