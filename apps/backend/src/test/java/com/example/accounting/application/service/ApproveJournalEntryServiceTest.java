package com.example.accounting.application.service;

import com.example.accounting.application.port.in.command.ApproveJournalEntryCommand;
import com.example.accounting.application.port.out.ApproveJournalEntryResult;
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
 * 仕訳承認サービスのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仕訳承認サービス")
class ApproveJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ApproveJournalEntryService approveJournalEntryService;

    @BeforeEach
    void setUp() {
        approveJournalEntryService = new ApproveJournalEntryService(journalEntryRepository);
    }

    @Nested
    @DisplayName("承認成功")
    class SuccessfulApproval {

        @Test
        @DisplayName("承認待ち仕訳を承認できる")
        void shouldApprovePendingJournalEntry() {
            ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(10, "approver-1");
            JournalEntry existingEntry = pendingEntry();

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Optional.of(existingEntry));
            when(journalEntryRepository.save(any(JournalEntry.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ApproveJournalEntryResult result = approveJournalEntryService.execute(command);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(10);
            assertThat(result.status()).isEqualTo(JournalEntryStatus.APPROVED.name());
            assertThat(result.approvedBy()).isEqualTo("approver-1");
            assertThat(result.approvedAt()).isNotNull();
            assertThat(result.message()).isEqualTo("仕訳を承認しました");
            assertThat(result.errorMessage()).isNull();

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            verify(journalEntryRepository).save(captor.capture());
            JournalEntry savedEntry = captor.getValue();

            assertThat(savedEntry.getStatus()).isEqualTo(JournalEntryStatus.APPROVED);
            assertThat(savedEntry.getApprovedBy()).isEqualTo(UserId.of("approver-1"));
            assertThat(savedEntry.getApprovedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("承認失敗")
    class FailedApproval {

        @Test
        @DisplayName("仕訳が存在しない場合はエラー")
        void shouldFailWhenJournalEntryNotFound() {
            ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(99, "approver-1");

            when(journalEntryRepository.findById(JournalEntryId.of(99)))
                    .thenReturn(Optional.empty());

            ApproveJournalEntryResult result = approveJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("仕訳が見つかりません");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("下書き状態では承認できない")
        void shouldFailWhenStatusIsDraft() {
            ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(10, "approver-1");
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

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Optional.of(existingEntry));

            ApproveJournalEntryResult result = approveJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("承認待ち状態の仕訳のみ承認可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("承認済み状態では再承認できない")
        void shouldFailWhenStatusIsApproved() {
            ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(10, "approver-1");
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

            when(journalEntryRepository.findById(JournalEntryId.of(10)))
                    .thenReturn(Optional.of(existingEntry));

            ApproveJournalEntryResult result = approveJournalEntryService.execute(command);

            assertThat(result.success()).isFalse();
            assertThat(result.errorMessage()).isEqualTo("承認待ち状態の仕訳のみ承認可能です");
            verify(journalEntryRepository, never()).save(any(JournalEntry.class));
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
