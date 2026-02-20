package com.example.accounting.domain.model.auto_journal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.Objects;

@Value
@With
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.AvoidThrowStatement")
public class AutoJournalPatternItem {
    Integer lineNumber;
    String debitCreditType;
    String accountCode;
    String amountFormula;
    String descriptionTemplate;

    public static AutoJournalPatternItem create(Integer lineNumber,
                                                String debitCreditType,
                                                String accountCode,
                                                String amountFormula,
                                                String descriptionTemplate) {
        Objects.requireNonNull(lineNumber, "行番号は必須です");
        Objects.requireNonNull(debitCreditType, "貸借区分は必須です");
        Objects.requireNonNull(accountCode, "勘定科目コードは必須です");
        Objects.requireNonNull(amountFormula, "金額計算式は必須です");
        if (!"D".equals(debitCreditType) && !"C".equals(debitCreditType)) {
            throw new IllegalArgumentException("貸借区分は 'D' または 'C' である必要があります");
        }
        return new AutoJournalPatternItem(lineNumber, debitCreditType, accountCode, amountFormula, descriptionTemplate);
    }

    public static AutoJournalPatternItem reconstruct(Integer lineNumber,
                                                     String debitCreditType,
                                                     String accountCode,
                                                     String amountFormula,
                                                     String descriptionTemplate) {
        return new AutoJournalPatternItem(lineNumber, debitCreditType, accountCode, amountFormula, descriptionTemplate);
    }
}
