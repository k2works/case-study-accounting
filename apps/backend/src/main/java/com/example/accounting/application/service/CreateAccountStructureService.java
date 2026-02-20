package com.example.accounting.application.service;

import com.example.accounting.application.port.in.CreateAccountStructureUseCase;
import com.example.accounting.application.port.in.command.CreateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.CreateAccountStructureResult;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateAccountStructureService implements CreateAccountStructureUseCase {

    private final AccountStructureRepository accountStructureRepository;
    private final AccountRepository accountRepository;

    public CreateAccountStructureService(AccountStructureRepository accountStructureRepository,
                                         AccountRepository accountRepository) {
        this.accountStructureRepository = accountStructureRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public CreateAccountStructureResult execute(CreateAccountStructureCommand command) {
        return buildCreateProgram(command).unsafeRun();
    }

    IO<CreateAccountStructureResult> buildCreateProgram(CreateAccountStructureCommand command) {
        return validateCreateIO(command)
                .map(either -> either.flatMap(this::createStructureFromCommand))
                .flatMap(this::processCreateResult);
    }

    private IO<Either<String, CreateAccountStructureContext>> validateCreateIO(CreateAccountStructureCommand command) {
        return IO.delay(() -> validateAccountAndStructure(command)
                .flatMap(v -> resolveParentContext(command)));
    }

    private Either<String, Void> validateAccountAndStructure(CreateAccountStructureCommand command) {
        if (!accountRepository.findByCode(command.accountCode())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)).isPresent()) {
            return Either.left("勘定科目が存在しません");
        }
        if (accountStructureRepository.existsByCode(command.accountCode())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
            return Either.left("勘定科目構成は既に登録されています");
        }
        return Either.right(null);
    }

    private Either<String, CreateAccountStructureContext> resolveParentContext(CreateAccountStructureCommand command) {
        String parentCode = command.parentAccountCode();
        if (parentCode == null) {
            return Either.right(new CreateAccountStructureContext(command, null));
        }
        return validateParent(command.accountCode(), parentCode)
                .flatMap(v -> findParentPath(command, parentCode));
    }

    private Either<String, Void> validateParent(String accountCode, String parentCode) {
        if (accountCode.equals(parentCode)) {
            return Either.left("自分自身を親勘定科目にはできません");
        }
        if (!accountRepository.findByCode(parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)).isPresent()) {
            return Either.left("親勘定科目が存在しません");
        }
        if (accountStructureRepository.hasCircularReference(accountCode, parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
            return Either.left("循環参照になるため登録できません");
        }
        return Either.right(null);
    }

    private Either<String, CreateAccountStructureContext> findParentPath(
            CreateAccountStructureCommand command, String parentCode) {
        return accountStructureRepository.findByCode(parentCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, CreateAccountStructureContext>>map(parent -> Either.right(
                        new CreateAccountStructureContext(command, parent.getAccountPath())))
                .orElseGet(() -> Either.left("親勘定科目の構成が存在しません"));
    }

    private Either<String, AccountStructure> createStructureFromCommand(CreateAccountStructureContext context) {
        return AccountStructure.create(
                context.command().accountCode(),
                context.command().parentAccountCode(),
                context.parentPath(),
                context.command().displayOrder()
        );
    }

    private IO<CreateAccountStructureResult> processCreateResult(Either<String, AccountStructure> result) {
        return result.fold(
                error -> IO.pure(CreateAccountStructureResult.failure(error)),
                accountStructure -> createAccountStructureIO(accountStructure).map(this::toSuccessResult)
        );
    }

    private IO<AccountStructure> createAccountStructureIO(AccountStructure accountStructure) {
        return IO.delay(() -> accountStructureRepository.save(accountStructure)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private CreateAccountStructureResult toSuccessResult(AccountStructure accountStructure) {
        return CreateAccountStructureResult.success(
                accountStructure.getAccountCode(),
                accountStructure.getAccountPath(),
                accountStructure.getHierarchyLevel(),
                accountStructure.getParentAccountCode(),
                accountStructure.getDisplayOrder()
        );
    }

    private record CreateAccountStructureContext(CreateAccountStructureCommand command, String parentPath) {
    }
}
