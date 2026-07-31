package com.fitness.ocr.service;

import com.fitness.ocr.dto.DailyNutritionStatsDTO;
import com.fitness.ocr.dto.NutritionRecordDTO;
import com.fitness.ocr.dto.NutritionRecordRequest;
import com.fitness.ocr.entity.NutritionRecord;
import com.fitness.ocr.repository.NutritionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营养记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutritionRecordRepository nutritionRecordRepository;

    /**
     * 创建营养记录
     */
    @Transactional
    public NutritionRecordDTO createRecord(Long userId, NutritionRecordRequest request) {
        NutritionRecord record = new NutritionRecord();

        // 设置用户ID
        record.setUserId(String.valueOf(userId));

        // 设置基本字段
        record.setFoodName(request.getFoodName());
        record.setBrand(request.getBrand());
        record.setServingAmount(request.getServingAmount());
        record.setServingUnit(request.getServingUnit());

        // 设置营养数据
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setFat(request.getFat());
        record.setSaturatedFat(request.getSaturatedFat());
        record.setCarbohydrates(request.getCarbohydrates());
        record.setFiber(request.getFiber());
        record.setSodium(request.getSodium());
        record.setSugar(request.getSugar());
        record.setCalcium(request.getCalcium());

        // 设置其他字段
        record.setRemark(request.getRemark());
        record.setOcrText(request.getOcrText());
        record.setMealType(request.getMealType());

        // 解析日期
        if (request.getRecordDate() != null && !request.getRecordDate().isEmpty()) {
            record.setRecordDate(LocalDate.parse(request.getRecordDate()));
        } else {
            record.setRecordDate(LocalDate.now());
        }

        // 直接新增的记录默认为已吃（正常状态）
        record.setEaten(true);

        NutritionRecord saved = nutritionRecordRepository.save(record);
        log.info("创建营养记录成功: userId={}, id={}, foodName={}", userId, saved.getId(), saved.getFoodName());

        return toDTO(saved);
    }

    /**
     * 更新营养记录
     */
    @Transactional
    public NutritionRecordDTO updateRecord(Long userId, Long id, NutritionRecordRequest request) {
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: " + id));

        // 权限检查
        if (!String.valueOf(userId).equals(record.getUserId())) {
            throw new RuntimeException("无权限修改此记录");
        }

        // 更新基本字段
        record.setFoodName(request.getFoodName());
        record.setBrand(request.getBrand());
        record.setServingAmount(request.getServingAmount());
        record.setServingUnit(request.getServingUnit());

        // 更新营养数据
        record.setCalories(request.getCalories());
        record.setProtein(request.getProtein());
        record.setFat(request.getFat());
        record.setSaturatedFat(request.getSaturatedFat());
        record.setCarbohydrates(request.getCarbohydrates());
        record.setFiber(request.getFiber());
        record.setSodium(request.getSodium());
        record.setSugar(request.getSugar());
        record.setCalcium(request.getCalcium());

        // 更新其他字段
        record.setRemark(request.getRemark());
        record.setOcrText(request.getOcrText());
        record.setMealType(request.getMealType());

        // 更新日期
        if (request.getRecordDate() != null && !request.getRecordDate().isEmpty()) {
            record.setRecordDate(LocalDate.parse(request.getRecordDate()));
        }

        NutritionRecord saved = nutritionRecordRepository.save(record);
        log.info("更新营养记录成功: userId={}, id={}", userId, saved.getId());

        return toDTO(saved);
    }

    /**
     * 获取用户所有记录
     */
    public List<NutritionRecordDTO> getUserRecords(Long userId) {
        return nutritionRecordRepository.findByUserIdOrderByCreateTimeDesc(String.valueOf(userId))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户指定日期的记录
     */
    public List<NutritionRecordDTO> getUserRecordsByDate(Long userId, LocalDate date) {
        return nutritionRecordRepository.findByUserIdAndRecordDate(String.valueOf(userId), date)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户日期范围内的记录
     */
    public List<NutritionRecordDTO> getUserRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return nutritionRecordRepository.findByUserIdAndRecordDateBetween(String.valueOf(userId), startDate, endDate)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单条记录
     */
    public NutritionRecordDTO getRecordById(Long userId, Long id) {
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: " + id));

        // 权限检查
        if (!String.valueOf(userId).equals(record.getUserId())) {
            throw new RuntimeException("无权限访问此记录");
        }

        return toDTO(record);
    }

    /**
     * 删除记录
     */
    @Transactional
    public void deleteRecord(Long userId, Long id) {
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: " + id));

        // 权限检查
        if (!String.valueOf(userId).equals(record.getUserId())) {
            throw new RuntimeException("无权限删除此记录");
        }

        nutritionRecordRepository.deleteById(id);
        log.info("删除营养记录: userId={}, id={}", userId, id);
    }

    /**
     * 导入指定日期的记录到今天，标记为未吃
     * @param userId 用户ID
     * @param sourceDate 源日期 yyyy-MM-dd（通常为昨天）
     * @return 导入条数
     */
    @Transactional
    public int importRecords(Long userId, String sourceDate) {
        LocalDate source = LocalDate.parse(sourceDate);
        LocalDate today = LocalDate.now();
        List<NutritionRecord> sourceRecords = nutritionRecordRepository
                .findByUserIdAndRecordDate(String.valueOf(userId), source);

        if (sourceRecords.isEmpty()) {
            return 0;
        }

        List<NutritionRecord> newRecords = sourceRecords.stream().map(r -> {
            NutritionRecord copy = new NutritionRecord();
            copy.setUserId(r.getUserId());
            copy.setRecordDate(today);
            copy.setMealType(r.getMealType());
            copy.setFoodName(r.getFoodName());
            copy.setBrand(r.getBrand());
            copy.setServingAmount(r.getServingAmount());
            copy.setServingUnit(r.getServingUnit());
            copy.setCalories(r.getCalories());
            copy.setProtein(r.getProtein());
            copy.setFat(r.getFat());
            copy.setSaturatedFat(r.getSaturatedFat());
            copy.setCarbohydrates(r.getCarbohydrates());
            copy.setFiber(r.getFiber());
            copy.setSodium(r.getSodium());
            copy.setSugar(r.getSugar());
            copy.setCalcium(r.getCalcium());
            copy.setRemark(r.getRemark());
            copy.setOcrText(r.getOcrText());
            copy.setEaten(false); // 导入的记录默认未吃，待用户确认
            return copy;
        }).collect(Collectors.toList());

        List<NutritionRecord> saved = nutritionRecordRepository.saveAll(newRecords);
        log.info("导入记录成功: userId={}, sourceDate={}, 导入{}条", userId, sourceDate, saved.size());
        return saved.size();
    }

    /**
     * 标记记录是否已吃
     */
    @Transactional
    public NutritionRecordDTO markEaten(Long userId, Long id, Boolean eaten) {
        NutritionRecord record = nutritionRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: " + id));

        // 权限检查
        if (!String.valueOf(userId).equals(record.getUserId())) {
            throw new RuntimeException("无权限修改此记录");
        }

        record.setEaten(eaten);
        NutritionRecord saved = nutritionRecordRepository.save(record);
        log.info("标记已吃状态: userId={}, id={}, eaten={}", userId, id, eaten);
        return toDTO(saved);
    }

    /**
     * 获取用户今日统计
     */
    public DailyNutritionStatsDTO getTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        log.info("获取今日统计: userId={}, date={}", userId, today);
        Object[] stats = nutritionRecordRepository.getStatsByUserIdAndDate(String.valueOf(userId), today);
        log.info("统计结果: {}", stats != null ? java.util.Arrays.toString(stats) : "null");
        return parseStats(stats, today);
    }

    /**
     * 获取用户指定日期的统计
     */
    public DailyNutritionStatsDTO getStatsByDate(Long userId, LocalDate date) {
        Object[] stats = nutritionRecordRepository.getStatsByUserIdAndDate(String.valueOf(userId), date);
        return parseStats(stats, date);
    }

    /**
     * 转换为 DTO
     */
    private NutritionRecordDTO toDTO(NutritionRecord record) {
        return NutritionRecordDTO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .recordDate(record.getRecordDate())
                .mealType(record.getMealType())
                .foodName(record.getFoodName())
                .brand(record.getBrand())
                .servingAmount(record.getServingAmount())
                .servingUnit(record.getServingUnit())
                .calories(record.getCalories())
                .protein(record.getProtein())
                .fat(record.getFat())
                .saturatedFat(record.getSaturatedFat())
                .carbohydrates(record.getCarbohydrates())
                .fiber(record.getFiber())
                .sodium(record.getSodium())
                .sugar(record.getSugar())
                .calcium(record.getCalcium())
                .remark(record.getRemark())
                .ocrText(record.getOcrText())
                .createdAt(record.getCreateTime())
                .updatedAt(record.getUpdateTime())
                .eaten(record.getEaten())
                .build();
    }

    /**
     * 解析统计数据
     */
    private DailyNutritionStatsDTO parseStats(Object[] stats, LocalDate date) {
        log.info("parseStats called: stats={}, statsType={}, statsLength={}",
                 stats != null ? java.util.Arrays.toString(stats) : "null",
                 stats != null ? stats.getClass().getSimpleName() : "null",
                 stats != null ? stats.length : -1);

        if (stats == null || stats.length == 0) {
            log.warn("parseStats: stats is null or empty, returning empty");
            return DailyNutritionStatsDTO.builder()
                    .recordCount(0)
                    .totalCalories(BigDecimal.ZERO)
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build();
        }

        // 原生查询返回的可能是嵌套数组: [[count, calories, ...]]
        Object[] data = stats;
        if (stats.length == 1 && stats[0] instanceof Object[]) {
            data = (Object[]) stats[0];
            log.info("parseStats: unwrapped nested array, dataLength={}", data.length);
        }

        if (data.length < 10) {
            log.warn("parseStats: data length < 10, returning empty");
            return DailyNutritionStatsDTO.builder()
                    .recordCount(0)
                    .totalCalories(BigDecimal.ZERO)
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build();
        }

        return DailyNutritionStatsDTO.builder()
                .recordCount(((Number) data[0]).intValue())
                .totalCalories(toBigDecimal(data[1]))
                .totalProtein(toBigDecimal(data[2]))
                .totalFat(toBigDecimal(data[3]))
                .totalSaturatedFat(toBigDecimal(data[4]))
                .totalCarbohydrates(toBigDecimal(data[5]))
                .totalFiber(toBigDecimal(data[6]))
                .totalSodium(toBigDecimal(data[7]))
                .totalSugar(toBigDecimal(data[8]))
                .totalCalcium(toBigDecimal(data[9]))
                .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();
    }

    /**
     * 转换为 BigDecimal
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}