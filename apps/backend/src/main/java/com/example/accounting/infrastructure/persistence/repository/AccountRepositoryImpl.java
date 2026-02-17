package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.AccountEntity;
import com.example.accounting.infrastructure.persistence.mapper.AccountMapper;
import io.vavr.control.Try;
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
    public Try<Account> save(Account account) {
        return Try.of(() -> {
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
        });
    }

    @Override
    public Try<Optional<Account>> findById(AccountId id) {
        return Try.of(() -> accountMapper.findById(id.value())
                .map(AccountEntity::toDomain));
    }

    @Override
    public Try<Optional<Account>> findByCode(AccountCode code) {
        return Try.of(() -> accountMapper.findByCode(code.value())
                .map(AccountEntity::toDomain));
    }

    @Override
    public Try<Optional<Account>> findByCode(String code) {
        return Try.of(() -> accountMapper.findByCode(code)
                .map(AccountEntity::toDomain));
    }

    @Override
    public Try<List<Account>> findAll() {
        return Try.of(() -> accountMapper.findAll().stream()
                .map(AccountEntity::toDomain)
                .toList());
    }

    @Override
    public Try<List<Account>> findByType(AccountType type) {
        return Try.of(() -> accountMapper.findByType(type.name()).stream()
                .map(AccountEntity::toDomain)
                .toList());
    }

    @Override
    public Try<List<Account>> search(AccountType type, String keyword) {
        return Try.of(() -> {
            String accountTypeStr = type != null ? type.name() : null;
            return accountMapper.search(accountTypeStr, keyword).stream()
                    .map(AccountEntity::toDomain)
                    .toList();
        });
    }

    @Override
    public Try<Void> deleteById(AccountId id) {
        return Try.run(() -> accountMapper.deleteById(id.value()));
    }

    @Override
    public Try<Boolean> existsByCode(AccountCode code) {
        return Try.of(() -> accountMapper.existsByCode(code.value()));
    }
}
