package com.fitness.ocr.service;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.entity.User;
import com.fitness.ocr.entity.UserProfile;
import com.fitness.ocr.repository.UserProfileRepository;
import com.fitness.ocr.repository.UserRepository;
import com.fitness.ocr.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * 用户认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        // 创建默认用户档案
        UserProfile profile = new UserProfile();
        profile.setUserId(savedUser.getId());
        userProfileRepository.save(profile);

        // 生成 Token
        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration())
                .user(LoginResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .username(savedUser.getUsername())
                        .nickname(savedUser.getNickname())
                        .avatar(savedUser.getAvatar())
                        .build())
                .build();
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .build())
                .build();
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public LoginResponse.UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 检查密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}