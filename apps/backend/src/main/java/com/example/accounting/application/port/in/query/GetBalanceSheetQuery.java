package com.example.accounting.application.port.in.query;

import java.time.LocalDate;

/**
 * 貸借対照表照会クエリ
 */
public record GetBalanceSheetQuery(
        LocalDate date,
        LocalDate comparativeDate
) {
    public GetBalanceSheetQuery {
        // date は null 許容（null の場合は全期間）
        // comparativeDate は null 許容（null の場合は前期比較なし）
    }
}
