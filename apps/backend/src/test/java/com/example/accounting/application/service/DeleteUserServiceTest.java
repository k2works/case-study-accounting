package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.DeleteUserCommand;
import com.example.accounting.application.port.out.DeleteUserResult;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー削除サービス")
class DeleteUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private DeleteUserService deleteUserService;

    @BeforeEach
    void setUp() {
        deleteUserService = new DeleteUserService(userRepository);
    }

    @Nested
    @DisplayName("削除成功")
    class SuccessfulDelete {

        @Test
        @DisplayName("有効なユーザーを無効化できる")
        void shouldDeactivateUser() {
            User existingUser = buildUser("user-1", "user", "user@example.com", "Pass123!", "表示名", Role.USER);
            DeleteUserCommand command = new DeleteUserCommand("user-1");

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.of(existingUser)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            DeleteUserResult result = deleteUserService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("削除失敗")
    class FailedDelete {

        @Test
        @DisplayName("ユーザーが存在しない場合は削除に失敗する")
        void shouldFailWhenUserDoesNotExist() {
            DeleteUserCommand command = new DeleteUserCommand("user-1");

            when(userRepository.findById(UserId.of("user-1"))).thenReturn(Try.success(Optional.empty()));

            DeleteUserResult result = deleteUserService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("ユーザーが見つかりません");
            verify(userRepository, never()).save(any(User.class));
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
