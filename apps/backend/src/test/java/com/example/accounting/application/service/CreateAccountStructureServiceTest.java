package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.CreateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.CreateAccountStructureResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.domain.model.account.AccountType;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("勘定科目構成登録サービス")
class CreateAccountStructureServiceTest {

    @Mock
    private AccountStructureRepository accountStructureRepository;

    @Mock
    private AccountRepository accountRepository;

    private CreateAccountStructureService service;

    @BeforeEach
    void setUp() {
        service = new CreateAccountStructureService(accountStructureRepository, accountRepository);
    }

    @Test
    @DisplayName("有効なコマンドで構成を登録できる")
    void executeWithValidCommandShouldCreateStructure() {
        CreateAccountStructureCommand command = CreateAccountStructureCommand.of("1000", null, 1).get();
        Account account = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);
        AccountStructure saved = AccountStructure.reconstruct("1000", "1000", 1, null, 1);

        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(account)));
        when(accountStructureRepository.existsByCode("1000")).thenReturn(Try.success(false));
        when(accountStructureRepository.save(any(AccountStructure.class))).thenReturn(Try.success(saved));

        CreateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.accountCode()).isEqualTo("1000");
        assertThat(result.accountPath()).isEqualTo("1000");
        assertThat(result.hierarchyLevel()).isEqualTo(1);
        assertThat(result.parentAccountCode()).isNull();
        assertThat(result.displayOrder()).isEqualTo(1);
        assertThat(result.errorMessage()).isNull();
        verify(accountStructureRepository).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("勘定科目が存在しない場合は失敗する")
    void executeWithNonExistentAccountShouldReturnFailure() {
        CreateAccountStructureCommand command = CreateAccountStructureCommand.of("1000", null, 1).get();

        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.empty()));

        CreateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("勘定科目が存在しません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("重複した構成の場合は失敗する")
    void executeWithDuplicateStructureShouldReturnFailure() {
        CreateAccountStructureCommand command = CreateAccountStructureCommand.of("1000", null, 1).get();
        Account account = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);

        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(account)));
        when(accountStructureRepository.existsByCode("1000")).thenReturn(Try.success(true));

        CreateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("勘定科目構成は既に登録されています");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("自分自身を親に指定した場合は失敗する")
    void executeWithSelfReferenceShouldReturnFailure() {
        CreateAccountStructureCommand command = CreateAccountStructureCommand.of("1000", "1000", 1).get();
        Account account = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);

        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(account)));
        when(accountStructureRepository.existsByCode("1000")).thenReturn(Try.success(false));

        CreateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("自分自身を親勘定科目にはできません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("循環参照になる場合は失敗する")
    void executeWithCircularReferenceShouldReturnFailure() {
        CreateAccountStructureCommand command = CreateAccountStructureCommand.of("1100", "1000", 1).get();
        Account account = Account.reconstruct(AccountId.of(2), AccountCode.of("1100"), "売掛金", AccountType.ASSET);
        Account parent = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);

        when(accountRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(account)));
        when(accountStructureRepository.existsByCode("1100")).thenReturn(Try.success(false));
        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(parent)));
        when(accountStructureRepository.hasCircularReference("1100", "1000")).thenReturn(Try.success(true));

        CreateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("循環参照になるため登録できません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }
}
