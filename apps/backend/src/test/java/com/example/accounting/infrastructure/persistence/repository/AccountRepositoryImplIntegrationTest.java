package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 勘定科目リポジトリ統合テスト
 *
 * <p>Testcontainers を使用して実際の PostgreSQL データベースと連携し、
 * リポジトリの CRUD 操作をテストする。</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("勘定科目リポジトリ統合テスト")
class AccountRepositoryImplIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    // テスト用ヘルパーメソッド
    private Account createTestAccount(String code, String name, AccountType type) {
        return Account.create(AccountCode.of(code), name, type);
    }

    @Nested
    @DisplayName("save メソッド")
    class SaveMethod {

        @Test
        @DisplayName("新規勘定科目を保存できる")
        void shouldSaveNewAccount() {
            // Given
            Account account = createTestAccount("1001", "現金", AccountType.ASSET);
            assertThat(account.getId()).isNull(); // 新規作成時は ID なし

            // When
            Account savedAccount = accountRepository.save(account);

            // Then
            assertThat(savedAccount).isNotNull();
            assertThat(savedAccount.getId()).isNotNull(); // DB 保存後に ID が採番される
            assertThat(savedAccount.getAccountCode()).isEqualTo(account.getAccountCode());
            assertThat(savedAccount.getAccountName()).isEqualTo("現金");
            assertThat(savedAccount.getAccountType()).isEqualTo(AccountType.ASSET);
        }

        @Test
        @DisplayName("既存勘定科目を更新できる")
        void shouldUpdateExistingAccount() {
            // Given
            Account account = createTestAccount("1002", "売掛金", AccountType.ASSET);
            Account savedAccount = accountRepository.save(account);

            // When（イミュータブルなので結果を受け取る）
            Account changedAccount = savedAccount.withAccountName("売掛金（更新後）");
            Account updatedAccount = accountRepository.save(changedAccount);

            // Then
            assertThat(updatedAccount.getAccountName()).isEqualTo("売掛金（更新後）");
        }
    }

    @Nested
    @DisplayName("findById メソッド")
    class FindByIdMethod {

        @Test
        @DisplayName("既存勘定科目を ID で検索できる")
        void shouldFindExistingAccountById() {
            // Given
            Account account = createTestAccount("2001", "買掛金", AccountType.LIABILITY);
            Account savedAccount = accountRepository.save(account);

            // When（保存後の ID を使用）
            Optional<Account> found = accountRepository.findById(savedAccount.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getAccountName()).isEqualTo("買掛金");
        }

        @Test
        @DisplayName("存在しない ID では empty を返す")
        void shouldReturnEmptyForNonExistentId() {
            // Given
            AccountId nonExistentId = AccountId.of(999999);

            // When
            Optional<Account> found = accountRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCode メソッド")
    class FindByCodeMethod {

        @Test
        @DisplayName("既存勘定科目をコードで検索できる")
        void shouldFindExistingAccountByCode() {
            // Given
            Account account = createTestAccount("2002", "仕入高", AccountType.EXPENSE);
            accountRepository.save(account);

            // When
            Optional<Account> found = accountRepository.findByCode(AccountCode.of("2002"));

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getAccountName()).isEqualTo("仕入高");
        }

        @Test
        @DisplayName("存在しないコードでは empty を返す")
        void shouldReturnEmptyForNonExistentCode() {
            // When
            Optional<Account> found = accountRepository.findByCode(AccountCode.of("9999"));

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByType メソッド")
    class FindByTypeMethod {

        @Test
        @DisplayName("勘定科目種別で検索できる")
        void shouldFindAccountsByType() {
            // Given
            Account assetAccount = createTestAccount("3001", "現金預金", AccountType.ASSET);
            Account liabilityAccount = createTestAccount("3002", "未払金", AccountType.LIABILITY);
            accountRepository.save(assetAccount);
            accountRepository.save(liabilityAccount);

            // When
            List<Account> accounts = accountRepository.findByType(AccountType.ASSET);

            // Then
            assertThat(accounts).isNotEmpty();
            assertThat(accounts).allMatch(account -> account.getAccountType() == AccountType.ASSET);
            assertThat(accounts)
                    .extracting(Account::getAccountCode)
                    .contains(AccountCode.of("3001"));
        }
    }

    @Nested
    @DisplayName("findAll メソッド")
    class FindAllMethod {

        @Test
        @DisplayName("全勘定科目を取得できる")
        void shouldFindAllAccounts() {
            // Given
            Account account1 = createTestAccount("4001", "通信費", AccountType.EXPENSE);
            Account account2 = createTestAccount("4002", "売上高", AccountType.REVENUE);
            accountRepository.save(account1);
            accountRepository.save(account2);

            // When
            List<Account> accounts = accountRepository.findAll();

            // Then
            assertThat(accounts)
                    .extracting(Account::getAccountCode)
                    .contains(AccountCode.of("4001"), AccountCode.of("4002"));
        }
    }

    @Nested
    @DisplayName("deleteById メソッド")
    class DeleteByIdMethod {

        @Test
        @DisplayName("既存勘定科目を削除できる")
        void shouldDeleteExistingAccount() {
            // Given
            Account account = createTestAccount("5001", "地代家賃", AccountType.EXPENSE);
            Account savedAccount = accountRepository.save(account);

            // When（保存後の ID を使用）
            accountRepository.deleteById(savedAccount.getId());

            // Then
            Optional<Account> found = accountRepository.findById(savedAccount.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByCode メソッド")
    class ExistsByCodeMethod {

        @Test
        @DisplayName("存在するコードで true を返す")
        void shouldReturnTrueForExistingCode() {
            // Given
            Account account = createTestAccount("6001", "雑収入", AccountType.REVENUE);
            accountRepository.save(account);

            // When
            boolean exists = accountRepository.existsByCode(AccountCode.of("6001"));

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("存在しないコードで false を返す")
        void shouldReturnFalseForNonExistentCode() {
            // When
            boolean exists = accountRepository.existsByCode(AccountCode.of("6002"));

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("ドメインモデル変換")
    class DomainModelConversion {

        @Test
        @DisplayName("全てのフィールドが正しく保存・復元される")
        void shouldPreserveAllFields() {
            // Given
            Account account = createTestAccount("7001", "資本金", AccountType.EQUITY);

            // When
            Account savedAccount = accountRepository.save(account);
            Optional<Account> found = accountRepository.findById(savedAccount.getId());

            // Then
            assertThat(found).isPresent();
            Account restored = found.get();
            assertThat(restored.getId()).isEqualTo(savedAccount.getId());
            assertThat(restored.getAccountCode()).isEqualTo(account.getAccountCode());
            assertThat(restored.getAccountName()).isEqualTo(account.getAccountName());
            assertThat(restored.getAccountType()).isEqualTo(account.getAccountType());
        }
    }
}
