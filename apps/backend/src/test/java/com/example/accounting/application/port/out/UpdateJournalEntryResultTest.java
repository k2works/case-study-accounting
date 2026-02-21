package com.example.accounting.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateJournalEntryResult")
class UpdateJournalEntryResultTest {

    @Test
    @DisplayName("success で成功結果を生成できる")
    void shouldCreateSuccessResult() {
        LocalDate journalDate = LocalDate.of(2024, 2, 1);

        UpdateJournalEntryResult result = UpdateJournalEntryResult.success(
                10,
                journalDate,
                "摘要",
                "DRAFT",
                2
        );

        assertThat(result.success()).isTrue();
        assertThat(result.journalEntryId()).isEqualTo(10);
        assertThat(result.journalDate()).isEqualTo(journalDate);
        assertThat(result.description()).isEqualTo("摘要");
        assertThat(result.status()).isEqualTo("DRAFT");
        assertThat(result.version()).isEqualTo(2);
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    @DisplayName("failure でエラー結果を生成できる")
    void shouldCreateFailureResult() {
        UpdateJournalEntryResult result = UpdateJournalEntryResult.failure("エラー");

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("エラー");
        assertThat(result.journalEntryId()).isNull();
        assertThat(result.journalDate()).isNull();
        assertThat(result.description()).isNull();
        assertThat(result.status()).isNull();
        assertThat(result.version()).isNull();
    }
}
