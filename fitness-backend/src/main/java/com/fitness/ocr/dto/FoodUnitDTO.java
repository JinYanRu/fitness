package com.fitness.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 食物计量单位 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodUnitDTO {

    private Long id;

    /**
     * 单位名称（如：包、片、个、杯）
     */
    @NotBlank(message = "单位名称不能为空")
    private String unitName;

    /**
     * 单位对应基准单位的量
     */
    @NotNull(message = "单位换算值不能为空")
    @Min(value = 0, message = "换算值必须大于0")
    private BigDecimal unitValue;

    /**
     * 是否为默认单位
     */
    private Boolean isDefault;
}