package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetUsersUseCase;
import com.example.accounting.application.port.in.query.GetUsersQuery;
import com.example.accounting.application.port.in.query.UserSummary;
import com.example.accounting.infrastructure.web.dto.UserResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ユーザー一覧取得コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー一覧取得コントローラ")
class UserControllerTest {

    @Mock
    private GetUsersUseCase getUsersUseCase;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(getUsersUseCase);
    }

    @Nested
    @DisplayName("ユーザー一覧取得")
    class GetUsers {

        @Test
        @DisplayName("ユーザー一覧を取得できる")
        void shouldReturnUsers() {
            // Given
            UserSummary summary = new UserSummary(
                    "user-1",
                    "user1",
                    "user1@example.com",
                    "User One",
                    "USER",
                    LocalDateTime.of(2024, 1, 1, 10, 0)
            );
            when(getUsersUseCase.execute(new GetUsersQuery(null, null)))
                    .thenReturn(List.of(summary));

            // When
            ResponseEntity<List<UserResponse>> response = userController.getUsers(null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            UserResponse body = response.getBody().get(0);
            assertThat(body.id()).isEqualTo("user-1");
            assertThat(body.username()).isEqualTo("user1");
            assertThat(body.email()).isEqualTo("user1@example.com");
            assertThat(body.displayName()).isEqualTo("User One");
            assertThat(body.role()).isEqualTo("USER");
            assertThat(body.lastLoginAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        }

        @Test
        @DisplayName("リクエストパラメータが GetUsersQuery に正しく変換される")
        void shouldConvertParamsToQuery() {
            // Given
            when(getUsersUseCase.execute(new GetUsersQuery("MANAGER", "man")))
                    .thenReturn(List.of());

            // When
            userController.getUsers("MANAGER", "man");

            // Then
            ArgumentCaptor<GetUsersQuery> captor = ArgumentCaptor.forClass(GetUsersQuery.class);
            verify(getUsersUseCase).execute(captor.capture());
            assertThat(captor.getValue().role()).isEqualTo("MANAGER");
            assertThat(captor.getValue().keyword()).isEqualTo("man");
        }
    }
}
