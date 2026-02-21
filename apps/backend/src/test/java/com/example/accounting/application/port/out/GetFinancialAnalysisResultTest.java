package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetFinancialAnalysisResult.FinancialIndicator;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.IndicatorCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetFinancialAnalysisResultTest {

    @Test
    void shouldDefaultCategoriesToEmptyListWhenNull() {
        GetFinancialAnalysisResult result = new GetFinancialAnalysisResult(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                null,
                null,
                null
        );

        assertThat(result.categories()).isEmpty();
        assertThatThrownBy(() -> result.categories().add(buildCategory()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldCopyCategoriesAndIndicatorsList() {
        List<FinancialIndicator> indicators = new ArrayList<>();
        indicators.add(buildIndicator());

        List<IndicatorCategory> categories = new ArrayList<>();
        categories.add(new IndicatorCategory("profitability", "収益性", indicators));

        GetFinancialAnalysisResult result = new GetFinancialAnalysisResult(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                categories
        );

        indicators.clear();
        categories.clear();

        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().getFirst().indicators()).hasSize(1);
        assertThatThrownBy(() -> result.categories().add(buildCategory()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.categories().getFirst().indicators().add(buildIndicator()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private IndicatorCategory buildCategory() {
        return new IndicatorCategory("profitability", "収益性", List.of(buildIndicator()));
    }

    private FinancialIndicator buildIndicator() {
        return new FinancialIndicator(
                "ROA",
                "%",
                new BigDecimal("10.5"),
                new BigDecimal("9.8"),
                new BigDecimal("0.7"),
                new BigDecimal("7.14"),
                "当期純利益 ÷ 総資産 × 100",
                new BigDecimal("8.5")
        );
    }
}
