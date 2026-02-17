package com.example.accounting.application.port.in.query;

import io.vavr.control.Either;

import java.time.LocalDate;

public record GetGeneralLedgerQuery(
        Integer accountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size
) {

    public static Either<String, GetGeneralLedgerQuery> of(
            Integer accountId,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    ) {
        if (accountId == null) {
            return Either.left("勘定科目 ID は必須です");
        }
        if (page < 0) {
            return Either.left("ページ番号は 0 以上である必要があります");
        }
        if (size < 1 || size > 100) {
            return Either.left("ページサイズは 1 以上 100 以下である必要があります");
        }
        return Either.right(new GetGeneralLedgerQuery(accountId, dateFrom, dateTo, page, size));
    }
}
