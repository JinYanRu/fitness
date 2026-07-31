package com.fitness.ocr.repository;

import com.fitness.ocr.entity.NutritionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 营养记录 Repository
 */
@Repository
public interface NutritionRecordRepository extends JpaRepository<NutritionRecord, Long> {

    /**
     * 查询指定日期的记录
     */
    List<NutritionRecord> findByRecordDate(LocalDate recordDate);

    /**
     * 按用户查询所有记录（按时间倒序）
     */
    List<NutritionRecord> findByUserIdOrderByCreateTimeDesc(String userId);

    /**
     * 查询指定用户指定日期的记录
     */
    @Query(value = "SELECT * FROM nutrition_record WHERE user_id = :userId AND DATE(record_date) = :date", nativeQuery = true)
    List<NutritionRecord> findByUserIdAndRecordDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 查询指定日期范围内的记录
     */
    List<NutritionRecord> findByRecordDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 查询指定用户指定日期范围内的记录
     */
    List<NutritionRecord> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取指定日期的营养统计
     */
    @Query("SELECT COUNT(n), " +
           "COALESCE(SUM(n.calories), 0), " +
           "COALESCE(SUM(n.protein), 0), " +
           "COALESCE(SUM(n.fat), 0), " +
           "COALESCE(SUM(n.saturatedFat), 0), " +
           "COALESCE(SUM(n.carbohydrates), 0), " +
           "COALESCE(SUM(n.fiber), 0), " +
           "COALESCE(SUM(n.sodium), 0), " +
           "COALESCE(SUM(n.sugar), 0), " +
           "COALESCE(SUM(n.calcium), 0) " +
           "FROM NutritionRecord n WHERE n.recordDate = :date AND n.eaten = true")
    Object[] getStatsByDate(@Param("date") LocalDate date);

    /**
     * 获取指定用户指定日期的营养统计
     */
    @Query(value = "SELECT COUNT(*), " +
           "COALESCE(SUM(calories), 0), " +
           "COALESCE(SUM(protein), 0), " +
           "COALESCE(SUM(fat), 0), " +
           "COALESCE(SUM(saturated_fat), 0), " +
           "COALESCE(SUM(carbohydrates), 0), " +
           "COALESCE(SUM(fiber), 0), " +
           "COALESCE(SUM(sodium), 0), " +
           "COALESCE(SUM(sugar), 0), " +
           "COALESCE(SUM(calcium), 0) " +
           "FROM nutrition_record WHERE user_id = :userId AND DATE(record_date) = DATE(:date) AND eaten = 1", nativeQuery = true)
    Object[] getStatsByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 按日期分组查询统计
     */
    @Query("SELECT n.recordDate, COUNT(n), " +
           "COALESCE(SUM(n.calories), 0), " +
           "COALESCE(SUM(n.protein), 0), " +
           "COALESCE(SUM(n.fat), 0) " +
           "FROM NutritionRecord n " +
           "WHERE n.recordDate BETWEEN :startDate AND :endDate AND n.eaten = true " +
           "GROUP BY n.recordDate ORDER BY n.recordDate DESC")
    List<Object[]> getDailyStatsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}