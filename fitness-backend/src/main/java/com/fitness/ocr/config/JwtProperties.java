package com.fitness.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret = "fitness-ocr-default-secret-key-please-change-in-production";

    /**
     * Token 过期时间（秒），默认 7 天
     */
    private Long expiration = 7 * 24 * 60 * 60L;

    /**
     * Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 请求头名称
     */
    private String header = "Authorization";
}