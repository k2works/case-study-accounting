package com.example.accounting.application.port.in.command;

import java.util.List;

public record CreateAutoJournalPatternCommand(
        String patternCode,
        String patternName,
        String sourceTableName,
        String description,
        List<PatternItemCommand> items
) {
    public CreateAutoJournalPatternCommand {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public record PatternItemCommand(
            Integer lineNumber,
            String debitCreditType,
            String accountCode,
            String amountFormula,
            String descriptionTemplate
    ) {
    }
}
