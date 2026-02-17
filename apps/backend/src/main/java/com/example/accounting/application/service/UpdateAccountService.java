package com.example.accounting.application.service;

import com.example.accounting.application.port.in.UpdateAccountUseCase;
import com.example.accounting.application.port.in.command.UpdateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountUsageChecker;
import com.example.accounting.application.port.out.UpdateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 勘定科目更新サービス（UpdateAccountUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class UpdateAccountService implements UpdateAccountUseCase {

    private final AccountRepository accountRepository;
    private final AccountUsageChecker accountUsageChecker;

    public UpdateAccountService(AccountRepository accountRepository,
                                AccountUsageChecker accountUsageChecker) {
        this.accountRepository = accountRepository;
        this.accountUsageChecker = accountUsageChecker;
    }

    /**
     * 勘定科目更新を実行する
     *
     * @param command 勘定科目更新コマンド
     * @return 勘定科目更新結果
     */
    @Override
    public UpdateAccountResult execute(UpdateAccountCommand command) {
        return buildUpdateProgram(command).unsafeRun();
    }

    IO<UpdateAccountResult> buildUpdateProgram(UpdateAccountCommand command) {
        return findAccountIO(command.accountId())
                .map(either -> either.flatMap(account -> validateAccountTypeChange(command, account)))
                .map(either -> either.flatMap(account -> updateAccountFromCommand(command, account)))
                .flatMap(this::processUpdateResult);
    }

    private IO<Either<String, Account>> findAccountIO(Integer accountId) {
        return IO.delay(() -> accountRepository.findById(AccountId.of(accountId))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, Account>>map(Either::right)
                .orElseGet(() -> Either.left("勘定科目が見つかりません")));
    }

    private Either<String, Account> validateAccountTypeChange(UpdateAccountCommand command, Account account) {
        AccountType newAccountType = AccountType.fromCode(command.accountType());
        if (!account.getAccountType().equals(newAccountType)
                && accountUsageChecker.isAccountInUse(account.getId())) {
            return Either.left("使用中の勘定科目の種別は変更できません");
        }
        return Either.right(account);
    }

    private Either<String, Account> updateAccountFromCommand(UpdateAccountCommand command, Account account) {
        try {
            AccountType newAccountType = AccountType.fromCode(command.accountType());
            Account updatedAccount = account.withAccountName(command.accountName())
                    .withAccountType(newAccountType);
            return Either.right(updatedAccount);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<UpdateAccountResult> processUpdateResult(Either<String, Account> result) {
        return result.fold(
                error -> IO.pure(UpdateAccountResult.failure(error)),
                account -> updateAccountIO(account).map(this::updateAccountResult)
        );
    }

    private IO<Account> updateAccountIO(Account account) {
        return IO.delay(() -> accountRepository.save(account)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private UpdateAccountResult updateAccountResult(Account account) {
        return UpdateAccountResult.success(
                account.getId().value(),
                account.getAccountCode().value(),
                account.getAccountName(),
                account.getAccountType().name(),
                "勘定科目を更新しました"
        );
    }
}
