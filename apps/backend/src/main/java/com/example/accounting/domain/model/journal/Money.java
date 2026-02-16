package com.example.accounting.domain.model.journal;

import io.vavr.control.Either;

import java.math.BigDecimal;

/**
 * 金額を表す値オブジェクト
 */
public record Money(BigDecimal value) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    /**
     * 金額を生成する
     *
     * @param value 金額
     * @return Money インスタンス
     */
    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    /**
     * バリデーション付きファクトリメソッド
     *
     * @param value 金額
     * @return Either（左: エラーメッセージ、右: Money インスタンス）
     */
    public static Either<String, Money> validated(BigDecimal value) {
        if (value == null) {
            return Either.left("金額は必須です");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return Either.left("金額は 0 以上である必要があります");
        }
        return Either.right(new Money(value));
    }

    /**
     * DB からの復元用ファクトリメソッド
     *
     * @param value 金額
     * @return Money インスタンス
     */
    public static Money reconstruct(BigDecimal value) {
        return new Money(value);
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(this.value.subtract(other.value));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
