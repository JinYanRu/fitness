package com.fitness.ocr.repository;

import com.fitness.ocr.entity.UserFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户食物库 Repository
 */
@Repository
public interface UserFoodRepository extends JpaRepository<UserFood, Long> {

    /**
     * 查询用户的所有食物
     */
    List<UserFood> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 分页查询用户食物
     */
    Page<UserFood> findByUserId(Long userId, Pageable pageable);

    /**
     * 搜索用户食物
     */
    @Query("SELECT f FROM UserFood f WHERE f.userId = :userId AND " +
           "(f.foodName LIKE %:keyword% OR f.brand LIKE %:keyword%)")
    List<UserFood> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    /**
     * 统计用户食物数量
     */
    long countByUserId(Long userId);
}