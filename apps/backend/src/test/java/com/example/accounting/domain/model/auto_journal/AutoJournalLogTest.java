package com.example.accounting.domain.model.auto_journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("自動仕訳実行ログ")
class AutoJournalLogTest {

    @Test
    @DisplayName("createSuccess で成功ログを生成できる")
    void shouldCreateSuccessLog() {
        AutoJournalLog log = AutoJournalLog.createSuccess(1L, "仕訳生成成功");

        assertThat(log.getId()).isNull();
        assertThat(log.getPatternId()).isEqualTo(1L);
        assertThat(log.getExecutedAt()).isNotNull();
        assertThat(log.getProcessedCount()).isEqualTo(1);
        assertThat(log.getGeneratedCount()).isEqualTo(1);
        assertThat(log.getStatus()).isEqualTo("SUCCESS");
        assertThat(log.getMessage()).isEqualTo("仕訳生成成功");
        assertThat(log.getErrorDetail()).isNull();
    }

    @Test
    @DisplayName("createFailure で失敗ログを生成できる")
    void shouldCreateFailureLog() {
        AutoJournalLog log = AutoJournalLog.createFailure(1L, "仕訳生成失敗", "validation error");

        assertThat(log.getId()).isNull();
        assertThat(log.getPatternId()).isEqualTo(1L);
        assertThat(log.getExecutedAt()).isNotNull();
        assertThat(log.getProcessedCount()).isEqualTo(1);
        assertThat(log.getGeneratedCount()).isEqualTo(0);
        assertThat(log.getStatus()).isEqualTo("FAILED");
        assertThat(log.getMessage()).isEqualTo("仕訳生成失敗");
        assertThat(log.getErrorDetail()).isEqualTo("validation error");
    }

    @Test
    @DisplayName("patternId が null の場合は NullPointerException")
    void shouldThrowWhenPatternIdIsNull() {
        assertThatThrownBy(() -> AutoJournalLog.createSuccess(null, "msg"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("パターンIDは必須です");

        assertThatThrownBy(() -> AutoJournalLog.createFailure(null, "msg", "err"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("パターンIDは必須です");
    }

    @Test
    @DisplayName("reconstruct で全フィールドを復元できる")
    void shouldReconstructAllFields() {
        OffsetDateTime executedAt = OffsetDateTime.parse("2024-01-31T10:15:30+09:00");

        AutoJournalLog log = AutoJournalLog.reconstruct(
                10L,
                2L,
                executedAt,
                3,
                2,
                "PARTIAL",
                "一部失敗",
                "detail"
        );

        assertThat(log.getId()).isEqualTo(10L);
        assertThat(log.getPatternId()).isEqualTo(2L);
        assertThat(log.getExecutedAt()).isEqualTo(executedAt);
        assertThat(log.getProcessedCount()).isEqualTo(3);
        assertThat(log.getGeneratedCount()).isEqualTo(2);
        assertThat(log.getStatus()).isEqualTo("PARTIAL");
        assertThat(log.getMessage()).isEqualTo("一部失敗");
        assertThat(log.getErrorDetail()).isEqualTo("detail");
    }
}
