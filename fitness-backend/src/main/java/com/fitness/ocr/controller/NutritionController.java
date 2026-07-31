package com.fitness.ocr.controller;

import com.fitness.ocr.dto.*;
import com.fitness.ocr.service.NutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 营养记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    /**
     * 创建营养记录
     */
    @PostMapping
    public Result<NutritionRecordDTO> create(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NutritionRecordRequest request) {
        log.info("创建营养记录: userId={}, foodName={}", userId, request.getFoodName());
        NutritionRecordDTO record = nutritionService.createRecord(userId, request);
        return Result.success(record);
    }

    /**
     * 更新营养记录
     */
    @PutMapping("/{id}")
    public Result<NutritionRecordDTO> update(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody NutritionRecordRequest request) {
        log.info("更新营养记录: userId={}, id={}", userId, id);
        NutritionRecordDTO record = nutritionService.updateRecord(userId, id, request);
        return Result.success(record);
    }

    /**
     * 获取用户所有记录
     */
    @GetMapping
    public Result<List<NutritionRecordDTO>> getList(@RequestAttribute("userId") Long userId) {
        List<NutritionRecordDTO> records = nutritionService.getUserRecords(userId);
        return Result.success(records);
    }

    /**
     * 获取单条记录
     */
    @GetMapping("/{id}")
    public Result<NutritionRecordDTO> getById(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        NutritionRecordDTO record = nutritionService.getRecordById(userId, id);
        return Result.success(record);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        log.info("删除营养记录: userId={}, id={}", userId, id);
        nutritionService.deleteRecord(userId, id);
        return Result.success();
    }

    /**
     * 导入指定日期的记录到今天（标记为未吃）
     */
    @PostMapping("/import")
    public Result<Integer> importRecords(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ImportRecordRequest request) {
        log.info("导入记录: userId={}, sourceDate={}", userId, request.getSourceDate());
        int count = nutritionService.importRecords(userId, request.getSourceDate());
        return Result.success(count);
    }

    /**
     * 标记记录是否已吃
     */
    @PutMapping("/{id}/eaten")
    public Result<NutritionRecordDTO> markEaten(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean eaten = body.getOrDefault("eaten", Boolean.TRUE);
        log.info("标记已吃: userId={}, id={}, eaten={}", userId, id, eaten);
        NutritionRecordDTO record = nutritionService.markEaten(userId, id, eaten);
        return Result.success(record);
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/stats/today")
    public Result<DailyNutritionStatsDTO> getTodayStats(@RequestAttribute("userId") Long userId) {
        DailyNutritionStatsDTO stats = nutritionService.getTodayStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取指定日期的统计
     */
    @GetMapping("/stats/{date}")
    public Result<DailyNutritionStatsDTO> getStatsByDate(
            @RequestAttribute("userId") Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DailyNutritionStatsDTO stats = nutritionService.getStatsByDate(userId, date);
        return Result.success(stats);
    }

    /**
     * 获取日期范围内的记录
     */
    @GetMapping("/range")
    public Result<List<NutritionRecordDTO>> getByDateRange(
            @RequestAttribute("userId") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<NutritionRecordDTO> records = nutritionService.getUserRecordsByDateRange(userId, startDate, endDate);
        return Result.success(records);
    }

    /**
     * 获取指定日期的记录
     */
    @GetMapping("/daily/{date}")
    public Result<List<NutritionRecordDTO>> getByDate(
            @RequestAttribute("userId") Long userId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<NutritionRecordDTO> records = nutritionService.getUserRecordsByDate(userId, date);
        return Result.success(records);
    }
}