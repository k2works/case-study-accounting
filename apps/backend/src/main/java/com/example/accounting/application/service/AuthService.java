package com.example.accounting.application.service;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.in.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.infrastructure.security.JwtService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 認証サービス（AuthUseCase の実装）
 *
 * <p>Vavr の Option/Either を使用した関数型スタイルでエラーハンドリングを行う。</p>
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
        return findUser(command.username())
                .flatMap(this::validateAccountNotLocked)
                .flatMap(this::validateAccountActive)
                .flatMap(user -> validatePassword(user, command.password()))
                .map(this::processSuccessfulLogin)
                .fold(
                        LoginResult::failure,
                        this::createLoginResult
                );
    }

    /**
     * ユーザーを検索する
     */
    private Either<String, User> findUser(String username) {
        return Option.ofOptional(userRepository.findByUsername(username))
                .toEither("ユーザー名またはパスワードが正しくありません");
    }

    /**
     * アカウントがロックされていないか検証する
     */
    private Either<String, User> validateAccountNotLocked(User user) {
        return user.isLocked()
                ? Either.left("アカウントがロックされています")
                : Either.right(user);
    }

    /**
     * アカウントが有効か検証する
     */
    private Either<String, User> validateAccountActive(User user) {
        return user.isActive()
                ? Either.right(user)
                : Either.left("アカウントが無効化されています");
    }

    /**
     * パスワードを検証する
     */
    private Either<String, User> validatePassword(User user, String password) {
        if (user.verifyPassword(password)) {
            return Either.right(user);
        }
        User updatedUser = user.recordFailedLoginAttempt();
        userRepository.save(updatedUser);
        return Either.left("ユーザー名またはパスワードが正しくありません");
    }

    /**
     * ログイン成功を処理する
     */
    private User processSuccessfulLogin(User user) {
        User updatedUser = user.recordSuccessfulLogin();
        userRepository.save(updatedUser);
        return updatedUser;
    }

    /**
     * ログイン結果を生成する
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
