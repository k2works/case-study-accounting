package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountEntity 変換")
class AccountEntityTest {

    @Test
    @DisplayName("fromDomain でエンティティに変換できる（id あり）")
    void shouldConvertFromDomainWithId() {
        // reconstruct で id 付きの Account を作成（DB から復元したケース）
        Account account = Account.reconstruct(
                AccountId.of(1),
                AccountCode.of("1101"),
                "現金",
                AccountType.ASSET
        );

        AccountEntity entity = AccountEntity.fromDomain(account);

        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getCode()).isEqualTo("1101");
        assertThat(entity.getName()).isEqualTo("現金");
        assertThat(entity.getAccountType()).isEqualTo("ASSET");
    }

    @Test
    @DisplayName("fromDomain でエンティティに変換できる（新規作成、id なし）")
    void shouldConvertFromDomainWithoutId() {
        // create で id なしの Account を作成（新規作成ケース）
        Account account = Account.create(
                AccountCode.of("1101"),
                "現金",
                AccountType.ASSET
        );

        AccountEntity entity = AccountEntity.fromDomain(account);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isEqualTo("1101");
        assertThat(entity.getName()).isEqualTo("現金");
        assertThat(entity.getAccountType()).isEqualTo("ASSET");
    }

    @Test
    @DisplayName("toDomain でドメインモデルに変換できる")
    void shouldConvertToDomain() {
        AccountEntity entity = new AccountEntity();
        entity.setId(2);
        entity.setCode("2101");
        entity.setName("買掛金");
        entity.setAccountType("LIABILITY");

        Account account = entity.toDomain();

        assertThat(account.getId()).isEqualTo(AccountId.of(2));
        assertThat(account.getAccountCode()).isEqualTo(AccountCode.of("2101"));
        assertThat(account.getAccountName()).isEqualTo("買掛金");
        assertThat(account.getAccountType()).isEqualTo(AccountType.LIABILITY);
    }

    @Nested
    @DisplayName("追加フィールドの Getter/Setter")
    class AdditionalFields {

        @Test
        @DisplayName("設計書で追加されたカラムを設定・取得できる")
        void shouldSetAndGetAdditionalFields() {
            AccountEntity entity = new AccountEntity();
            OffsetDateTime now = OffsetDateTime.now();

            entity.setKana("ゲンキン");
            entity.setBsplCategory("BS");
            entity.setTransactionElementCategory("DEBIT");
            entity.setExpenseCategory("GENERAL");
            entity.setSummaryAccount(true);
            entity.setDisplayOrder(10);
            entity.setAggregationTarget(false);
            entity.setBalance(new BigDecimal("100000"));
            entity.setTaxTransactionCode("T01");
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            assertThat(entity.getKana()).isEqualTo("ゲンキン");
            assertThat(entity.getBsplCategory()).isEqualTo("BS");
            assertThat(entity.getTransactionElementCategory()).isEqualTo("DEBIT");
            assertThat(entity.getExpenseCategory()).isEqualTo("GENERAL");
            assertThat(entity.getSummaryAccount()).isTrue();
            assertThat(entity.getDisplayOrder()).isEqualTo(10);
            assertThat(entity.getAggregationTarget()).isFalse();
            assertThat(entity.getBalance()).isEqualByComparingTo("100000");
            assertThat(entity.getTaxTransactionCode()).isEqualTo("T01");
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("初期状態で追加フィールドが null")
        void shouldHaveNullAdditionalFieldsByDefault() {
            AccountEntity entity = new AccountEntity();

            assertThat(entity.getKana()).isNull();
            assertThat(entity.getBsplCategory()).isNull();
            assertThat(entity.getTransactionElementCategory()).isNull();
            assertThat(entity.getExpenseCategory()).isNull();
            assertThat(entity.getSummaryAccount()).isNull();
            assertThat(entity.getDisplayOrder()).isNull();
            assertThat(entity.getAggregationTarget()).isNull();
            assertThat(entity.getBalance()).isNull();
            assertThat(entity.getTaxTransactionCode()).isNull();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }
}
