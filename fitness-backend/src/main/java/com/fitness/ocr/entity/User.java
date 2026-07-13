package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @Column(name = "username", unique = true, nullable = false, length = 64)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password_hash", nullable = false, length = 256)
    private String passwordHash;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 64)
    private String nickname;

    /**
     * 头像
     */
    @Column(name = "avatar", length = 256)
    private String avatar;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 128)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 状态：0-禁用，1-正常
     */
    @Column(name = "status")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
