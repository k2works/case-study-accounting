package com.example.accounting.domain.model.auto_journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("自動仕訳パターン ID")
class AutoJournalPatternIdTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("有効な値で ID を生成できる")
        void shouldCreateIdWithValidValue() {
            AutoJournalPatternId id = AutoJournalPatternId.of(1L);

            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null の場合は例外をスローする")
        void shouldThrowWhenValueIsNull() {
            assertThatThrownBy(() -> AutoJournalPatternId.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("自動仕訳パターンIDは必須です");
        }
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("同じ値の ID は等しい")
        void shouldBeEqualWithSameValue() {
            AutoJournalPatternId id1 = AutoJournalPatternId.of(1L);
            AutoJournalPatternId id2 = AutoJournalPatternId.of(1L);

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("異なる値の ID は等しくない")
        void shouldNotBeEqualWithDifferentValue() {
            AutoJournalPatternId id1 = AutoJournalPatternId.of(1L);
            AutoJournalPatternId id2 = AutoJournalPatternId.of(2L);

            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
