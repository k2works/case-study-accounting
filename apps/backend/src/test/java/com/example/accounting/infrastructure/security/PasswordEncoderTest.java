package com.example.accounting.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordEncoder")
class PasswordEncoderTest {
    
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
    }
    
    @Nested
    @DisplayName("パスワードのエンコード")
    class Encode {
        
        @Test
        @DisplayName("パスワードをエンコードできる")
        void shouldEncodePassword() {
            String rawPassword = "password123";
            
            String encoded = passwordEncoder.encode(rawPassword);
            
            assertThat(encoded).isNotBlank();
            assertThat(encoded).isNotEqualTo(rawPassword);
        }
        
        @Test
        @DisplayName("同じパスワードでも異なるハッシュが生成される（ソルト効果）")
        void shouldGenerateDifferentHashesForSamePassword() {
            String rawPassword = "password123";
            
            String encoded1 = passwordEncoder.encode(rawPassword);
            String encoded2 = passwordEncoder.encode(rawPassword);
            
            assertThat(encoded1).isNotEqualTo(encoded2);
        }
    }
    
    @Nested
    @DisplayName("パスワードの照合")
    class Matches {
        
        @Test
        @DisplayName("正しいパスワードはtrueを返す")
        void shouldReturnTrueForCorrectPassword() {
            String rawPassword = "password123";
            String encoded = passwordEncoder.encode(rawPassword);
            
            boolean matches = passwordEncoder.matches(rawPassword, encoded);
            
            assertThat(matches).isTrue();
        }
        
        @Test
        @DisplayName("間違ったパスワードはfalseを返す")
        void shouldReturnFalseForWrongPassword() {
            String rawPassword = "password123";
            String encoded = passwordEncoder.encode(rawPassword);
            
            boolean matches = passwordEncoder.matches("wrongpassword", encoded);
            
            assertThat(matches).isFalse();
        }
        
        @Test
        @DisplayName("不正なエンコード文字列はfalseを返す")
        void shouldReturnFalseForInvalidEncodedString() {
            boolean matches = passwordEncoder.matches("password123", "invalid-hash");
            
            assertThat(matches).isFalse();
        }
    }
}
