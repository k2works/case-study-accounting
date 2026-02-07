package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.UpdateUserUseCase;
import com.example.accounting.application.port.in.command.UpdateUserCommand;
import com.example.accounting.application.port.out.UpdateUserResult;
import com.example.accounting.infrastructure.web.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ユーザー更新コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー更新コントローラ")
class UpdateUserControllerTest {

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UpdateUserController updateUserController = new UpdateUserController(updateUserUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(updateUserController).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("ユーザー更新")
    class Update {

        @Test
        @DisplayName("有効な情報でユーザーを更新できる")
        void shouldUpdateUserWithValidRequest() throws Exception {
            // Given
            UpdateUserRequest request = new UpdateUserRequest(
                    "新しい表示名",
                    "NewPass123!",
                    "MANAGER"
            );
            UpdateUserResult result = UpdateUserResult.success(
                    "user-1",
                    "olduser",
                    "old@example.com",
                    "新しい表示名",
                    "MANAGER"
            );
            when(updateUserUseCase.execute(any(UpdateUserCommand.class))).thenReturn(result);

            // When
            mockMvc.perform(put("/api/users/{id}", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    // Then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.id").value("user-1"))
                    .andExpect(jsonPath("$.username").value("olduser"))
                    .andExpect(jsonPath("$.email").value("old@example.com"))
                    .andExpect(jsonPath("$.displayName").value("新しい表示名"))
                    .andExpect(jsonPath("$.role").value("MANAGER"))
                    .andExpect(jsonPath("$.errorMessage").value(nullValue()));
        }

        @Test
        @DisplayName("リクエストの内容が UpdateUserCommand に正しく変換される")
        void shouldConvertRequestToCommand() throws Exception {
            // Given
            UpdateUserRequest request = new UpdateUserRequest(
                    "新しい表示名",
                    "NewPass123!",
                    "MANAGER"
            );
            when(updateUserUseCase.execute(any(UpdateUserCommand.class)))
                    .thenReturn(UpdateUserResult.failure("error"));

            // When
            mockMvc.perform(put("/api/users/{id}", "user-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then
            ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
            verify(updateUserUseCase).execute(captor.capture());
            UpdateUserCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo("user-1");
            assertThat(command.displayName()).isEqualTo("新しい表示名");
            assertThat(command.password()).isEqualTo("NewPass123!");
            assertThat(command.role()).isEqualTo("MANAGER");
        }

        @Test
        @DisplayName("更新に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenUpdateFails() throws Exception {
            // Given
            UpdateUserRequest request = new UpdateUserRequest(
                    "新しい表示名",
                    "NewPass123!",
                    "MANAGER"
            );
            UpdateUserResult result = UpdateUserResult.failure("ユーザーが見つかりません");
            when(updateUserUseCase.execute(any(UpdateUserCommand.class))).thenReturn(result);

            // When
            mockMvc.perform(put("/api/users/{id}", "user-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorMessage").value("ユーザーが見つかりません"));
        }
    }
}
