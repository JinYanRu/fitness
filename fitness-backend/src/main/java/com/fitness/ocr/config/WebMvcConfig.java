package com.fitness.ocr.config;

import com.fitness.ocr.security.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns(
                        "/api/auth/login",       // 登录接口
                        "/api/auth/register",    // 注册接口
                        "/api/ocr/**",           // OCR 相关接口（暂时开放）
                        "/api/food/common/**"    // 公共食物库（暂时开放）
                );
    }
}