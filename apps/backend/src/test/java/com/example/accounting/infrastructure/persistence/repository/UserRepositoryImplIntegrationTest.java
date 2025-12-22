package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ユーザーリポジトリ統合テスト
 *
 * <p>Testcontainers を使用して実際の PostgreSQL データベースと連携し、
 * リポジトリの CRUD 操作をテストする。</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("ユーザーリポジトリ統合テスト")
class UserRepositoryImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("save メソッド")
    class SaveMethod {

        @Test
        @DisplayName("新規ユーザーを保存できる")
        void shouldSaveNewUser() {
            // Given
            User user = User.create(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    Role.USER
            );

            // When
            User savedUser = userRepository.save(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isEqualTo(user.getId());
            assertThat(savedUser.getUsername()).isEqualTo("newuser");
            assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getDisplayName()).isEqualTo("新規ユーザー");
            assertThat(savedUser.getRole()).isEqualTo(Role.USER);
            assertThat(savedUser.isActive()).isTrue();
            assertThat(savedUser.isLocked()).isFalse();
        }

        @Test
        @DisplayName("既存ユーザーを更新できる")
        void shouldUpdateExistingUser() {
            // Given
            User user = User.create(
                    "updateuser",
                    "updateuser@example.com",
                    "Password123!",
                    "更新前ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When（イミュータブルなので結果を受け取る）
            User changedUser = user.changeRole(Role.MANAGER);
            User updatedUser = userRepository.save(changedUser);

            // Then
            assertThat(updatedUser.getRole()).isEqualTo(Role.MANAGER);
        }

        @Test
        @DisplayName("ロック状態を保存できる")
        void shouldSaveLockedStatus() {
            // Given
            User user = User.create(
                    "lockuser",
                    "lockuser@example.com",
                    "Password123!",
                    "ロックユーザー",
                    Role.USER
            );
            // イミュータブルなので結果を受け取る
            User lockedUser = user
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt()
                    .recordFailedLoginAttempt();

            // When
            User savedUser = userRepository.save(lockedUser);

            // Then
            assertThat(savedUser.isLocked()).isTrue();
            assertThat(savedUser.getFailedLoginAttempts()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findById メソッド")
    class FindByIdMethod {

        @Test
        @DisplayName("既存ユーザーを ID で検索できる")
        void shouldFindExistingUserById() {
            // Given
            User user = User.create(
                    "findbyiduser",
                    "findbyiduser@example.com",
                    "Password123!",
                    "ID検索ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            Optional<User> found = userRepository.findById(user.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("findbyiduser");
        }

        @Test
        @DisplayName("存在しない ID では empty を返す")
        void shouldReturnEmptyForNonExistentId() {
            // Given
            UserId nonExistentId = UserId.generate();

            // When
            Optional<User> found = userRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsername メソッド")
    class FindByUsernameMethod {

        @Test
        @DisplayName("既存ユーザーをユーザー名で検索できる")
        void shouldFindExistingUserByUsername() {
            // Given
            User user = User.create(
                    "findbyusernameuser",
                    "findbyusernameuser@example.com",
                    "Password123!",
                    "ユーザー名検索ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            Optional<User> found = userRepository.findByUsername("findbyusernameuser");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("findbyusernameuser@example.com");
        }

        @Test
        @DisplayName("存在しないユーザー名では empty を返す")
        void shouldReturnEmptyForNonExistentUsername() {
            // When
            Optional<User> found = userRepository.findByUsername("nonexistent");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail メソッド")
    class FindByEmailMethod {

        @Test
        @DisplayName("既存ユーザーをメールアドレスで検索できる")
        void shouldFindExistingUserByEmail() {
            // Given
            User user = User.create(
                    "findbyemailuser",
                    "findbyemailuser@example.com",
                    "Password123!",
                    "メール検索ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            Optional<User> found = userRepository.findByEmail("findbyemailuser@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("findbyemailuser");
        }

        @Test
        @DisplayName("存在しないメールアドレスでは empty を返す")
        void shouldReturnEmptyForNonExistentEmail() {
            // When
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll メソッド")
    class FindAllMethod {

        @Test
        @DisplayName("全ユーザーを取得できる")
        void shouldFindAllUsers() {
            // Given - 初期データに admin, manager, user, viewer, locked が存在
            // When
            List<User> users = userRepository.findAll();

            // Then
            assertThat(users).isNotEmpty();
            assertThat(users.size()).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("deleteById メソッド")
    class DeleteByIdMethod {

        @Test
        @DisplayName("既存ユーザーを削除できる")
        void shouldDeleteExistingUser() {
            // Given
            User user = User.create(
                    "deleteuser",
                    "deleteuser@example.com",
                    "Password123!",
                    "削除ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            userRepository.deleteById(user.getId());

            // Then
            Optional<User> found = userRepository.findById(user.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUsername メソッド")
    class ExistsByUsernameMethod {

        @Test
        @DisplayName("存在するユーザー名で true を返す")
        void shouldReturnTrueForExistingUsername() {
            // Given
            User user = User.create(
                    "existsusernameuser",
                    "existsusernameuser@example.com",
                    "Password123!",
                    "存在確認ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            boolean exists = userRepository.existsByUsername("existsusernameuser");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("存在しないユーザー名で false を返す")
        void shouldReturnFalseForNonExistentUsername() {
            // When
            boolean exists = userRepository.existsByUsername("nonexistentusername");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByEmail メソッド")
    class ExistsByEmailMethod {

        @Test
        @DisplayName("存在するメールアドレスで true を返す")
        void shouldReturnTrueForExistingEmail() {
            // Given
            User user = User.create(
                    "existsemailuser",
                    "existsemailuser@example.com",
                    "Password123!",
                    "メール存在確認ユーザー",
                    Role.USER
            );
            userRepository.save(user);

            // When
            boolean exists = userRepository.existsByEmail("existsemailuser@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("存在しないメールアドレスで false を返す")
        void shouldReturnFalseForNonExistentEmail() {
            // When
            boolean exists = userRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("ドメインモデル変換")
    class DomainModelConversion {

        @Test
        @DisplayName("全てのフィールドが正しく保存・復元される")
        void shouldPreserveAllFields() {
            // Given
            User user = User.create(
                    "fullfieldsuser",
                    "fullfieldsuser@example.com",
                    "Password123!",
                    "全フィールドユーザー",
                    Role.ADMIN
            );
            // イミュータブルなので結果を受け取る
            User loggedInUser = user.recordSuccessfulLogin();

            // When
            userRepository.save(loggedInUser);
            Optional<User> found = userRepository.findById(loggedInUser.getId());

            // Then
            assertThat(found).isPresent();
            User restored = found.get();
            assertThat(restored.getId()).isEqualTo(loggedInUser.getId());
            assertThat(restored.getUsername()).isEqualTo(loggedInUser.getUsername());
            assertThat(restored.getEmail()).isEqualTo(loggedInUser.getEmail());
            assertThat(restored.getDisplayName()).isEqualTo(loggedInUser.getDisplayName());
            assertThat(restored.getRole()).isEqualTo(loggedInUser.getRole());
            assertThat(restored.isActive()).isEqualTo(loggedInUser.isActive());
            assertThat(restored.isLocked()).isEqualTo(loggedInUser.isLocked());
            assertThat(restored.getFailedLoginAttempts()).isEqualTo(loggedInUser.getFailedLoginAttempts());
            assertThat(restored.getLastLoginAt()).isNotNull();
            assertThat(restored.getCreatedAt()).isNotNull();
            assertThat(restored.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("パスワードが正しく保存される（ハッシュ化済み）")
        void shouldPreserveHashedPassword() {
            // Given
            String rawPassword = "Password123!";
            User user = User.create(
                    "passworduser",
                    "passworduser@example.com",
                    rawPassword,
                    "パスワードユーザー",
                    Role.USER
            );

            // When
            userRepository.save(user);
            Optional<User> found = userRepository.findById(user.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().verifyPassword(rawPassword)).isTrue();
            assertThat(found.get().verifyPassword("wrongpassword")).isFalse();
        }
    }
}
