package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

public record DeleteAccountStructureCommand(String accountCode) {

    public static Either<String, DeleteAccountStructureCommand> of(String accountCode) {
        if (accountCode == null || accountCode.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        return Either.right(new DeleteAccountStructureCommand(accountCode));
    }
}
