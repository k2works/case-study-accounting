package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteAccountCommand;
import com.example.accounting.application.port.in.DeleteAccountResult;
import com.example.accounting.application.port.in.DeleteAccountUseCase;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountUsageChecker;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 勘定科目削除サービス（DeleteAccountUseCase の実装）
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteAccountService implements DeleteAccountUseCase {

    private final AccountRepository accountRepository;
    private final AccountUsageChecker accountUsageChecker;

    @Override
    public DeleteAccountResult execute(DeleteAccountCommand command) {
        return buildDeleteProgram(command).unsafeRun();
    }

    IO<DeleteAccountResult> buildDeleteProgram(DeleteAccountCommand command) {
        return findAccountIO(command.accountId())
                .map(either -> either.flatMap(this::validateNotInUse))
                .flatMap(this::processDeleteResult);
    }

    private IO<Either<String, Account>> findAccountIO(Integer accountId) {
        return IO.delay(() -> accountRepository.findById(AccountId.of(accountId))
                .<Either<String, Account>>map(Either::right)
                .orElseGet(() -> Either.left("勘定科目が見つかりません")));
    }

    private Either<String, Account> validateNotInUse(Account account) {
        if (accountUsageChecker.isAccountInUse(account.getId())) {
            return Either.left("この勘定科目は仕訳で使用されているため削除できません");
        }
        return Either.right(account);
    }

    private IO<DeleteAccountResult> processDeleteResult(Either<String, Account> result) {
        return result.fold(
                error -> IO.pure(DeleteAccountResult.failure(error)),
                account -> deleteAccountIO(account.getId())
                        .map(ignored -> DeleteAccountResult.success(account.getId().value()))
        );
    }

    private IO<Void> deleteAccountIO(AccountId accountId) {
        return IO.effect(() -> accountRepository.deleteById(accountId));
    }
}
