package com.example.accounting.application.service;

import com.example.accounting.application.port.in.RegisterUserUseCase;
import com.example.accounting.application.port.in.command.RegisterUserCommand;
import com.example.accounting.application.port.out.RegisterUserResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Email;
import com.example.accounting.domain.model.user.Password;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.Username;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー登録サービス（RegisterUserUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー登録を実行する
     *
     * @param command ユーザー登録コマンド
     * @return ユーザー登録結果
     */
    @Override
    public RegisterUserResult execute(RegisterUserCommand command) {
        return buildRegisterProgram(command).unsafeRun();
    }

    IO<RegisterUserResult> buildRegisterProgram(RegisterUserCommand command) {
        return validateUniquenessIO(command)
                .map(either -> either.flatMap(this::createUserFromCommand))
                .flatMap(this::processRegisterResult);
    }

    private IO<Either<String, RegisterUserCommand>> validateUniquenessIO(RegisterUserCommand command) {
        return IO.delay(() -> {
            if (userRepository.existsByUsername(command.username())
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
                return Either.left("ユーザー名は既に使用されています");
            }
            if (userRepository.existsByEmail(command.email())
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
                return Either.left("メールアドレスは既に使用されています");
            }
            return Either.right(command);
        });
    }

    private Either<String, User> createUserFromCommand(RegisterUserCommand command) {
        try {
            User user = User.create(
                    Username.of(command.username()),
                    Email.of(command.email()),
                    Password.fromRawPassword(command.password()),
                    command.displayName(),
                    Role.fromCode(command.role())
            );
            return Either.right(user);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<RegisterUserResult> processRegisterResult(Either<String, User> result) {
        return result.fold(
                error -> IO.pure(RegisterUserResult.failure(error)),
                user -> registerUserIO(user).map(this::createRegisterResult)
        );
    }

    private IO<User> registerUserIO(User user) {
        return IO.delay(() -> userRepository.save(user)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private RegisterUserResult createRegisterResult(User user) {
        return RegisterUserResult.success(
                user.getUsernameValue(),
                user.getEmailValue(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }
}
