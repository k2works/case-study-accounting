package com.example.accounting.application.port.out;

public record DeleteAccountStructureResult(
        boolean success,
        String accountCode,
        String message,
        String errorMessage
) {
    public static DeleteAccountStructureResult success(String accountCode) {
        return new DeleteAccountStructureResult(true, accountCode, "勘定科目構成を削除しました", null);
    }

    public static DeleteAccountStructureResult failure(String errorMessage) {
        return new DeleteAccountStructureResult(false, null, null, errorMessage);
    }
}
