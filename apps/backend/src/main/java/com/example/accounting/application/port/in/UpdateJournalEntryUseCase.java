package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.UpdateJournalEntryCommand;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;

/**
 * 仕訳編集ユースケース
 */
public interface UpdateJournalEntryUseCase {
    UpdateJournalEntryResult execute(UpdateJournalEntryCommand command);
}
