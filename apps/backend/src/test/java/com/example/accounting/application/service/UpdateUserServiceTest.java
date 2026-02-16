package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.UpdateUserCommand;
import com.example.accounting.application.port.out.UpdateUserResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ユーザー更新サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー更新サービス")
class UpdateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UpdateUserService updateUserService;

    @BeforeEach
    void setUp() {
        updateUserService = new UpdateUserService(userRepository);
    }

    @Nested
    @DisplayName("更新成功")
    class SuccessfulUpdate {

        @Test
        @DisplayName("有効な情報でユーザーを更新できる")
        void shouldUpdateUserWithValidCommand() {
            // Given
            User existingUser = buildUser("user-1", "olduser", "old@example.com", "OldPass123!", "旧表示名", Role.USER);
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    "NewPass123!",
                    "MANAGER"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.id()).isEqualTo("user-1");
            assertThat(result.username()).isEqualTo("olduser");
            assertThat(result.email()).isEqualTo("old@example.com");
            assertThat(result.displayName()).isEqualTo("新しい表示名");
            assertThat(result.role()).isEqualTo("MANAGER");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getId().value()).isEqualTo("user-1");
            assertThat(savedUser.getDisplayName()).isEqualTo("新しい表示名");
            assertThat(savedUser.getRole()).isEqualTo(Role.MANAGER);
            assertThat(savedUser.verifyPassword("NewPass123!")).isTrue();
        }

        @Test
        @DisplayName("password が null の場合はパスワードを変更しない")
        void shouldSkipPasswordChangeWhenPasswordIsNull() {
            // Given
            User existingUser = buildUser("user-1", "olduser", "old@example.com", "OldPass123!", "旧表示名", Role.USER);
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    null,
                    "USER"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then
            assertThat(result.success()).isTrue();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.verifyPassword("OldPass123!")).isTrue();
        }

        @Test
        @DisplayName("password が空文字の場合はパスワードを変更しない")
        void shouldSkipPasswordChangeWhenPasswordIsEmpty() {
            // Given
            User existingUser = buildUser("user-1", "olduser", "old@example.com", "OldPass123!", "旧表示名", Role.USER);
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    "",
                    "USER"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then
            assertThat(result.success()).isTrue();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.verifyPassword("OldPass123!")).isTrue();
        }
    }

    @Nested
    @DisplayName("更新失敗")
    class FailedUpdate {

        @Test
        @DisplayName("ユーザーが存在しない場合は更新に失敗する")
        void shouldFailWhenUserDoesNotExist() {
            // Given
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    "NewPass123!",
                    "USER"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.empty()));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("ユーザーが見つかりません");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("不正なロールの場合は更新に失敗する")
        void shouldFailWhenRoleIsInvalid() {
            // Given
            User existingUser = buildUser("user-1", "olduser", "old@example.com", "OldPass123!", "旧表示名", Role.USER);
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    "NewPass123!",
                    "INVALID"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("無効なロールコードです: INVALID");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("短いパスワードでも fromRawPassword はバリデーションしないため更新される")
        void shouldAcceptShortPasswordSinceValidationMovedToValidated() {
            // Given
            User existingUser = buildUser("user-1", "olduser", "old@example.com", "OldPass123!", "旧表示名", Role.USER);
            UpdateUserCommand command = new UpdateUserCommand(
                    "user-1",
                    "新しい表示名",
                    "short",
                    "USER"
            );

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            // When
            UpdateUserResult result = updateUserService.execute(command);

            // Then - fromRawPassword no longer validates; validation is via Password.validated()
            assertThat(result.success()).isTrue();
            assertThat(result.id()).isEqualTo("user-1");
        }
    }

    private User buildUser(String id,
                           String username,
                           String email,
                           String password,
                           String displayName,
                           Role role) {
        LocalDateTime now = LocalDateTime.now();
        return User.reconstruct(
                UserId.of(id),
                Username.of(username),
                Email.of(email),
                Password.fromRawPassword(password),
                displayName,
                role,
                true,
                false,
                0,
                null,
                now.minusDays(1),
                now.minusDays(1)
        );
    }
}
