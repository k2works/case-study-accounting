package com.example.accounting.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 仕訳編集コマンド
 *
 * @param journalEntryId   仕訳ID
 * @param journalDate      仕訳日
 * @param description      摘要
 * @param lines            仕訳明細
 * @param version          楽観的ロック用バージョン
 */
public record UpdateJournalEntryCommand(
        Integer journalEntryId,
        LocalDate journalDate,
        String description,
        List<JournalEntryLineInput> lines,
        Integer version
) {
    /**
     * コンパクトコンストラクタ - 防御的コピーを作成
     */
    public UpdateJournalEntryCommand {
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
