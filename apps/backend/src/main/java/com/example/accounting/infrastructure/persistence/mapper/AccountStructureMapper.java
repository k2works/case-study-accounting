package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.AccountStructureEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AccountStructureMapper {

    void insert(AccountStructureEntity entity);

    int update(AccountStructureEntity entity);

    Optional<AccountStructureEntity> findByCode(String accountCode);

    List<AccountStructureEntity> findAll();

    List<AccountStructureEntity> findByParentCode(String parentAccountCode);

    void deleteByCode(String accountCode);

    boolean existsByCode(String accountCode);
}
