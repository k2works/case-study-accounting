package com.example.accounting.domain.model.journal;

import java.math.BigDecimal;

/**
 * 金額を表す値オブジェクト
 */
public record Money(BigDecimal value) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (value == null) {
            throw new IllegalArgumentException("金額は必須です");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金額は 0 以上である必要があります");
        }
    }

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
     * DB からの復元用ファクトリメソッド
     *
     * @param value 金額
     * @return Money インスタンス
     */
    public static Money reconstruct(BigDecimal value) {
        return new Money(value);
    }

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("加算対象の金額は必須です");
        }
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("減算対象の金額は必須です");
        }
        return new Money(this.value.subtract(other.value));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
