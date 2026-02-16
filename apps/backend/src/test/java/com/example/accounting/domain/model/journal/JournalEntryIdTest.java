package com.example.accounting.domain.model.journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryId")
class JournalEntryIdTest {

    @Test
    @DisplayName("generate で一意な ID が生成される")
    void shouldGenerateUniqueId() {
        JournalEntryId first = JournalEntryId.generate();
        JournalEntryId second = JournalEntryId.generate();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("値オブジェクトとして正しく動作する")
    void shouldBehaveAsValueObject() {
        JournalEntryId first = JournalEntryId.of(200);
        JournalEntryId second = JournalEntryId.of(200);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
        assertThat(first.value()).isEqualTo(200);
    }

    @Test
    @DisplayName("null の場合はエラーメッセージを返す")
    void shouldReturnLeftWhenValueIsNull() {
        assertThat(JournalEntryId.validated(null).getLeft()).isEqualTo("仕訳 ID は必須です");
    }
}
