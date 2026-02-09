package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.UserEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * ユーザー MyBatis Mapper
 */
@Mapper
public interface UserMapper {

    @Insert("""
            INSERT INTO users (
                id, username, email, password, display_name, role,
                active, locked, failed_login_attempts, last_login_at,
                created_at, updated_at
            ) VALUES (
                #{id}, #{username}, #{email}, #{password}, #{displayName}, #{role},
                #{active}, #{locked}, #{failedLoginAttempts}, #{lastLoginAt},
                #{createdAt}, #{updatedAt}
            )
            """)
    void insert(UserEntity entity);

    @Update("""
            UPDATE users SET
                email = #{email},
                password = #{password},
                display_name = #{displayName},
                role = #{role},
                active = #{active},
                locked = #{locked},
                failed_login_attempts = #{failedLoginAttempts},
                last_login_at = #{lastLoginAt},
                updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(UserEntity entity);

    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "displayName", column = "display_name"),
        @Result(property = "role", column = "role"),
        @Result(property = "active", column = "active"),
        @Result(property = "locked", column = "locked"),
        @Result(property = "failedLoginAttempts", column = "failed_login_attempts"),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<UserEntity> findById(String id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    @ResultMap("userResultMap")
    Optional<UserEntity> findByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    @ResultMap("userResultMap")
    Optional<UserEntity> findByEmail(String email);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    @ResultMap("userResultMap")
    List<UserEntity> findAll();

    @Select("""
            <script>
            SELECT * FROM users
            WHERE 1=1
            <if test="role != null">
                AND role = #{role}
            </if>
            <if test="keyword != null">
                AND (username LIKE CONCAT('%', #{keyword}, '%') OR display_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY created_at DESC
            </script>
            """)
    @ResultMap("userResultMap")
    List<UserEntity> search(@Param("role") String role, @Param("keyword") String keyword);

    @Delete("DELETE FROM users WHERE id = #{id}")
    void deleteById(String id);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);
}
