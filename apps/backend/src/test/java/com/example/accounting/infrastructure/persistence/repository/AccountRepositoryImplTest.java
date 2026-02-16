package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.AccountEntity;
import com.example.accounting.infrastructure.persistence.mapper.AccountMapper;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountRepositoryImpl 単体テスト")
class AccountRepositoryImplTest {

    @Mock
    private AccountMapper accountMapper;

    private AccountRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AccountRepositoryImpl(accountMapper);
    }

    private AccountEntity createAccountEntity(int id, String code, String name, String type) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setName(name);
        entity.setAccountType(type);
        return entity;
    }

    @Test
    @DisplayName("findByCode(String) でマッパーに委譲する")
    void shouldDelegateFindByCodeString() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.findByCode("1100")).thenReturn(Optional.of(entity));

        Optional<Account> result = repository.findByCode("1100")
                .getOrElse(Optional.empty());

        assertThat(result).isPresent();
        assertThat(result.get().getAccountCode().value()).isEqualTo("1100");
        verify(accountMapper).findByCode("1100");
    }

    @Test
    @DisplayName("findByCode(AccountCode) でマッパーに委譲する")
    void shouldDelegateFindByCodeAccountCode() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.findByCode("1100")).thenReturn(Optional.of(entity));

        Optional<Account> result = repository.findByCode(AccountCode.of("1100"))
                .getOrElse(Optional.empty());

        assertThat(result).isPresent();
        verify(accountMapper).findByCode("1100");
    }

    @Test
    @DisplayName("findByCode で見つからない場合は empty")
    void shouldReturnEmptyWhenNotFoundByCode() {
        when(accountMapper.findByCode("9999")).thenReturn(Optional.empty());

        Optional<Account> result = repository.findByCode("9999")
                .getOrElse(Optional.empty());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById でマッパーに委譲する")
    void shouldDelegateFindById() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.findById(1)).thenReturn(Optional.of(entity));

        Optional<Account> result = repository.findById(AccountId.of(1))
                .getOrElse(Optional.empty());

        assertThat(result).isPresent();
        verify(accountMapper).findById(1);
    }

    @Test
    @DisplayName("findAll でマッパーに委譲する")
    void shouldDelegateFindAll() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.findAll()).thenReturn(List.of(entity));

        List<Account> result = repository.findAll()
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        verify(accountMapper).findAll();
    }

    @Test
    @DisplayName("findByType でマッパーに委譲する")
    void shouldDelegateFindByType() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.findByType("ASSET")).thenReturn(List.of(entity));

        List<Account> result = repository.findByType(AccountType.ASSET)
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        verify(accountMapper).findByType("ASSET");
    }

    @Test
    @DisplayName("search でマッパーに委譲する")
    void shouldDelegateSearch() {
        AccountEntity entity = createAccountEntity(1, "1100", "現金", "ASSET");
        when(accountMapper.search("ASSET", "現金")).thenReturn(List.of(entity));

        List<Account> result = repository.search(AccountType.ASSET, "現金")
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        verify(accountMapper).search("ASSET", "現金");
    }

    @Test
    @DisplayName("search で type が null の場合")
    void shouldDelegateSearchWithNullType() {
        when(accountMapper.search(null, "現金")).thenReturn(List.of());

        List<Account> result = repository.search(null, "現金")
                .getOrElse(List.of());

        assertThat(result).isEmpty();
        verify(accountMapper).search(null, "現金");
    }

    @Test
    @DisplayName("deleteById でマッパーに委譲する")
    void shouldDelegateDeleteById() {
        repository.deleteById(AccountId.of(1))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        verify(accountMapper).deleteById(1);
    }

    @Test
    @DisplayName("existsByCode でマッパーに委譲する")
    void shouldDelegateExistsByCode() {
        when(accountMapper.existsByCode("1100")).thenReturn(true);

        boolean result = repository.existsByCode(AccountCode.of("1100"))
                .getOrElse(false);

        assertThat(result).isTrue();
        verify(accountMapper).existsByCode("1100");
    }

    @Test
    @DisplayName("save で新規作成する（id が null）")
    void shouldSaveNewAccount() {
        Account account = Account.create(AccountCode.of("1100"), "現金", AccountType.ASSET);
        AccountEntity savedEntity = createAccountEntity(1, "1100", "現金", "ASSET");

        when(accountMapper.findById(any())).thenReturn(Optional.of(savedEntity));

        repository.save(account)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        verify(accountMapper).insert(any(AccountEntity.class));
    }

    @Test
    @DisplayName("save で既存レコードを更新する")
    void shouldSaveExistingAccount() {
        Account account = Account.reconstruct(
                AccountId.of(1), AccountCode.of("1100"), "現金", AccountType.ASSET);
        AccountEntity existingEntity = createAccountEntity(1, "1100", "現金", "ASSET");

        when(accountMapper.findById(1)).thenReturn(Optional.of(existingEntity));

        repository.save(account)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        verify(accountMapper).update(any(AccountEntity.class));
    }

    @Test
    @DisplayName("save 後の取得に失敗した場合は例外")
    void shouldThrowWhenSaveFails() {
        Account account = Account.create(AccountCode.of("1100"), "現金", AccountType.ASSET);

        when(accountMapper.findById(any())).thenReturn(Optional.empty());

        Try<Account> result = repository.save(account);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("保存後の勘定科目取得に失敗しました");
    }
}
