package com.fitness.ocr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入历史记录请求
 * 把指定日期的记录复制到今天，标记为未吃
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRecordRequest {

    /**
     * 源日期 yyyy-MM-dd（通常为昨天）
     */
    @NotBlank(message = "源日期不能为空")
    private String sourceDate;
}
