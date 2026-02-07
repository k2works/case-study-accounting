package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.DeleteUserUseCase;
import com.example.accounting.application.port.in.command.DeleteUserCommand;
import com.example.accounting.application.port.out.DeleteUserResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ユーザー削除コントローラ")
class DeleteUserControllerTest {

    @Mock
    private DeleteUserUseCase deleteUserUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DeleteUserController deleteUserController = new DeleteUserController(deleteUserUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(deleteUserController).build();
    }

    @Nested
    @DisplayName("ユーザー削除")
    class Delete {

        @Test
        @DisplayName("ユーザーを削除できる")
        void shouldDeleteUser() throws Exception {
            DeleteUserResult result = DeleteUserResult.ofSuccess();
            when(deleteUserUseCase.execute(any(DeleteUserCommand.class))).thenReturn(result);

            mockMvc.perform(delete("/api/users/{id}", "user-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.errorMessage").value(nullValue()));
        }

        @Test
        @DisplayName("リクエストの内容が DeleteUserCommand に正しく変換される")
        void shouldConvertRequestToCommand() throws Exception {
            when(deleteUserUseCase.execute(any(DeleteUserCommand.class)))
                    .thenReturn(DeleteUserResult.failure("error"));

            mockMvc.perform(delete("/api/users/{id}", "user-1"));

            ArgumentCaptor<DeleteUserCommand> captor = ArgumentCaptor.forClass(DeleteUserCommand.class);
            verify(deleteUserUseCase).execute(captor.capture());
            DeleteUserCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo("user-1");
        }

        @Test
        @DisplayName("削除に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenDeleteFails() throws Exception {
            DeleteUserResult result = DeleteUserResult.failure("ユーザーが見つかりません");
            when(deleteUserUseCase.execute(any(DeleteUserCommand.class))).thenReturn(result);

            mockMvc.perform(delete("/api/users/{id}", "user-1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errorMessage").value("ユーザーが見つかりません"));
        }
    }
}
