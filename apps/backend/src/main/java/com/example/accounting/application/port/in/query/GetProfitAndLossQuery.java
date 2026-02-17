package com.example.accounting.application.port.in.query;

import java.time.LocalDate;

/**
 * 損益計算書照会クエリ
 */
public record GetProfitAndLossQuery(
        LocalDate dateFrom,
        LocalDate dateTo,
        LocalDate comparativeDateFrom,
        LocalDate comparativeDateTo
) {
    public GetProfitAndLossQuery {
        // dateFrom, dateTo は null 許容（null の場合は全期間）
        // comparativeDateFrom, comparativeDateTo は null 許容（null の場合は前期比較なし）
    }
}
