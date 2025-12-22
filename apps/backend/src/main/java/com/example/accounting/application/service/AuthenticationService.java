package com.example.accounting.application.service;

import com.example.accounting.domain.model.User;
import com.example.accounting.domain.repository.UserRepository;
import com.example.accounting.infrastructure.security.JwtService;
import com.example.accounting.infrastructure.security.PasswordEncoder;
import com.example.accounting.infrastructure.web.dto.LoginRequest;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException("メールアドレスまたはパスワードが正しくありません"));
        
        if (user.isAccountLocked()) {
            throw new BusinessException("アカウントがロックされています。管理者にお問い合わせください");
        }
        
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BusinessException("メールアドレスまたはパスワードが正しくありません");
        }
        
        // ログイン成功時の処理
        resetFailedAttempts(user);
        updateLastLogin(user);
        
        // JWT トークン生成
        Map<String, Object> claims = Map.of(
            "userId", user.getId(),
            "role", user.getRole()
        );
        String token = jwtService.generateToken(user.getEmail(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        
        return new LoginResponse(
            token,
            refreshToken,
            user.getEmail(),
            user.getName(),
            user.getRole()
        );
    }
    
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        userRepository.updateFailedLoginAttempts(user.getId(), attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            userRepository.lockAccount(user.getId());
        }
    }
    
    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            userRepository.updateFailedLoginAttempts(user.getId(), 0);
        }
    }
    
    private void updateLastLogin(User user) {
        userRepository.updateLastLoginAt(user.getId(), OffsetDateTime.now());
    }
}
