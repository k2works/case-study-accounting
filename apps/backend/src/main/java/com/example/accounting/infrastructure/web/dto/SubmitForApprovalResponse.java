package com.example.accounting.infrastructure.web.dto;

/**
 * 仕訳承認申請レスポンス
 */
public record SubmitForApprovalResponse(
        boolean success,
        Integer journalEntryId,
        String status,
        String message,
        String errorMessage
) {
    public static SubmitForApprovalResponse success(Integer journalEntryId, String status, String message) {
        return new SubmitForApprovalResponse(true, journalEntryId, status, message, null);
    }

    public static SubmitForApprovalResponse failure(String errorMessage) {
        return new SubmitForApprovalResponse(false, null, null, null, errorMessage);
    }
}
