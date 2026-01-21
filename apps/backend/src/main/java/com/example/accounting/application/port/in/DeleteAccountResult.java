package com.example.accounting.application.port.in;

public record DeleteAccountResult(
        boolean success,
        Integer accountId,
        String message,
        String errorMessage
) {
    public static DeleteAccountResult success(Integer accountId) {
        return new DeleteAccountResult(true, accountId, "勘定科目を削除しました", null);
    }

    public static DeleteAccountResult failure(String errorMessage) {
        return new DeleteAccountResult(false, null, null, errorMessage);
    }
}
