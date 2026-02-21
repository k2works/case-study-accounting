package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.GenerateAutoJournalCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AutoJournalLogRepository;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.GenerateAutoJournalResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.domain.model.auto_journal.AutoJournalLog;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("自動仕訳生成サービス")
class GenerateAutoJournalServiceTest {

    @Mock
    private AutoJournalPatternRepository patternRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AutoJournalLogRepository logRepository;

    private GenerateAutoJournalService service;

    @BeforeEach
    void setUp() {
        service = new GenerateAutoJournalService(patternRepository, accountRepository, journalEntryRepository, logRepository);
    }

    @Test
    @DisplayName("有効なパターンから仕訳を生成して保存できる")
    void shouldGenerateAndSaveJournalEntry() {
        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                1L,
                Map.of("amount", new BigDecimal("10000")),
                LocalDate.of(2024, 1, 31),
                "売上計上",
                "user-1"
        );

        AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L),
                "PAT001",
                "売上自動仕訳",
                "sales",
                "売上データから作成",
                true,
                List.of(
                        AutoJournalPatternItem.reconstruct(1, "D", "1110", "amount", null),
                        AutoJournalPatternItem.reconstruct(2, "C", "4100", "amount", null)
                )
        );

        when(patternRepository.findById(AutoJournalPatternId.of(1L))).thenReturn(Try.success(Optional.of(pattern)));
        when(accountRepository.findByCode("1110")).thenReturn(Try.success(Optional.of(dummyAccount(1, "1110"))));
        when(accountRepository.findByCode("4100")).thenReturn(Try.success(Optional.of(dummyAccount(2, "4100"))));
        when(journalEntryRepository.save(any(JournalEntry.class)))
                .thenAnswer(invocation -> {
                    JournalEntry entry = invocation.getArgument(0);
                    return Try.success(entry.withId(JournalEntryId.of(10)));
                });

        GenerateAutoJournalResult result = service.execute(command);

        assertThat(result.success()).isTrue();
        assertThat(result.journalEntryId()).isEqualTo(10);
        assertThat(result.journalDate()).isEqualTo(LocalDate.of(2024, 1, 31));
        assertThat(result.description()).isEqualTo("売上計上");
        assertThat(result.status()).isEqualTo(JournalEntryStatus.DRAFT.name());
        assertThat(result.errorMessage()).isNull();

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        JournalEntry savedTarget = captor.getValue();

        assertThat(savedTarget.getLines()).hasSize(2);
        assertThat(savedTarget.getLines().get(0).accountId()).isEqualTo(AccountId.of(1));
        assertThat(savedTarget.getLines().get(0).debitAmount().value()).isEqualByComparingTo("10000");
        assertThat(savedTarget.getLines().get(0).creditAmount()).isNull();
        assertThat(savedTarget.getLines().get(1).accountId()).isEqualTo(AccountId.of(2));
        assertThat(savedTarget.getLines().get(1).debitAmount()).isNull();
        assertThat(savedTarget.getLines().get(1).creditAmount().value()).isEqualByComparingTo("10000");

        ArgumentCaptor<AutoJournalLog> logCaptor = ArgumentCaptor.forClass(AutoJournalLog.class);
        verify(logRepository).save(logCaptor.capture());
        AutoJournalLog log = logCaptor.getValue();
        assertThat(log.getPatternId()).isEqualTo(1L);
        assertThat(log.getStatus()).isEqualTo("SUCCESS");
        assertThat(log.getGeneratedCount()).isEqualTo(1);
        assertThat(log.getMessage()).isEqualTo("仕訳ID: 10");
    }

    @Test
    @DisplayName("パターンが見つからない場合は失敗する")
    void shouldFailWhenPatternNotFound() {
        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                99L,
                Map.of("amount", new BigDecimal("10000")),
                LocalDate.of(2024, 1, 31),
                null,
                "user-1"
        );

        when(patternRepository.findById(AutoJournalPatternId.of(99L))).thenReturn(Try.success(Optional.empty()));

        GenerateAutoJournalResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("指定されたパターンが見つかりません");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(logRepository).save(any(AutoJournalLog.class));
    }

    @Test
    @DisplayName("パターンが無効な場合は失敗する")
    void shouldFailWhenPatternIsInactive() {
        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                1L,
                Map.of("amount", new BigDecimal("10000")),
                LocalDate.of(2024, 1, 31),
                null,
                "user-1"
        );

        AutoJournalPattern inactivePattern = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L),
                "PAT001",
                "売上自動仕訳",
                "sales",
                null,
                false,
                List.of(AutoJournalPatternItem.reconstruct(1, "D", "1110", "amount", null))
        );

        when(patternRepository.findById(AutoJournalPatternId.of(1L)))
                .thenReturn(Try.success(Optional.of(inactivePattern)));

        GenerateAutoJournalResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("指定されたパターンは無効です");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(logRepository).save(any(AutoJournalLog.class));
    }

    @Test
    @DisplayName("パターンに明細がない場合は失敗する")
    void shouldFailWhenPatternHasNoItems() {
        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                1L,
                Map.of("amount", new BigDecimal("10000")),
                LocalDate.of(2024, 1, 31),
                null,
                "user-1"
        );

        AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L),
                "PAT001",
                "売上自動仕訳",
                "sales",
                null,
                true,
                List.of()
        );

        when(patternRepository.findById(AutoJournalPatternId.of(1L))).thenReturn(Try.success(Optional.of(pattern)));

        GenerateAutoJournalResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("パターンに明細が定義されていません");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(logRepository).save(any(AutoJournalLog.class));
    }

    @Test
    @DisplayName("勘定科目が見つからない場合は失敗する")
    void shouldFailWhenAccountNotFound() {
        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                1L,
                Map.of("amount", new BigDecimal("10000")),
                LocalDate.of(2024, 1, 31),
                null,
                "user-1"
        );

        AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                AutoJournalPatternId.of(1L),
                "PAT001",
                "売上自動仕訳",
                "sales",
                null,
                true,
                List.of(AutoJournalPatternItem.reconstruct(1, "D", "9999", "amount", null))
        );

        when(patternRepository.findById(AutoJournalPatternId.of(1L))).thenReturn(Try.success(Optional.of(pattern)));
        when(accountRepository.findByCode("9999")).thenReturn(Try.success(Optional.empty()));

        GenerateAutoJournalResult result = service.execute(command);

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("勘定科目コード '9999' が見つかりません");
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        verify(logRepository).save(any(AutoJournalLog.class));
    }

    private Account dummyAccount(Integer id, String code) {
        return Account.reconstruct(
                AccountId.of(id),
                AccountCode.of(code),
                "勘定科目-" + code,
                AccountType.ASSET
        );
    }
}
