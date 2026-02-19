package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.CreateAutoJournalPatternResult;

public interface CreateAutoJournalPatternUseCase {
    CreateAutoJournalPatternResult execute(CreateAutoJournalPatternCommand command);
}
