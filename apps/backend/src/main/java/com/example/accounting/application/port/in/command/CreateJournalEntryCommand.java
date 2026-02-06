package com.example.accounting.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳登録コマンド
 *
 * @param journalDate      仕訳日
 * @param description      摘要
 * @param createdByUserId  作成者ユーザーID
 * @param lines            仕訳明細
 */
public record CreateJournalEntryCommand(
        LocalDate journalDate,
        String description,
        String createdByUserId,
        List<JournalEntryLineInput> lines
) {
    /**
     * コンパクトコンストラクタ - 防御的コピーを作成
     */
    public CreateJournalEntryCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public record JournalEntryLineInput(
            Integer lineNumber,
            Integer accountId,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {
    }
}
