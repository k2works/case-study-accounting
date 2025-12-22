package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.domain.model.User;
import com.example.accounting.domain.repository.UserRepository;
import com.example.accounting.infrastructure.security.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("AuthController Integration Test")
class AuthControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    
    @BeforeEach
    void setUp() {
        // MockMvc setup
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // テストユーザーの作成
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setName("テストユーザー");
        user.setRole("STAFF");
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }
    
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {
        
        @Test
        @DisplayName("正しいメールアドレスとパスワードでログインできる")
        void shouldLoginWithValidCredentials() throws Exception {
            Map<String, String> request = Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value("テストユーザー"))
                .andExpect(jsonPath("$.role").value("STAFF"));
        }
        
        @Test
        @DisplayName("間違ったパスワードでログインできない")
        void shouldNotLoginWithWrongPassword() throws Exception {
            Map<String, String> request = Map.of(
                "email", TEST_EMAIL,
                "password", "wrongpassword"
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("メールアドレスまたはパスワードが正しくありません"));
        }
        
        @Test
        @DisplayName("存在しないメールアドレスでログインできない")
        void shouldNotLoginWithNonExistentEmail() throws Exception {
            Map<String, String> request = Map.of(
                "email", "nonexistent@example.com",
                "password", TEST_PASSWORD
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("メールアドレスまたはパスワードが正しくありません"));
        }
        
        @Test
        @DisplayName("メールアドレスが空の場合はバリデーションエラー")
        void shouldReturnValidationErrorWhenEmailIsEmpty() throws Exception {
            Map<String, String> request = Map.of(
                "email", "",
                "password", TEST_PASSWORD
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("パスワードが空の場合はバリデーションエラー")
        void shouldReturnValidationErrorWhenPasswordIsEmpty() throws Exception {
            Map<String, String> request = Map.of(
                "email", TEST_EMAIL,
                "password", ""
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("3回連続でログイン失敗するとアカウントがロックされる")
        void shouldLockAccountAfterThreeFailedAttempts() throws Exception {
            Map<String, String> request = Map.of(
                "email", TEST_EMAIL,
                "password", "wrongpassword"
            );
            
            // 3回失敗
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            }
            
            // 正しいパスワードでもロックされているためログインできない
            Map<String, String> validRequest = Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("アカウントがロックされています。管理者にお問い合わせください"));
        }
        
        @Test
        @DisplayName("ロック済みアカウントではログインできない")
        void shouldNotLoginWithLockedAccount() throws Exception {
            // アカウントをロック
            User user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
            userRepository.lockAccount(user.getId());
            
            Map<String, String> request = Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
            );
            
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("アカウントがロックされています。管理者にお問い合わせください"));
        }
    }
}
