package com.example.accounting.domain.repository;

import com.example.accounting.domain.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper
public interface UserRepository {
    
    Optional<User> findByEmail(@Param("email") String email);
    
    void updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") OffsetDateTime lastLoginAt);
    
    void updateFailedLoginAttempts(@Param("id") Long id, @Param("attempts") int attempts);
    
    void lockAccount(@Param("id") Long id);
    
    void save(User user);
}
