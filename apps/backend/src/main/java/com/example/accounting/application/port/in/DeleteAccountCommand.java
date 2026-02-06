package com.example.accounting.application.port.in;

public record DeleteAccountCommand(Integer accountId) {
    public DeleteAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("勘定科目IDは必須です");
        }
    }
}
