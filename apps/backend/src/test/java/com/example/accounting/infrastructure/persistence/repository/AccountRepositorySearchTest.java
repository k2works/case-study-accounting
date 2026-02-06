package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 勘定科目検索リポジトリテスト
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("勘定科目検索リポジトリテスト")
class AccountRepositorySearchTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account createTestAccount(String code, String name, AccountType type) {
        return Account.create(AccountCode.of(code), name, type);
    }

    @Test
    @DisplayName("種別フィルタのみで検索できる")
    void shouldSearchByTypeOnly() {
        // Given
        Account assetAccount = accountRepository.save(createTestAccount("9101", "検索資産", AccountType.ASSET));
        accountRepository.save(createTestAccount("9102", "検索負債", AccountType.LIABILITY));

        // When
        List<Account> accounts = accountRepository.search(AccountType.ASSET, null);

        // Then
        assertThat(accounts)
                .isNotEmpty()
                .allMatch(account -> account.getAccountType() == AccountType.ASSET);
        assertThat(accounts)
                .extracting(Account::getAccountCode)
                .contains(assetAccount.getAccountCode());
    }

    @Test
    @DisplayName("キーワードのみで検索できる（コード前方一致）")
    void shouldSearchByKeywordOnlyForCodePrefix() {
        // Given
        Account account = accountRepository.save(createTestAccount("9201", "検索コード", AccountType.ASSET));

        // When
        List<Account> accounts = accountRepository.search(null, "920");

        // Then
        assertThat(accounts)
                .extracting(Account::getAccountCode)
                .contains(account.getAccountCode());
    }

    @Test
    @DisplayName("キーワードのみで検索できる（名前部分一致）")
    void shouldSearchByKeywordOnlyForNameContains() {
        // Given
        Account account = accountRepository.save(createTestAccount("9301", "旅費交通費", AccountType.EXPENSE));

        // When
        List<Account> accounts = accountRepository.search(null, "交通");

        // Then
        assertThat(accounts)
                .extracting(Account::getAccountName)
                .contains(account.getAccountName());
    }

    @Test
    @DisplayName("種別とキーワードの複合検索ができる")
    void shouldSearchByTypeAndKeyword() {
        // Given
        Account account = accountRepository.save(createTestAccount("9401", "消耗品費", AccountType.EXPENSE));
        accountRepository.save(createTestAccount("9402", "消耗品費", AccountType.ASSET));

        // When
        List<Account> accounts = accountRepository.search(AccountType.EXPENSE, "消耗");

        // Then
        assertThat(accounts)
                .isNotEmpty()
                .allMatch(found -> found.getAccountType() == AccountType.EXPENSE);
        assertThat(accounts)
                .extracting(Account::getAccountCode)
                .contains(account.getAccountCode());
    }

    @Test
    @DisplayName("フィルタなしで全件取得できる")
    void shouldSearchAllWhenNoFilters() {
        // Given
        Account account = accountRepository.save(createTestAccount("9501", "全件取得", AccountType.EQUITY));

        // When
        List<Account> accounts = accountRepository.search(null, null);

        // Then
        assertThat(accounts)
                .extracting(Account::getAccountCode)
                .contains(account.getAccountCode());
    }
}
