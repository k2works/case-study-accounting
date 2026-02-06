package com.example.accounting.application.port.in.query;

import java.time.LocalDate;
import java.util.Objects;

public record GetDailyBalanceQuery(
        Integer accountId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public GetDailyBalanceQuery {
        Objects.requireNonNull(accountId, "accountId must not be null");
    }
}
