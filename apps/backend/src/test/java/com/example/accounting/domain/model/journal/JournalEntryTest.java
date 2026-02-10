package com.example.accounting.domain.model.journal;

import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JournalEntry エンティティ")
class JournalEntryTest {

    private static final LocalDate JOURNAL_DATE = LocalDate.of(2024, 1, 31);
    private static final UserId CREATED_BY = UserId.of("user-1");

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("create で下書きの仕訳を作成できる")
        void shouldCreateJournalEntry() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

            JournalEntry entry = JournalEntry.create(
                    JOURNAL_DATE,
                    "売上計上",
                    CREATED_BY,
                    0,
                    () -> now
            );

            assertThat(entry.getId()).isNull();
            assertThat(entry.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(entry.getDescription()).isEqualTo("売上計上");
            assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
            assertThat(entry.getLines()).isEmpty();
            assertThat(entry.getCreatedBy()).isEqualTo(CREATED_BY);
            assertThat(entry.getCreatedAt()).isEqualTo(now);
            assertThat(entry.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("仕訳日が null の場合は例外をスローする")
        void shouldThrowExceptionWhenJournalDateIsNull() {
            assertThatThrownBy(() -> JournalEntry.create(null, "摘要", CREATED_BY, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("仕訳日は必須");
        }

        @Test
        @DisplayName("摘要が null の場合は例外をスローする")
        void shouldThrowExceptionWhenDescriptionIsNull() {
            assertThatThrownBy(() -> JournalEntry.create(JOURNAL_DATE, null, CREATED_BY, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("摘要は必須");
        }

        @Test
        @DisplayName("作成者が null の場合は例外をスローする")
        void shouldThrowExceptionWhenCreatedByIsNull() {
            assertThatThrownBy(() -> JournalEntry.create(JOURNAL_DATE, "摘要", null, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("作成者は必須");
        }

        @Test
        @DisplayName("時刻取得関数が null の場合は例外をスローする")
        void shouldThrowExceptionWhenClockSupplierIsNull() {
            assertThatThrownBy(() -> JournalEntry.create(JOURNAL_DATE, "摘要", CREATED_BY, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("時刻取得関数は必須");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("下書き状態の仕訳を更新できる")
        void shouldUpdateDraftJournalEntry() {
            LocalDate newJournalDate = LocalDate.of(2024, 2, 1);
            List<JournalEntryLine> newLines = List.of(debitLine(1), creditLine(2));
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "旧摘要", CREATED_BY, 0);

            JournalEntry updated = entry.update(newJournalDate, "更新後", newLines);

            assertThat(updated.getJournalDate()).isEqualTo(newJournalDate);
            assertThat(updated.getDescription()).isEqualTo("更新後");
            assertThat(updated.getLines()).hasSize(2);
            assertThat(updated.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
            assertThat(entry.getJournalDate()).isEqualTo(JOURNAL_DATE);
            assertThat(entry.getDescription()).isEqualTo("旧摘要");
        }

        @Test
        @DisplayName("仕訳日を変更できる")
        void shouldUpdateJournalDate() {
            LocalDate newJournalDate = LocalDate.of(2024, 2, 1);
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "摘要", CREATED_BY, 0);

            JournalEntry updated = entry.update(newJournalDate, "摘要", List.of());

            assertThat(updated.getJournalDate()).isEqualTo(newJournalDate);
            assertThat(updated.getDescription()).isEqualTo("摘要");
        }

        @Test
        @DisplayName("摘要を変更できる")
        void shouldUpdateDescription() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "旧摘要", CREATED_BY, 0);

            JournalEntry updated = entry.update(JOURNAL_DATE, "新摘要", List.of());

            assertThat(updated.getDescription()).isEqualTo("新摘要");
            assertThat(updated.getJournalDate()).isEqualTo(JOURNAL_DATE);
        }

        @Test
        @DisplayName("明細行を変更できる")
        void shouldUpdateLines() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "明細変更", CREATED_BY, 0)
                    .addLine(debitLine(1))
                    .addLine(creditLine(2));
            List<JournalEntryLine> newLines = List.of(debitLine(1), creditLine(2, new BigDecimal("2000")));

            JournalEntry updated = entry.update(JOURNAL_DATE, "明細変更", newLines);

            assertThat(updated.getLines()).isEqualTo(newLines);
            assertThat(entry.getLines()).hasSize(2);
        }

        @Test
        @DisplayName("更新日時が更新される")
        void shouldUpdateUpdatedAt() {
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 9, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 9, 0, 0);
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "更新", CREATED_BY, 0, () -> createdAt);

            JournalEntry updated = entry.update(JOURNAL_DATE, "更新", List.of(), () -> updatedAt);

            assertThat(updated.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("下書き以外のステータスでは更新できない")
        void shouldThrowExceptionWhenStatusIsNotDraft() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "確定済み",
                    JournalEntryStatus.APPROVED,
                    1,
                    List.of(),
                    CREATED_BY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0)
            );

            assertThatThrownBy(() -> entry.update(JOURNAL_DATE, "更新", List.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("下書き状態");
        }

        @Test
        @DisplayName("仕訳日が null の場合は例外をスローする")
        void shouldThrowExceptionWhenJournalDateIsNull() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "摘要", CREATED_BY, 0);

            assertThatThrownBy(() -> entry.update(null, "摘要", List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("仕訳日は必須");
        }

        @Test
        @DisplayName("摘要が null の場合は例外をスローする")
        void shouldThrowExceptionWhenDescriptionIsNull() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "摘要", CREATED_BY, 0);

            assertThatThrownBy(() -> entry.update(JOURNAL_DATE, null, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("摘要は必須");
        }
    }

    @Test
    @DisplayName("reconstruct で DB から復元できる")
    void shouldReconstructFromDb() {
        JournalEntryId id = JournalEntryId.of(1);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 9, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 9, 0, 0);
        List<JournalEntryLine> lines = List.of(debitLine(1), creditLine(2));

        JournalEntry entry = JournalEntry.reconstruct(
                id,
                JOURNAL_DATE,
                "仕訳復元",
                JournalEntryStatus.APPROVED,
                3,
                lines,
                CREATED_BY,
                UserId.of("approver-1"),
                LocalDateTime.of(2024, 1, 2, 12, 0, 0),
                null,
                null,
                null,
                createdAt,
                updatedAt
        );

        assertThat(entry.getId()).isEqualTo(id);
        assertThat(entry.getLines()).hasSize(2);
        assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.APPROVED);
        assertThat(entry.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entry.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("addLine で明細行を追加できる")
    void shouldAddLine() {
        JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "追加", CREATED_BY, 0);

        JournalEntry updated = entry.addLine(debitLine(1));

        assertThat(entry.getLines()).isEmpty();
        assertThat(updated.getLines()).hasSize(1);
        assertThat(updated.getLines().get(0).lineNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("addLine に null を渡すと例外をスローする")
    void shouldThrowExceptionWhenAddLineIsNull() {
        JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "追加", CREATED_BY, 0);

        assertThatThrownBy(() -> entry.addLine(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("明細行は必須");
    }

    @Test
    @DisplayName("removeLine で指定行を削除できる")
    void shouldRemoveLine() {
        JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "削除", CREATED_BY, 0)
                .addLine(debitLine(1))
                .addLine(creditLine(2));

        JournalEntry updated = entry.removeLine(1);

        assertThat(updated.getLines()).hasSize(1);
        assertThat(updated.getLines().get(0).lineNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("removeLine に null を渡すと例外をスローする")
    void shouldThrowExceptionWhenRemoveLineIsNull() {
        JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "削除", CREATED_BY, 0);

        assertThatThrownBy(() -> entry.removeLine(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("行番号は必須");
    }

    @Test
    @DisplayName("reconstruct に null の lines を渡すと例外をスローする")
    void shouldThrowExceptionWhenReconstructWithNullLines() {
        JournalEntryId id = JournalEntryId.of(1);
        LocalDateTime now = LocalDateTime.now();

        assertThatThrownBy(() -> JournalEntry.reconstruct(
                id, JOURNAL_DATE, "摘要", JournalEntryStatus.DRAFT,
                0, null, CREATED_BY, null, null, null, null, null, now, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("明細は必須");
    }

    @Test
    @DisplayName("借方合計と貸方合計を取得できる")
    void shouldCalculateTotals() {
        JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "合計", CREATED_BY, 0)
                .addLine(debitLine(1))
                .addLine(creditLine(2));

        assertThat(entry.totalDebitAmount().value()).isEqualByComparingTo("1000");
        assertThat(entry.totalCreditAmount().value()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("貸借一致の判定ができる")
    void shouldCheckBalance() {
        JournalEntry balanced = JournalEntry.create(JOURNAL_DATE, "一致", CREATED_BY, 0)
                .addLine(debitLine(1))
                .addLine(creditLine(2));
        JournalEntry unbalanced = JournalEntry.create(JOURNAL_DATE, "不一致", CREATED_BY, 0)
                .addLine(debitLine(1))
                .addLine(creditLine(2, new BigDecimal("500")));

        assertThat(balanced.isBalanced()).isTrue();
        assertThat(unbalanced.isBalanced()).isFalse();
    }

    @Nested
    @DisplayName("validateForSave")
    class ValidateForSave {

        @Test
        @DisplayName("貸借一致かつ 1 行以上なら保存可能")
        void shouldValidateForSave() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "保存", CREATED_BY, 0)
                    .addLine(debitLine(1))
                    .addLine(creditLine(2));

            assertThatCode(entry::validateForSave).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("明細が 0 行の場合は例外をスローする")
        void shouldThrowExceptionWhenNoLines() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "保存", CREATED_BY, 0);

            assertThatThrownBy(entry::validateForSave)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("明細は 1 行以上");
        }

        @Test
        @DisplayName("貸借不一致の場合は例外をスローする")
        void shouldThrowExceptionWhenNotBalanced() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "保存", CREATED_BY, 0)
                    .addLine(debitLine(1))
                    .addLine(creditLine(2, new BigDecimal("500")));

            assertThatThrownBy(entry::validateForSave)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("貸借一致");
        }
    }

    @Nested
    @DisplayName("submitForApproval")
    class SubmitForApproval {

        @Test
        @DisplayName("下書き状態の仕訳を承認申請できる")
        void shouldSubmitDraftJournalEntry() {
            JournalEntry entry = JournalEntry.create(JOURNAL_DATE, "承認申請", CREATED_BY, 0);

            JournalEntry submitted = entry.submitForApproval();

            assertThat(submitted.getStatus()).isEqualTo(JournalEntryStatus.PENDING);
            assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
        }

        @Test
        @DisplayName("下書き以外のステータスでは承認申請できない")
        void shouldThrowExceptionWhenStatusIsNotDraft() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "承認申請",
                    JournalEntryStatus.APPROVED,
                    1,
                    List.of(),
                    CREATED_BY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0)
            );

            assertThatThrownBy(entry::submitForApproval)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("下書き状態の仕訳のみ承認申請可能です");
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("承認待ち状態の仕訳を承認できる")
        void shouldApprovePendingJournalEntry() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "承認",
                    JournalEntryStatus.PENDING,
                    1,
                    List.of(),
                    CREATED_BY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0)
            );
            LocalDateTime approvedAt = LocalDateTime.of(2024, 2, 1, 10, 0, 0);

            JournalEntry approved = entry.approve(UserId.of("approver-1"), approvedAt);

            assertThat(approved.getStatus()).isEqualTo(JournalEntryStatus.APPROVED);
            assertThat(approved.getApprovedBy()).isEqualTo(UserId.of("approver-1"));
            assertThat(approved.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("承認待ち以外のステータスでは承認できない")
        void shouldThrowExceptionWhenStatusIsNotPending() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "承認",
                    JournalEntryStatus.DRAFT,
                    1,
                    List.of(),
                    CREATED_BY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0)
            );

            assertThatThrownBy(() -> entry.approve(UserId.of("approver-1"), LocalDateTime.now()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("承認待ち状態の仕訳のみ承認可能です");
        }

        @Test
        @DisplayName("承認者が null の場合は例外をスローする")
        void shouldThrowExceptionWhenApproverIsNull() {
            JournalEntry entry = JournalEntry.reconstruct(
                    JournalEntryId.of(1),
                    JOURNAL_DATE,
                    "承認",
                    JournalEntryStatus.PENDING,
                    1,
                    List.of(),
                    CREATED_BY,
                    null,
                    null,
                    null,  // rejectedBy
                    null,  // rejectedAt
                    null,  // rejectionReason
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0)
            );

            assertThatThrownBy(() -> entry.approve(null, LocalDateTime.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("承認者は必須です");
        }
    }

    private JournalEntryLine debitLine(int lineNumber) {
        return JournalEntryLine.of(
                lineNumber,
                AccountId.of(100),
                Money.of(new BigDecimal("1000")),
                null
        );
    }

    private JournalEntryLine creditLine(int lineNumber) {
        return creditLine(lineNumber, new BigDecimal("1000"));
    }

    private JournalEntryLine creditLine(int lineNumber, BigDecimal amount) {
        return JournalEntryLine.of(
                lineNumber,
                AccountId.of(200),
                null,
                Money.of(amount)
        );
    }
}
