package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.AccountId;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳削除サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳削除サービス")
class DeleteJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private DeleteJournalEntryService deleteJournalEntryService;

    @BeforeEach
    void setUp() {
        deleteJournalEntryService = new DeleteJournalEntryService(journalEntryRepository);
    }

    @Nested
    @DisplayName("削除成功")
    class SuccessfulDelete {

        @Test
        @DisplayName("下書き仕訳を削除できる")
        void shouldDeleteDraftJournalEntry() {
            DeleteJournalEntryCommand command = new DeleteJournalEntryCommand(10);
            JournalEntry existingEntry = draftEntry(10);

            when(journalEntryRepository.findById(JournalEntryId.of(command.journalEntryId())))
                    .thenReturn(Optional.of(existingEntry));

            DeleteJournalEntryResult result = deleteJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.errorMessage()).isNull();

            verify(journalEntryRepository).deleteById(JournalEntryId.of(10));
        }
    }

    @Nested
    @DisplayName("削除失敗")
    class FailedDelete {

        @Test
        @DisplayName("仕訳が存在しない場合は削除に失敗する")
        void shouldFailWhenJournalEntryNotFound() {
            DeleteJournalEntryCommand command = new DeleteJournalEntryCommand(99);

            when(journalEntryRepository.findById(JournalEntryId.of(command.journalEntryId())))
                    .thenReturn(Optional.empty());

            DeleteJournalEntryResult result = deleteJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).deleteById(any(JournalEntryId.class));
        }

        @Test
        @DisplayName("ステータスが DRAFT 以外の場合は削除に失敗する")
        void shouldFailWhenStatusIsNotDraft() {
            DeleteJournalEntryCommand command = new DeleteJournalEntryCommand(10);
            JournalEntry existingEntry = approvedEntry(10);

            when(journalEntryRepository.findById(JournalEntryId.of(command.journalEntryId())))
                    .thenReturn(Optional.of(existingEntry));

            DeleteJournalEntryResult result = deleteJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("下書き状態の仕訳のみ削除できます");
            verify(journalEntryRepository, never()).deleteById(any(JournalEntryId.class));
        }
    }

    private JournalEntry draftEntry(Integer id) {
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
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
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }

    private JournalEntry approvedEntry(Integer id) {
        return JournalEntry.reconstruct(
                JournalEntryId.of(id),
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
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }
}
