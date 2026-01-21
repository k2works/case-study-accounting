package com.example.accounting.application.port.in;

public interface DeleteAccountUseCase {
    DeleteAccountResult execute(DeleteAccountCommand command);
}
