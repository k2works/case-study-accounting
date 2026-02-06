package com.example.accounting.application.port.in.query;

import java.time.LocalDate;
import java.util.Objects;

public record GetGeneralLedgerQuery(
        Integer accountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
) {
    public GetGeneralLedgerQuery {
        Objects.requireNonNull(accountId, "accountId must not be null");
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be 1-100");
        }
    }
}
