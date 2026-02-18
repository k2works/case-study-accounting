package com.example.accounting.application.port.in.command;

import io.vavr.control.Either;

public record UpdateAccountStructureCommand(
        String accountCode,
        String parentAccountCode,
        int displayOrder
) {
    public static Either<String, UpdateAccountStructureCommand> of(
            String accountCode,
            String parentAccountCode,
            int displayOrder
    ) {
        if (accountCode == null || accountCode.isBlank()) {
            return Either.left("勘定科目コードは必須です");
        }
        return Either.right(new UpdateAccountStructureCommand(accountCode, normalizeParentCode(parentAccountCode), displayOrder));
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private static String normalizeParentCode(String parentAccountCode) {
        if (parentAccountCode == null || parentAccountCode.isBlank()) {
            return null;
        }
        return parentAccountCode;
    }
}
