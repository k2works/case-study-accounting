package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.RejectJournalEntryCommand;
import com.example.accounting.application.port.out.RejectJournalEntryResult;

/**
 * 仕訳差し戻しユースケース
 */
public interface RejectJournalEntryUseCase {
    RejectJournalEntryResult execute(RejectJournalEntryCommand command);
}
