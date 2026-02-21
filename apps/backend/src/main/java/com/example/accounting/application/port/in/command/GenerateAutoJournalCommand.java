package com.example.accounting.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

public record GenerateAutoJournalCommand(
        Long patternId,
        Map<String, BigDecimal> amounts,
        LocalDate journalDate,
        String description,
        String createdByUserId
) {
    public GenerateAutoJournalCommand {
        Objects.requireNonNull(patternId, "パターンIDは必須です");
        Objects.requireNonNull(amounts, "金額パラメータは必須です");
        Objects.requireNonNull(journalDate, "仕訳日は必須です");
        Objects.requireNonNull(createdByUserId, "作成者ユーザーIDは必須です");
        amounts = Map.copyOf(amounts);
    }
}
