package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.UpdateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.UpdateAutoJournalPatternResult;

public interface UpdateAutoJournalPatternUseCase {
    UpdateAutoJournalPatternResult execute(UpdateAutoJournalPatternCommand command);
}
