package com.fitness.ocr.repository;

import com.fitness.ocr.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户档案 Repository
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 根据用户ID查询档案
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 根据用户ID删除档案
     */
    void deleteByUserId(Long userId);
}