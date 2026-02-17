package com.example.accounting.domain.model.journal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money")
class MoneyTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("0 以上の金額で生成できる")
        void shouldCreateWithNonNegativeValue() {
            Money money = Money.of(new BigDecimal("1000"));

            assertThat(money.value()).isEqualByComparingTo("1000");
        }
    }

    @Nested
    @DisplayName("validated")
    class Validated {

        @Test
        @DisplayName("null の場合はエラーメッセージを返す")
        void shouldReturnLeftForNull() {
            assertThat(Money.validated(null).getLeft()).isEqualTo("金額は必須です");
        }

        @Test
        @DisplayName("負の金額の場合はエラーメッセージを返す")
        void shouldReturnLeftForNegativeValue() {
            assertThat(Money.validated(new BigDecimal("-1")).getLeft())
                    .isEqualTo("金額は 0 以上である必要があります");
        }

        @Test
        @DisplayName("0 以上の金額で Right を返す")
        void shouldReturnRightForValidValue() {
            assertThat(Money.validated(new BigDecimal("1000")).isRight()).isTrue();
            assertThat(Money.validated(new BigDecimal("1000")).get().value())
                    .isEqualByComparingTo("1000");
        }
    }

    @Test
    @DisplayName("reconstruct で復元できる")
    void shouldReconstruct() {
        Money money = Money.reconstruct(new BigDecimal("500"));

        assertThat(money.value()).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("加算と減算ができる")
    void shouldAddAndSubtract() {
        Money base = Money.of(new BigDecimal("100"));

        Money added = base.add(Money.of(new BigDecimal("50")));
        Money subtracted = base.subtract(Money.of(new BigDecimal("40")));

        assertThat(added.value()).isEqualByComparingTo("150");
        assertThat(subtracted.value()).isEqualByComparingTo("60");
    }

    @Test
    @DisplayName("add に null を渡すと NullPointerException をスローする")
    void shouldThrowNpeWhenAddNull() {
        Money base = Money.of(new BigDecimal("100"));

        assertThatThrownBy(() -> base.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("subtract に null を渡すと NullPointerException をスローする")
    void shouldThrowNpeWhenSubtractNull() {
        Money base = Money.of(new BigDecimal("100"));

        assertThatThrownBy(() -> base.subtract(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("減算結果が負になる場合でも計算される")
    void shouldAllowNegativeSubtractionResult() {
        Money base = Money.of(new BigDecimal("100"));
        Money subtrahend = Money.of(new BigDecimal("200"));

        Money result = base.subtract(subtrahend);

        assertThat(result.value()).isEqualByComparingTo("-100");
    }

    @Test
    @DisplayName("ゼロ判定と正数判定ができる")
    void shouldCheckZeroAndPositive() {
        Money zero = Money.ZERO;
        Money positive = Money.of(BigDecimal.TEN);

        assertThat(zero.isZero()).isTrue();
        assertThat(zero.isPositive()).isFalse();
        assertThat(positive.isZero()).isFalse();
        assertThat(positive.isPositive()).isTrue();
    }
}
