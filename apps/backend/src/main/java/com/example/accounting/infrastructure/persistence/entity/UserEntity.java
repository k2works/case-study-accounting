package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ（永続化用）
 */
public class UserEntity {

    private String id;
    private String username;
    private String email;
    private String password;
    private String displayName;
    private String role;
    private boolean active;
    private boolean locked;
    private int failedLoginAttempts;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserEntity() {
    }

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId().value();
        entity.username = user.getUsernameValue();
        entity.email = user.getEmailValue();
        entity.password = user.getPasswordValue();
        entity.displayName = user.getDisplayName();
        entity.role = user.getRole().name();
        entity.active = user.isActive();
        entity.locked = user.isLocked();
        entity.failedLoginAttempts = user.getFailedLoginAttempts();
        entity.lastLoginAt = user.getLastLoginAt();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public User toDomain() {
        return User.reconstruct(
                UserId.of(id),
                Username.reconstruct(username),
                Email.reconstruct(email),
                Password.reconstruct(password),
                displayName,
                Role.fromCode(role),
                active,
                locked,
                failedLoginAttempts,
                lastLoginAt,
                createdAt,
                updatedAt
        );
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
