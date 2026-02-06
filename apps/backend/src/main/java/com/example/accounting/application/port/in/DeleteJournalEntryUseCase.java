package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;

/**
 * 仕訳削除ユースケース
 */
public interface DeleteJournalEntryUseCase {
    DeleteJournalEntryResult execute(DeleteJournalEntryCommand command);
}
