package com.example.accounting.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JournalEntryDebitCreditEntity")
class JournalEntryDebitCreditEntityTest {

    @Test
    @DisplayName("全フィールドを設定・取得できる")
    void shouldSetAndGetAllFields() {
        JournalEntryDebitCreditEntity entity = new JournalEntryDebitCreditEntity();
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate dueDate = LocalDate.of(2024, 3, 31);

        entity.setJournalEntryId(1);
        entity.setLineNumber(1);
        entity.setDebitCreditType("D");
        entity.setCurrencyCode("JPY");
        entity.setExchangeRate(BigDecimal.ONE);
        entity.setDepartmentCode("D001");
        entity.setProjectCode("P001");
        entity.setAccountCode("1101");
        entity.setSubAccountCode("001");
        entity.setAmount(new BigDecimal("10000"));
        entity.setBaseCurrencyAmount(new BigDecimal("10000"));
        entity.setTaxCategory("TAX");
        entity.setTaxRate(10);
        entity.setTaxCalculationCategory("INCLUSIVE");
        entity.setDueDate(dueDate);
        entity.setCashFlowFlag(1);
        entity.setSegmentCode("S001");
        entity.setCounterAccountCode("2101");
        entity.setCounterSubAccountCode("002");
        entity.setTagCode("T001");
        entity.setTagContent("タグ内容");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getJournalEntryId()).isEqualTo(1);
        assertThat(entity.getLineNumber()).isEqualTo(1);
        assertThat(entity.getDebitCreditType()).isEqualTo("D");
        assertThat(entity.getCurrencyCode()).isEqualTo("JPY");
        assertThat(entity.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(entity.getDepartmentCode()).isEqualTo("D001");
        assertThat(entity.getProjectCode()).isEqualTo("P001");
        assertThat(entity.getAccountCode()).isEqualTo("1101");
        assertThat(entity.getSubAccountCode()).isEqualTo("001");
        assertThat(entity.getAmount()).isEqualByComparingTo("10000");
        assertThat(entity.getBaseCurrencyAmount()).isEqualByComparingTo("10000");
        assertThat(entity.getTaxCategory()).isEqualTo("TAX");
        assertThat(entity.getTaxRate()).isEqualTo(10);
        assertThat(entity.getTaxCalculationCategory()).isEqualTo("INCLUSIVE");
        assertThat(entity.getDueDate()).isEqualTo(dueDate);
        assertThat(entity.getCashFlowFlag()).isEqualTo(1);
        assertThat(entity.getSegmentCode()).isEqualTo("S001");
        assertThat(entity.getCounterAccountCode()).isEqualTo("2101");
        assertThat(entity.getCounterSubAccountCode()).isEqualTo("002");
        assertThat(entity.getTagCode()).isEqualTo("T001");
        assertThat(entity.getTagContent()).isEqualTo("タグ内容");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("初期状態で全フィールドが null")
    void shouldHaveNullFieldsByDefault() {
        JournalEntryDebitCreditEntity entity = new JournalEntryDebitCreditEntity();

        assertThat(entity.getJournalEntryId()).isNull();
        assertThat(entity.getLineNumber()).isNull();
        assertThat(entity.getDebitCreditType()).isNull();
        assertThat(entity.getAmount()).isNull();
    }
}
