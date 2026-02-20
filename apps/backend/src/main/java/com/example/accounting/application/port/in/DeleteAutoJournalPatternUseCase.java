package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.DeleteAutoJournalPatternCommand;
import com.example.accounting.application.port.out.DeleteAutoJournalPatternResult;

public interface DeleteAutoJournalPatternUseCase {
    DeleteAutoJournalPatternResult execute(DeleteAutoJournalPatternCommand command);
}
