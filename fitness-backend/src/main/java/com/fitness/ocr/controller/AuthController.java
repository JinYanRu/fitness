package com.fitness.ocr.controller;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.service.AuthService;
import com.fitness.ocr.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserProfileService userProfileService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        LoginResponse response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<LoginResponse.UserInfo> getProfile(@RequestAttribute("userId") Long userId) {
        LoginResponse.UserInfo userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 获取用户档案（包含营养目标）
     */
    @GetMapping("/profile/detail")
    public Result<UserProfileDTO> getProfileDetail(@RequestAttribute("userId") Long userId) {
        UserProfileDTO profile = userProfileService.getProfile(userId);
        return Result.success(profile);
    }

    /**
     * 更新用户档案
     */
    @PutMapping("/profile")
    public Result<UserProfileDTO> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("更新用户档案: userId={}", userId);
        UserProfileDTO profile = userProfileService.updateProfile(userId, request);
        return Result.success(profile);
    }
}