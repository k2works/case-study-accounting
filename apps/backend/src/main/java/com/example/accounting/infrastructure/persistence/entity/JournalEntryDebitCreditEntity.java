package com.example.accounting.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 仕訳貸借明細エンティティ（永続化用）
 * 第3層：借方・貸方の詳細情報を保持
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyFields"})
public class JournalEntryDebitCreditEntity {

    private Integer journalEntryId;
    private Integer lineNumber;
    private String debitCreditType;  // D=借方, C=貸方
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String departmentCode;
    private String projectCode;
    private String accountCode;
    private String subAccountCode;
    private BigDecimal amount;
    private BigDecimal baseCurrencyAmount;
    private String taxCategory;
    private Integer taxRate;
    private String taxCalculationCategory;
    private LocalDate dueDate;
    private Integer cashFlowFlag;
    private String segmentCode;
    private String counterAccountCode;
    private String counterSubAccountCode;
    private String tagCode;
    private String tagContent;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Getters and Setters
    public Integer getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Integer journalEntryId) {
        this.journalEntryId = journalEntryId;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getSubAccountCode() {
        return subAccountCode;
    }

    public void setSubAccountCode(String subAccountCode) {
        this.subAccountCode = subAccountCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBaseCurrencyAmount() {
        return baseCurrencyAmount;
    }

    public void setBaseCurrencyAmount(BigDecimal baseCurrencyAmount) {
        this.baseCurrencyAmount = baseCurrencyAmount;
    }

    public String getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(String taxCategory) {
        this.taxCategory = taxCategory;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxCalculationCategory() {
        return taxCalculationCategory;
    }

    public void setTaxCalculationCategory(String taxCalculationCategory) {
        this.taxCalculationCategory = taxCalculationCategory;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getCashFlowFlag() {
        return cashFlowFlag;
    }

    public void setCashFlowFlag(Integer cashFlowFlag) {
        this.cashFlowFlag = cashFlowFlag;
    }

    public String getSegmentCode() {
        return segmentCode;
    }

    public void setSegmentCode(String segmentCode) {
        this.segmentCode = segmentCode;
    }

    public String getCounterAccountCode() {
        return counterAccountCode;
    }

    public void setCounterAccountCode(String counterAccountCode) {
        this.counterAccountCode = counterAccountCode;
    }

    public String getCounterSubAccountCode() {
        return counterSubAccountCode;
    }

    public void setCounterSubAccountCode(String counterSubAccountCode) {
        this.counterSubAccountCode = counterSubAccountCode;
    }

    public String getTagCode() {
        return tagCode;
    }

    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }

    public String getTagContent() {
        return tagContent;
    }

    public void setTagContent(String tagContent) {
        this.tagContent = tagContent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
