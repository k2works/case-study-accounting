package com.example.accounting.integration;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.JournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 仕訳 API 統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("仕訳 API 統合テスト")
class JournalEntryApiIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;
    private Integer debitAccountId;
    private Integer creditAccountId;

    private RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void setUp() {
        restClient = createRestClient();
        // 仕訳に使用する勘定科目を事前に登録
        String adminToken = loginAndGetToken("admin", "Password123!");
        debitAccountId = createAccountIfNotExists(adminToken, "1111", "現金", "ASSET");
        creditAccountId = createAccountIfNotExists(adminToken, "4111", "売上高", "REVENUE");
    }

    private Integer createAccountIfNotExists(String token, String code, String name, String type) {
        String requestBody = createAccountRequestBody(code, name, type);
        CreateAccountResponse response = restClient.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .exchange((req, res) -> res.bodyTo(CreateAccountResponse.class));

        if (response.success()) {
            return response.accountId();
        }
        // 既に存在する場合は一覧から取得
        return findAccountIdByCode(token, code);
    }

    private Integer findAccountIdByCode(String token, String code) {
        com.example.accounting.infrastructure.web.dto.AccountResponse[] accounts = restClient.get()
                .uri("/api/accounts")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(com.example.accounting.infrastructure.web.dto.AccountResponse[].class);

        if (accounts != null) {
            for (var account : accounts) {
                if (code.equals(account.accountCode())) {
                    return account.accountId();
                }
            }
        }
        throw new IllegalStateException("Account not found: " + code);
    }

    private LoginResponse performLogin(String requestBody) {
        return restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(LoginResponse.class);
    }

    private String createLoginRequestBody(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }

    private String loginAndGetToken(String username, String password) {
        LoginResponse response = performLogin(createLoginRequestBody(username, password));
        return response.accessToken();
    }

    private String createAccountRequestBody(String accountCode, String accountName, String accountType) {
        return """
                {
                    "accountCode": "%s",
                    "accountName": "%s",
                    "accountType": "%s"
                }
                """.formatted(accountCode, accountName, accountType);
    }

    private String createJournalEntryRequestBody(String journalDate, String description,
                                                  Integer debitAcctId, Integer creditAcctId,
                                                  String amount) {
        return """
                {
                    "journalDate": "%s",
                    "description": "%s",
                    "lines": [
                        {
                            "lineNumber": 1,
                            "accountId": %d,
                            "debitAmount": %s,
                            "creditAmount": null
                        },
                        {
                            "lineNumber": 2,
                            "accountId": %d,
                            "debitAmount": null,
                            "creditAmount": %s
                        }
                    ]
                }
                """.formatted(journalDate, description, debitAcctId, amount, creditAcctId, amount);
    }

    private String updateJournalEntryRequestBody(String journalDate, String description,
                                                  Integer debitAcctId, Integer creditAcctId,
                                                  String amount, Integer version) {
        return """
                {
                    "journalDate": "%s",
                    "description": "%s",
                    "lines": [
                        {
                            "lineNumber": 1,
                            "accountId": %d,
                            "debitAmount": %s,
                            "creditAmount": null
                        },
                        {
                            "lineNumber": 2,
                            "accountId": %d,
                            "debitAmount": null,
                            "creditAmount": %s
                        }
                    ],
                    "version": %d
                }
                """.formatted(journalDate, description, debitAcctId, amount, creditAcctId, amount, version);
    }

    private CreateJournalEntryResponse performCreateJournalEntry(String token, String requestBody) {
        return restClient.post()
                .uri("/api/journal-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(CreateJournalEntryResponse.class);
    }

    private UpdateJournalEntryResponse performUpdateJournalEntry(String token, Integer id, String requestBody) {
        return restClient.put()
                .uri("/api/journal-entries/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(UpdateJournalEntryResponse.class);
    }

    @Nested
    @DisplayName("仕訳登録 API")
    class CreateJournalEntryApi {

        @Test
        @DisplayName("管理者は仕訳を登録できる")
        void shouldCreateJournalEntryAsAdmin() {
            // Given
            String token = loginAndGetToken("admin", "Password123!");
            String requestBody = createJournalEntryRequestBody(
                    "2024-01-31", "売上計上", debitAccountId, creditAccountId, "10000");

            // When
            CreateJournalEntryResponse response = performCreateJournalEntry(token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.journalEntryId()).isNotNull();
                        assertThat(r.journalDate()).isEqualTo(java.time.LocalDate.of(2024, 1, 31));
                        assertThat(r.description()).isEqualTo("売上計上");
                        assertThat(r.status()).isEqualTo("DRAFT");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("経理責任者は仕訳を登録できる")
        void shouldCreateJournalEntryAsManager() {
            // Given
            String token = loginAndGetToken("manager", "Password123!");
            String requestBody = createJournalEntryRequestBody(
                    "2024-02-01", "仕入計上", debitAccountId, creditAccountId, "5000");

            // When
            CreateJournalEntryResponse response = performCreateJournalEntry(token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.journalEntryId()).isNotNull();
                    });
        }

        @Test
        @DisplayName("一般ユーザーは仕訳を登録できる")
        void shouldCreateJournalEntryAsUser() {
            // Given
            String token = loginAndGetToken("user", "Password123!");
            String requestBody = createJournalEntryRequestBody(
                    "2024-02-02", "経費精算", debitAccountId, creditAccountId, "3000");

            // When
            CreateJournalEntryResponse response = performCreateJournalEntry(token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.journalEntryId()).isNotNull();
                    });
        }

        @Test
        @DisplayName("認証なしでは仕訳を登録できない")
        void shouldRejectCreateJournalEntryWithoutAuth() {
            // Given
            String requestBody = createJournalEntryRequestBody(
                    "2024-01-31", "不正な仕訳", debitAccountId, creditAccountId, "1000");

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/journal-entries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(CreateJournalEntryResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("貸借不一致の場合は登録に失敗する")
        void shouldFailWhenDebitCreditNotBalanced() {
            // Given
            String token = loginAndGetToken("admin", "Password123!");
            String requestBody = """
                    {
                        "journalDate": "2024-01-31",
                        "description": "貸借不一致",
                        "lines": [
                            {
                                "lineNumber": 1,
                                "accountId": %d,
                                "debitAmount": 10000,
                                "creditAmount": null
                            },
                            {
                                "lineNumber": 2,
                                "accountId": %d,
                                "debitAmount": null,
                                "creditAmount": 5000
                            }
                        ]
                    }
                    """.formatted(debitAccountId, creditAccountId);

            // When
            CreateJournalEntryResponse response = restClient.post()
                    .uri("/api/journal-entries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .exchange((req, res) -> res.bodyTo(CreateJournalEntryResponse.class));

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isFalse();
                        assertThat(r.journalEntryId()).isNull();
                        assertThat(r.errorMessage()).contains("貸借");
                    });
        }
    }

    @Nested
    @DisplayName("仕訳一覧取得 API")
    class FindAllJournalEntriesApi {

        @Test
        @DisplayName("認証済みユーザーは仕訳一覧を取得できる")
        void shouldFindAllJournalEntriesAsAuthenticatedUser() {
            // Given - 仕訳を事前に登録
            String adminToken = loginAndGetToken("admin", "Password123!");
            performCreateJournalEntry(adminToken, createJournalEntryRequestBody(
                    "2024-03-01", "テスト仕訳", debitAccountId, creditAccountId, "1000"));

            // When - 一般ユーザーで一覧取得
            String userToken = loginAndGetToken("user", "Password123!");
            GetJournalEntriesResult response = restClient.get()
                    .uri("/api/journal-entries")
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(GetJournalEntriesResult.class);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.content()).isNotNull();
            assertThat(response.content().size()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("認証なしでは仕訳一覧を取得できない")
        void shouldRejectFindAllWithoutAuth() {
            // When / Then
            assertThatThrownBy(() -> restClient.get()
                    .uri("/api/journal-entries")
                    .retrieve()
                    .body(GetJournalEntriesResult.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("仕訳詳細取得 API")
    class FindJournalEntryByIdApi {

        @Test
        @DisplayName("認証済みユーザーは仕訳詳細を取得できる")
        void shouldFindJournalEntryByIdAsAuthenticatedUser() {
            // Given - 仕訳を登録
            String adminToken = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(adminToken,
                    createJournalEntryRequestBody("2024-04-01", "詳細取得テスト",
                            debitAccountId, creditAccountId, "2000"));

            // When - 一般ユーザーで詳細取得
            String userToken = loginAndGetToken("user", "Password123!");
            JournalEntryResponse response = restClient.get()
                    .uri("/api/journal-entries/{id}", createResponse.journalEntryId())
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(JournalEntryResponse.class);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.journalEntryId()).isEqualTo(createResponse.journalEntryId());
                        assertThat(r.journalDate()).isEqualTo(java.time.LocalDate.of(2024, 4, 1));
                        assertThat(r.description()).isEqualTo("詳細取得テスト");
                        assertThat(r.status()).isEqualTo("DRAFT");
                        assertThat(r.version()).isEqualTo(1);
                        assertThat(r.lines()).hasSize(2);
                    });
        }

        @Test
        @DisplayName("存在しない仕訳は取得できない")
        void shouldReturn404WhenJournalEntryDoesNotExist() {
            // Given
            String token = loginAndGetToken("user", "Password123!");

            // When
            HttpStatus statusCode = restClient.get()
                    .uri("/api/journal-entries/{id}", 99999)
                    .header("Authorization", "Bearer " + token)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

            // Then
            assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("認証なしでは仕訳詳細を取得できない")
        void shouldRejectFindByIdWithoutAuth() {
            // When / Then
            assertThatThrownBy(() -> restClient.get()
                    .uri("/api/journal-entries/{id}", 1)
                    .retrieve()
                    .body(JournalEntryResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("仕訳編集 API")
    class UpdateJournalEntryApi {

        @Test
        @DisplayName("管理者は仕訳を編集できる")
        void shouldUpdateJournalEntryAsAdmin() {
            // Given - 仕訳を登録
            String token = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(token,
                    createJournalEntryRequestBody("2024-05-01", "編集前",
                            debitAccountId, creditAccountId, "5000"));

            // When - 編集
            String updateRequestBody = updateJournalEntryRequestBody("2024-05-02", "編集後",
                    debitAccountId, creditAccountId, "6000", 1);
            UpdateJournalEntryResponse response = performUpdateJournalEntry(
                    token, createResponse.journalEntryId(), updateRequestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.journalEntryId()).isEqualTo(createResponse.journalEntryId());
                        assertThat(r.journalDate()).isEqualTo(java.time.LocalDate.of(2024, 5, 2));
                        assertThat(r.description()).isEqualTo("編集後");
                        assertThat(r.status()).isEqualTo("DRAFT");
                        assertThat(r.version()).isEqualTo(2);
                        assertThat(r.message()).isEqualTo("仕訳を更新しました");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("経理責任者は仕訳を編集できる")
        void shouldUpdateJournalEntryAsManager() {
            // Given - admin で仕訳を登録
            String adminToken = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(adminToken,
                    createJournalEntryRequestBody("2024-06-01", "Manager編集前",
                            debitAccountId, creditAccountId, "7000"));

            // When - manager で編集
            String managerToken = loginAndGetToken("manager", "Password123!");
            String updateRequestBody = updateJournalEntryRequestBody("2024-06-02", "Manager編集後",
                    debitAccountId, creditAccountId, "8000", 1);
            UpdateJournalEntryResponse response = performUpdateJournalEntry(
                    managerToken, createResponse.journalEntryId(), updateRequestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.version()).isEqualTo(2);
                    });
        }

        @Test
        @DisplayName("一般ユーザーは仕訳を編集できる")
        void shouldUpdateJournalEntryAsUser() {
            // Given - admin で仕訳を登録
            String adminToken = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(adminToken,
                    createJournalEntryRequestBody("2024-07-01", "User編集前",
                            debitAccountId, creditAccountId, "9000"));

            // When - user で編集
            String userToken = loginAndGetToken("user", "Password123!");
            String updateRequestBody = updateJournalEntryRequestBody("2024-07-02", "User編集後",
                    debitAccountId, creditAccountId, "9500", 1);
            UpdateJournalEntryResponse response = performUpdateJournalEntry(
                    userToken, createResponse.journalEntryId(), updateRequestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.version()).isEqualTo(2);
                    });
        }

        @Test
        @DisplayName("認証なしでは仕訳を編集できない")
        void shouldRejectUpdateJournalEntryWithoutAuth() {
            // Given
            String updateRequestBody = updateJournalEntryRequestBody("2024-01-31", "不正な編集",
                    debitAccountId, creditAccountId, "1000", 1);

            // When / Then
            assertThatThrownBy(() -> restClient.put()
                    .uri("/api/journal-entries/{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updateRequestBody)
                    .retrieve()
                    .body(UpdateJournalEntryResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("存在しない仕訳は編集できない")
        void shouldReturn404WhenJournalEntryDoesNotExist() {
            // Given
            String token = loginAndGetToken("admin", "Password123!");
            String updateRequestBody = updateJournalEntryRequestBody("2024-01-31", "存在しない仕訳",
                    debitAccountId, creditAccountId, "1000", 1);

            // When
            HttpStatus statusCode = restClient.put()
                    .uri("/api/journal-entries/{id}", 99999)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(updateRequestBody)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

            // Then
            assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("楽観的ロックエラーの場合は 409 を返す")
        void shouldReturn409WhenOptimisticLockFails() {
            // Given - 仕訳を登録
            String token = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(token,
                    createJournalEntryRequestBody("2024-08-01", "楽観的ロックテスト",
                            debitAccountId, creditAccountId, "10000"));

            // 1回目の更新（version 1 -> 2）
            String firstUpdateBody = updateJournalEntryRequestBody("2024-08-02", "1回目更新",
                    debitAccountId, creditAccountId, "11000", 1);
            UpdateJournalEntryResponse firstResponse = performUpdateJournalEntry(
                    token, createResponse.journalEntryId(), firstUpdateBody);
            assertThat(firstResponse.success()).isTrue();
            assertThat(firstResponse.version()).isEqualTo(2);

            // When - 古い version で更新を試みる
            String secondUpdateBody = updateJournalEntryRequestBody("2024-08-03", "2回目更新",
                    debitAccountId, creditAccountId, "12000", 1); // version 1 は古い

            HttpStatus statusCode = restClient.put()
                    .uri("/api/journal-entries/{id}", createResponse.journalEntryId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(secondUpdateBody)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

            // Then
            assertThat(statusCode).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("貸借不一致の場合は編集に失敗する")
        void shouldFailWhenDebitCreditNotBalancedOnUpdate() {
            // Given - 仕訳を登録
            String token = loginAndGetToken("admin", "Password123!");
            CreateJournalEntryResponse createResponse = performCreateJournalEntry(token,
                    createJournalEntryRequestBody("2024-09-01", "貸借チェックテスト",
                            debitAccountId, creditAccountId, "5000"));

            // When - 貸借不一致で更新
            String updateRequestBody = """
                    {
                        "journalDate": "2024-09-02",
                        "description": "貸借不一致更新",
                        "lines": [
                            {
                                "lineNumber": 1,
                                "accountId": %d,
                                "debitAmount": 10000,
                                "creditAmount": null
                            },
                            {
                                "lineNumber": 2,
                                "accountId": %d,
                                "debitAmount": null,
                                "creditAmount": 5000
                            }
                        ],
                        "version": 1
                    }
                    """.formatted(debitAccountId, creditAccountId);

            UpdateJournalEntryResponse response = restClient.put()
                    .uri("/api/journal-entries/{id}", createResponse.journalEntryId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(updateRequestBody)
                    .exchange((req, res) -> res.bodyTo(UpdateJournalEntryResponse.class));

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isFalse();
                        assertThat(r.errorMessage()).contains("貸借");
                    });
        }
    }
}
