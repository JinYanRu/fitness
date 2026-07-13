package com.fitness.ocr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.ocr.dto.NutritionRecordDTO;
import com.fitness.ocr.dto.NutritionRecordRequest;
import com.fitness.ocr.service.NutritionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NutritionController 单元测试
 */
@WebMvcTest(NutritionController.class)
class NutritionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NutritionService nutritionService;

    @Test
    @DisplayName("创建营养记录 - 成功")
    void testCreateRecord() throws Exception {
        // Arrange
        NutritionRecordRequest request = NutritionRecordRequest.builder()
                .foodName("苹果")
                .calories(new BigDecimal("52"))
                .build();

        NutritionRecordDTO response = NutritionRecordDTO.builder()
                .id(1L)
                .foodName("苹果")
                .calories(new BigDecimal("52"))
                .createdAt(LocalDateTime.now())
                .build();

        when(nutritionService.createRecord(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/nutrition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.foodName").value("苹果"));
    }

    @Test
    @DisplayName("创建营养记录 - 食物名称为空")
    void testCreateRecordWithoutFoodName() throws Exception {
        // Arrange
        NutritionRecordRequest request = NutritionRecordRequest.builder()
                .calories(new BigDecimal("52"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/nutrition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取记录列表")
    void testGetList() throws Exception {
        // Arrange
        NutritionRecordDTO record = NutritionRecordDTO.builder()
                .id(1L)
                .foodName("苹果")
                .calories(new BigDecimal("52"))
                .build();

        when(nutritionService.getAllRecords()).thenReturn(List.of(record));

        // Act & Assert
        mockMvc.perform(get("/api/nutrition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].foodName").value("苹果"));
    }

    @Test
    @DisplayName("获取单条记录")
    void testGetById() throws Exception {
        // Arrange
        NutritionRecordDTO record = NutritionRecordDTO.builder()
                .id(1L)
                .foodName("苹果")
                .build();

        when(nutritionService.getRecordById(1L)).thenReturn(record);

        // Act & Assert
        mockMvc.perform(get("/api/nutrition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("删除记录")
    void testDelete() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/nutrition/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取今日统计")
    void testGetTodayStats() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/nutrition/stats/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
