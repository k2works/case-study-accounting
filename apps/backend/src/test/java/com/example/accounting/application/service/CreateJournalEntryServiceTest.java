package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
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
import io.vavr.control.Try;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳登録サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳登録サービス")
class CreateJournalEntryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private CreateJournalEntryService createJournalEntryService;

    @BeforeEach
    void setUp() {
        createJournalEntryService = new CreateJournalEntryService(accountRepository, journalEntryRepository);
    }

    @Nested
    @DisplayName("登録成功")
    class SuccessfulRegister {

        @Test
        @DisplayName("有効な情報で仕訳を登録できる")
        void shouldCreateJournalEntryWithValidCommand() {
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    "user-1",
                    List.of(
                            new CreateJournalEntryCommand.JournalEntryLineInput(
                                    1,
                                    1,
                                    new BigDecimal("1000"),
                                    null
                            ),
                            new CreateJournalEntryCommand.JournalEntryLineInput(
                                    2,
                                    2,
                                    null,
                                    new BigDecimal("1000")
                            )
                    )
            );

            when(accountRepository.findById(AccountId.of(1)))
                    .thenReturn(Try.success(Optional.of(dummyAccount(1))));
            when(accountRepository.findById(AccountId.of(2)))
                    .thenReturn(Try.success(Optional.of(dummyAccount(2))));
            when(journalEntryRepository.save(any(JournalEntry.class)))
                    .thenAnswer(invocation -> {
                        JournalEntry entry = invocation.getArgument(0);
                        return Try.success(entry.withId(JournalEntryId.of(10)));
                    });

            CreateJournalEntryResult result = createJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.journalDate()).isEqualTo(LocalDate.of(2024, 1, 31));
            assertThat(result.description()).isEqualTo("売上計上");
            assertThat(result.status()).isEqualTo(JournalEntryStatus.DRAFT.name());
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry entry = captor.getValue();

            assertThat(entry.getId()).isNull();
            assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
            assertThat(entry.getCreatedBy()).isEqualTo(UserId.of("user-1"));
            assertThat(entry.getLines()).hasSize(2);

            JournalEntryLine firstLine = entry.getLines().get(0);
            assertThat(firstLine.lineNumber()).isEqualTo(1);
            assertThat(firstLine.accountId()).isEqualTo(AccountId.of(1));
            assertThat(firstLine.debitAmount()).isEqualTo(Money.of(new BigDecimal("1000")));
            assertThat(firstLine.creditAmount()).isNull();

            JournalEntryLine secondLine = entry.getLines().get(1);
            assertThat(secondLine.lineNumber()).isEqualTo(2);
            assertThat(secondLine.accountId()).isEqualTo(AccountId.of(2));
            assertThat(secondLine.debitAmount()).isNull();
            assertThat(secondLine.creditAmount()).isEqualTo(Money.of(new BigDecimal("1000")));
        }
    }

    @Nested
    @DisplayName("登録失敗")
    class FailedRegister {

        @Test
        @DisplayName("勘定科目が存在しない場合は登録に失敗する")
        void shouldFailWhenAccountNotFound() {
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    "user-1",
                    List.of(
                            new CreateJournalEntryCommand.JournalEntryLineInput(
                                    1,
                                    1,
                                    new BigDecimal("1000"),
                                    null
                            )
                    )
            );

            when(accountRepository.findById(AccountId.of(1))).thenReturn(Try.success(Optional.empty()));

            CreateJournalEntryResult result = createJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("勘定科目が存在しません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("貸借が一致しない場合は登録に失敗する")
        void shouldFailWhenNotBalanced() {
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    "user-1",
                    List.of(
                            new CreateJournalEntryCommand.JournalEntryLineInput(
                                    1,
                                    1,
                                    new BigDecimal("1000"),
                                    null
                            ),
                            new CreateJournalEntryCommand.JournalEntryLineInput(
                                    2,
                                    2,
                                    null,
                                    new BigDecimal("900")
                            )
                    )
            );

            when(accountRepository.findById(AccountId.of(1)))
                    .thenReturn(Try.success(Optional.of(dummyAccount(1))));
            when(accountRepository.findById(AccountId.of(2)))
                    .thenReturn(Try.success(Optional.of(dummyAccount(2))));

            CreateJournalEntryResult result = createJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("貸借一致していません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("明細が 1 行もない場合は登録に失敗する")
        void shouldFailWhenNoLinesProvided() {
            CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                    LocalDate.of(2024, 1, 31),
                    "売上計上",
                    "user-1",
                    List.of()
            );

            CreateJournalEntryResult result = createJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳明細は 1 行以上必要です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }
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
