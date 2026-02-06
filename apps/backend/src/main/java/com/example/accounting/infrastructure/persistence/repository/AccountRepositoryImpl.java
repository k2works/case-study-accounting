package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.AccountEntity;
import com.example.accounting.infrastructure.persistence.mapper.AccountMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 勘定科目リポジトリ実装
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountMapper accountMapper;

    public AccountRepositoryImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountEntity.fromDomain(account);

        // id が null でなく、既存レコードが存在する場合は更新
        if (entity.getId() != null && accountMapper.findById(entity.getId()).isPresent()) {
            accountMapper.update(entity);
        } else {
            // INSERT 後、自動生成された id が entity にセットされる
            accountMapper.insert(entity);
        }

        return accountMapper.findById(entity.getId())
                .map(AccountEntity::toDomain)
                .orElseThrow(() -> new IllegalStateException("保存後の勘定科目取得に失敗しました"));
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return accountMapper.findById(id.value())
                .map(AccountEntity::toDomain);
    }

    @Override
    public Optional<Account> findByCode(AccountCode code) {
        return accountMapper.findByCode(code.value())
                .map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return accountMapper.findAll().stream()
                .map(AccountEntity::toDomain)
                .toList();
    }

    @Override
    public List<Account> findByType(AccountType type) {
        return accountMapper.findByType(type.name()).stream()
                .map(AccountEntity::toDomain)
                .toList();
    }

    @Override
    public List<Account> search(AccountType type, String keyword) {
        String accountTypeStr = type != null ? type.name() : null;
        return accountMapper.search(accountTypeStr, keyword).stream()
                .map(AccountEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(AccountId id) {
        accountMapper.deleteById(id.value());
    }

    @Override
    public boolean existsByCode(AccountCode code) {
        return accountMapper.existsByCode(code.value());
    }
}
