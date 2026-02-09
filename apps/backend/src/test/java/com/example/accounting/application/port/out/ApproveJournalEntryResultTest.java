package com.example.accounting.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApproveJournalEntryResult")
class ApproveJournalEntryResultTest {

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethods {

        @Test
        @DisplayName("success メソッドで成功結果を作成できる")
        void shouldCreateSuccessResult() {
            LocalDateTime approvedAt = LocalDateTime.of(2024, 2, 15, 10, 30);
            ApproveJournalEntryResult result = ApproveJournalEntryResult.success(
                    1, "APPROVED", "manager", approvedAt);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(1);
            assertThat(result.status()).isEqualTo("APPROVED");
            assertThat(result.approvedBy()).isEqualTo("manager");
            assertThat(result.approvedAt()).isEqualTo(approvedAt);
            assertThat(result.message()).isEqualTo("仕訳を承認しました");
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("failure メソッドで失敗結果を作成できる")
        void shouldCreateFailureResult() {
            ApproveJournalEntryResult result = ApproveJournalEntryResult.failure("エラーメッセージ");

            assertThat(result.success()).isFalse();
            assertThat(result.journalEntryId()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.approvedBy()).isNull();
            assertThat(result.approvedAt()).isNull();
            assertThat(result.message()).isNull();
            assertThat(result.errorMessage()).isEqualTo("エラーメッセージ");
        }
    }
}
