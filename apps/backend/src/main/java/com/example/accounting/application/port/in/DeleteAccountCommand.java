package com.example.accounting.application.port.in;

import io.vavr.control.Either;

public record DeleteAccountCommand(Integer accountId) {

    public static Either<String, DeleteAccountCommand> of(Integer accountId) {
        if (accountId == null) {
            return Either.left("勘定科目IDは必須です");
        }
        return Either.right(new DeleteAccountCommand(accountId));
    }
}
