package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetFinancialAnalysisQuery;
import com.example.accounting.application.port.out.BalanceSheetRepository;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.FinancialIndicator;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.IndicatorCategory;
import com.example.accounting.application.port.out.ProfitAndLossRepository;
import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("財務分析サービス")
class GetFinancialAnalysisServiceTest {

    @Mock
    private BalanceSheetRepository balanceSheetRepository;

    @Mock
    private ProfitAndLossRepository profitAndLossRepository;

    private GetFinancialAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new GetFinancialAnalysisService(balanceSheetRepository, profitAndLossRepository);
    }

    @Test
    @DisplayName("基本的な財務指標が正しく計算される")
    void shouldCalculateBasicFinancialIndicators() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(Try.success(List.of(
                createBalanceSheetEntity("100", "現金", "ASSET", new BigDecimal("10000")),
                createBalanceSheetEntity("200", "買掛金", "LIABILITY", new BigDecimal("-4000")),
                createBalanceSheetEntity("300", "資本金", "EQUITY", new BigDecimal("-6000"))
        )));
        when(profitAndLossRepository.findProfitAndLoss(null, null)).thenReturn(Try.success(List.of(
                createProfitAndLossEntity("400", "売上", "REVENUE", new BigDecimal("20000")),
                createProfitAndLossEntity("500", "費用", "EXPENSE", new BigDecimal("15000"))
        )));

        GetFinancialAnalysisResult result = service.execute(new GetFinancialAnalysisQuery(null, null, null, null));

        assertIndicatorValue(result, "PROFITABILITY", "ROE（自己資本利益率）", new BigDecimal("83.33"));
        assertIndicatorValue(result, "PROFITABILITY", "ROA（総資産利益率）", new BigDecimal("50.00"));
        assertIndicatorValue(result, "PROFITABILITY", "売上高利益率", new BigDecimal("25.00"));
        assertIndicatorValue(result, "SAFETY", "流動比率", new BigDecimal("250.00"));
        assertIndicatorValue(result, "SAFETY", "自己資本比率", new BigDecimal("60.00"));
        assertIndicatorValue(result, "SAFETY", "負債比率", new BigDecimal("66.67"));
        assertIndicatorValue(result, "EFFICIENCY", "総資産回転率", new BigDecimal("2.00"));
    }

    @Test
    @DisplayName("カテゴリが PROFITABILITY、SAFETY、EFFICIENCY の 3 つ生成される")
    void shouldBuildThreeCategories() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(Try.success(List.of()));
        when(profitAndLossRepository.findProfitAndLoss(null, null)).thenReturn(Try.success(List.of()));

        GetFinancialAnalysisResult result = service.execute(new GetFinancialAnalysisQuery(null, null, null, null));

        assertThat(result.categories()).hasSize(3);
        assertThat(result.categories().get(0).categoryName()).isEqualTo("PROFITABILITY");
        assertThat(result.categories().get(1).categoryName()).isEqualTo("SAFETY");
        assertThat(result.categories().get(2).categoryName()).isEqualTo("EFFICIENCY");
    }

    @Test
    @DisplayName("前期比較が正しく計算される")
    void shouldCalculateComparativeValues() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        LocalDate comparativeDateFrom = LocalDate.of(2025, 4, 1);
        LocalDate comparativeDateTo = LocalDate.of(2026, 3, 31);

        when(balanceSheetRepository.findBalanceSheet(dateTo)).thenReturn(Try.success(List.of(
                createBalanceSheetEntity("100", "現金", "ASSET", new BigDecimal("10000")),
                createBalanceSheetEntity("200", "買掛金", "LIABILITY", new BigDecimal("-4000")),
                createBalanceSheetEntity("300", "資本金", "EQUITY", new BigDecimal("-6000"))
        )));
        when(profitAndLossRepository.findProfitAndLoss(dateFrom, dateTo)).thenReturn(Try.success(List.of(
                createProfitAndLossEntity("400", "売上", "REVENUE", new BigDecimal("20000")),
                createProfitAndLossEntity("500", "費用", "EXPENSE", new BigDecimal("15000"))
        )));

        when(balanceSheetRepository.findBalanceSheet(comparativeDateTo)).thenReturn(Try.success(List.of(
                createBalanceSheetEntity("100", "現金", "ASSET", new BigDecimal("8000")),
                createBalanceSheetEntity("200", "買掛金", "LIABILITY", new BigDecimal("-3000")),
                createBalanceSheetEntity("300", "資本金", "EQUITY", new BigDecimal("-5000"))
        )));
        when(profitAndLossRepository.findProfitAndLoss(comparativeDateFrom, comparativeDateTo)).thenReturn(Try.success(List.of(
                createProfitAndLossEntity("400", "売上", "REVENUE", new BigDecimal("16000")),
                createProfitAndLossEntity("500", "費用", "EXPENSE", new BigDecimal("13000"))
        )));

        GetFinancialAnalysisResult result = service.execute(
                new GetFinancialAnalysisQuery(dateFrom, dateTo, comparativeDateFrom, comparativeDateTo));

        FinancialIndicator roe = findIndicator(result, "PROFITABILITY", "ROE（自己資本利益率）");
        assertThat(roe.previousValue()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(roe.difference()).isEqualByComparingTo(new BigDecimal("23.33"));
        assertThat(roe.changeRate()).isEqualByComparingTo(new BigDecimal("38.88"));

        FinancialIndicator currentRatio = findIndicator(result, "SAFETY", "流動比率");
        assertThat(currentRatio.previousValue()).isEqualByComparingTo(new BigDecimal("266.67"));
        assertThat(currentRatio.difference()).isEqualByComparingTo(new BigDecimal("-16.67"));
        assertThat(currentRatio.changeRate()).isEqualByComparingTo(new BigDecimal("-6.25"));
    }

    @Test
    @DisplayName("前期比較がない場合は previousValue、difference、changeRate が null")
    void shouldNotSetComparativeFieldsWhenNoComparativePeriod() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(Try.success(List.of(
                createBalanceSheetEntity("100", "現金", "ASSET", new BigDecimal("10000")),
                createBalanceSheetEntity("200", "買掛金", "LIABILITY", new BigDecimal("-4000")),
                createBalanceSheetEntity("300", "資本金", "EQUITY", new BigDecimal("-6000"))
        )));
        when(profitAndLossRepository.findProfitAndLoss(null, null)).thenReturn(Try.success(List.of(
                createProfitAndLossEntity("400", "売上", "REVENUE", new BigDecimal("20000")),
                createProfitAndLossEntity("500", "費用", "EXPENSE", new BigDecimal("15000"))
        )));

        GetFinancialAnalysisResult result = service.execute(new GetFinancialAnalysisQuery(null, null, null, null));

        for (IndicatorCategory category : result.categories()) {
            for (FinancialIndicator indicator : category.indicators()) {
                assertThat(indicator.previousValue()).isNull();
                assertThat(indicator.difference()).isNull();
                assertThat(indicator.changeRate()).isNull();
            }
        }
    }

    @Test
    @DisplayName("データがない場合はすべてゼロで計算される")
    void shouldReturnZeroWhenNoData() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(Try.success(List.of()));
        when(profitAndLossRepository.findProfitAndLoss(null, null)).thenReturn(Try.success(List.of()));

        GetFinancialAnalysisResult result = service.execute(new GetFinancialAnalysisQuery(null, null, null, null));

        for (IndicatorCategory category : result.categories()) {
            for (FinancialIndicator indicator : category.indicators()) {
                assertThat(indicator.value()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }
    }

    @Test
    @DisplayName("各指標に formula と industryAverage が設定される")
    void shouldSetFormulaAndIndustryAverage() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(Try.success(List.of()));
        when(profitAndLossRepository.findProfitAndLoss(null, null)).thenReturn(Try.success(List.of()));

        GetFinancialAnalysisResult result = service.execute(new GetFinancialAnalysisQuery(null, null, null, null));

        for (IndicatorCategory category : result.categories()) {
            for (FinancialIndicator indicator : category.indicators()) {
                assertThat(indicator.formula()).isNotBlank();
                assertThat(indicator.industryAverage()).isNotNull();
            }
        }
    }

    private FinancialIndicator findIndicator(GetFinancialAnalysisResult result, String categoryName, String indicatorName) {
        return result.categories().stream()
                .filter(category -> categoryName.equals(category.categoryName()))
                .findFirst()
                .orElseThrow()
                .indicators().stream()
                .filter(indicator -> indicatorName.equals(indicator.name()))
                .findFirst()
                .orElseThrow();
    }

    private void assertIndicatorValue(GetFinancialAnalysisResult result,
                                      String categoryName,
                                      String indicatorName,
                                      BigDecimal expected) {
        FinancialIndicator indicator = findIndicator(result, categoryName, indicatorName);
        assertThat(indicator.value()).isEqualByComparingTo(expected);
    }

    private BalanceSheetEntity createBalanceSheetEntity(String code, String name, String type, BigDecimal balance) {
        BalanceSheetEntity entity = new BalanceSheetEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setAccountType(type);
        entity.setTotalDebit(BigDecimal.ZERO);
        entity.setTotalCredit(BigDecimal.ZERO);
        entity.setBalance(balance);
        return entity;
    }

    private ProfitAndLossEntity createProfitAndLossEntity(String code, String name, String type, BigDecimal amount) {
        ProfitAndLossEntity entity = new ProfitAndLossEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setAccountType(type);
        entity.setTotalDebit(BigDecimal.ZERO);
        entity.setTotalCredit(BigDecimal.ZERO);
        entity.setAmount(amount);
        return entity;
    }
}
