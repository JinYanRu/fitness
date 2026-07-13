package com.fitness.ocr.repository;

import com.fitness.ocr.entity.CommonFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公共食物库 Repository
 */
@Repository
public interface CommonFoodRepository extends JpaRepository<CommonFood, Long> {

    /**
     * 查询所有启用的食物
     */
    List<CommonFood> findByIsActiveOrderByFoodName(Integer isActive);

    /**
     * 分页查询启用的食物
     */
    Page<CommonFood> findByIsActive(Integer isActive, Pageable pageable);

    /**
     * 按分类查询
     */
    List<CommonFood> findByCategoryAndIsActiveOrderByFoodName(String category, Integer isActive);

    /**
     * 搜索食物
     */
    @Query("SELECT f FROM CommonFood f WHERE f.isActive = 1 AND " +
           "(f.foodName LIKE %:keyword% OR f.brand LIKE %:keyword%)")
    List<CommonFood> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 按分类搜索
     */
    @Query("SELECT f FROM CommonFood f WHERE f.isActive = 1 AND f.category = :category AND " +
           "(f.foodName LIKE %:keyword% OR f.brand LIKE %:keyword%)")
    List<CommonFood> searchByCategoryAndKeyword(@Param("category") String category, @Param("keyword") String keyword);

    /**
     * 获取所有分类
     */
    @Query("SELECT DISTINCT f.category FROM CommonFood f WHERE f.isActive = 1 AND f.category IS NOT NULL")
    List<String> findAllCategories();

    /**
     * 统计数量
     */
    long countByIsActive(Integer isActive);
}