package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.infrastructure.persistence.entity.UserEntity;
import com.example.accounting.infrastructure.persistence.mapper.UserMapper;
import io.vavr.control.Try;
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
    public Try<User> save(User user) {
        return Try.of(() -> {
            UserEntity entity = UserEntity.fromDomain(user);

            if (userMapper.findById(entity.getId()).isPresent()) {
                userMapper.update(entity);
            } else {
                userMapper.insert(entity);
            }

            return userMapper.findById(entity.getId())
                    .map(UserEntity::toDomain)
                    .orElseThrow(() -> new IllegalStateException("保存後のユーザー取得に失敗しました"));
        });
    }

    @Override
    public Try<Optional<User>> findById(UserId id) {
        return Try.of(() -> userMapper.findById(id.value())
                .map(UserEntity::toDomain));
    }

    @Override
    public Try<Optional<User>> findByUsername(String username) {
        return Try.of(() -> userMapper.findByUsername(username)
                .map(UserEntity::toDomain));
    }

    @Override
    public Try<Optional<User>> findByEmail(String email) {
        return Try.of(() -> userMapper.findByEmail(email)
                .map(UserEntity::toDomain));
    }

    @Override
    public Try<List<User>> findAll() {
        return Try.of(() -> userMapper.findAll().stream()
                .map(UserEntity::toDomain)
                .toList());
    }

    @Override
    public Try<List<User>> search(String role, String keyword) {
        return Try.of(() -> userMapper.search(role, keyword).stream()
                .map(UserEntity::toDomain)
                .toList());
    }

    @Override
    public Try<Void> deleteById(UserId id) {
        return Try.run(() -> userMapper.deleteById(id.value()));
    }

    @Override
    public Try<Boolean> existsByUsername(String username) {
        return Try.of(() -> userMapper.existsByUsername(username));
    }

    @Override
    public Try<Boolean> existsByEmail(String email) {
        return Try.of(() -> userMapper.existsByEmail(email));
    }
}
