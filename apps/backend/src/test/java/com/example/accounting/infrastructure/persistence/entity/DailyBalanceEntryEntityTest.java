package com.example.accounting.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyBalanceEntryEntity")
class DailyBalanceEntryEntityTest {

    @Test
    @DisplayName("日次残高の各項目を設定・取得できる")
    void shouldSetAndGetFields() {
        DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();
        LocalDate date = LocalDate.of(2024, 1, 31);

        entity.setDate(date);
        entity.setDebitTotal(new BigDecimal("1000"));
        entity.setCreditTotal(new BigDecimal("500"));
        entity.setTransactionCount(3L);

        assertThat(entity.getDate()).isEqualTo(date);
        assertThat(entity.getDebitTotal()).isEqualByComparingTo("1000");
        assertThat(entity.getCreditTotal()).isEqualByComparingTo("500");
        assertThat(entity.getTransactionCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("初期状態では各項目が null である")
    void shouldHaveNullFieldsByDefault() {
        DailyBalanceEntryEntity entity = new DailyBalanceEntryEntity();

        assertThat(entity.getDate()).isNull();
        assertThat(entity.getDebitTotal()).isNull();
        assertThat(entity.getCreditTotal()).isNull();
        assertThat(entity.getTransactionCount()).isNull();
    }
}
