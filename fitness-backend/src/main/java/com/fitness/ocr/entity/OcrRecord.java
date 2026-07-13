package com.fitness.ocr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OCR 识别记录实体
 */
@Data
@Entity
@Table(name = "ocr_record")
public class OcrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "original_text", columnDefinition = "TEXT")
    private String originalText;

    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;

    @Column(name = "calories", length = 32)
    private String calories;

    @Column(name = "protein", length = 32)
    private String protein;

    @Column(name = "fat", length = 32)
    private String fat;

    @Column(name = "saturated_fat", length = 32)
    private String saturatedFat;

    @Column(name = "carbohydrates", length = 32)
    private String carbohydrates;

    @Column(name = "fiber", length = 32)
    private String fiber;

    @Column(name = "sodium", length = 32)
    private String sodium;

    @Column(name = "sugar", length = 32)
    private String sugar;

    @Column(name = "calcium", length = 32)
    private String calcium;

    @Column(name = "elapse_ms")
    private Long elapseMs;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "error_msg", length = 512)
    private String errorMsg;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

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