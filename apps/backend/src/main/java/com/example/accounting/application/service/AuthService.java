package com.example.accounting.application.service;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.in.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.infrastructure.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 認証サービス（AuthUseCase の実装）
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
        // ユーザーを検索
        Optional<User> userOptional = userRepository.findByUsername(command.username());

        if (userOptional.isEmpty()) {
            return LoginResult.failure("ユーザー名またはパスワードが正しくありません");
        }

        User user = userOptional.get();

        // アカウントロック確認
        if (user.isLocked()) {
            return LoginResult.failure("アカウントがロックされています");
        }

        // アカウント無効化確認
        if (!user.isActive()) {
            return LoginResult.failure("アカウントが無効化されています");
        }

        // パスワード検証
        if (!user.verifyPassword(command.password())) {
            user.recordFailedLoginAttempt();
            userRepository.save(user);
            return LoginResult.failure("ユーザー名またはパスワードが正しくありません");
        }

        // ログイン成功処理
        user.recordSuccessfulLogin();
        userRepository.save(user);

        // トークン生成
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "displayName", user.getDisplayName()
        );
        String accessToken = jwtService.generateToken(user.getUsername(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return LoginResult.success(accessToken, refreshToken, user.getUsername(), user.getRole());
    }
}
