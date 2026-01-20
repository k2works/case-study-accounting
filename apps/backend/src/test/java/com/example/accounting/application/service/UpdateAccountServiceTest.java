package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.UpdateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountUsageChecker;
import com.example.accounting.application.port.out.UpdateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 勘定科目更新サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目更新サービス")
class UpdateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUsageChecker accountUsageChecker;

    private UpdateAccountService updateAccountService;

    @BeforeEach
    void setUp() {
        updateAccountService = new UpdateAccountService(accountRepository, accountUsageChecker);
    }

    @Nested
    @DisplayName("更新成功")
    class SuccessfulUpdate {

        @Test
        @DisplayName("有効な情報で勘定科目を更新できる")
        void shouldUpdateAccountWithValidCommand() {
            UpdateAccountCommand command = new UpdateAccountCommand(
                    1,
                    "普通預金",
                    "EXPENSE"
            );
            Account existingAccount = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );
            Account updatedAccount = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "普通預金",
                    AccountType.EXPENSE
            );

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Optional.of(existingAccount));
            when(accountUsageChecker.isAccountInUse(AccountId.of(command.accountId())))
                    .thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

            UpdateAccountResult result = updateAccountService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.accountId()).isEqualTo(1);
            assertThat(result.accountCode()).isEqualTo("1100");
            assertThat(result.accountName()).isEqualTo("普通預金");
            assertThat(result.accountType()).isEqualTo("EXPENSE");
            assertThat(result.message()).isEqualTo("勘定科目を更新しました");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            Account accountToSave = accountCaptor.getValue();
            assertThat(accountToSave.getId().value()).isEqualTo(1);
            assertThat(accountToSave.getAccountCode().value()).isEqualTo("1100");
            assertThat(accountToSave.getAccountName()).isEqualTo("普通預金");
            assertThat(accountToSave.getAccountType()).isEqualTo(AccountType.EXPENSE);
            verify(accountUsageChecker).isAccountInUse(AccountId.of(1));
        }
    }

    @Nested
    @DisplayName("更新失敗")
    class FailedUpdate {

        @Test
        @DisplayName("勘定科目が存在しない場合は更新に失敗する")
        void shouldFailWhenAccountDoesNotExist() {
            UpdateAccountCommand command = new UpdateAccountCommand(
                    1,
                    "普通預金",
                    "ASSET"
            );

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Optional.empty());

            UpdateAccountResult result = updateAccountService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("勘定科目が見つかりません");
            verify(accountRepository, never()).save(any(Account.class));
            verify(accountUsageChecker, never()).isAccountInUse(any(AccountId.class));
        }

        @Test
        @DisplayName("使用中の勘定科目は種別を変更できない")
        void shouldFailWhenAccountTypeChangesAndAccountIsInUse() {
            UpdateAccountCommand command = new UpdateAccountCommand(
                    1,
                    "普通預金",
                    "EXPENSE"
            );
            Account existingAccount = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Optional.of(existingAccount));
            when(accountUsageChecker.isAccountInUse(AccountId.of(command.accountId())))
                    .thenReturn(true);

            UpdateAccountResult result = updateAccountService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("使用中の勘定科目の種別は変更できません");
            verify(accountRepository, never()).save(any(Account.class));
        }
    }
}
