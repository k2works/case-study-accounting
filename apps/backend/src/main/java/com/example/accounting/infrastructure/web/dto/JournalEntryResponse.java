package com.example.accounting.infrastructure.web.dto;

import com.example.accounting.application.port.out.JournalEntryDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "仕訳詳細レスポンス")
public record JournalEntryResponse(
        @Schema(description = "仕訳ID") Integer journalEntryId,
        @Schema(description = "仕訳日") LocalDate journalDate,
        @Schema(description = "摘要") String description,
        @Schema(description = "ステータス") String status,
        @Schema(description = "バージョン") Integer version,
        @Schema(description = "明細行") List<JournalEntryLineResponse> lines
) {
    @Schema(description = "仕訳明細行レスポンス")
    public record JournalEntryLineResponse(
            @Schema(description = "行番号") Integer lineNumber,
            @Schema(description = "勘定科目ID") Integer accountId,
            @Schema(description = "勘定科目コード") String accountCode,
            @Schema(description = "勘定科目名") String accountName,
            @Schema(description = "借方金額") BigDecimal debitAmount,
            @Schema(description = "貸方金額") BigDecimal creditAmount
    ) {
    }

    public static JournalEntryResponse from(JournalEntryDetailResult result) {
        List<JournalEntryLineResponse> lineResponses = result.lines().stream()
                .map(line -> new JournalEntryLineResponse(
                        line.lineNumber(),
                        line.accountId(),
                        line.accountCode(),
                        line.accountName(),
                        line.debitAmount(),
                        line.creditAmount()
                ))
                .toList();
        return new JournalEntryResponse(
                result.journalEntryId(),
                result.journalDate(),
                result.description(),
                result.status(),
                result.version(),
                lineResponses
        );
    }
}
