package com.example.accounting.application.port.in.query;

import java.util.Objects;

public record GetMonthlyBalanceQuery(
        String accountCode,
        Integer fiscalPeriod
) {
    public GetMonthlyBalanceQuery {
        Objects.requireNonNull(accountCode, "accountCode must not be null");
    }
}
