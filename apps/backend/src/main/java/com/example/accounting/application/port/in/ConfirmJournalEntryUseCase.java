package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.ConfirmJournalEntryCommand;
import com.example.accounting.application.port.out.ConfirmJournalEntryResult;

public interface ConfirmJournalEntryUseCase {
    ConfirmJournalEntryResult execute(ConfirmJournalEntryCommand command);
}
