package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountUseCase;
import com.example.accounting.application.port.in.DeleteAccountCommand;
import com.example.accounting.application.port.in.DeleteAccountResult;
import com.example.accounting.application.port.in.DeleteAccountUseCase;
import com.example.accounting.application.port.in.UpdateAccountUseCase;
import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.in.command.UpdateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.application.port.out.UpdateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.web.dto.AccountResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAccountResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAccountRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAccountResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private UpdateAccountUseCase updateAccountUseCase;

    @Mock
    private DeleteAccountUseCase deleteAccountUseCase;

    @Mock
    private AccountRepository accountRepository;

    private AccountController accountController;

    @BeforeEach
    void setUp() {
        accountController = new AccountController(
                createAccountUseCase,
                updateAccountUseCase,
                deleteAccountUseCase,
                accountRepository
        );
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

    @Nested
    @DisplayName("勘定科目更新")
    class UpdateAccount {

        @Test
        @DisplayName("有効な情報で勘定科目を更新できる")
        void shouldUpdateAccountWithValidRequest() {
            UpdateAccountRequest request = new UpdateAccountRequest(
                    "普通預金",
                    "ASSET"
            );
            UpdateAccountResult result = UpdateAccountResult.success(
                    1,
                    "1100",
                    "普通預金",
                    "ASSET",
                    "勘定科目を更新しました"
            );
            when(updateAccountUseCase.execute(any(UpdateAccountCommand.class))).thenReturn(result);

            ResponseEntity<UpdateAccountResponse> response = accountController.update(1, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountId()).isEqualTo(1);
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
            assertThat(response.getBody().accountName()).isEqualTo("普通預金");
            assertThat(response.getBody().accountType()).isEqualTo("ASSET");
            assertThat(response.getBody().message()).isEqualTo("勘定科目を更新しました");
            assertThat(response.getBody().errorMessage()).isNull();
        }

        @Test
        @DisplayName("リクエストの内容が UpdateAccountCommand に正しく変換される")
        void shouldConvertRequestToCommand() {
            UpdateAccountRequest request = new UpdateAccountRequest(
                    "買掛金",
                    "LIABILITY"
            );
            when(updateAccountUseCase.execute(any(UpdateAccountCommand.class)))
                    .thenReturn(UpdateAccountResult.failure("error"));

            accountController.update(2, request);

            ArgumentCaptor<UpdateAccountCommand> captor = ArgumentCaptor.forClass(UpdateAccountCommand.class);
            verify(updateAccountUseCase).execute(captor.capture());
            assertThat(captor.getValue().accountId()).isEqualTo(2);
            assertThat(captor.getValue().accountName()).isEqualTo("買掛金");
            assertThat(captor.getValue().accountType()).isEqualTo("LIABILITY");
        }

        @Test
        @DisplayName("更新に失敗した場合は 400 を返す")
        void shouldReturnBadRequestWhenUpdateFails() {
            UpdateAccountRequest request = new UpdateAccountRequest(
                    "買掛金",
                    "LIABILITY"
            );
            UpdateAccountResult result = UpdateAccountResult.failure("勘定科目が見つかりません");
            when(updateAccountUseCase.execute(any(UpdateAccountCommand.class))).thenReturn(result);

            ResponseEntity<UpdateAccountResponse> response = accountController.update(1, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
            assertThat(response.getBody().errorMessage()).isEqualTo("勘定科目が見つかりません");
        }
    }

    @Nested
    @DisplayName("勘定科目一覧取得")
    class FindAllAccounts {

        @Test
        @DisplayName("勘定科目一覧を取得できる")
        void shouldReturnAllAccounts() {
            // Given
            Account account1 = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );
            Account account2 = Account.reconstruct(
                    AccountId.of(2),
                    AccountCode.of("2100"),
                    "買掛金",
                    AccountType.LIABILITY
            );
            when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

            // When
            ResponseEntity<List<AccountResponse>> response = accountController.findAll(null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).accountId()).isEqualTo(1);
            assertThat(response.getBody().get(0).accountCode()).isEqualTo("1100");
            assertThat(response.getBody().get(0).accountName()).isEqualTo("現金");
            assertThat(response.getBody().get(0).accountType()).isEqualTo("ASSET");
        }

        @Test
        @DisplayName("勘定科目がない場合は空リストを返す")
        void shouldReturnEmptyListWhenNoAccounts() {
            // Given
            when(accountRepository.findAll()).thenReturn(List.of());

            // When
            ResponseEntity<List<AccountResponse>> response = accountController.findAll(null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("勘定科目単体取得")
    class FindAccountById {

        @Test
        @DisplayName("IDで勘定科目を取得できる")
        void shouldReturnAccountById() {
            // Given
            Account account = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );
            when(accountRepository.findById(AccountId.of(1))).thenReturn(Optional.of(account));

            // When
            ResponseEntity<AccountResponse> response = accountController.findById(1);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accountId()).isEqualTo(1);
            assertThat(response.getBody().accountCode()).isEqualTo("1100");
            assertThat(response.getBody().accountName()).isEqualTo("現金");
            assertThat(response.getBody().accountType()).isEqualTo("ASSET");
        }

        @Test
        @DisplayName("存在しないIDの場合は404を返す")
        void shouldReturn404WhenAccountNotFound() {
            // Given
            when(accountRepository.findById(AccountId.of(999))).thenReturn(Optional.empty());

            // When
            ResponseEntity<AccountResponse> response = accountController.findById(999);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("勘定科目削除")
    class DeleteAccount {

        @Test
        @DisplayName("削除成功時は200を返す")
        void shouldReturnOkWhenDeleteSucceeds() {
            when(deleteAccountUseCase.execute(any(DeleteAccountCommand.class)))
                    .thenReturn(DeleteAccountResult.success(1));

            ResponseEntity<DeleteAccountResponse> response = accountController.deleteAccount(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isTrue();
            assertThat(response.getBody().accountId()).isEqualTo(1);
        }

        @Test
        @DisplayName("勘定科目が見つからない場合は404を返す")
        void shouldReturn404WhenAccountNotFound() {
            when(deleteAccountUseCase.execute(any(DeleteAccountCommand.class)))
                    .thenReturn(DeleteAccountResult.failure("勘定科目が見つかりません"));

            ResponseEntity<DeleteAccountResponse> response = accountController.deleteAccount(999);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }

        @Test
        @DisplayName("使用中の勘定科目の場合は409を返す")
        void shouldReturn409WhenAccountInUse() {
            when(deleteAccountUseCase.execute(any(DeleteAccountCommand.class)))
                    .thenReturn(DeleteAccountResult.failure("この勘定科目は仕訳で使用されているため削除できません"));

            ResponseEntity<DeleteAccountResponse> response = accountController.deleteAccount(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().success()).isFalse();
        }

        @Test
        @DisplayName("その他のエラーの場合は400を返す")
        void shouldReturn400WhenOtherError() {
            when(deleteAccountUseCase.execute(any(DeleteAccountCommand.class)))
                    .thenReturn(DeleteAccountResult.failure("不明なエラー"));

            ResponseEntity<DeleteAccountResponse> response = accountController.deleteAccount(1);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("勘定科目検索")
    class SearchAccounts {

        @Test
        @DisplayName("タイプで検索できる")
        void shouldSearchByType() {
            Account account = Account.reconstruct(
                    AccountId.of(1), AccountCode.of("1100"), "現金", AccountType.ASSET);
            when(accountRepository.search(eq(AccountType.ASSET), eq(null)))
                    .thenReturn(List.of(account));

            ResponseEntity<List<AccountResponse>> response = accountController.findAll("ASSET", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("キーワードで検索できる")
        void shouldSearchByKeyword() {
            Account account = Account.reconstruct(
                    AccountId.of(1), AccountCode.of("1100"), "現金", AccountType.ASSET);
            when(accountRepository.search(eq(null), eq("現金")))
                    .thenReturn(List.of(account));

            ResponseEntity<List<AccountResponse>> response = accountController.findAll(null, "現金");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("空文字のタイプは無視される")
        void shouldIgnoreBlankType() {
            when(accountRepository.findAll()).thenReturn(List.of());

            ResponseEntity<List<AccountResponse>> response = accountController.findAll("  ", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("空文字のキーワードは無視される")
        void shouldIgnoreBlankKeyword() {
            when(accountRepository.findAll()).thenReturn(List.of());

            ResponseEntity<List<AccountResponse>> response = accountController.findAll(null, "  ");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("不正なタイプの場合はBusinessExceptionをスローする")
        void shouldThrowBusinessExceptionForInvalidType() {
            assertThatThrownBy(() -> accountController.findAll("INVALID", null))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
