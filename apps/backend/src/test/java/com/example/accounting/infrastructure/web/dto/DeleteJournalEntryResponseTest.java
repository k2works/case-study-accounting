package com.example.accounting.infrastructure.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeleteJournalEntryResponse")
class DeleteJournalEntryResponseTest {

    @Test
    @DisplayName("成功レスポンスを生成できる")
    void shouldCreateSuccessResponse() {
        DeleteJournalEntryResponse response = DeleteJournalEntryResponse.success("仕訳を削除しました");

        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("仕訳を削除しました");
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    @DisplayName("失敗レスポンスを生成できる")
    void shouldCreateFailureResponse() {
        DeleteJournalEntryResponse response = DeleteJournalEntryResponse.failure("仕訳が見つかりません");

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isNull();
        assertThat(response.errorMessage()).isEqualTo("仕訳が見つかりません");
    }
}
