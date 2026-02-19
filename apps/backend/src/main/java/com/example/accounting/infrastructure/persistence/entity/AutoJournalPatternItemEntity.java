package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;

/**
 * 自動仕訳パターン明細エンティティ（永続化用）
 */
public class AutoJournalPatternItemEntity {

    private Long id;
    private Long patternId;
    private Integer lineNumber;
    private String debitCreditType;
    private String accountCode;
    private String amountFormula;
    private String descriptionTemplate;

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static AutoJournalPatternItemEntity fromDomain(AutoJournalPatternItem item, Long patternId) {
        AutoJournalPatternItemEntity entity = new AutoJournalPatternItemEntity();
        entity.setPatternId(patternId);
        entity.setLineNumber(item.getLineNumber());
        entity.setDebitCreditType(item.getDebitCreditType());
        entity.setAccountCode(item.getAccountCode());
        entity.setAmountFormula(item.getAmountFormula());
        entity.setDescriptionTemplate(item.getDescriptionTemplate());
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public AutoJournalPatternItem toDomain() {
        return AutoJournalPatternItem.reconstruct(
                lineNumber,
                debitCreditType,
                accountCode,
                amountFormula,
                descriptionTemplate
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatternId() {
        return patternId;
    }

    public void setPatternId(Long patternId) {
        this.patternId = patternId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDebitCreditType() {
        return debitCreditType;
    }

    public void setDebitCreditType(String debitCreditType) {
        this.debitCreditType = debitCreditType;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAmountFormula() {
        return amountFormula;
    }

    public void setAmountFormula(String amountFormula) {
        this.amountFormula = amountFormula;
    }

    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }

    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }
}
