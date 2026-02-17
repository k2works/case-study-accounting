package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.shared.OptimisticLockException;
import com.example.accounting.infrastructure.persistence.entity.DailyBalanceEntryEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineEntity;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import com.example.accounting.infrastructure.persistence.mapper.JournalEntryMapper;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JournalEntryRepositoryImpl")
class JournalEntryRepositoryImplTest {

    @Mock
    private JournalEntryMapper journalEntryMapper;

    private JournalEntryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JournalEntryRepositoryImpl(journalEntryMapper);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("既存仕訳は update と明細再登録を行う")
        void shouldUpdateExistingJournalEntry() {
            JournalEntry journalEntry = buildJournalEntryWithId(10, List.of(buildDebitLine(1)));
            JournalEntryEntity existingEntity = buildEntity(10, "既存");
            JournalEntryEntity savedEntity = buildEntity(10, "更新後");

            when(journalEntryMapper.findById(10))
                    .thenReturn(Optional.of(existingEntity), Optional.of(savedEntity));
            when(journalEntryMapper.update(any())).thenReturn(1);

            JournalEntry result = repository.save(journalEntry.withDescription("更新後"))
                    .getOrElse((JournalEntry) null);

            assertThat(result.getId()).isEqualTo(JournalEntryId.of(10));
            assertThat(result.getDescription()).isEqualTo("更新後");
            verify(journalEntryMapper).deleteLines(10);
            verify(journalEntryMapper).insertLines(any());
        }

        @Test
        @DisplayName("更新件数が 0 の場合は OptimisticLockException を投げる")
        void shouldThrowOptimisticLockExceptionWhenUpdateFailed() {
            JournalEntry journalEntry = buildJournalEntryWithId(20, List.of(buildDebitLine(1)));
            JournalEntryEntity existingEntity = buildEntity(20, "既存");

            when(journalEntryMapper.findById(20)).thenReturn(Optional.of(existingEntity));
            when(journalEntryMapper.update(any())).thenReturn(0);

            assertThrows(OptimisticLockException.class, () -> repository.save(journalEntry)
                    .getOrElseThrow(ex -> (RuntimeException) ex));
            verify(journalEntryMapper, never()).deleteLines(20);
        }

        @Test
        @DisplayName("新規仕訳は insert のみ行い明細が空なら登録しない")
        void shouldInsertNewJournalEntryWithoutLines() {
            JournalEntry journalEntry = buildNewJournalEntry();
            JournalEntryEntity savedEntity = buildEntity(100, "新規");

            doAnswer(invocation -> {
                JournalEntryEntity entity = invocation.getArgument(0);
                entity.setId(100);
                return null;
            }).when(journalEntryMapper).insert(any());
            when(journalEntryMapper.findById(100)).thenReturn(Optional.of(savedEntity));

            JournalEntry result = repository.save(journalEntry)
                    .getOrElse((JournalEntry) null);

            assertThat(result.getId()).isEqualTo(JournalEntryId.of(100));
            verify(journalEntryMapper, never()).insertLines(any());
        }
    }

    @Test
    @DisplayName("lineDescription が空の場合は仕訳ヘッダの摘要を使用する")
    void shouldFallbackToHeaderDescription() {
        JournalEntryLineWithHeaderEntity headerOnly = new JournalEntryLineWithHeaderEntity();
        headerOnly.setJournalEntryId(1);
        headerOnly.setJournalDate(LocalDate.of(2024, 1, 10));
        headerOnly.setDescription("ヘッダ摘要");
        headerOnly.setLineDescription(" ");
        headerOnly.setDebitAmount(new BigDecimal("100"));
        headerOnly.setCreditAmount(BigDecimal.ZERO);

        JournalEntryLineWithHeaderEntity lineSpecific = new JournalEntryLineWithHeaderEntity();
        lineSpecific.setJournalEntryId(2);
        lineSpecific.setJournalDate(LocalDate.of(2024, 1, 11));
        lineSpecific.setDescription("ヘッダ摘要2");
        lineSpecific.setLineDescription("行摘要");
        lineSpecific.setDebitAmount(BigDecimal.ZERO);
        lineSpecific.setCreditAmount(new BigDecimal("200"));

        when(journalEntryMapper.findPostedLinesByAccountAndPeriod(1, null, null, 0, 10))
                .thenReturn(List.of(headerOnly, lineSpecific));

        List<GeneralLedgerEntry> result = repository.findPostedLinesByAccountAndPeriod(1, null, null, 0, 10)
                .getOrElse(List.of());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).description()).isEqualTo("ヘッダ摘要");
        assertThat(result.get(1).description()).isEqualTo("行摘要");
    }

    @Test
    @DisplayName("lineDescription が null の場合は仕訳ヘッダの摘要を使用する")
    void shouldFallbackToHeaderDescriptionWhenLineDescriptionIsNull() {
        JournalEntryLineWithHeaderEntity entity = new JournalEntryLineWithHeaderEntity();
        entity.setJournalEntryId(3);
        entity.setJournalDate(LocalDate.of(2024, 1, 12));
        entity.setDescription("ヘッダ摘要3");
        entity.setLineDescription(null);
        entity.setDebitAmount(new BigDecimal("50"));
        entity.setCreditAmount(BigDecimal.ZERO);

        when(journalEntryMapper.findPostedLinesByAccountAndPeriod(1, null, null, 0, 10))
                .thenReturn(List.of(entity));

        List<GeneralLedgerEntry> result = repository.findPostedLinesByAccountAndPeriod(1, null, null, 0, 10)
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("ヘッダ摘要3");
    }

    @Test
    @DisplayName("transactionCount が null の場合は 0 として返す")
    void shouldDefaultTransactionCountToZero() {
        DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();
        entity.setDate(LocalDate.of(2024, 2, 1));
        entity.setDebitTotal(new BigDecimal("300"));
        entity.setCreditTotal(new BigDecimal("100"));
        entity.setTransactionCount(null);

        when(journalEntryMapper.findDailyBalanceByAccountAndPeriod(1, null, null))
                .thenReturn(List.of(entity));

        List<DailyBalanceEntry> result = repository.findDailyBalanceByAccountAndPeriod(1, null, null)
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).transactionCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("transactionCount が設定済みの場合はそのまま返す")
    void shouldReturnTransactionCountWhenPresent() {
        DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();
        entity.setDate(LocalDate.of(2024, 2, 2));
        entity.setDebitTotal(new BigDecimal("150"));
        entity.setCreditTotal(new BigDecimal("50"));
        entity.setTransactionCount(5L);

        when(journalEntryMapper.findDailyBalanceByAccountAndPeriod(1, null, null))
                .thenReturn(List.of(entity));

        List<DailyBalanceEntry> result = repository.findDailyBalanceByAccountAndPeriod(1, null, null)
                .getOrElse(List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).transactionCount()).isEqualTo(5L);
    }

    private JournalEntry buildJournalEntryWithId(int id, List<JournalEntryLine> lines) {
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
                LocalDate.of(2024, 1, 5),
                "既存",
                JournalEntryStatus.DRAFT,
                1,
                lines,
                UserId.of("user-1"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2024, 1, 5, 10, 0),
                LocalDateTime.of(2024, 1, 5, 10, 0)
        );
    }

    private JournalEntry buildNewJournalEntry() {
        return JournalEntry.create(
                LocalDate.of(2024, 1, 7),
                "新規",
                UserId.of("user-2"),
                1,
                () -> LocalDateTime.of(2024, 1, 7, 9, 0)
        );
    }

    private JournalEntryLine buildDebitLine(int lineNumber) {
        return JournalEntryLine.of(
                lineNumber,
                AccountId.of(10),
                Money.of(new BigDecimal("100")),
                null
        );
    }

    private JournalEntryEntity buildEntity(int id, String description) {
        JournalEntryEntity entity = new JournalEntryEntity();
        entity.setId(id);
        entity.setJournalDate(LocalDate.of(2024, 1, 5));
        entity.setDescription(description);
        entity.setStatus(JournalEntryStatus.DRAFT.name());
        entity.setVersion(1);
        entity.setCreatedBy("user-1");
        entity.setLines(List.of(buildLineEntity(id)));
        return entity;
    }

    private JournalEntryLineEntity buildLineEntity(int journalEntryId) {
        JournalEntryLineEntity lineEntity = new JournalEntryLineEntity();
        lineEntity.setJournalEntryId(journalEntryId);
        lineEntity.setLineNumber(1);
        lineEntity.setAccountId(10);
        lineEntity.setDebitAmount(new BigDecimal("100"));
        lineEntity.setCreditAmount(BigDecimal.ZERO);
        return lineEntity;
    }
}
