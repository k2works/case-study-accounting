package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteAccountStructureUseCase;
import com.example.accounting.application.port.in.command.DeleteAccountStructureCommand;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.DeleteAccountStructureResult;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteAccountStructureService implements DeleteAccountStructureUseCase {

    private final AccountStructureRepository accountStructureRepository;

    public DeleteAccountStructureService(AccountStructureRepository accountStructureRepository) {
        this.accountStructureRepository = accountStructureRepository;
    }

    @Override
    public DeleteAccountStructureResult execute(DeleteAccountStructureCommand command) {
        return buildDeleteProgram(command).unsafeRun();
    }

    IO<DeleteAccountStructureResult> buildDeleteProgram(DeleteAccountStructureCommand command) {
        return findAccountStructureIO(command.accountCode())
                .map(either -> either.flatMap(this::validateNoChildren))
                .flatMap(this::processDeleteResult);
    }

    private IO<Either<String, AccountStructure>> findAccountStructureIO(String accountCode) {
        return IO.delay(() -> accountStructureRepository.findByCode(accountCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, AccountStructure>>map(Either::right)
                .orElseGet(() -> Either.left("勘定科目構成が見つかりません")));
    }

    private Either<String, AccountStructure> validateNoChildren(AccountStructure accountStructure) {
        var children = accountStructureRepository.findByParentCode(accountStructure.getAccountCode())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        if (!children.isEmpty()) {
            return Either.left("子階層が存在するため削除できません");
        }
        return Either.right(accountStructure);
    }

    private IO<DeleteAccountStructureResult> processDeleteResult(Either<String, AccountStructure> result) {
        return result.fold(
                error -> IO.pure(DeleteAccountStructureResult.failure(error)),
                accountStructure -> deleteAccountStructureIO(accountStructure.getAccountCode())
                        .map(ignored -> DeleteAccountStructureResult.success(accountStructure.getAccountCode()))
        );
    }

    private IO<Void> deleteAccountStructureIO(String accountCode) {
        return IO.effect(() -> accountStructureRepository.deleteByCode(accountCode)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }
}
