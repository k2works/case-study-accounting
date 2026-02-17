package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
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
 * 勘定科目登録サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目登録サービス")
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private CreateAccountService createAccountService;

    @BeforeEach
    void setUp() {
        createAccountService = new CreateAccountService(accountRepository);
    }

    @Nested
    @DisplayName("登録成功")
    class SuccessfulRegister {

        @Test
        @DisplayName("有効な情報で勘定科目登録できる")
        void shouldCreateAccountWithValidCommand() {
            CreateAccountCommand command = new CreateAccountCommand(
                    "1100",
                    "現金",
                    "ASSET"
            );

            // 保存後に ID が採番された Account を返すようにモック
            Account savedAccountWithId = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );

            when(accountRepository.existsByCode(AccountCode.of(command.accountCode())))
                    .thenReturn(Try.success(false));
            when(accountRepository.save(any(Account.class))).thenReturn(Try.success(savedAccountWithId));

            CreateAccountResult result = createAccountService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.accountId()).isEqualTo(1);
            assertThat(result.accountCode()).isEqualTo("1100");
            assertThat(result.accountName()).isEqualTo("現金");
            assertThat(result.accountType()).isEqualTo("ASSET");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            Account accountToSave = accountCaptor.getValue();
            assertThat(accountToSave.getId()).isNull(); // 保存前は null
            assertThat(accountToSave.getAccountCode().value()).isEqualTo("1100");
            assertThat(accountToSave.getAccountName()).isEqualTo("現金");
            assertThat(accountToSave.getAccountType()).isEqualTo(AccountType.ASSET);
        }
    }

    @Nested
    @DisplayName("登録失敗")
    class FailedRegister {

        @Test
        @DisplayName("科目コードが既に存在する場合は登録に失敗する")
        void shouldFailWhenAccountCodeAlreadyExists() {
            CreateAccountCommand command = new CreateAccountCommand(
                    "1100",
                    "現金",
                    "ASSET"
            );

            when(accountRepository.existsByCode(AccountCode.of(command.accountCode())))
                    .thenReturn(Try.success(true));

            CreateAccountResult result = createAccountService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("勘定科目コードは既に使用されています");
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("無効なコマンドの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenCommandIsInvalid() {
            assertThat(CreateAccountCommand.of("12", "現金", "ASSET").getLeft())
                    .isEqualTo("勘定科目コードは 4 桁の数字である必要があります");
        }
    }
}
