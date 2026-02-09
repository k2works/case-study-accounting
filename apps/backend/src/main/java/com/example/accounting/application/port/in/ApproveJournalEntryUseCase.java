package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.ApproveJournalEntryCommand;
import com.example.accounting.application.port.out.ApproveJournalEntryResult;

/**
 * 仕訳承認ユースケース
 */
public interface ApproveJournalEntryUseCase {
    ApproveJournalEntryResult execute(ApproveJournalEntryCommand command);
}
