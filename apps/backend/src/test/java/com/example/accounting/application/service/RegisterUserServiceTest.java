package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.RegisterUserCommand;
import com.example.accounting.application.port.out.RegisterUserResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ユーザー登録サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー登録サービス")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(userRepository);
    }

    @Nested
    @DisplayName("登録成功")
    class SuccessfulRegister {

        @Test
        @DisplayName("有効な情報でユーザー登録できる")
        void shouldRegisterUserWithValidCommand() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );

            when(userRepository.existsByUsername(command.username())).thenReturn(Try.success(false));
            when(userRepository.existsByEmail(command.email())).thenReturn(Try.success(false));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            // When
            RegisterUserResult result = registerUserService.execute(command);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.username()).isEqualTo("newuser");
            assertThat(result.email()).isEqualTo("newuser@example.com");
            assertThat(result.displayName()).isEqualTo("新規ユーザー");
            assertThat(result.role()).isEqualTo("USER");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getUsernameValue()).isEqualTo("newuser");
            assertThat(savedUser.getEmailValue()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getDisplayName()).isEqualTo("新規ユーザー");
            assertThat(savedUser.getRole().name()).isEqualTo("USER");
            assertThat(savedUser.verifyPassword("Password123!")).isTrue();
        }
    }

    @Nested
    @DisplayName("登録失敗")
    class FailedRegister {

        @Test
        @DisplayName("ユーザー名が既に存在する場合は登録に失敗する")
        void shouldFailWhenUsernameAlreadyExists() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "existinguser",
                    "newemail@example.com",
                    "Password123!",
                    "既存ユーザー",
                    "USER"
            );

            when(userRepository.existsByUsername(command.username())).thenReturn(Try.success(true));

            // When
            RegisterUserResult result = registerUserService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("ユーザー名は既に使用されています");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("メールアドレスが既に存在する場合は登録に失敗する")
        void shouldFailWhenEmailAlreadyExists() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "newuser",
                    "existing@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );

            when(userRepository.existsByUsername(command.username())).thenReturn(Try.success(false));
            when(userRepository.existsByEmail(command.email())).thenReturn(Try.success(true));

            // When
            RegisterUserResult result = registerUserService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("メールアドレスは既に使用されています");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("不正なロールの場合は登録に失敗する")
        void shouldFailWhenRoleIsInvalid() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "INVALID"
            );

            when(userRepository.existsByUsername(command.username())).thenReturn(Try.success(false));
            when(userRepository.existsByEmail(command.email())).thenReturn(Try.success(false));

            // When
            RegisterUserResult result = registerUserService.execute(command);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("無効なロールコードです: INVALID");
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
