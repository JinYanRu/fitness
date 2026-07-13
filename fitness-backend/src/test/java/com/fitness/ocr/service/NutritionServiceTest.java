package com.fitness.ocr.service;

import com.fitness.ocr.dto.NutritionRecordDTO;
import com.fitness.ocr.dto.NutritionRecordRequest;
import com.fitness.ocr.entity.NutritionRecord;
import com.fitness.ocr.repository.NutritionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NutritionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private NutritionRecordRepository nutritionRecordRepository;

    @InjectMocks
    private NutritionService nutritionService;

    private NutritionRecord testRecord;

    @BeforeEach
    void setUp() {
        testRecord = new NutritionRecord();
        testRecord.setId(1L);
        testRecord.setFoodName("测试食物");
        testRecord.setRecordDate(LocalDate.now());
        testRecord.setCalories(new BigDecimal("100"));
        testRecord.setProtein(new BigDecimal("10"));
        testRecord.setFat(new BigDecimal("5"));
        testRecord.setCarbohydrates(new BigDecimal("15"));
    }

    @Test
    @DisplayName("创建营养记录 - 成功")
    void testCreateRecord() {
        // Arrange
        NutritionRecordRequest request = NutritionRecordRequest.builder()
                .foodName("苹果")
                .calories(new BigDecimal("52"))
                .protein(new BigDecimal("0.3"))
                .recordDate("2025-07-13")
                .build();

        when(nutritionRecordRepository.save(any(NutritionRecord.class)))
                .thenAnswer(invocation -> {
                    NutritionRecord record = invocation.getArgument(0);
                    record.setId(1L);
                    return record;
                });

        // Act
        NutritionRecordDTO result = nutritionService.createRecord(request);

        // Assert
        assertNotNull(result);
        assertEquals("苹果", result.getFoodName());
        assertEquals(new BigDecimal("52"), result.getCalories());
        verify(nutritionRecordRepository, times(1)).save(any(NutritionRecord.class));
    }

    @Test
    @DisplayName("获取单条记录 - 成功")
    void testGetRecordById() {
        // Arrange
        when(nutritionRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

        // Act
        NutritionRecordDTO result = nutritionService.getRecordById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试食物", result.getFoodName());
    }

    @Test
    @DisplayName("获取单条记录 - 记录不存在")
    void testGetRecordByIdNotFound() {
        // Arrange
        when(nutritionRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> nutritionService.getRecordById(999L));
    }

    @Test
    @DisplayName("删除记录 - 成功")
    void testDeleteRecord() {
        // Act
        nutritionService.deleteRecord(1L);

        // Assert
        verify(nutritionRecordRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("获取指定日期记录")
    void testGetRecordsByDate() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(nutritionRecordRepository.findByRecordDate(date)).thenReturn(List.of(testRecord));

        // Act
        List<NutritionRecordDTO> result = nutritionService.getRecordsByDate(date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试食物", result.get(0).getFoodName());
    }
}
