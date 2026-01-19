package com.example.accounting.domain.model.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccountType")
class AccountTypeTest {

    @Test
    @DisplayName("各種別の表示名を取得できる")
    void shouldGetDisplayName() {
        assertThat(AccountType.ASSET.getDisplayName()).isEqualTo("資産");
        assertThat(AccountType.LIABILITY.getDisplayName()).isEqualTo("負債");
        assertThat(AccountType.EQUITY.getDisplayName()).isEqualTo("純資産");
        assertThat(AccountType.REVENUE.getDisplayName()).isEqualTo("収益");
        assertThat(AccountType.EXPENSE.getDisplayName()).isEqualTo("費用");
    }

    @Test
    @DisplayName("各種別の B/S/P/L 区分を取得できる")
    void shouldGetBsPlType() {
        assertThat(AccountType.ASSET.getBsPlType()).isEqualTo("B");
        assertThat(AccountType.LIABILITY.getBsPlType()).isEqualTo("B");
        assertThat(AccountType.EQUITY.getBsPlType()).isEqualTo("B");
        assertThat(AccountType.REVENUE.getBsPlType()).isEqualTo("P");
        assertThat(AccountType.EXPENSE.getBsPlType()).isEqualTo("P");
    }

    @Test
    @DisplayName("借方残高の判定ができる")
    void shouldDetectDebitBalance() {
        assertThat(AccountType.ASSET.isDebitBalance()).isTrue();
        assertThat(AccountType.EXPENSE.isDebitBalance()).isTrue();
        assertThat(AccountType.LIABILITY.isDebitBalance()).isFalse();
        assertThat(AccountType.EQUITY.isDebitBalance()).isFalse();
        assertThat(AccountType.REVENUE.isDebitBalance()).isFalse();
    }

    @Test
    @DisplayName("B/S 科目の判定ができる")
    void shouldDetectBalanceSheet() {
        assertThat(AccountType.ASSET.isBalanceSheet()).isTrue();
        assertThat(AccountType.LIABILITY.isBalanceSheet()).isTrue();
        assertThat(AccountType.EQUITY.isBalanceSheet()).isTrue();
        assertThat(AccountType.REVENUE.isBalanceSheet()).isFalse();
        assertThat(AccountType.EXPENSE.isBalanceSheet()).isFalse();
    }

    @Test
    @DisplayName("P/L 科目の判定ができる")
    void shouldDetectProfitAndLoss() {
        assertThat(AccountType.REVENUE.isProfitAndLoss()).isTrue();
        assertThat(AccountType.EXPENSE.isProfitAndLoss()).isTrue();
        assertThat(AccountType.ASSET.isProfitAndLoss()).isFalse();
        assertThat(AccountType.LIABILITY.isProfitAndLoss()).isFalse();
        assertThat(AccountType.EQUITY.isProfitAndLoss()).isFalse();
    }

    @Test
    @DisplayName("コードから種別を取得できる")
    void shouldGetFromCode() {
        assertThat(AccountType.fromCode("ASSET")).isEqualTo(AccountType.ASSET);
        assertThat(AccountType.fromCode("LIABILITY")).isEqualTo(AccountType.LIABILITY);
        assertThat(AccountType.fromCode("EQUITY")).isEqualTo(AccountType.EQUITY);
        assertThat(AccountType.fromCode("REVENUE")).isEqualTo(AccountType.REVENUE);
        assertThat(AccountType.fromCode("EXPENSE")).isEqualTo(AccountType.EXPENSE);
    }

    @Test
    @DisplayName("無効なコードの場合は例外をスローする")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> AccountType.fromCode("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("勘定科目種別");
    }
}
