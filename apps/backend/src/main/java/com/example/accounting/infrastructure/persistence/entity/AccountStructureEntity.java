package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.AccountStructure;

import java.time.OffsetDateTime;

public class AccountStructureEntity {

    private String accountCode;
    private String accountPath;
    private Integer hierarchyLevel;
    private String parentAccountCode;
    private Integer displayOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AccountStructureEntity fromDomain(AccountStructure accountStructure) {
        AccountStructureEntity entity = new AccountStructureEntity();
        entity.setAccountCode(accountStructure.getAccountCode());
        entity.setAccountPath(accountStructure.getAccountPath());
        entity.setHierarchyLevel(accountStructure.getHierarchyLevel());
        entity.setParentAccountCode(accountStructure.getParentAccountCode());
        entity.setDisplayOrder(accountStructure.getDisplayOrder());
        return entity;
    }

    public AccountStructure toDomain() {
        return AccountStructure.reconstruct(
                accountCode,
                accountPath,
                hierarchyLevel,
                parentAccountCode,
                displayOrder
        );
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountPath() {
        return accountPath;
    }

    public void setAccountPath(String accountPath) {
        this.accountPath = accountPath;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getParentAccountCode() {
        return parentAccountCode;
    }

    public void setParentAccountCode(String parentAccountCode) {
        this.parentAccountCode = parentAccountCode;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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
