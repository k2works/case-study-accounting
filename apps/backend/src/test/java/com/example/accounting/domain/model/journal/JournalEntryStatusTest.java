package com.example.accounting.domain.model.journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JournalEntryStatus")
class JournalEntryStatusTest {

    @Test
    @DisplayName("各ステータスの表示名を取得できる")
    void shouldGetDisplayName() {
        assertThat(JournalEntryStatus.DRAFT.getDisplayName()).isEqualTo("下書き");
        assertThat(JournalEntryStatus.PENDING.getDisplayName()).isEqualTo("承認待ち");
        assertThat(JournalEntryStatus.APPROVED.getDisplayName()).isEqualTo("承認済み");
        assertThat(JournalEntryStatus.CONFIRMED.getDisplayName()).isEqualTo("確定済み");
    }

    @Test
    @DisplayName("コードからステータスを取得できる")
    void shouldGetFromCode() {
        assertThat(JournalEntryStatus.fromCode("DRAFT")).isEqualTo(JournalEntryStatus.DRAFT);
        assertThat(JournalEntryStatus.fromCode("PENDING")).isEqualTo(JournalEntryStatus.PENDING);
        assertThat(JournalEntryStatus.fromCode("APPROVED")).isEqualTo(JournalEntryStatus.APPROVED);
        assertThat(JournalEntryStatus.fromCode("CONFIRMED")).isEqualTo(JournalEntryStatus.CONFIRMED);
    }

    @Test
    @DisplayName("無効なコードの場合は例外をスローする")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> JournalEntryStatus.fromCode("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仕訳ステータス");
    }
}
