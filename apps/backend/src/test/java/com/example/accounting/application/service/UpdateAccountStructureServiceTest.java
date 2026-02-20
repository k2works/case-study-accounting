package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.UpdateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.UpdateAccountStructureResult;
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
@DisplayName("勘定科目構成更新サービス")
class UpdateAccountStructureServiceTest {

    @Mock
    private AccountStructureRepository accountStructureRepository;

    @Mock
    private AccountRepository accountRepository;

    private UpdateAccountStructureService service;

    @BeforeEach
    void setUp() {
        service = new UpdateAccountStructureService(accountStructureRepository, accountRepository);
    }

    @Test
    @DisplayName("有効な更新で構成を更新できる")
    void executeWithValidUpdateShouldUpdateStructure() {
        UpdateAccountStructureCommand command = UpdateAccountStructureCommand.of("1100", "1000", 2).get();
        AccountStructure existing = AccountStructure.reconstruct("1100", "1100", 1, null, 1);
        AccountStructure parentStructure = AccountStructure.reconstruct("1000", "1000", 1, null, 1);
        AccountStructure saved = AccountStructure.reconstruct("1100", "1000~1100", 2, "1000", 2);
        Account account = Account.reconstruct(AccountId.of(2), AccountCode.of("1100"), "売掛金", AccountType.ASSET);
        Account parent = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(existing)));
        when(accountRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(account)));
        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(parent)));
        when(accountStructureRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(parentStructure)));
        when(accountStructureRepository.hasCircularReference("1100", "1000")).thenReturn(Try.success(false));
        when(accountStructureRepository.save(any(AccountStructure.class))).thenReturn(Try.success(saved));

        UpdateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.accountCode()).isEqualTo("1100");
        assertThat(result.accountPath()).isEqualTo("1000~1100");
        assertThat(result.hierarchyLevel()).isEqualTo(2);
        assertThat(result.parentAccountCode()).isEqualTo("1000");
        assertThat(result.displayOrder()).isEqualTo(2);
        assertThat(result.message()).isEqualTo("勘定科目構成を更新しました");
        assertThat(result.errorMessage()).isNull();
        verify(accountStructureRepository).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("既存構成がない場合は失敗する")
    void executeWithNonExistentStructureShouldReturnFailure() {
        UpdateAccountStructureCommand command = UpdateAccountStructureCommand.of("1100", null, 2).get();
        Account account = Account.reconstruct(AccountId.of(2), AccountCode.of("1100"), "売掛金", AccountType.ASSET);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.empty()));
        when(accountRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(account)));

        UpdateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("勘定科目構成が見つかりません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("自分自身を親に指定した場合は失敗する")
    void executeWithSelfReferenceShouldReturnFailure() {
        UpdateAccountStructureCommand command = UpdateAccountStructureCommand.of("1100", "1100", 2).get();
        AccountStructure existing = AccountStructure.reconstruct("1100", "1100", 1, null, 1);
        Account account = Account.reconstruct(AccountId.of(2), AccountCode.of("1100"), "売掛金", AccountType.ASSET);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(existing)));
        when(accountRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(account)));

        UpdateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("自分自身を親勘定科目にはできません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }

    @Test
    @DisplayName("循環参照になる場合は失敗する")
    void executeWithCircularReferenceShouldReturnFailure() {
        UpdateAccountStructureCommand command = UpdateAccountStructureCommand.of("1100", "1000", 2).get();
        AccountStructure existing = AccountStructure.reconstruct("1100", "1100", 1, null, 1);
        AccountStructure parentStructure = AccountStructure.reconstruct("1000", "1000", 1, null, 1);
        Account account = Account.reconstruct(AccountId.of(2), AccountCode.of("1100"), "売掛金", AccountType.ASSET);
        Account parent = Account.reconstruct(AccountId.of(1), AccountCode.of("1000"), "現金", AccountType.ASSET);

        when(accountStructureRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(existing)));
        when(accountRepository.findByCode("1100")).thenReturn(Try.success(Optional.of(account)));
        when(accountRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(parent)));
        when(accountStructureRepository.findByCode("1000")).thenReturn(Try.success(Optional.of(parentStructure)));
        when(accountStructureRepository.hasCircularReference("1100", "1000")).thenReturn(Try.success(true));

        UpdateAccountStructureResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("循環参照になるため更新できません");
        verify(accountStructureRepository, never()).save(any(AccountStructure.class));
    }
}
