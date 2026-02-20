package com.example.accounting.application.port.out;

public record UpdateAccountStructureResult(
        boolean success,
        String accountCode,
        String accountPath,
        int hierarchyLevel,
        String parentAccountCode,
        int displayOrder,
        String message,
        String errorMessage
) {
    public static UpdateAccountStructureResult success(
            String accountCode,
            String accountPath,
            int hierarchyLevel,
            String parentAccountCode,
            int displayOrder,
            String message
    ) {
        return new UpdateAccountStructureResult(
                true,
                accountCode,
                accountPath,
                hierarchyLevel,
                parentAccountCode,
                displayOrder,
                message,
                null
        );
    }

    public static UpdateAccountStructureResult failure(String errorMessage) {
        return new UpdateAccountStructureResult(false, null, null, 0, null, 0, null, errorMessage);
    }
}
