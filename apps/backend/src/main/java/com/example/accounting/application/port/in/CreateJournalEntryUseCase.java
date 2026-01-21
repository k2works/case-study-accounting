package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.out.CreateJournalEntryResult;

/**
 * 仕訳登録ユースケース
 */
public interface CreateJournalEntryUseCase {
    CreateJournalEntryResult execute(CreateJournalEntryCommand command);
}
