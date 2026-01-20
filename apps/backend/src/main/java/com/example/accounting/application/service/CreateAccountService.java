package com.example.accounting.application.service;

import com.example.accounting.application.port.in.CreateAccountUseCase;
import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountCode;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 勘定科目登録サービス（CreateAccountUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;

    public CreateAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 勘定科目登録を実行する
     *
     * @param command 勘定科目登録コマンド
     * @return 勘定科目登録結果
     */
    @Override
    public CreateAccountResult execute(CreateAccountCommand command) {
        return buildCreateProgram(command).unsafeRun();
    }

    IO<CreateAccountResult> buildCreateProgram(CreateAccountCommand command) {
        return validateUniquenessIO(command)
                .map(either -> either.flatMap(this::createAccountFromCommand))
                .flatMap(this::processCreateResult);
    }

    private IO<Either<String, CreateAccountCommand>> validateUniquenessIO(CreateAccountCommand command) {
        return IO.delay(() -> {
            AccountCode accountCode = AccountCode.of(command.accountCode());
            if (accountRepository.existsByCode(accountCode)) {
                return Either.left("勘定科目コードは既に使用されています");
            }
            return Either.right(command);
        });
    }

    private Either<String, Account> createAccountFromCommand(CreateAccountCommand command) {
        try {
            Account account = Account.create(
                    AccountCode.of(command.accountCode()),
                    command.accountName(),
                    AccountType.fromCode(command.accountType())
            );
            return Either.right(account);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<CreateAccountResult> processCreateResult(Either<String, Account> result) {
        return result.fold(
                error -> IO.pure(CreateAccountResult.failure(error)),
                account -> createAccountIO(account).map(this::createAccountResult)
        );
    }

    private IO<Account> createAccountIO(Account account) {
        return IO.delay(() -> accountRepository.save(account));
    }

    private CreateAccountResult createAccountResult(Account account) {
        return CreateAccountResult.success(
                account.getId().value(),
                account.getAccountCode().value(),
                account.getAccountName(),
                account.getAccountType().name()
        );
    }
}
