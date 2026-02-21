package com.example.accounting.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfirmJournalEntryResult")
class ConfirmJournalEntryResultTest {

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethods {

        @Test
        @DisplayName("success メソッドで成功結果を作成できる")
        void shouldCreateSuccessResult() {
            LocalDateTime confirmedAt = LocalDateTime.of(2024, 2, 15, 10, 30);
            ConfirmJournalEntryResult result = ConfirmJournalEntryResult.success(
                    1, "CONFIRMED", "manager", confirmedAt);

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(1);
            assertThat(result.status()).isEqualTo("CONFIRMED");
            assertThat(result.confirmedBy()).isEqualTo("manager");
            assertThat(result.confirmedAt()).isEqualTo(confirmedAt);
            assertThat(result.message()).isEqualTo("仕訳を確定しました");
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("failure メソッドで失敗結果を作成できる")
        void shouldCreateFailureResult() {
            ConfirmJournalEntryResult result = ConfirmJournalEntryResult.failure("エラーメッセージ");

            assertThat(result.success()).isFalse();
            assertThat(result.journalEntryId()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.confirmedBy()).isNull();
            assertThat(result.confirmedAt()).isNull();
            assertThat(result.message()).isNull();
            assertThat(result.errorMessage()).isEqualTo("エラーメッセージ");
        }
    }
}
