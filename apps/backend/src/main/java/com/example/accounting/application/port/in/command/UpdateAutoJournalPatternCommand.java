package com.example.accounting.application.port.in.command;

import java.util.List;

public record UpdateAutoJournalPatternCommand(
        Long patternId,
        String patternName,
        String sourceTableName,
        String description,
        Boolean isActive,
        List<CreateAutoJournalPatternCommand.PatternItemCommand> items
) {
    public UpdateAutoJournalPatternCommand {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
