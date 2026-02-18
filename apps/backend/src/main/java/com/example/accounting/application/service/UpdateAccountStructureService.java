package com.example.accounting.application.service;

import com.example.accounting.application.port.in.UpdateAccountStructureUseCase;
import com.example.accounting.application.port.in.command.UpdateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.UpdateAccountStructureResult;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateAccountStructureService implements UpdateAccountStructureUseCase {

    private final AccountStructureRepository accountStructureRepository;
    private final AccountRepository accountRepository;

    public UpdateAccountStructureService(AccountStructureRepository accountStructureRepository,
                                         AccountRepository accountRepository) {
        this.accountStructureRepository = accountStructureRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public UpdateAccountStructureResult execute(UpdateAccountStructureCommand command) {
        return buildUpdateProgram(command).unsafeRun();
    }

    IO<UpdateAccountStructureResult> buildUpdateProgram(UpdateAccountStructureCommand command) {
        return findAccountStructureIO(command.accountCode())
                .flatMap(either -> applyUpdateValidationIO(command, either))
                .map(either -> either.flatMap(ignored -> updateFromCommand(command)))
                .flatMap(this::processUpdateResult);
    }

    private IO<Either<String, AccountStructure>> applyUpdateValidationIO(
            UpdateAccountStructureCommand command,
            Either<String, AccountStructure> structureResult
    ) {
        return validateUpdateIO(command)
                .map(validationResult -> structureResult.flatMap(
                        structure -> validationResult.map(ignored -> structure)
                ));
    }

    private IO<Either<String, AccountStructure>> findAccountStructureIO(String accountCode) {
        return IO.delay(() -> accountStructureRepository.findByCode(accountCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, AccountStructure>>map(Either::right)
                .orElseGet(() -> Either.left("勘定科目構成が見つかりません")));
    }

    private IO<Either<String, Void>> validateUpdateIO(UpdateAccountStructureCommand command) {
        return IO.delay(() -> validateAccount(command.accountCode())
                .flatMap(v -> validateUpdateParent(command)));
    }

    private Either<String, Void> validateAccount(String accountCode) {
        if (!accountRepository.findByCode(accountCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)).isPresent()) {
            return Either.left("勘定科目が存在しません");
        }
        return Either.right(null);
    }

    private Either<String, Void> validateUpdateParent(UpdateAccountStructureCommand command) {
        String parentCode = command.parentAccountCode();
        if (parentCode == null) {
            return Either.right(null);
        }
        if (command.accountCode().equals(parentCode)) {
            return Either.left("自分自身を親勘定科目にはできません");
        }
        if (!accountRepository.findByCode(parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)).isPresent()) {
            return Either.left("親勘定科目が存在しません");
        }
        if (!accountStructureRepository.findByCode(parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)).isPresent()) {
            return Either.left("親勘定科目の構成が存在しません");
        }
        if (accountStructureRepository.hasCircularReference(command.accountCode(), parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
            return Either.left("循環参照になるため更新できません");
        }
        return Either.right(null);
    }

    private Either<String, AccountStructure> updateFromCommand(UpdateAccountStructureCommand command) {
        String parentPath = resolveParentPath(command.parentAccountCode());
        return AccountStructure.create(
                command.accountCode(),
                command.parentAccountCode(),
                parentPath,
                command.displayOrder()
        );
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private String resolveParentPath(String parentAccountCode) {
        if (parentAccountCode == null) {
            return null;
        }
        return accountStructureRepository.findByCode(parentAccountCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .map(AccountStructure::getAccountPath)
                .orElse(null);
    }

    private IO<UpdateAccountStructureResult> processUpdateResult(Either<String, AccountStructure> result) {
        return result.fold(
                error -> IO.pure(UpdateAccountStructureResult.failure(error)),
                accountStructure -> updateAccountStructureIO(accountStructure).map(this::toSuccessResult)
        );
    }

    private IO<AccountStructure> updateAccountStructureIO(AccountStructure accountStructure) {
        return IO.delay(() -> accountStructureRepository.save(accountStructure)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private UpdateAccountStructureResult toSuccessResult(AccountStructure accountStructure) {
        return UpdateAccountStructureResult.success(
                accountStructure.getAccountCode(),
                accountStructure.getAccountPath(),
                accountStructure.getHierarchyLevel(),
                accountStructure.getParentAccountCode(),
                accountStructure.getDisplayOrder(),
                "勘定科目構成を更新しました"
        );
    }
}
