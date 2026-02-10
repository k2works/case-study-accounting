package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.SubmitForApprovalCommand;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.SubmitForApprovalResult;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.account.AccountId;
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
 * 仕訳承認申請サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳承認申請サービス")
class SubmitForApprovalServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private SubmitForApprovalService submitForApprovalService;

    @BeforeEach
    void setUp() {
        submitForApprovalService = new SubmitForApprovalService(journalEntryRepository);
    }

    @Nested
    @DisplayName("申請成功")
    class SuccessfulSubmit {

        @Test
        @DisplayName("下書き仕訳を承認申請できる")
        void shouldSubmitDraftJournalEntry() {
            SubmitForApprovalCommand command = new SubmitForApprovalCommand(10);
            JournalEntry existingEntry = draftEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Optional.of(existingEntry));
            when(journalEntryRepository.save(any(JournalEntry.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            SubmitForApprovalResult result = submitForApprovalService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.status()).isEqualTo(JournalEntryStatus.PENDING.name());
            assertThat(result.message()).isEqualTo("仕訳を承認申請しました");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry savedEntry = captor.getValue();

            assertThat(savedEntry.getStatus()).isEqualTo(JournalEntryStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("申請失敗")
    class FailedSubmit {

        @Test
        @DisplayName("仕訳が存在しない場合はエラー")
        void shouldFailWhenJournalEntryNotFound() {
            SubmitForApprovalCommand command = new SubmitForApprovalCommand(99);

            when(journalEntryRepository.findById(JournalEntryId.of(99)))
                    .thenReturn(Optional.empty());

            SubmitForApprovalResult result = submitForApprovalService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("下書き以外のステータスの場合はエラー")
        void shouldFailWhenStatusIsNotDraft() {
            SubmitForApprovalCommand command = new SubmitForApprovalCommand(10);
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
                    LocalDateTime.of(2024, 1, 31, 10, 0),
                    LocalDateTime.of(2024, 1, 31, 10, 0)
            );

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Optional.of(existingEntry));

            SubmitForApprovalResult result = submitForApprovalService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("下書き状態の仕訳のみ承認申請可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }
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
                LocalDateTime.of(2024, 1, 31, 10, 0),
                LocalDateTime.of(2024, 1, 31, 10, 0)
        );
    }
}
