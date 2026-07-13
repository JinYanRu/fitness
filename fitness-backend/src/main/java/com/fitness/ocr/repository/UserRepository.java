package com.fitness.ocr.repository;

import com.fitness.ocr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据邮箱查询
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询
     */
    Optional<User> findByPhone(String phone);
}