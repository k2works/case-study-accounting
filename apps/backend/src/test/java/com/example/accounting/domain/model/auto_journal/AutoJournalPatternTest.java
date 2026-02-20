package com.example.accounting.domain.model.auto_journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("自動仕訳パターン")
class AutoJournalPatternTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("有効なパラメータでパターンを生成できる")
        void shouldCreatePatternWithValidParams() {
            AutoJournalPattern pattern = AutoJournalPattern.create(
                    "AP001", "売上計上", "sales", "売上データから自動仕訳を生成"
            );

            assertThat(pattern.getId()).isNull();
            assertThat(pattern.getPatternCode()).isEqualTo("AP001");
            assertThat(pattern.getPatternName()).isEqualTo("売上計上");
            assertThat(pattern.getSourceTableName()).isEqualTo("sales");
            assertThat(pattern.getDescription()).isEqualTo("売上データから自動仕訳を生成");
            assertThat(pattern.getIsActive()).isTrue();
            assertThat(pattern.getItems()).isEmpty();
        }

        @Test
        @DisplayName("description が null でも生成できる")
        void shouldCreatePatternWithNullDescription() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);

            assertThat(pattern.getDescription()).isNull();
        }

        @Test
        @DisplayName("patternCode が null の場合は例外をスローする")
        void shouldThrowWhenPatternCodeIsNull() {
            assertThatThrownBy(() -> AutoJournalPattern.create(null, "売上計上", "sales", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("パターンコードは必須です");
        }

        @Test
        @DisplayName("patternName が null の場合は例外をスローする")
        void shouldThrowWhenPatternNameIsNull() {
            assertThatThrownBy(() -> AutoJournalPattern.create("AP001", null, "sales", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("パターン名は必須です");
        }

        @Test
        @DisplayName("sourceTableName が null の場合は例外をスローする")
        void shouldThrowWhenSourceTableNameIsNull() {
            assertThatThrownBy(() -> AutoJournalPattern.create("AP001", "売上計上", null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("ソーステーブル名は必須です");
        }
    }

    @Nested
    @DisplayName("reconstruct")
    class Reconstruct {

        @Test
        @DisplayName("全フィールドを指定して再構築できる")
        void shouldReconstructWithAllFields() {
            AutoJournalPatternId id = AutoJournalPatternId.of(1L);
            AutoJournalPatternItem item = AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上");
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    id, "AP001", "売上計上", "sales", "説明", true, List.of(item)
            );

            assertThat(pattern.getId()).isEqualTo(id);
            assertThat(pattern.getPatternCode()).isEqualTo("AP001");
            assertThat(pattern.getPatternName()).isEqualTo("売上計上");
            assertThat(pattern.getSourceTableName()).isEqualTo("sales");
            assertThat(pattern.getDescription()).isEqualTo("説明");
            assertThat(pattern.getIsActive()).isTrue();
            assertThat(pattern.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("items が null の場合は空リストになる")
        void shouldHandleNullItems() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, true, null
            );

            assertThat(pattern.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("明細を追加できる")
        void shouldAddItem() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);
            AutoJournalPatternItem item = AutoJournalPatternItem.create(1, "D", "1100", "amount", "売上");

            AutoJournalPattern updated = pattern.addItem(item);

            assertThat(updated.getItems()).hasSize(1);
            assertThat(updated.getItems().get(0).getLineNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("複数の明細を追加できる")
        void shouldAddMultipleItems() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);
            AutoJournalPatternItem debit = AutoJournalPatternItem.create(1, "D", "1100", "amount", "売上");
            AutoJournalPatternItem credit = AutoJournalPatternItem.create(2, "C", "4000", "amount", "売上");

            AutoJournalPattern updated = pattern.addItem(debit).addItem(credit);

            assertThat(updated.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("null の明細は追加できない")
        void shouldThrowWhenItemIsNull() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);

            assertThatThrownBy(() -> pattern.addItem(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("明細は必須です");
        }
    }

    @Nested
    @DisplayName("activate / deactivate")
    class ActivateDeactivate {

        @Test
        @DisplayName("activate で有効にできる")
        void shouldActivate() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", null, false, List.of()
            );

            AutoJournalPattern activated = pattern.activate();

            assertThat(activated.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("deactivate で無効にできる")
        void shouldDeactivate() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);

            AutoJournalPattern deactivated = pattern.deactivate();

            assertThat(deactivated.getIsActive()).isFalse();
        }
    }
}
