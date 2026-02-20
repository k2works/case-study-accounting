package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("自動仕訳パターンエンティティ")
class AutoJournalPatternEntityTest {

    @Nested
    @DisplayName("fromDomain")
    class FromDomain {

        @Test
        @DisplayName("ドメインモデルからエンティティを生成できる")
        void shouldCreateEntityFromDomain() {
            AutoJournalPattern pattern = AutoJournalPattern.reconstruct(
                    AutoJournalPatternId.of(1L), "AP001", "売上計上", "sales", "説明", true,
                    List.of(AutoJournalPatternItem.reconstruct(1, "D", "1100", "amount", "売上"))
            );

            AutoJournalPatternEntity entity = AutoJournalPatternEntity.fromDomain(pattern);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getPatternCode()).isEqualTo("AP001");
            assertThat(entity.getPatternName()).isEqualTo("売上計上");
            assertThat(entity.getSourceTableName()).isEqualTo("sales");
            assertThat(entity.getDescription()).isEqualTo("説明");
            assertThat(entity.getIsActive()).isTrue();
            assertThat(entity.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("ID が null のドメインモデルからエンティティを生成できる")
        void shouldCreateEntityFromDomainWithNullId() {
            AutoJournalPattern pattern = AutoJournalPattern.create("AP001", "売上計上", "sales", null);

            AutoJournalPatternEntity entity = AutoJournalPatternEntity.fromDomain(pattern);

            assertThat(entity.getId()).isNull();
            assertThat(entity.getPatternCode()).isEqualTo("AP001");
            assertThat(entity.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("エンティティからドメインモデルを再構築できる")
        void shouldConvertToDomain() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setId(1L);
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setDescription("説明");
            entity.setIsActive(true);

            AutoJournalPatternItemEntity itemEntity = new AutoJournalPatternItemEntity();
            itemEntity.setId(10L);
            itemEntity.setPatternId(1L);
            itemEntity.setLineNumber(1);
            itemEntity.setDebitCreditType("D");
            itemEntity.setAccountCode("1100");
            itemEntity.setAmountFormula("amount");
            itemEntity.setDescriptionTemplate("売上");
            entity.setItems(List.of(itemEntity));

            AutoJournalPattern pattern = entity.toDomain();

            assertThat(pattern.getId()).isEqualTo(AutoJournalPatternId.of(1L));
            assertThat(pattern.getPatternCode()).isEqualTo("AP001");
            assertThat(pattern.getPatternName()).isEqualTo("売上計上");
            assertThat(pattern.getSourceTableName()).isEqualTo("sales");
            assertThat(pattern.getDescription()).isEqualTo("説明");
            assertThat(pattern.getIsActive()).isTrue();
            assertThat(pattern.getItems()).hasSize(1);
            assertThat(pattern.getItems().get(0).getLineNumber()).isEqualTo(1);
            assertThat(pattern.getItems().get(0).getDebitCreditType()).isEqualTo("D");
        }

        @Test
        @DisplayName("ID が null のエンティティからドメインモデルを再構築できる")
        void shouldConvertToDomainWithNullId() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setIsActive(true);

            AutoJournalPattern pattern = entity.toDomain();

            assertThat(pattern.getId()).isNull();
        }

        @Test
        @DisplayName("items が null のエンティティからドメインモデルを再構築できる")
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        void shouldConvertToDomainWithNullItems() throws Exception {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setId(1L);
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setIsActive(true);
            // setItems(null) は ArrayList に変換するため、リフレクションで直接 null を設定
            Field itemsField = AutoJournalPatternEntity.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            itemsField.set(entity, null);

            AutoJournalPattern pattern = entity.toDomain();

            assertThat(pattern.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("setItems")
    class SetItems {

        @Test
        @DisplayName("null を設定すると空リストになる")
        void shouldHandleNullItems() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setItems(null);

            assertThat(entity.getItems()).isEmpty();
        }

        @Test
        @DisplayName("リストを設定すると可変コピーが作成される")
        void shouldCreateMutableCopy() {
            AutoJournalPatternItemEntity item = new AutoJournalPatternItemEntity();
            item.setLineNumber(1);
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setItems(List.of(item));

            assertThat(entity.getItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getter / setter")
    class GetterSetter {

        @Test
        @DisplayName("全フィールドの getter/setter が機能する")
        void shouldGetAndSetAllFields() {
            AutoJournalPatternEntity entity = new AutoJournalPatternEntity();
            entity.setId(1L);
            entity.setPatternCode("AP001");
            entity.setPatternName("売上計上");
            entity.setSourceTableName("sales");
            entity.setDescription("説明");
            entity.setIsActive(true);
            entity.setCreatedAt(null);
            entity.setUpdatedAt(null);

            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getPatternCode()).isEqualTo("AP001");
            assertThat(entity.getPatternName()).isEqualTo("売上計上");
            assertThat(entity.getSourceTableName()).isEqualTo("sales");
            assertThat(entity.getDescription()).isEqualTo("説明");
            assertThat(entity.getIsActive()).isTrue();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }
}
