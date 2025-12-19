package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.repository.UserRepository;
import com.example.accounting.infrastructure.persistence.entity.UserEntity;
import com.example.accounting.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーリポジトリ実装
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);

        if (userMapper.findById(entity.getId()).isPresent()) {
            userMapper.update(entity);
        } else {
            userMapper.insert(entity);
        }

        return userMapper.findById(entity.getId())
                .map(UserEntity::toDomain)
                .orElseThrow(() -> new IllegalStateException("保存後のユーザー取得に失敗しました"));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userMapper.findById(id.value())
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userMapper.findByUsername(username)
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userMapper.findByEmail(email)
                .map(UserEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll().stream()
                .map(UserEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UserId id) {
        userMapper.deleteById(id.value());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
}
