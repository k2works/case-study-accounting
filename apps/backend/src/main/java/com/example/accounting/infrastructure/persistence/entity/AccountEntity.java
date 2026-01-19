package com.example.accounting.infrastructure.persistence.entity;

import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;

import java.time.OffsetDateTime;

/**
 * 勘定科目エンティティ（永続化用）
 */
public class AccountEntity {

    private Integer id;
    private String code;
    private String name;
    private String accountType;
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
}
