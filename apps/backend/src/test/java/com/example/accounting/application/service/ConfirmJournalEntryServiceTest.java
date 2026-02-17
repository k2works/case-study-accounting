package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.ConfirmJournalEntryCommand;
import com.example.accounting.application.port.out.ConfirmJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.AccountId;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仕訳確定サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳確定サービス")
class ConfirmJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ConfirmJournalEntryService confirmJournalEntryService;

    @BeforeEach
    void setUp() {
        confirmJournalEntryService = new ConfirmJournalEntryService(journalEntryRepository);
    }

    @Nested
    @DisplayName("確定成功")
    class SuccessfulConfirmation {

        @Test
        @DisplayName("承認済み仕訳を確定できる")
        void shouldConfirmApprovedJournalEntry() {
            ConfirmJournalEntryCommand command = new ConfirmJournalEntryCommand(10, "confirmer-1");
            JournalEntry existingEntry = approvedEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Try.success(Optional.of(existingEntry)));
            when(journalEntryRepository.save(any(JournalEntry.class)))
                    .thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            ConfirmJournalEntryResult result = confirmJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.status()).isEqualTo(JournalEntryStatus.CONFIRMED.name());
            assertThat(result.confirmedBy()).isEqualTo("confirmer-1");
            assertThat(result.confirmedAt()).isNotNull();
            assertThat(result.message()).isEqualTo("仕訳を確定しました");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry savedEntry = captor.getValue();

            assertThat(savedEntry.getStatus()).isEqualTo(JournalEntryStatus.CONFIRMED);
            assertThat(savedEntry.getConfirmedBy()).isEqualTo(UserId.of("confirmer-1"));
            assertThat(savedEntry.getConfirmedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("確定失敗")
    class FailedConfirmation {

        @Test
        @DisplayName("仕訳が存在しない場合はエラー")
        void shouldFailWhenJournalEntryNotFound() {
            ConfirmJournalEntryCommand command = new ConfirmJournalEntryCommand(99, "confirmer-1");

            when(journalEntryRepository.findById(JournalEntryId.of(99)))
                    .thenReturn(Try.success(Optional.empty()));

            ConfirmJournalEntryResult result = confirmJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("承認済み以外のステータスでは確定できない")
        void shouldFailWhenStatusIsNotApproved() {
            ConfirmJournalEntryCommand command = new ConfirmJournalEntryCommand(10, "confirmer-1");
            JournalEntry existingEntry = pendingEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Try.success(Optional.of(existingEntry)));

            ConfirmJournalEntryResult result = confirmJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("承認済み状態の仕訳のみ確定可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }
    }

    private JournalEntry approvedEntry() {
        return JournalEntry.reconstruct(
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
    }

    private JournalEntry pendingEntry() {
        return JournalEntry.reconstruct(
                JournalEntryId.of(10),
                LocalDate.of(2024, 1, 31),
                "売上計上",
                JournalEntryStatus.PENDING,
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
    }
}
