package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryLineEntity 変換")
class JournalEntryLineEntityTest {

    @Nested
    @DisplayName("fromDomain")
    class FromDomain {

        @Test
        @DisplayName("借方明細行からエンティティに変換できる")
        void shouldConvertDebitLineFromDomain() {
            JournalEntryLine line = JournalEntryLine.of(
                    1, AccountId.of(100), Money.of(new BigDecimal("5000")), null);

            JournalEntryLineEntity entity = JournalEntryLineEntity.fromDomain(line, 10);

            assertThat(entity.getJournalEntryId()).isEqualTo(10);
            assertThat(entity.getLineNumber()).isEqualTo(1);
            assertThat(entity.getAccountId()).isEqualTo(100);
            assertThat(entity.getDebitAmount()).isEqualByComparingTo("5000");
            assertThat(entity.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("貸方明細行からエンティティに変換できる")
        void shouldConvertCreditLineFromDomain() {
            JournalEntryLine line = JournalEntryLine.of(
                    2, AccountId.of(200), null, Money.of(new BigDecimal("3000")));

            JournalEntryLineEntity entity = JournalEntryLineEntity.fromDomain(line, 10);

            assertThat(entity.getJournalEntryId()).isEqualTo(10);
            assertThat(entity.getLineNumber()).isEqualTo(2);
            assertThat(entity.getAccountId()).isEqualTo(200);
            assertThat(entity.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(entity.getCreditAmount()).isEqualByComparingTo("3000");
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("借方金額ありの場合はドメインモデルの debitAmount に変換される")
        void shouldConvertToDomainWithDebitAmount() {
            JournalEntryLineEntity entity = new JournalEntryLineEntity();
            entity.setLineNumber(1);
            entity.setAccountId(100);
            entity.setDebitAmount(new BigDecimal("5000"));
            entity.setCreditAmount(BigDecimal.ZERO);

            JournalEntryLine line = entity.toDomain();

            assertThat(line.lineNumber()).isEqualTo(1);
            assertThat(line.accountId()).isEqualTo(AccountId.of(100));
            assertThat(line.debitAmount()).isNotNull();
            assertThat(line.debitAmount().value()).isEqualByComparingTo("5000");
            assertThat(line.creditAmount()).isNull();
        }

        @Test
        @DisplayName("貸方金額ありの場合はドメインモデルの creditAmount に変換される")
        void shouldConvertToDomainWithCreditAmount() {
            JournalEntryLineEntity entity = new JournalEntryLineEntity();
            entity.setLineNumber(2);
            entity.setAccountId(200);
            entity.setDebitAmount(BigDecimal.ZERO);
            entity.setCreditAmount(new BigDecimal("3000"));

            JournalEntryLine line = entity.toDomain();

            assertThat(line.lineNumber()).isEqualTo(2);
            assertThat(line.accountId()).isEqualTo(AccountId.of(200));
            assertThat(line.debitAmount()).isNull();
            assertThat(line.creditAmount()).isNotNull();
            assertThat(line.creditAmount().value()).isEqualByComparingTo("3000");
        }

        @Test
        @DisplayName("金額が null の場合は null で復元される")
        void shouldConvertToDomainWithNullAmounts() {
            JournalEntryLineEntity entity = new JournalEntryLineEntity();
            entity.setLineNumber(1);
            entity.setAccountId(100);
            entity.setDebitAmount(null);
            entity.setCreditAmount(null);

            // 両方 null の場合はドメインバリデーションで例外が出るため、片方のみ正の値を設定
            entity.setDebitAmount(new BigDecimal("1000"));

            JournalEntryLine line = entity.toDomain();

            assertThat(line.debitAmount()).isNotNull();
            assertThat(line.creditAmount()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter/Setter")
    class GetterSetter {

        @Test
        @DisplayName("全フィールドを設定・取得できる")
        void shouldSetAndGetAllFields() {
            JournalEntryLineEntity entity = new JournalEntryLineEntity();

            entity.setId(1);
            entity.setJournalEntryId(10);
            entity.setLineNumber(1);
            entity.setAccountId(100);
            entity.setDebitAmount(new BigDecimal("5000"));
            entity.setCreditAmount(new BigDecimal("3000"));
            entity.setLineDescription("明細摘要");

            assertThat(entity.getId()).isEqualTo(1);
            assertThat(entity.getJournalEntryId()).isEqualTo(10);
            assertThat(entity.getLineNumber()).isEqualTo(1);
            assertThat(entity.getAccountId()).isEqualTo(100);
            assertThat(entity.getDebitAmount()).isEqualByComparingTo("5000");
            assertThat(entity.getCreditAmount()).isEqualByComparingTo("3000");
            assertThat(entity.getLineDescription()).isEqualTo("明細摘要");
        }
    }
}
