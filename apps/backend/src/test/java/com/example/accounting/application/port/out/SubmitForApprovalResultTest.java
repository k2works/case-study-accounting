package com.example.accounting.application.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubmitForApprovalResult")
class SubmitForApprovalResultTest {

    @Nested
    @DisplayName("ファクトリメソッド")
    class FactoryMethods {

        @Test
        @DisplayName("success メソッドで成功結果を作成できる")
        void shouldCreateSuccessResult() {
            SubmitForApprovalResult result = SubmitForApprovalResult.success(1, "PENDING");

            assertThat(result.success()).isTrue();
            assertThat(result.journalEntryId()).isEqualTo(1);
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.message()).isEqualTo("仕訳を承認申請しました");
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("failure メソッドで失敗結果を作成できる")
        void shouldCreateFailureResult() {
            SubmitForApprovalResult result = SubmitForApprovalResult.failure("エラーメッセージ");

            assertThat(result.success()).isFalse();
            assertThat(result.journalEntryId()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.message()).isNull();
            assertThat(result.errorMessage()).isEqualTo("エラーメッセージ");
        }
    }
}
