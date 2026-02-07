package com.example.accounting.application.service;

import com.example.accounting.application.port.in.UpdateUserUseCase;
import com.example.accounting.application.port.in.command.UpdateUserCommand;
import com.example.accounting.application.port.out.UpdateUserResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.Role;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー更新サービス（UpdateUserUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository userRepository;

    public UpdateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザー更新を実行する
     *
     * @param command ユーザー更新コマンド
     * @return ユーザー更新結果
     */
    @Override
    public UpdateUserResult execute(UpdateUserCommand command) {
        return buildUpdateProgram(command).unsafeRun();
    }

    IO<UpdateUserResult> buildUpdateProgram(UpdateUserCommand command) {
        return findUserIO(command.userId())
                .map(either -> either.flatMap(user -> updateUserFromCommand(command, user)))
                .flatMap(this::processUpdateResult);
    }

    private IO<Either<String, User>> findUserIO(String userId) {
        return IO.delay(() -> userRepository.findById(UserId.of(userId))
                .<Either<String, User>>map(Either::right)
                .orElseGet(() -> Either.left("ユーザーが見つかりません")));
    }

    private Either<String, User> updateUserFromCommand(UpdateUserCommand command, User user) {
        try {
            User updatedUser = user.withDisplayName(command.displayName())
                    .changeRole(Role.fromCode(command.role()));
            if (command.password() != null && !command.password().isBlank()) {
                updatedUser = updatedUser.changePassword(command.password());
            }
            return Either.right(updatedUser);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<UpdateUserResult> processUpdateResult(Either<String, User> result) {
        return result.fold(
                error -> IO.pure(UpdateUserResult.failure(error)),
                user -> updateUserIO(user).map(this::updateUserResult)
        );
    }

    private IO<User> updateUserIO(User user) {
        return IO.delay(() -> userRepository.save(user));
    }

    private UpdateUserResult updateUserResult(User user) {
        return UpdateUserResult.success(
                user.getId().value(),
                user.getUsernameValue(),
                user.getEmailValue(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }
}
