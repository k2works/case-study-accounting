package com.example.accounting.application.port.in.query;

import java.time.LocalDate;

/**
 * 残高試算表照会クエリ
 */
public record GetTrialBalanceQuery(
        LocalDate date
) {
    public GetTrialBalanceQuery {
        // date は null 許容（null の場合は全期間）
    }
}
