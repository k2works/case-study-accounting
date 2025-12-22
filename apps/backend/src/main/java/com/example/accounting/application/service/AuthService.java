package com.example.accounting.application.service;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.in.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.shared.IO;
import com.example.accounting.infrastructure.security.JwtService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 認証サービス（AuthUseCase の実装）
 *
 * <p>Vavr の Option/Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 *
 * <p>副作用（DB アクセス、トークン生成）は IO でラップされ、
 * 計算の記述と実行が分離されている。</p>
 */
@Service
@Transactional
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * ログインを実行する
     *
     * @param command ログインコマンド
     * @return ログイン結果
     */
    @Override
    public LoginResult execute(LoginCommand command) {
        // ログイン処理を IO として構築し、実行
        return buildLoginProgram(command).unsafeRun();
    }

    /**
     * ログイン処理を IO プログラムとして構築する（純粋関数）
     *
     * <p>この時点では副作用は発生しない。
     * unsafeRun() が呼ばれた時点で実行される。</p>
     *
     * @param command ログインコマンド
     * @return ログイン処理を表す IO
     */
    IO<LoginResult> buildLoginProgram(LoginCommand command) {
        return findUserIO(command.username())
                .map(either -> either
                        .flatMap(this::validateAccountNotLocked)
                        .flatMap(this::validateAccountActive)
                        .flatMap(user -> validatePassword(user, command.password())))
                .flatMap(this::processLoginResult);
    }

    /**
     * ユーザーを検索する IO を返す
     */
    private IO<Either<String, User>> findUserIO(String username) {
        return IO.delay(() ->
                Option.ofOptional(userRepository.findByUsername(username))
                        .toEither("ユーザー名またはパスワードが正しくありません")
        );
    }

    /**
     * アカウントがロックされていないか検証する（純粋関数）
     */
    private Either<String, User> validateAccountNotLocked(User user) {
        return user.isLocked()
                ? Either.left("アカウントがロックされています")
                : Either.right(user);
    }

    /**
     * アカウントが有効か検証する（純粋関数）
     */
    private Either<String, User> validateAccountActive(User user) {
        return user.isActive()
                ? Either.right(user)
                : Either.left("アカウントが無効化されています");
    }

    /**
     * パスワードを検証する
     *
     * <p>検証失敗時は失敗回数を記録する副作用が発生する。</p>
     */
    private Either<String, User> validatePassword(User user, String password) {
        if (user.verifyPassword(password)) {
            return Either.right(user);
        }
        // パスワード検証失敗時の副作用
        User updatedUser = user.recordFailedLoginAttempt();
        userRepository.save(updatedUser);
        return Either.left("ユーザー名またはパスワードが正しくありません");
    }

    /**
     * ログイン結果に応じて処理を行う IO を返す
     */
    private IO<LoginResult> processLoginResult(Either<String, User> result) {
        return result.fold(
                error -> IO.pure(LoginResult.failure(error)),
                user -> processSuccessfulLoginIO(user).map(this::createLoginResult)
        );
    }

    /**
     * ログイン成功を処理する IO を返す
     */
    private IO<User> processSuccessfulLoginIO(User user) {
        return IO.delay(() -> {
            User updatedUser = user.recordSuccessfulLogin();
            userRepository.save(updatedUser);
            return updatedUser;
        });
    }

    /**
     * ログイン結果を生成する（純粋関数）
     */
    private LoginResult createLoginResult(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "displayName", user.getDisplayName()
        );
        String accessToken = jwtService.generateToken(user.getUsernameValue(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsernameValue());

        return LoginResult.success(accessToken, refreshToken, user.getUsernameValue(), user.getRole());
    }
}
