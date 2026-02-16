package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteAccountCommand;
import com.example.accounting.application.port.in.DeleteAccountResult;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountUsageChecker;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 勘定科目削除サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目削除サービス")
class DeleteAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUsageChecker accountUsageChecker;

    private DeleteAccountService deleteAccountService;

    @BeforeEach
    void setUp() {
        deleteAccountService = new DeleteAccountService(accountRepository, accountUsageChecker);
    }

    @Nested
    @DisplayName("削除成功")
    class SuccessfulDelete {

        @Test
        @DisplayName("勘定科目を削除できる")
        void shouldDeleteAccount() {
            DeleteAccountCommand command = new DeleteAccountCommand(1);
            Account existingAccount = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Try.success(Optional.of(existingAccount)));
            when(accountUsageChecker.isAccountInUse(AccountId.of(command.accountId())))
                    .thenReturn(false);
            when(accountRepository.deleteById(AccountId.of(command.accountId())))
                    .thenReturn(Try.success(null));

            DeleteAccountResult result = deleteAccountService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.accountId()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("勘定科目を削除しました");
            assertThat(result.errorMessage()).isNull();

            verify(accountRepository).deleteById(AccountId.of(1));
            verify(accountUsageChecker).isAccountInUse(AccountId.of(1));
        }
    }

    @Nested
    @DisplayName("削除失敗")
    class FailedDelete {

        @Test
        @DisplayName("勘定科目が存在しない場合は削除に失敗する")
        void shouldFailWhenAccountDoesNotExist() {
            DeleteAccountCommand command = new DeleteAccountCommand(1);

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Try.success(Optional.empty()));

            DeleteAccountResult result = deleteAccountService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("勘定科目が見つかりません");
            verify(accountRepository, never()).deleteById(any(AccountId.class));
            verify(accountUsageChecker, never()).isAccountInUse(any(AccountId.class));
        }

        @Test
        @DisplayName("使用中の勘定科目は削除できない")
        void shouldFailWhenAccountIsInUse() {
            DeleteAccountCommand command = new DeleteAccountCommand(1);
            Account existingAccount = Account.reconstruct(
                    AccountId.of(1),
                    AccountCode.of("1100"),
                    "現金",
                    AccountType.ASSET
            );

            when(accountRepository.findById(AccountId.of(command.accountId())))
                    .thenReturn(Try.success(Optional.of(existingAccount)));
            when(accountUsageChecker.isAccountInUse(AccountId.of(command.accountId())))
                    .thenReturn(true);

            DeleteAccountResult result = deleteAccountService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage())
                    .isEqualTo("この勘定科目は仕訳で使用されているため削除できません");
            verify(accountRepository, never()).deleteById(any(AccountId.class));
        }
    }
}
