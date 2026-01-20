package com.example.accounting.integration;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.infrastructure.web.dto.AccountResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAccountResponse;
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
 * 勘定科目 API 統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("勘定科目 API 統合テスト")
class AccountApiIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private LoginResponse performLogin(RestClient restClient, String requestBody) {
        return restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(LoginResponse.class);
    }

    private CreateAccountResponse performCreateAccount(RestClient restClient, String token, String requestBody) {
        return restClient.post()
                .uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(CreateAccountResponse.class);
    }

    private UpdateAccountResponse performUpdateAccount(RestClient restClient, String token, Integer accountId,
                                                       String requestBody) {
        return restClient.put()
                .uri("/api/accounts/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .retrieve()
                .body(UpdateAccountResponse.class);
    }

    private String createLoginRequestBody(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
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

    private String updateAccountRequestBody(String accountName, String accountType) {
        return """
                {
                    "accountName": "%s",
                    "accountType": "%s"
                }
                """.formatted(accountName, accountType);
    }

    private String loginAndGetToken(RestClient restClient, String username, String password) {
        LoginResponse response = performLogin(restClient, createLoginRequestBody(username, password));
        return response.accessToken();
    }

    @Nested
    @DisplayName("勘定科目登録 API")
    class CreateAccountApi {

        @Test
        @DisplayName("管理者は勘定科目を登録できる")
        void shouldCreateAccountAsAdmin() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");
            String requestBody = createAccountRequestBody("1101", "現金", "ASSET");

            // When
            CreateAccountResponse response = performCreateAccount(restClient, token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.accountId()).isNotNull();
                        assertThat(r.accountCode()).isEqualTo("1101");
                        assertThat(r.accountName()).isEqualTo("現金");
                        assertThat(r.accountType()).isEqualTo("ASSET");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("経理責任者は勘定科目を登録できる")
        void shouldCreateAccountAsManager() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "manager", "Password123!");
            String requestBody = createAccountRequestBody("2101", "買掛金", "LIABILITY");

            // When
            CreateAccountResponse response = performCreateAccount(restClient, token, requestBody);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.accountId()).isNotNull();
                        assertThat(r.accountCode()).isEqualTo("2101");
                        assertThat(r.accountName()).isEqualTo("買掛金");
                        assertThat(r.accountType()).isEqualTo("LIABILITY");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("一般ユーザーは勘定科目を登録できない")
        void shouldRejectCreateAccountForUser() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "user", "Password123!");
            String requestBody = createAccountRequestBody("3101", "資本金", "EQUITY");

            // When - exchange を使って直接ステータスコードを取得
            HttpStatus statusCode = restClient.post()
                    .uri("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

            // Then
            assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("認証なしでは勘定科目を登録できない")
        void shouldRejectCreateAccountWithoutAuth() {
            // Given
            RestClient restClient = createRestClient();
            String requestBody = createAccountRequestBody("4101", "売上高", "REVENUE");

            // When / Then
            assertThatThrownBy(() -> restClient.post()
                    .uri("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(CreateAccountResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("重複する勘定科目コードでは登録に失敗する")
        void shouldFailWhenAccountCodeAlreadyExists() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");
            String requestBody = createAccountRequestBody("5101", "仕入高", "EXPENSE");

            // 最初の登録
            CreateAccountResponse firstResponse = performCreateAccount(restClient, token, requestBody);
            assertThat(firstResponse.success()).isTrue();

            // When - 同じコードで再度登録（BadRequest を受け入れるように修正）
            CreateAccountResponse secondResponse = restClient.post()
                    .uri("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(requestBody)
                    .exchange((req, res) -> {
                        if (res.getStatusCode().is4xxClientError()) {
                            return res.bodyTo(CreateAccountResponse.class);
                        }
                        return res.bodyTo(CreateAccountResponse.class);
                    });

            // Then
            assertThat(secondResponse)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isFalse();
                        assertThat(r.accountId()).isNull();
                        assertThat(r.errorMessage()).isEqualTo("勘定科目コードは既に使用されています");
                    });
        }

        @Test
        @DisplayName("すべての勘定科目種別で登録できる")
        void shouldCreateAccountWithAllAccountTypes() {
            // Given
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");

            // 資産
            CreateAccountResponse assetResponse = performCreateAccount(restClient, token,
                    createAccountRequestBody("1201", "普通預金", "ASSET"));
            assertThat(assetResponse.success()).isTrue();
            assertThat(assetResponse.accountType()).isEqualTo("ASSET");

            // 負債
            CreateAccountResponse liabilityResponse = performCreateAccount(restClient, token,
                    createAccountRequestBody("2201", "未払金", "LIABILITY"));
            assertThat(liabilityResponse.success()).isTrue();
            assertThat(liabilityResponse.accountType()).isEqualTo("LIABILITY");

            // 純資産
            CreateAccountResponse equityResponse = performCreateAccount(restClient, token,
                    createAccountRequestBody("3201", "資本金", "EQUITY"));
            assertThat(equityResponse.success()).isTrue();
            assertThat(equityResponse.accountType()).isEqualTo("EQUITY");

            // 収益
            CreateAccountResponse revenueResponse = performCreateAccount(restClient, token,
                    createAccountRequestBody("4201", "売上高", "REVENUE"));
            assertThat(revenueResponse.success()).isTrue();
            assertThat(revenueResponse.accountType()).isEqualTo("REVENUE");

            // 費用
            CreateAccountResponse expenseResponse = performCreateAccount(restClient, token,
                    createAccountRequestBody("5201", "給料手当", "EXPENSE"));
            assertThat(expenseResponse.success()).isTrue();
            assertThat(expenseResponse.accountType()).isEqualTo("EXPENSE");
        }
    }

    @Nested
    @DisplayName("勘定科目更新 API")
    class UpdateAccountApi {

        @Test
        @DisplayName("管理者は勘定科目を更新できる")
        void shouldUpdateAccountAsAdmin() {
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");
            CreateAccountResponse createResponse = performCreateAccount(
                    restClient,
                    token,
                    createAccountRequestBody("6101", "立替金", "ASSET")
            );

            UpdateAccountResponse response = performUpdateAccount(
                    restClient,
                    token,
                    createResponse.accountId(),
                    updateAccountRequestBody("立替金(社員)", "EXPENSE")
            );

            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isTrue();
                        assertThat(r.accountId()).isEqualTo(createResponse.accountId());
                        assertThat(r.accountCode()).isEqualTo("6101");
                        assertThat(r.accountName()).isEqualTo("立替金(社員)");
                        assertThat(r.accountType()).isEqualTo("EXPENSE");
                        assertThat(r.message()).isEqualTo("勘定科目を更新しました");
                        assertThat(r.errorMessage()).isNull();
                    });
        }

        @Test
        @DisplayName("存在しない勘定科目は更新できない")
        void shouldFailWhenAccountDoesNotExist() {
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "admin", "Password123!");

            UpdateAccountResponse response = restClient.put()
                    .uri("/api/accounts/{id}", 99999)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .body(updateAccountRequestBody("不明", "ASSET"))
                    .exchange((req, res) -> res.bodyTo(UpdateAccountResponse.class));

            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.success()).isFalse();
                        assertThat(r.errorMessage()).isEqualTo("勘定科目が見つかりません");
                    });
        }
    }

    @Nested
    @DisplayName("勘定科目一覧取得 API")
    class FindAllAccountsApi {

        @Test
        @DisplayName("認証済みユーザーは勘定科目一覧を取得できる")
        void shouldFindAllAccountsAsAuthenticatedUser() {
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "user", "Password123!");

            // 勘定科目を事前に登録（admin で実行）
            String adminToken = loginAndGetToken(restClient, "admin", "Password123!");
            performCreateAccount(restClient, adminToken, createAccountRequestBody("7101", "現金7", "ASSET"));

            // 一覧取得（一般ユーザーで実行）
            AccountResponse[] responses = restClient.get()
                    .uri("/api/accounts")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(AccountResponse[].class);

            assertThat(responses).isNotNull();
            assertThat(responses.length).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("認証なしでは勘定科目一覧を取得できない")
        void shouldRejectFindAllWithoutAuth() {
            RestClient restClient = createRestClient();

            assertThatThrownBy(() -> restClient.get()
                    .uri("/api/accounts")
                    .retrieve()
                    .body(AccountResponse[].class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("勘定科目単体取得 API")
    class FindAccountByIdApi {

        @Test
        @DisplayName("認証済みユーザーは勘定科目を単体取得できる")
        void shouldFindAccountByIdAsAuthenticatedUser() {
            RestClient restClient = createRestClient();
            String adminToken = loginAndGetToken(restClient, "admin", "Password123!");

            // 勘定科目を登録
            CreateAccountResponse createResponse = performCreateAccount(
                    restClient,
                    adminToken,
                    createAccountRequestBody("8101", "現金8", "ASSET")
            );

            // 単体取得（一般ユーザーで実行）
            String userToken = loginAndGetToken(restClient, "user", "Password123!");
            AccountResponse response = restClient.get()
                    .uri("/api/accounts/{id}", createResponse.accountId())
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(AccountResponse.class);

            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.accountId()).isEqualTo(createResponse.accountId());
                        assertThat(r.accountCode()).isEqualTo("8101");
                        assertThat(r.accountName()).isEqualTo("現金8");
                        assertThat(r.accountType()).isEqualTo("ASSET");
                    });
        }

        @Test
        @DisplayName("存在しない勘定科目は取得できない")
        void shouldReturn404WhenAccountDoesNotExist() {
            RestClient restClient = createRestClient();
            String token = loginAndGetToken(restClient, "user", "Password123!");

            HttpStatus statusCode = restClient.get()
                    .uri("/api/accounts/{id}", 99999)
                    .header("Authorization", "Bearer " + token)
                    .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

            assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("認証なしでは勘定科目を単体取得できない")
        void shouldRejectFindByIdWithoutAuth() {
            RestClient restClient = createRestClient();

            assertThatThrownBy(() -> restClient.get()
                    .uri("/api/accounts/{id}", 1)
                    .retrieve()
                    .body(AccountResponse.class))
                    .isInstanceOf(HttpClientErrorException.class)
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(HttpClientErrorException.class))
                    .extracting(HttpClientErrorException::getStatusCode)
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
