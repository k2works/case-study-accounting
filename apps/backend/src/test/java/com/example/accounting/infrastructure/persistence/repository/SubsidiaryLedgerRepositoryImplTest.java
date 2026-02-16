package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import com.example.accounting.infrastructure.persistence.entity.JournalEntryLineWithHeaderEntity;
import com.example.accounting.infrastructure.persistence.mapper.SubsidiaryLedgerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubsidiaryLedgerRepositoryImpl 単体テスト")
class SubsidiaryLedgerRepositoryImplTest {

    @Mock
    private SubsidiaryLedgerMapper subsidiaryLedgerMapper;

    private SubsidiaryLedgerRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new SubsidiaryLedgerRepositoryImpl(subsidiaryLedgerMapper);
    }

    private JournalEntryLineWithHeaderEntity createEntity(int id, LocalDate date,
                                                           String description,
                                                           BigDecimal debit,
                                                           BigDecimal credit) {
        JournalEntryLineWithHeaderEntity entity = new JournalEntryLineWithHeaderEntity();
        entity.setJournalEntryId(id);
        entity.setJournalDate(date);
        entity.setDescription(description);
        entity.setDebitAmount(debit);
        entity.setCreditAmount(credit);
        return entity;
    }

    @Test
    @DisplayName("仕訳行を取得しエントリにマッピングする")
    void shouldFindAndMapEntries() {
        LocalDate dateFrom = LocalDate.of(2024, 4, 1);
        LocalDate dateTo = LocalDate.of(2024, 4, 30);
        JournalEntryLineWithHeaderEntity entity = createEntity(1, LocalDate.of(2024, 4, 1),
                "テスト仕訳", new BigDecimal("1000"), BigDecimal.ZERO);

        when(subsidiaryLedgerMapper.findPostedLinesByAccountAndSubAccountAndPeriod(
                "1100", "001", dateFrom, dateTo, 0, 20))
                .thenReturn(List.of(entity));

        List<SubsidiaryLedgerEntry> result = repository
                .findPostedLinesByAccountAndSubAccountAndPeriod("1100", "001", dateFrom, dateTo, 0, 20);

        assertThat(result).hasSize(1);
        SubsidiaryLedgerEntry entry = result.get(0);
        assertThat(entry.journalEntryId()).isEqualTo(1);
        assertThat(entry.journalDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(entry.description()).isEqualTo("テスト仕訳");
        assertThat(entry.debitAmount()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(entry.creditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entry.runningBalance()).isNull();

        verify(subsidiaryLedgerMapper).findPostedLinesByAccountAndSubAccountAndPeriod(
                "1100", "001", dateFrom, dateTo, 0, 20);
    }

    @Test
    @DisplayName("マッパーが空リストを返す場合は空リストを返す")
    void shouldReturnEmptyListWhenMapperReturnsEmpty() {
        LocalDate dateFrom = LocalDate.of(2024, 4, 1);
        LocalDate dateTo = LocalDate.of(2024, 4, 30);

        when(subsidiaryLedgerMapper.findPostedLinesByAccountAndSubAccountAndPeriod(
                "9999", null, dateFrom, dateTo, 0, 20))
                .thenReturn(List.of());

        List<SubsidiaryLedgerEntry> result = repository
                .findPostedLinesByAccountAndSubAccountAndPeriod("9999", null, dateFrom, dateTo, 0, 20);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("件数取得をマッパーに委譲する")
    void shouldDelegateCountToMapper() {
        LocalDate dateFrom = LocalDate.of(2024, 4, 1);
        LocalDate dateTo = LocalDate.of(2024, 4, 30);

        when(subsidiaryLedgerMapper.countPostedLinesByAccountAndSubAccountAndPeriod(
                "1100", "001", dateFrom, dateTo))
                .thenReturn(42L);

        long count = repository.countPostedLinesByAccountAndSubAccountAndPeriod(
                "1100", "001", dateFrom, dateTo);

        assertThat(count).isEqualTo(42L);
        verify(subsidiaryLedgerMapper).countPostedLinesByAccountAndSubAccountAndPeriod(
                "1100", "001", dateFrom, dateTo);
    }

    @Test
    @DisplayName("期首残高計算をマッパーに委譲する")
    void shouldDelegateCalculateBalanceToMapper() {
        LocalDate date = LocalDate.of(2024, 4, 1);

        when(subsidiaryLedgerMapper.calculateBalanceBeforeDateByAccountAndSubAccount(
                "1100", "001", date))
                .thenReturn(new BigDecimal("50000"));

        BigDecimal balance = repository.calculateBalanceBeforeDateByAccountAndSubAccount(
                "1100", "001", date);

        assertThat(balance).isEqualByComparingTo(new BigDecimal("50000"));
        verify(subsidiaryLedgerMapper).calculateBalanceBeforeDateByAccountAndSubAccount(
                "1100", "001", date);
    }
}
