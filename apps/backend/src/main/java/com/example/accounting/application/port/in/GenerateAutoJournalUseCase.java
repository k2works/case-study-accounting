package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.GenerateAutoJournalCommand;
import com.example.accounting.application.port.out.GenerateAutoJournalResult;

public interface GenerateAutoJournalUseCase {
    GenerateAutoJournalResult execute(GenerateAutoJournalCommand command);
}
