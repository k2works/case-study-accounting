package com.example.accounting.infrastructure.web.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "record の amounts は Map.copyOf で不変コピー済み")
public record GenerateAutoJournalRequest(
        @NotNull(message = "パターンIDは必須です")
        Long patternId,
        @NotNull(message = "金額パラメータは必須です")
        Map<String, BigDecimal> amounts,
        @NotNull(message = "仕訳日は必須です")
        LocalDate journalDate,
        String description
) {
    public GenerateAutoJournalRequest {
        if (amounts != null) {
            amounts = Map.copyOf(amounts);
        }
    }
}
