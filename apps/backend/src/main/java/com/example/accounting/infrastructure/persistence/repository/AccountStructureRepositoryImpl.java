package com.example.accounting.infrastructure.persistence.repository;

import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.infrastructure.persistence.entity.AccountStructureEntity;
import com.example.accounting.infrastructure.persistence.mapper.AccountStructureMapper;
import io.vavr.control.Try;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountStructureRepositoryImpl implements AccountStructureRepository {

    private final AccountStructureMapper accountStructureMapper;

    public AccountStructureRepositoryImpl(AccountStructureMapper accountStructureMapper) {
        this.accountStructureMapper = accountStructureMapper;
    }

    @Override
    public Try<AccountStructure> save(AccountStructure accountStructure) {
        return Try.of(() -> {
            AccountStructureEntity entity = AccountStructureEntity.fromDomain(accountStructure);

            if (accountStructureMapper.existsByCode(entity.getAccountCode())) {
                accountStructureMapper.update(entity);
            } else {
                accountStructureMapper.insert(entity);
            }

            return accountStructureMapper.findByCode(entity.getAccountCode())
                    .map(AccountStructureEntity::toDomain)
                    .orElseThrow(() -> new IllegalStateException("保存後の勘定科目構成取得に失敗しました"));
        });
    }

    @Override
    public Try<Optional<AccountStructure>> findByCode(String accountCode) {
        return Try.of(() -> accountStructureMapper.findByCode(accountCode)
                .map(AccountStructureEntity::toDomain));
    }

    @Override
    public Try<List<AccountStructure>> findAll() {
        return Try.of(() -> accountStructureMapper.findAll().stream()
                .map(AccountStructureEntity::toDomain)
                .toList());
    }

    @Override
    public Try<List<AccountStructure>> findByParentCode(String parentAccountCode) {
        return Try.of(() -> accountStructureMapper.findByParentCode(parentAccountCode).stream()
                .map(AccountStructureEntity::toDomain)
                .toList());
    }

    @Override
    public Try<Void> deleteByCode(String accountCode) {
        return Try.run(() -> accountStructureMapper.deleteByCode(accountCode));
    }

    @Override
    public Try<Boolean> existsByCode(String accountCode) {
        return Try.of(() -> accountStructureMapper.existsByCode(accountCode));
    }

    @Override
    public Try<Boolean> hasCircularReference(String accountCode, String parentAccountCode) {
        return Try.of(() -> {
            if (parentAccountCode == null || parentAccountCode.isBlank()) {
                return false;
            }
            return accountStructureMapper.findByCode(parentAccountCode)
                    .map(parent -> containsCodeInPath(parent.getAccountPath(), accountCode))
                    .orElse(false);
        });
    }

    private boolean containsCodeInPath(String path, String accountCode) {
        return java.util.Arrays.asList(path.split("~")).contains(accountCode);
    }
}
