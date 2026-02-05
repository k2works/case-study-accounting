package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.in.DeleteAccountResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeleteAccountResponse")
class DeleteAccountResponseTest {

    @Test
    @DisplayName("成功結果からレスポンスを生成できる")
    void shouldCreateFromSuccessResult() {
        DeleteAccountResult result = DeleteAccountResult.success(1);

        DeleteAccountResponse response = DeleteAccountResponse.from(result);

        assertThat(response.success()).isTrue();
        assertThat(response.accountId()).isEqualTo(1);
        assertThat(response.message()).isEqualTo("勘定科目を削除しました");
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    @DisplayName("失敗結果からレスポンスを生成できる")
    void shouldCreateFromFailureResult() {
        DeleteAccountResult result = DeleteAccountResult.failure("勘定科目が見つかりません");

        DeleteAccountResponse response = DeleteAccountResponse.from(result);

        assertThat(response.success()).isFalse();
        assertThat(response.accountId()).isNull();
        assertThat(response.message()).isNull();
        assertThat(response.errorMessage()).isEqualTo("勘定科目が見つかりません");
    }
}
