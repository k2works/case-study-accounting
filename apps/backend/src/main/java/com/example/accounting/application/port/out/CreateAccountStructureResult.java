package com.example.accounting.application.port.out;

public record CreateAccountStructureResult(
        boolean success,
        String accountCode,
        String accountPath,
        int hierarchyLevel,
        String parentAccountCode,
        int displayOrder,
        String errorMessage
) {
    public static CreateAccountStructureResult success(
            String accountCode,
            String accountPath,
            int hierarchyLevel,
            String parentAccountCode,
            int displayOrder
    ) {
        return new CreateAccountStructureResult(
                true,
                accountCode,
                accountPath,
                hierarchyLevel,
                parentAccountCode,
                displayOrder,
                null
        );
    }

    public static CreateAccountStructureResult failure(String errorMessage) {
        return new CreateAccountStructureResult(false, null, null, 0, null, 0, errorMessage);
    }
}
