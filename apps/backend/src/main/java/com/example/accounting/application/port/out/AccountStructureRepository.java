package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.account.AccountStructure;
import io.vavr.control.Try;

import java.util.List;
import java.util.Optional;

public interface AccountStructureRepository {

    Try<AccountStructure> save(AccountStructure accountStructure);

    Try<Optional<AccountStructure>> findByCode(String accountCode);

    Try<List<AccountStructure>> findAll();

    Try<List<AccountStructure>> findByParentCode(String parentAccountCode);

    Try<Void> deleteByCode(String accountCode);

    Try<Boolean> existsByCode(String accountCode);

    @SuppressWarnings("PMD.LinguisticNaming")
    Try<Boolean> hasCircularReference(String accountCode, String parentAccountCode);
}
