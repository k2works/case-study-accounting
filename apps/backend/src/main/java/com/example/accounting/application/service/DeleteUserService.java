package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteUserUseCase;
import com.example.accounting.application.port.in.command.DeleteUserCommand;
import com.example.accounting.application.port.out.DeleteUserResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー削除サービス（DeleteUserUseCase の実装）
 */
@Service
@Transactional
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public DeleteUserResult execute(DeleteUserCommand command) {
        return buildDeleteProgram(command).unsafeRun();
    }

    IO<DeleteUserResult> buildDeleteProgram(DeleteUserCommand command) {
        return findUserIO(command.userId())
                .map(either -> either.map(User::deactivate))
                .flatMap(this::processDeleteResult);
    }

    private IO<Either<String, User>> findUserIO(String userId) {
        return IO.delay(() -> userRepository.findById(UserId.of(userId))
                .<Either<String, User>>map(Either::right)
                .orElseGet(() -> Either.left("ユーザーが見つかりません")));
    }

    private IO<DeleteUserResult> processDeleteResult(Either<String, User> result) {
        return result.fold(
                error -> IO.pure(DeleteUserResult.failure(error)),
                user -> updateUserIO(user).map(ignored -> DeleteUserResult.ofSuccess())
        );
    }

    private IO<User> updateUserIO(User user) {
        return IO.delay(() -> userRepository.save(user));
    }
}
