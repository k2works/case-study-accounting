package com.example.accounting.domain.model.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountId")
class AccountIdTest {

    @Test
    @DisplayName("generate で一意な ID が生成される")
    void shouldGenerateUniqueId() {
        AccountId first = AccountId.generate();
        AccountId second = AccountId.generate();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("値オブジェクトとして正しく動作する")
    void shouldBehaveAsValueObject() {
        AccountId first = AccountId.of(100);
        AccountId second = AccountId.of(100);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first.value()).isEqualTo(100);
    }
}
