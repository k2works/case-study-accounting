package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.RegisterUserUseCase;
import com.example.accounting.application.port.in.command.RegisterUserCommand;
import com.example.accounting.application.port.out.RegisterUserResult;
import com.example.accounting.infrastructure.web.dto.RegisterUserRequest;
import com.example.accounting.infrastructure.web.dto.RegisterUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ユーザー登録コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー登録コントローラ")
class RegisterUserControllerTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    private RegisterUserController registerUserController;

    @BeforeEach
    void setUp() {
        registerUserController = new RegisterUserController(registerUserUseCase);
    }

    @Nested
    @DisplayName("ユーザー登録")
    class Register {

        @Test
        @DisplayName("有効な情報でユーザー登録できる")
        void shouldRegisterUserWithValidRequest() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );
            RegisterUserResult result = RegisterUserResult.success(
                    "newuser",
                    "newuser@example.com",
                    "新規ユーザー",
                    "USER"
            );
            when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(result);

            // When
            ResponseEntity<RegisterUserResponse> response = registerUserController.register(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().username()).isEqualTo("newuser");
            assertThat(response.getBody().email()).isEqualTo("newuser@example.com");
            assertThat(response.getBody().displayName()).isEqualTo("新規ユーザー");
            assertThat(response.getBody().role()).isEqualTo("USER");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("リクエストの内容が RegisterUserCommand に正しく変換される")
        void shouldConvertRequestToCommand() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );
            when(registerUserUseCase.execute(any(RegisterUserCommand.class)))
                    .thenReturn(RegisterUserResult.failure("error"));

            // When
            registerUserController.register(request);

            // Then
            ArgumentCaptor<RegisterUserCommand> captor = ArgumentCaptor.forClass(RegisterUserCommand.class);
            verify(registerUserUseCase).execute(captor.capture());
            assertThat(captor.getValue().username()).isEqualTo("newuser");
            assertThat(captor.getValue().email()).isEqualTo("newuser@example.com");
            assertThat(captor.getValue().password()).isEqualTo("Password123!");
            assertThat(captor.getValue().displayName()).isEqualTo("新規ユーザー");
            assertThat(captor.getValue().role()).isEqualTo("USER");
        }

        @Test
        @DisplayName("登録に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenRegistrationFails() {
            // Given
            RegisterUserRequest request = new RegisterUserRequest(
                    "newuser",
                    "newuser@example.com",
                    "Password123!",
                    "新規ユーザー",
                    "USER"
            );
            RegisterUserResult result = RegisterUserResult.failure("ユーザー名は既に使用されています");
            when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(result);

            // When
            ResponseEntity<RegisterUserResponse> response = registerUserController.register(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("ユーザー名は既に使用されています");
        }
    }
}
