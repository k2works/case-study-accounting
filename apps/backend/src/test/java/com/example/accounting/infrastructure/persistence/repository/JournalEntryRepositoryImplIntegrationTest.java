package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.model.user.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 仕訳リポジトリ統合テスト
 *
 * <p>Testcontainers を使用して実際の PostgreSQL データベースと連携し、
 * リポジトリの CRUD 操作をテストする。</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("仕訳リポジトリ統合テスト")
class JournalEntryRepositoryImplIntegrationTest {

    private static final LocalDate JOURNAL_DATE = LocalDate.of(2024, 1, 31);

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private int codeCounter = 1000;

    private Account createTestAccount(String codePrefix, String name, AccountType type) {
        // 4桁のコードを生成（プレフィックス2桁 + 連番2桁）
        String code = codePrefix + String.format("%02d", codeCounter++ % 100);
        return Account.create(AccountCode.of(code), name, type);
    }

    private User createTestUser(String suffix) {
        return User.create(
                Username.of("user-" + suffix),
                Email.of("user-" + suffix + "@example.com"),
                Password.fromRawPassword("password123"),
                "テストユーザー",
                Role.USER
        );
    }

    private JournalEntry createTestJournalEntry(UserId userId, Account debitAccount, Account creditAccount) {
        return JournalEntry.create(JOURNAL_DATE, "売上計上", userId)
                .addLine(JournalEntryLine.of(
                        1,
                        debitAccount.getId(),
                        Money.of(new BigDecimal("1000")),
                        null
                ))
                .addLine(JournalEntryLine.of(
                        2,
                        creditAccount.getId(),
                        null,
                        Money.of(new BigDecimal("1000"))
                ));
    }

    @Nested
    @DisplayName("save メソッド")
    class SaveMethod {

        @Test
        @DisplayName("新規仕訳を保存できる")
        void shouldSaveNewJournalEntry() {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User savedUser = userRepository.save(createTestUser(suffix));
            Account debitAccount = accountRepository.save(createTestAccount("11", "現金", AccountType.ASSET));
            Account creditAccount = accountRepository.save(createTestAccount("41", "売上", AccountType.REVENUE));
            JournalEntry entry = createTestJournalEntry(savedUser.getId(), debitAccount, creditAccount);

            JournalEntry savedEntry = journalEntryRepository.save(entry);

            assertThat(savedEntry.getId()).isNotNull();
            assertThat(savedEntry.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(savedEntry.getDescription()).isEqualTo("売上計上");
            assertThat(savedEntry.getLines()).hasSize(2);
            assertThat(savedEntry.getCreatedBy()).isEqualTo(savedUser.getId());
        }
    }

    @Nested
    @DisplayName("findById メソッド")
    class FindByIdMethod {

        @Test
        @DisplayName("既存仕訳を ID で検索できる")
        void shouldFindExistingJournalEntry() {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User savedUser = userRepository.save(createTestUser(suffix));
            Account debitAccount = accountRepository.save(createTestAccount("12", "預金", AccountType.ASSET));
            Account creditAccount = accountRepository.save(createTestAccount("42", "売上", AccountType.REVENUE));
            JournalEntry savedEntry = journalEntryRepository.save(
                    createTestJournalEntry(savedUser.getId(), debitAccount, creditAccount)
            );

            Optional<JournalEntry> found = journalEntryRepository.findById(savedEntry.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getLines()).hasSize(2);
            assertThat(found.get().getDescription()).isEqualTo("売上計上");
        }
    }

    @Nested
    @DisplayName("update メソッド")
    class UpdateMethod {

        @Test
        @DisplayName("既存仕訳を更新できる")
        void shouldUpdateJournalEntry() {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User savedUser = userRepository.save(createTestUser(suffix));
            Account debitAccount = accountRepository.save(createTestAccount("13", "現金", AccountType.ASSET));
            Account creditAccount = accountRepository.save(createTestAccount("43", "売上", AccountType.REVENUE));
            JournalEntry savedEntry = journalEntryRepository.save(
                    createTestJournalEntry(savedUser.getId(), debitAccount, creditAccount)
            );

            JournalEntry updatedEntry = savedEntry.withDescription("売上計上（更新）")
                    .withLines(List.of(
                            JournalEntryLine.of(
                                    1,
                                    debitAccount.getId(),
                                    Money.of(new BigDecimal("2000")),
                                    null
                            ),
                            JournalEntryLine.of(
                                    2,
                                    creditAccount.getId(),
                                    null,
                                    Money.of(new BigDecimal("2000"))
                            )
                    ));

            JournalEntry result = journalEntryRepository.save(updatedEntry);

            assertThat(result.getDescription()).isEqualTo("売上計上（更新）");
            assertThat(result.getLines()).hasSize(2);
            assertThat(result.getLines().get(0).debitAmount().value()).isEqualByComparingTo("2000");
        }
    }

    @Nested
    @DisplayName("findAll メソッド")
    class FindAllMethod {

        @Test
        @DisplayName("全仕訳を取得できる")
        void shouldFindAllJournalEntries() {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User savedUser = userRepository.save(createTestUser(suffix));
            Account debitAccount = accountRepository.save(createTestAccount("14", "現金", AccountType.ASSET));
            Account creditAccount = accountRepository.save(createTestAccount("44", "売上", AccountType.REVENUE));
            journalEntryRepository.save(createTestJournalEntry(savedUser.getId(), debitAccount, creditAccount));

            List<JournalEntry> entries = journalEntryRepository.findAll();

            assertThat(entries).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById メソッド")
    class DeleteByIdMethod {

        @Test
        @DisplayName("既存仕訳を削除できる")
        void shouldDeleteJournalEntry() {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User savedUser = userRepository.save(createTestUser(suffix));
            Account debitAccount = accountRepository.save(createTestAccount("15", "現金", AccountType.ASSET));
            Account creditAccount = accountRepository.save(createTestAccount("45", "売上", AccountType.REVENUE));
            JournalEntry savedEntry = journalEntryRepository.save(
                    createTestJournalEntry(savedUser.getId(), debitAccount, creditAccount)
            );

            journalEntryRepository.deleteById(savedEntry.getId());

            Optional<JournalEntry> found = journalEntryRepository.findById(savedEntry.getId());
            assertThat(found).isEmpty();
        }
    }
}
