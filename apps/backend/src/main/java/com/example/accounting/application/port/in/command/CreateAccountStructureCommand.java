package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

public record CreateAccountStructureCommand(
        String accountCode,
        String parentAccountCode,
        int displayOrder
) {
    public static Either<String, CreateAccountStructureCommand> of(
            String accountCode,
            String parentAccountCode,
            int displayOrder
    ) {
        if (accountCode == null || accountCode.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        return Either.right(new CreateAccountStructureCommand(accountCode, normalizeParentCode(parentAccountCode), displayOrder));
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private static String normalizeParentCode(String parentAccountCode) {
        if (parentAccountCode == null || parentAccountCode.isBlank()) {
            return null;
        }
        return parentAccountCode;
    }
}
