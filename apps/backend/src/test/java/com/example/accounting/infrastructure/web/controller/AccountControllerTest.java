package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountUseCase;
import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.infrastructure.web.dto.CreateAccountRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
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
 * 勘定科目登録コントローラのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目登録コントローラ")
class AccountControllerTest {

    @Mock
    private CreateAccountUseCase createAccountUseCase;

    private AccountController accountController;

    @BeforeEach
    void setUp() {
        accountController = new AccountController(createAccountUseCase);
    }

    @Nested
    @DisplayName("勘定科目登録")
    class CreateAccount {

        @Test
        @DisplayName("有効な情報で勘定科目を登録できる")
        void shouldCreateAccountWithValidRequest() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "1100",
                    "現金",
                    "ASSET"
            );
            CreateAccountResult result = CreateAccountResult.success(
                    1,
                    "1100",
                    "現金",
                    "ASSET"
            );
            when(createAccountUseCase.execute(any(CreateAccountCommand.class))).thenReturn(result);

            // When
            ResponseEntity<CreateAccountResponse> response = accountController.create(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountId()).isEqualTo(1);
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
            assertThat(response.getBody().accountName()).isEqualTo("現金");
            assertThat(response.getBody().accountType()).isEqualTo("ASSET");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("リクエストの内容が CreateAccountCommand に正しく変換される")
        void shouldConvertRequestToCommand() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "2100",
                    "買掛金",
                    "LIABILITY"
            );
            when(createAccountUseCase.execute(any(CreateAccountCommand.class)))
                    .thenReturn(CreateAccountResult.failure("error"));

            // When
            accountController.create(request);

            // Then
            ArgumentCaptor<CreateAccountCommand> captor = ArgumentCaptor.forClass(CreateAccountCommand.class);
            verify(createAccountUseCase).execute(captor.capture());
            assertThat(captor.getValue().accountCode()).isEqualTo("2100");
            assertThat(captor.getValue().accountName()).isEqualTo("買掛金");
            assertThat(captor.getValue().accountType()).isEqualTo("LIABILITY");
        }

        @Test
        @DisplayName("登録に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenCreationFails() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "1100",
                    "現金",
                    "ASSET"
            );
            CreateAccountResult result = CreateAccountResult.failure("勘定科目コードは既に使用されています");
            when(createAccountUseCase.execute(any(CreateAccountCommand.class))).thenReturn(result);

            // When
            ResponseEntity<CreateAccountResponse> response = accountController.create(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("勘定科目コードは既に使用されています");
        }
    }
}
