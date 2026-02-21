package com.example.accounting.domain.model.auto_journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("金額計算式評価")
class AmountFormulaEvaluatorTest {

    @Test
    @DisplayName("単一変数参照を評価できる")
    void shouldEvaluateSingleVariable() {
        BigDecimal result = AmountFormulaEvaluator.evaluate("amount", Map.of("amount", new BigDecimal("10000")));

        assertThat(result).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("乗算を評価できる")
    void shouldEvaluateMultiplication() {
        BigDecimal result = AmountFormulaEvaluator.evaluate("amount * 0.1", Map.of("amount", new BigDecimal("10000")));

        assertThat(result).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("加算を評価できる")
    void shouldEvaluateAddition() {
        BigDecimal result = AmountFormulaEvaluator.evaluate("amount + 500", Map.of("amount", new BigDecimal("10000")));

        assertThat(result).isEqualByComparingTo("10500");
    }

    @Test
    @DisplayName("減算を評価できる")
    void shouldEvaluateSubtraction() {
        BigDecimal result = AmountFormulaEvaluator.evaluate("amount - 200", Map.of("amount", new BigDecimal("10000")));

        assertThat(result).isEqualByComparingTo("9800");
    }

    @Test
    @DisplayName("除算を評価できる")
    void shouldEvaluateDivision() {
        BigDecimal result = AmountFormulaEvaluator.evaluate("amount / 2", Map.of("amount", new BigDecimal("10000")));

        assertThat(result).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("ゼロ除算の場合は例外をスローする")
    void shouldThrowWhenDivisionByZero() {
        assertThatThrownBy(() -> AmountFormulaEvaluator.evaluate("amount / 0", Map.of("amount", new BigDecimal("10000"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ゼロ除算は許可されていません");
    }

    @Test
    @DisplayName("存在しない変数の場合は例外をスローする")
    void shouldThrowWhenVariableNotFound() {
        assertThatThrownBy(() -> AmountFormulaEvaluator.evaluate("amount", Map.of("price", new BigDecimal("10000"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("パラメータ 'amount' が見つかりません");
    }

    @Test
    @DisplayName("サポートされない数式の場合は例外をスローする")
    void shouldThrowWhenUnsupportedFormat() {
        assertThatThrownBy(() -> AmountFormulaEvaluator.evaluate("amount * tax", Map.of("amount", new BigDecimal("10000"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("サポートされていない数式フォーマット: amount * tax");
    }

    @Test
    @DisplayName("数式が null の場合は例外をスローする")
    void shouldThrowWhenFormulaIsNull() {
        assertThatThrownBy(() -> AmountFormulaEvaluator.evaluate(null, Map.of("amount", new BigDecimal("10000"))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("数式は必須です");
    }

    @Test
    @DisplayName("パラメータが null の場合は例外をスローする")
    void shouldThrowWhenParamsIsNull() {
        assertThatThrownBy(() -> AmountFormulaEvaluator.evaluate("amount", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("パラメータは必須です");
    }
}
