package com.example.accounting.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenerateAutoJournalResult")
class GenerateAutoJournalResultTest {

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethod {

        @Test
        @DisplayName("success() は成功結果を返す")
        void shouldCreateSuccessResult() {
            GenerateAutoJournalResult result = GenerateAutoJournalResult.success(
                    100,
                    LocalDate.of(2026, 2, 21),
                    "月次自動仕訳",
                    "APPROVED"
            );

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(100);
            assertThat(result.journalDate()).isEqualTo(LocalDate.of(2026, 2, 21));
            assertThat(result.description()).isEqualTo("月次自動仕訳");
            assertThat(result.status()).isEqualTo("APPROVED");
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("failure() は失敗結果を返す")
        void shouldCreateFailureResult() {
            GenerateAutoJournalResult result = GenerateAutoJournalResult.failure("仕訳パターンが見つかりません");

            assertThat(result.success()).isFalse();
            assertThat(result.journalEntryId()).isNull();
            assertThat(result.journalDate()).isNull();
            assertThat(result.description()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.errorMessage()).isEqualTo("仕訳パターンが見つかりません");
        }
    }
}
