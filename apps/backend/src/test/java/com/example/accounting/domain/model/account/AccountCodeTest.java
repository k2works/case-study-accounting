package com.example.accounting.domain.model.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccountCode")
class AccountCodeTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("有効な 4 桁コードでインスタンスを生成できる")
        void shouldCreateWithValidCode() {
            AccountCode code = AccountCode.of("1101");

            assertThat(code.value()).isEqualTo("1101");
        }

        @Test
        @DisplayName("null の場合は例外をスローする")
        void shouldThrowExceptionForNull() {
            assertThatThrownBy(() -> AccountCode.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("空文字の場合は例外をスローする")
        void shouldThrowExceptionForEmpty() {
            assertThatThrownBy(() -> AccountCode.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("空白のみの場合は例外をスローする")
        void shouldThrowExceptionForBlank() {
            assertThatThrownBy(() -> AccountCode.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは必須です");
        }

        @Test
        @DisplayName("3 桁の場合は例外をスローする")
        void shouldThrowExceptionForThreeDigits() {
            assertThatThrownBy(() -> AccountCode.of("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは 4 桁の数字である必要があります");
        }

        @Test
        @DisplayName("5 桁の場合は例外をスローする")
        void shouldThrowExceptionForFiveDigits() {
            assertThatThrownBy(() -> AccountCode.of("12345"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは 4 桁の数字である必要があります");
        }

        @Test
        @DisplayName("数字以外を含む場合は例外をスローする")
        void shouldThrowExceptionForNonDigit() {
            assertThatThrownBy(() -> AccountCode.of("12a4"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("勘定科目コードは 4 桁の数字である必要があります");
        }
    }

    @Nested
    @DisplayName("category")
    class Category {

        @Test
        @DisplayName("資産科目を判定できる")
        void shouldDetectAssetAccount() {
            AccountCode code = AccountCode.of("1101");

            assertThat(code.isAssetAccount()).isTrue();
            assertThat(code.isBalanceSheetAccount()).isTrue();
            assertThat(code.isProfitLossAccount()).isFalse();
        }

        @Test
        @DisplayName("負債科目を判定できる")
        void shouldDetectLiabilityAccount() {
            AccountCode code = AccountCode.of("2101");

            assertThat(code.isLiabilityAccount()).isTrue();
            assertThat(code.isBalanceSheetAccount()).isTrue();
            assertThat(code.isProfitLossAccount()).isFalse();
        }

        @Test
        @DisplayName("純資産科目を判定できる")
        void shouldDetectEquityAccount() {
            AccountCode code = AccountCode.of("3101");

            assertThat(code.isEquityAccount()).isTrue();
            assertThat(code.isBalanceSheetAccount()).isTrue();
            assertThat(code.isProfitLossAccount()).isFalse();
        }

        @Test
        @DisplayName("収益科目を判定できる")
        void shouldDetectRevenueAccount() {
            AccountCode code = AccountCode.of("4101");

            assertThat(code.isRevenueAccount()).isTrue();
            assertThat(code.isBalanceSheetAccount()).isFalse();
            assertThat(code.isProfitLossAccount()).isTrue();
        }

        @Test
        @DisplayName("費用科目を判定できる")
        void shouldDetectExpenseAccount() {
            AccountCode code = AccountCode.of("5101");

            assertThat(code.isExpenseAccount()).isTrue();
            assertThat(code.isBalanceSheetAccount()).isFalse();
            assertThat(code.isProfitLossAccount()).isTrue();
        }
    }
}
