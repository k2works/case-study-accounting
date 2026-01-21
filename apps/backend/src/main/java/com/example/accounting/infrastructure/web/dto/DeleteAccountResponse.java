package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.in.DeleteAccountResult;

public record DeleteAccountResponse(
        boolean success,
        Integer accountId,
        String message,
        String errorMessage
) {
    public static DeleteAccountResponse from(DeleteAccountResult result) {
        return new DeleteAccountResponse(
                result.success(),
                result.accountId(),
                result.message(),
                result.errorMessage()
        );
    }
}
