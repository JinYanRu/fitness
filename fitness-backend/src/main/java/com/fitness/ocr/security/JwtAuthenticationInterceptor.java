package com.fitness.ocr.security;

import com.fitness.ocr.config.JwtProperties;
import com.fitness.ocr.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 对于 OPTIONS 请求直接放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);

        if (token == null) {
            log.warn("请求缺少认证 Token: {}", request.getRequestURI());
            sendErrorResponse(response, "未登录或 Token 已过期", 401);
            return false;
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("Token 无效或已过期: {}", request.getRequestURI());
            sendErrorResponse(response, "Token 无效或已过期", 401);
            return false;
        }

        // 将用户ID和用户名存入请求属性
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        log.debug("用户认证成功: userId={}, username={}, uri={}", userId, username, request.getRequestURI());

        return true;
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeader());
        if (header == null || !header.startsWith(jwtProperties.getTokenPrefix())) {
            return null;
        }
        return header.substring(jwtProperties.getTokenPrefix().length());
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int code) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}