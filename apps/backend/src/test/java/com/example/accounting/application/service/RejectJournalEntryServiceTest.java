package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.RejectJournalEntryCommand;
import com.example.accounting.application.port.out.RejectJournalEntryResult;
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
 * 仕訳差し戻しサービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳差し戻しサービス")
class RejectJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private RejectJournalEntryService rejectJournalEntryService;

    @BeforeEach
    void setUp() {
        rejectJournalEntryService = new RejectJournalEntryService(journalEntryRepository);
    }

    @Nested
    @DisplayName("差し戻し成功")
    class SuccessfulRejection {

        @Test
        @DisplayName("承認待ち仕訳を差し戻しできる")
        void shouldRejectPendingJournalEntry() {
            RejectJournalEntryCommand command = new RejectJournalEntryCommand(10, "manager-1", "金額に誤りがあります");
            JournalEntry existingEntry = pendingEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Try.success(Optional.of(existingEntry)));
            when(journalEntryRepository.save(any(JournalEntry.class)))
                    .thenAnswer(invocation -> Try.success(invocation.getArgument(0)));

            RejectJournalEntryResult result = rejectJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.status()).isEqualTo(JournalEntryStatus.DRAFT.name());
            assertThat(result.rejectedBy()).isEqualTo("manager-1");
            assertThat(result.rejectedAt()).isNotNull();
            assertThat(result.rejectionReason()).isEqualTo("金額に誤りがあります");
            assertThat(result.message()).isEqualTo("仕訳を差し戻しました");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry savedEntry = captor.getValue();

            assertThat(savedEntry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
            assertThat(savedEntry.getRejectedBy()).isEqualTo(UserId.of("manager-1"));
            assertThat(savedEntry.getRejectedAt()).isNotNull();
            assertThat(savedEntry.getRejectionReason()).isEqualTo("金額に誤りがあります");
        }
    }

    @Nested
    @DisplayName("差し戻し失敗")
    class FailedRejection {

        @Test
        @DisplayName("仕訳が存在しない場合はエラー")
        void shouldFailWhenJournalEntryNotFound() {
            RejectJournalEntryCommand command = new RejectJournalEntryCommand(99, "manager-1", "理由");

            when(journalEntryRepository.findById(JournalEntryId.of(99)))
                    .thenReturn(Try.success(Optional.empty()));

            RejectJournalEntryResult result = rejectJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("下書き状態では差し戻しできない")
        void shouldFailWhenStatusIsDraft() {
            RejectJournalEntryCommand command = new RejectJournalEntryCommand(10, "manager-1", "理由");
            JournalEntry existingEntry = draftEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Try.success(Optional.of(existingEntry)));

            RejectJournalEntryResult result = rejectJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("承認待ち状態の仕訳のみ差し戻し可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("承認済み状態では差し戻しできない")
        void shouldFailWhenStatusIsApproved() {
            RejectJournalEntryCommand command = new RejectJournalEntryCommand(10, "manager-1", "理由");
            JournalEntry existingEntry = approvedEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Try.success(Optional.of(existingEntry)));

            RejectJournalEntryResult result = rejectJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("承認待ち状態の仕訳のみ差し戻し可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }
    }

    @Nested
    @DisplayName("コマンドバリデーション")
    class CommandValidation {

        @Test
        @DisplayName("仕訳IDがnullの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenJournalEntryIdIsNull() {
            assertThat(RejectJournalEntryCommand.of(null, "manager-1", "理由").getLeft())
                    .isEqualTo("仕訳IDは必須です");
        }

        @Test
        @DisplayName("差し戻し者IDがnullの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenRejectorIdIsNull() {
            assertThat(RejectJournalEntryCommand.of(1, null, "理由").getLeft())
                    .isEqualTo("差し戻し者IDは必須です");
        }

        @Test
        @DisplayName("差し戻し者IDが空白の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenRejectorIdIsBlank() {
            assertThat(RejectJournalEntryCommand.of(1, "  ", "理由").getLeft())
                    .isEqualTo("差し戻し者IDは必須です");
        }

        @Test
        @DisplayName("差し戻し理由がnullの場合はバリデーションエラーになる")
        void shouldReturnLeftWhenReasonIsNull() {
            assertThat(RejectJournalEntryCommand.of(1, "manager-1", null).getLeft())
                    .isEqualTo("差し戻し理由は必須です");
        }

        @Test
        @DisplayName("差し戻し理由が空白の場合はバリデーションエラーになる")
        void shouldReturnLeftWhenReasonIsBlank() {
            assertThat(RejectJournalEntryCommand.of(1, "manager-1", "  ").getLeft())
                    .isEqualTo("差し戻し理由は必須です");
        }
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
                null,  // rejectedBy
                null,  // rejectedAt
                null,  // rejectionReason
                null,
                null,
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }

    private JournalEntry draftEntry() {
        return JournalEntry.reconstruct(
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
                null,  // rejectedBy
                null,  // rejectedAt
                null,  // rejectionReason
                null,
                null,
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
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
                null,  // rejectedBy
                null,  // rejectedAt
                null,  // rejectionReason
                null,
                null,
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }
}
