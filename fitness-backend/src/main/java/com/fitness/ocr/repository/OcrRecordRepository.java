package com.fitness.ocr.repository;

import com.fitness.ocr.entity.OcrRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OCR 记录 Repository
 */
@Repository
public interface OcrRecordRepository extends JpaRepository<OcrRecord, Long> {

    /**
     * 按用户ID查询记录
     */
    List<OcrRecord> findByUserIdOrderByCreateTimeDesc(String userId);

    /**
     * 按状态查询记录
     */
    List<OcrRecord> findByStatusOrderByCreateTimeDesc(Integer status);
}