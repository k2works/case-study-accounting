package com.example.accounting.application.port.out;

/**
 * 仕訳承認申請結果
 */
public record SubmitForApprovalResult(
        boolean success,
        Integer journalEntryId,
        String status,
        String message,
        String errorMessage
) {
    public static SubmitForApprovalResult success(Integer journalEntryId, String status) {
        return new SubmitForApprovalResult(true, journalEntryId, status, "仕訳を承認申請しました", null);
    }

    public static SubmitForApprovalResult failure(String errorMessage) {
        return new SubmitForApprovalResult(false, null, null, null, errorMessage);
    }
}
