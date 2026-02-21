package com.example.accounting.application.port.in.query;

import java.time.LocalDate;

/**
 * 財務分析照会クエリ
 */
public record GetFinancialAnalysisQuery(
        LocalDate dateFrom,
        LocalDate dateTo,
        LocalDate comparativeDateFrom,
        LocalDate comparativeDateTo
) {
    public GetFinancialAnalysisQuery {
        // dateFrom, dateTo は null 許容（null の場合は全期間）
        // comparativeDateFrom, comparativeDateTo は null 許容（null の場合は前期比較なし）
    }
}
