package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("自動仕訳パターン明細エンティティ")
class AutoJournalPatternItemEntityTest {

    @Nested
    @DisplayName("fromDomain")
    class FromDomain {

        @Test
        @DisplayName("ドメインモデルからエンティティを生成できる")
        void shouldCreateEntityFromDomain() {
            AutoJournalPatternItem item = AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上");

            AutoJournalPatternItemEntity entity = AutoJournalPatternItemEntity.fromDomain(item, 1L);

            assertThat(entity.getPatternId()).isEqualTo(1L);
            assertThat(entity.getLineNumber()).isEqualTo(1);
            assertThat(entity.getDebitCreditType()).isEqualTo("D");
            assertThat(entity.getAccountCode()).isEqualTo("1100");
            assertThat(entity.getAmountFormula()).isEqualTo("amount");
            assertThat(entity.getDescriptionTemplate()).isEqualTo("売上");
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("エンティティからドメインモデルを再構築できる")
        void shouldConvertToDomain() {
            AutoJournalPatternItemEntity entity = new AutoJournalPatternItemEntity();
            entity.setId(10L);
            entity.setPatternId(1L);
            entity.setLineNumber(1);
            entity.setDebitCreditType("D");
            entity.setAccountCode("1100");
            entity.setAmountFormula("amount");
            entity.setDescriptionTemplate("売上");

            AutoJournalPatternItem item = entity.toDomain();

            assertThat(item.getLineNumber()).isEqualTo(1);
            assertThat(item.getDebitCreditType()).isEqualTo("D");
            assertThat(item.getAccountCode()).isEqualTo("1100");
            assertThat(item.getAmountFormula()).isEqualTo("amount");
            assertThat(item.getDescriptionTemplate()).isEqualTo("売上");
        }
    }

    @Nested
    @DisplayName("getter / setter")
    class GetterSetter {

        @Test
        @DisplayName("全フィールドの getter/setter が機能する")
        void shouldGetAndSetAllFields() {
            AutoJournalPatternItemEntity entity = new AutoJournalPatternItemEntity();
            entity.setId(10L);
            entity.setPatternId(1L);
            entity.setLineNumber(1);
            entity.setDebitCreditType("D");
            entity.setAccountCode("1100");
            entity.setAmountFormula("amount");
            entity.setDescriptionTemplate("売上");

            assertThat(entity.getId()).isEqualTo(10L);
            assertThat(entity.getPatternId()).isEqualTo(1L);
            assertThat(entity.getLineNumber()).isEqualTo(1);
            assertThat(entity.getDebitCreditType()).isEqualTo("D");
            assertThat(entity.getAccountCode()).isEqualTo("1100");
            assertThat(entity.getAmountFormula()).isEqualTo("amount");
            assertThat(entity.getDescriptionTemplate()).isEqualTo("売上");
        }
    }
}
