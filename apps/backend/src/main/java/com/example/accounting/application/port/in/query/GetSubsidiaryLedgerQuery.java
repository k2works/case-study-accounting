package com.example.accounting.application.port.in.query;

import io.vavr.control.Either;

import java.time.LocalDate;

public record GetSubsidiaryLedgerQuery(
        String accountCode,
        String subAccountCode,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
) {

    public static Either<String, GetSubsidiaryLedgerQuery> of(
            String accountCode,
            String subAccountCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        if (accountCode == null) {
            return Either.left("勘定科目コードは必須です");
        }
        if (page < 0) {
            return Either.left("ページ番号は 0 以上である必要があります");
        }
        if (size < 1 || size > 100) {
            return Either.left("ページサイズは 1 以上 100 以下である必要があります");
        }
        return Either.right(new GetSubsidiaryLedgerQuery(accountCode, subAccountCode, dateFrom, dateTo, page, size));
    }
}
