package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
