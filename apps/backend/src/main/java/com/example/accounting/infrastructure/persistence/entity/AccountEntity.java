package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;

import java.time.OffsetDateTime;

/**
 * 勘定科目エンティティ（永続化用）
 */
@SuppressWarnings({"PMD.BooleanGetMethodName"})
public class AccountEntity {

    private Integer id;
    private String code;
    private String name;
    private String accountType;
    private String kana;
    private String bsplCategory;
    private String transactionElementCategory;
    private String expenseCategory;
    private Boolean summaryAccount;
    private Integer displayOrder;
    private Boolean aggregationTarget;
    private java.math.BigDecimal balance;
    private String taxTransactionCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * ドメインモデルからエンティティを生成する
     */
    public static AccountEntity fromDomain(Account account) {
        AccountEntity entity = new AccountEntity();
        // id は null の場合がある（新規作成時）
        if (account.getId() != null) {
            entity.setId(account.getId().value());
        }
        entity.setCode(account.getAccountCode().value());
        entity.setName(account.getAccountName());
        entity.setAccountType(account.getAccountType().name());
        return entity;
    }

    /**
     * エンティティからドメインモデルを再構築する
     */
    public Account toDomain() {
        return Account.reconstruct(
                AccountId.of(id),
                AccountCode.reconstruct(code),
                name,
                AccountType.fromCode(accountType)
        );
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public String getKana() {
        return kana;
    }

    public void setKana(String kana) {
        this.kana = kana;
    }

    public String getBsplCategory() {
        return bsplCategory;
    }

    public void setBsplCategory(String bsplCategory) {
        this.bsplCategory = bsplCategory;
    }

    public String getTransactionElementCategory() {
        return transactionElementCategory;
    }

    public void setTransactionElementCategory(String transactionElementCategory) {
        this.transactionElementCategory = transactionElementCategory;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public Boolean getSummaryAccount() {
        return summaryAccount;
    }

    public void setSummaryAccount(Boolean summaryAccount) {
        this.summaryAccount = summaryAccount;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getAggregationTarget() {
        return aggregationTarget;
    }

    public void setAggregationTarget(Boolean aggregationTarget) {
        this.aggregationTarget = aggregationTarget;
    }

    public java.math.BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(java.math.BigDecimal balance) {
        this.balance = balance;
    }

    public String getTaxTransactionCode() {
        return taxTransactionCode;
    }

    public void setTaxTransactionCode(String taxTransactionCode) {
        this.taxTransactionCode = taxTransactionCode;
    }
}
