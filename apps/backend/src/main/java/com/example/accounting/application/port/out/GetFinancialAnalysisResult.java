package com.example.accounting.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 財務分析照会結果
 */
public record GetFinancialAnalysisResult(
        LocalDate dateFrom,
        LocalDate dateTo,
        LocalDate comparativeDateFrom,
        LocalDate comparativeDateTo,
        List<IndicatorCategory> categories
) {
    public GetFinancialAnalysisResult {
        categories = categories == null ? List.of() : List.copyOf(categories);
    }

    /**
     * 指標カテゴリ（収益性・安全性・効率性）
     */
    public record IndicatorCategory(
            String categoryName,
            String categoryDisplayName,
            List<FinancialIndicator> indicators
    ) {
        public IndicatorCategory {
            indicators = indicators == null ? List.of() : List.copyOf(indicators);
        }
    }

    /**
     * 財務指標
     */
    public record FinancialIndicator(
            String name,
            String unit,
            BigDecimal value,
            BigDecimal previousValue,
            BigDecimal difference,
            BigDecimal changeRate,
            String formula,
            BigDecimal industryAverage
    ) {
    }
}
