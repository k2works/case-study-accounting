package com.example.accounting.application.service;

import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.JournalEntryDetailResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetJournalEntryService")
class GetJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountRepository accountRepository;

    private GetJournalEntryService service;

    @BeforeEach
    void setUp() {
        service = new GetJournalEntryService(journalEntryRepository, accountRepository);
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("ID で仕訳を取得できる")
        void shouldFindById() {
            JournalEntry entry = createTestEntry();
            Account account = Account.reconstruct(
                    AccountId.of(100), AccountCode.of("1101"), "現金", AccountType.ASSET);

            when(journalEntryRepository.findById(JournalEntryId.of(1)))
                    .thenReturn(Optional.of(entry));
            when(accountRepository.findById(AccountId.of(100)))
                    .thenReturn(Optional.of(account));
            when(accountRepository.findById(AccountId.of(200)))
                    .thenReturn(Optional.empty());

            Optional<JournalEntryDetailResult> result = service.findById(1);

            assertThat(result).isPresent();
            assertThat(result.get().journalEntryId()).isEqualTo(1);
            assertThat(result.get().journalDate()).isEqualTo(LocalDate.of(2024, 1, 31));
            assertThat(result.get().description()).isEqualTo("売上計上");
            assertThat(result.get().status()).isEqualTo("DRAFT");
            assertThat(result.get().lines()).hasSize(2);
        }

        @Test
        @DisplayName("null の ID の場合は empty を返す")
        void shouldReturnEmptyForNullId() {
            Optional<JournalEntryDetailResult> result = service.findById(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("存在しない ID の場合は empty を返す")
        void shouldReturnEmptyForNonExistentId() {
            when(journalEntryRepository.findById(JournalEntryId.of(999)))
                    .thenReturn(Optional.empty());

            Optional<JournalEntryDetailResult> result = service.findById(999);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("勘定科目が見つからない場合は「不明」と表示する")
        void shouldShowUnknownForMissingAccount() {
            JournalEntry entry = createTestEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(1)))
                    .thenReturn(Optional.of(entry));
            when(accountRepository.findById(any(AccountId.class)))
                    .thenReturn(Optional.empty());

            Optional<JournalEntryDetailResult> result = service.findById(1);

            assertThat(result).isPresent();
            JournalEntryDetailResult.JournalEntryLineDetail firstLine = result.get().lines().get(0);
            assertThat(firstLine.accountCode()).isEqualTo("不明");
            assertThat(firstLine.accountName()).isEqualTo("不明");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("全仕訳を取得できる")
        void shouldFindAll() {
            JournalEntry entry = createTestEntry();
            Account account = Account.reconstruct(
                    AccountId.of(100), AccountCode.of("1101"), "現金", AccountType.ASSET);

            when(journalEntryRepository.findAll()).thenReturn(List.of(entry));
            when(accountRepository.findById(AccountId.of(100)))
                    .thenReturn(Optional.of(account));
            when(accountRepository.findById(AccountId.of(200)))
                    .thenReturn(Optional.empty());

            List<JournalEntryDetailResult> results = service.findAll();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).journalEntryId()).isEqualTo(1);
        }

        @Test
        @DisplayName("仕訳がない場合は空リストを返す")
        void shouldReturnEmptyListWhenNoEntries() {
            when(journalEntryRepository.findAll()).thenReturn(List.of());

            List<JournalEntryDetailResult> results = service.findAll();

            assertThat(results).isEmpty();
        }
    }

    private JournalEntry createTestEntry() {
        JournalEntryLine debitLine = JournalEntryLine.of(
                1, AccountId.of(100), Money.of(new BigDecimal("1000")), null);
        JournalEntryLine creditLine = JournalEntryLine.of(
                2, AccountId.of(200), null, Money.of(new BigDecimal("1000")));

        return JournalEntry.reconstruct(
                JournalEntryId.of(1),
                LocalDate.of(2024, 1, 31),
                "売上計上",
                JournalEntryStatus.DRAFT,
                1,
                List.of(debitLine, creditLine),
                UserId.of("user-1"),
                null,
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                LocalDateTime.of(2024, 1, 1, 10, 0, 0)
        );
    }
}
