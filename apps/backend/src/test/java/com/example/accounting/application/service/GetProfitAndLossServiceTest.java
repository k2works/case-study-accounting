package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetProfitAndLossQuery;
import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossEntry;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossSection;
import com.example.accounting.application.port.out.ProfitAndLossRepository;
import com.example.accounting.infrastructure.persistence.entity.ProfitAndLossEntity;
import io.vavr.control.Try;
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
@DisplayName("損益計算書サービス")
class GetProfitAndLossServiceTest {

    @Mock
    private ProfitAndLossRepository profitAndLossRepository;

    private GetProfitAndLossService service;

    @BeforeEach
    void setUp() {
        service = new GetProfitAndLossService(profitAndLossRepository);
    }

    @Test
    @DisplayName("当期純利益 = 収益合計 - 費用合計")
    void shouldReturnCorrectNetIncome() {
        ProfitAndLossEntity revenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("100000"));
        ProfitAndLossEntity expense = createEntity("5000", "給料", "EXPENSE",
                new BigDecimal("60000"), BigDecimal.ZERO, new BigDecimal("60000"));

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(revenue, expense)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        assertThat(result.totalRevenue()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(result.totalExpense()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(result.netIncome()).isEqualByComparingTo(new BigDecimal("40000"));
    }

    @Test
    @DisplayName("セクションが REVENUE, EXPENSE の 2 つ生成される")
    void shouldBuildTwoSections() {
        ProfitAndLossEntity revenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("100000"));
        ProfitAndLossEntity expense = createEntity("5000", "給料", "EXPENSE",
                new BigDecimal("60000"), BigDecimal.ZERO, new BigDecimal("60000"));

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(revenue, expense)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        assertThat(result.sections()).hasSize(2);
        assertThat(result.sections().get(0).sectionType()).isEqualTo("REVENUE");
        assertThat(result.sections().get(0).sectionDisplayName()).isEqualTo("収益の部");
        assertThat(result.sections().get(1).sectionType()).isEqualTo("EXPENSE");
        assertThat(result.sections().get(1).sectionDisplayName()).isEqualTo("費用の部");
    }

    @Test
    @DisplayName("収益の金額はそのまま表示される")
    void shouldCalculateRevenueAmountsCorrectly() {
        ProfitAndLossEntity revenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("100000"));

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(revenue)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        ProfitAndLossSection revenueSection = result.sections().get(0);
        assertThat(revenueSection.entries()).hasSize(1);
        assertThat(revenueSection.entries().get(0).amount())
                .isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(revenueSection.subtotal())
                .isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("費用の金額はそのまま表示される")
    void shouldCalculateExpenseAmountsCorrectly() {
        ProfitAndLossEntity expense = createEntity("5000", "給料", "EXPENSE",
                new BigDecimal("60000"), BigDecimal.ZERO, new BigDecimal("60000"));

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(expense)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        ProfitAndLossSection expenseSection = result.sections().get(1);
        assertThat(expenseSection.entries()).hasSize(1);
        assertThat(expenseSection.entries().get(0).amount())
                .isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(expenseSection.subtotal())
                .isEqualByComparingTo(new BigDecimal("60000"));
    }

    @Test
    @DisplayName("前期比較データが正しく計算される")
    void shouldCalculateComparativeData() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        LocalDate compDateFrom = LocalDate.of(2025, 4, 1);
        LocalDate compDateTo = LocalDate.of(2026, 3, 31);

        ProfitAndLossEntity currentRevenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("120000"), new BigDecimal("120000"));
        ProfitAndLossEntity currentExpense = createEntity("5000", "給料", "EXPENSE",
                new BigDecimal("80000"), BigDecimal.ZERO, new BigDecimal("80000"));

        ProfitAndLossEntity prevRevenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("100000"));
        ProfitAndLossEntity prevExpense = createEntity("5000", "給料", "EXPENSE",
                new BigDecimal("60000"), BigDecimal.ZERO, new BigDecimal("60000"));

        when(profitAndLossRepository.findProfitAndLoss(dateFrom, dateTo))
                .thenReturn(Try.success(List.of(currentRevenue, currentExpense)));
        when(profitAndLossRepository.findProfitAndLoss(compDateFrom, compDateTo))
                .thenReturn(Try.success(List.of(prevRevenue, prevExpense)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(dateFrom, dateTo, compDateFrom, compDateTo));

        assertThat(result.comparativeDateFrom()).isEqualTo(compDateFrom);
        assertThat(result.comparativeDateTo()).isEqualTo(compDateTo);

        ProfitAndLossSection revenueSection = result.sections().get(0);
        ProfitAndLossEntry revenueEntry = revenueSection.entries().get(0);
        assertThat(revenueEntry.comparative()).isNotNull();
        assertThat(revenueEntry.comparative().previousAmount())
                .isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(revenueEntry.comparative().difference())
                .isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(revenueEntry.comparative().changeRate())
                .isEqualByComparingTo(new BigDecimal("20.00"));

        assertThat(revenueSection.comparativeSubtotal()).isNotNull();
        assertThat(revenueSection.comparativeSubtotal().previousAmount())
                .isEqualByComparingTo(new BigDecimal("100000"));
    }

    @Test
    @DisplayName("前期比較期間が null の場合は比較データなし")
    void shouldNotIncludeComparativeDataWhenNoPreviousPeriod() {
        ProfitAndLossEntity revenue = createEntity("4000", "売上高", "REVENUE",
                BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("100000"));

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(revenue)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        assertThat(result.comparativeDateFrom()).isNull();
        assertThat(result.comparativeDateTo()).isNull();
        ProfitAndLossEntry entry = result.sections().get(0).entries().get(0);
        assertThat(entry.comparative()).isNull();
        assertThat(result.sections().get(0).comparativeSubtotal()).isNull();
    }

    @Test
    @DisplayName("データがない場合は空の結果を返す")
    void shouldReturnEmptyResultWhenNoData() {
        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of()));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        assertThat(result.sections()).hasSize(2);
        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.netIncome()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("指定期間の損益計算書を取得できる")
    void shouldExecuteQueryWithSpecifiedPeriod() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);

        when(profitAndLossRepository.findProfitAndLoss(dateFrom, dateTo))
                .thenReturn(Try.success(List.of()));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(dateFrom, dateTo, null, null));

        assertThat(result.dateFrom()).isEqualTo(dateFrom);
        assertThat(result.dateTo()).isEqualTo(dateTo);

        verify(profitAndLossRepository).findProfitAndLoss(dateFrom, dateTo);
    }

    @Test
    @DisplayName("null の amount を 0 として扱う")
    void shouldHandleNullAmountAsZero() {
        ProfitAndLossEntity entityWithNullAmount = new ProfitAndLossEntity();
        entityWithNullAmount.setAccountCode("4000");
        entityWithNullAmount.setAccountName("売上高");
        entityWithNullAmount.setAccountType("REVENUE");
        entityWithNullAmount.setTotalDebit(BigDecimal.ZERO);
        entityWithNullAmount.setTotalCredit(BigDecimal.ZERO);
        entityWithNullAmount.setAmount(null);

        when(profitAndLossRepository.findProfitAndLoss(null, null))
                .thenReturn(Try.success(List.of(entityWithNullAmount)));

        GetProfitAndLossResult result = service.execute(
                new GetProfitAndLossQuery(null, null, null, null));

        assertThat(result.sections().get(0).entries()).hasSize(1);
        assertThat(result.sections().get(0).entries().get(0).amount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    private ProfitAndLossEntity createEntity(String code, String name, String type,
                                              BigDecimal debit, BigDecimal credit, BigDecimal amount) {
        ProfitAndLossEntity entity = new ProfitAndLossEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setAccountType(type);
        entity.setTotalDebit(debit);
        entity.setTotalCredit(credit);
        entity.setAmount(amount);
        return entity;
    }
}
