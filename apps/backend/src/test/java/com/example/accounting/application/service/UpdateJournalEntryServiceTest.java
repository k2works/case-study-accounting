package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.UpdateJournalEntryCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳編集サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳編集サービス")
class UpdateJournalEntryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private UpdateJournalEntryService updateJournalEntryService;

    @BeforeEach
    void setUp() {
        updateJournalEntryService = new UpdateJournalEntryService(accountRepository, journalEntryRepository);
    }

    @Nested
    @DisplayName("更新成功")
    class SuccessfulUpdate {

        @Test
        @DisplayName("下書き仕訳を正常に更新できる")
        void shouldUpdateDraftJournalEntry() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上（更新）",
                    List.of(
                            debitLineInput(1, 1, "2000"),
                            creditLineInput(2, 2, "2000")
                    ),
                    1
            );
            JournalEntry existingEntry = draftEntry(1);

            stubJournalEntryFound(existingEntry);
            stubAccountsExistence(1, 2);
            stubSavePassThrough();

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.journalDate()).isEqualTo(LocalDate.of(2024, 2, 1));
            assertThat(result.description()).isEqualTo("売上計上（更新）");
            assertThat(result.status()).isEqualTo(JournalEntryStatus.DRAFT.name());
            assertThat(result.version()).isEqualTo(1);
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry updatedEntry = captor.getValue();

            assertThat(updatedEntry.getId()).isEqualTo(JournalEntryId.of(10));
            assertThat(updatedEntry.getDescription()).isEqualTo("売上計上（更新）");
            assertThat(updatedEntry.getLines()).hasSize(2);
            assertThat(updatedEntry.getLines().get(0).debitAmount())
                    .isEqualTo(Money.of(new BigDecimal("2000")));
            assertThat(updatedEntry.getLines().get(1).creditAmount())
                    .isEqualTo(Money.of(new BigDecimal("2000")));
        }
    }

    @Nested
    @DisplayName("更新失敗")
    class FailedUpdate {

        @Test
        @DisplayName("仕訳が存在しない場合はエラー")
        void shouldFailWhenJournalEntryNotFound() {
            UpdateJournalEntryCommand command = updateCommand(
                    99,
                    "売上計上",
                    List.of(debitLineInput(1, 1, "1000")),
                    1
            );

            when(journalEntryRepository.findById(JournalEntryId.of(99)))
                    .thenReturn(Optional.empty());

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
            verify(accountRepository, never()).findById(any(AccountId.class));
        }

        @Test
        @DisplayName("ステータスが DRAFT 以外の場合はエラー")
        void shouldFailWhenStatusIsNotDraft() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上",
                    List.of(debitLineInput(1, 1, "1000")),
                    1
            );

            JournalEntry existingEntry = JournalEntry.reconstruct(
                    JournalEntryId.of(10),
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    JournalEntryStatus.APPROVED,
                    1,
                    List.of(
                            JournalEntryLine.of(
                                    1,
                                    AccountId.of(1),
                                    Money.of(new BigDecimal("1000")),
                                    null
                            )
                    ),
                    UserId.of("user-1"),
                    UserId.of("approver-1"),
                    LocalDateTime.of(2024, 2, 1, 10, 0),
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 31, 10, 0),
                    LocalDateTime.of(2024, 1, 31, 10, 0)
            );

            stubJournalEntryFound(existingEntry);

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("下書き状態の仕訳のみ編集可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
            verify(accountRepository, never()).findById(any(AccountId.class));
        }

        @Test
        @DisplayName("バージョンが一致しない場合はエラー")
        void shouldFailWhenVersionMismatch() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上",
                    List.of(debitLineInput(1, 1, "1000")),
                    2
            );

            JournalEntry existingEntry = JournalEntry.reconstruct(
                    JournalEntryId.of(10),
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    JournalEntryStatus.DRAFT,
                    1,
                    List.of(
                            JournalEntryLine.of(
                                    1,
                                    AccountId.of(1),
                                    Money.of(new BigDecimal("1000")),
                                    null
                            )
                    ),
                    UserId.of("user-1"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 31, 10, 0),
                    LocalDateTime.of(2024, 1, 31, 10, 0)
            );

            stubJournalEntryFound(existingEntry);

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳のバージョンが一致しません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
            verify(accountRepository, never()).findById(any(AccountId.class));
        }

        @Test
        @DisplayName("明細行が空の場合はエラー")
        void shouldFailWhenLinesEmpty() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上",
                    List.of(),
                    1
            );

            JournalEntry existingEntry = draftEntry(1);

            stubJournalEntryFound(existingEntry);

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳明細は 1 行以上必要です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
            verify(accountRepository, never()).findById(any(AccountId.class));
        }

        @Test
        @DisplayName("勘定科目が存在しない場合はエラー")
        void shouldFailWhenAccountNotFound() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上",
                    List.of(debitLineInput(1, 1, "1000")),
                    1
            );

            JournalEntry existingEntry = draftEntry(1);

            stubJournalEntryFound(existingEntry);
            when(accountRepository.findById(AccountId.of(1)))
                    .thenReturn(Optional.empty());

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("勘定科目が存在しません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("貸借一致しない場合はエラー")
        void shouldFailWhenNotBalanced() {
            UpdateJournalEntryCommand command = updateCommand(
                    10,
                    "売上計上",
                    List.of(
                            debitLineInput(1, 1, "1000"),
                            creditLineInput(2, 2, "900")
                    ),
                    1
            );

            JournalEntry existingEntry = draftEntry(1);

            stubJournalEntryFound(existingEntry);
            stubAccountsExistence(1, 2);

            UpdateJournalEntryResult result = updateJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("貸借一致していません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }
    }

    private JournalEntry draftEntry(Integer version) {
        return JournalEntry.reconstruct(
                JournalEntryId.of(10),
                LocalDate.of(2024, 1, 31),
                "売上計上",
                JournalEntryStatus.DRAFT,
                version,
                List.of(
                        JournalEntryLine.of(
                                1,
                                AccountId.of(1),
                                Money.of(new BigDecimal("1000")),
                                null
                        ),
                        JournalEntryLine.of(
                                2,
                                AccountId.of(2),
                                null,
                                Money.of(new BigDecimal("1000"))
                        )
                ),
                UserId.of("user-1"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }

    private UpdateJournalEntryCommand updateCommand(Integer journalEntryId,
                                                    String description,
                                                    List<UpdateJournalEntryCommand.JournalEntryLineInput> lines,
                                                    Integer version) {
        return new UpdateJournalEntryCommand(
                journalEntryId,
                LocalDate.of(2024, 2, 1),
                description,
                lines,
                version
        );
    }

    private UpdateJournalEntryCommand.JournalEntryLineInput debitLineInput(int lineNumber,
                                                                           int accountId,
                                                                           String amount) {
        return new UpdateJournalEntryCommand.JournalEntryLineInput(
                lineNumber,
                accountId,
                new BigDecimal(amount),
                null
        );
    }

    private UpdateJournalEntryCommand.JournalEntryLineInput creditLineInput(int lineNumber,
                                                                            int accountId,
                                                                            String amount) {
        return new UpdateJournalEntryCommand.JournalEntryLineInput(
                lineNumber,
                accountId,
                null,
                new BigDecimal(amount)
        );
    }

    private void stubJournalEntryFound(JournalEntry journalEntry) {
        when(journalEntryRepository.findById(journalEntry.getId()))
                .thenReturn(Optional.of(journalEntry));
    }

    private void stubAccountsExistence(Integer... ids) {
        for (Integer id : ids) {
            when(accountRepository.findById(AccountId.of(id)))
                    .thenReturn(Optional.of(dummyAccount(id)));
        }
    }

    private void stubSavePassThrough() {
        when(journalEntryRepository.save(any(JournalEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Account dummyAccount(Integer id) {
        return Account.reconstruct(
                AccountId.of(id),
                AccountCode.of("1100"),
                "現金",
                AccountType.ASSET
        );
    }
}
