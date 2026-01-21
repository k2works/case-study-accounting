package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.TestcontainersConfiguration;
import com.example.accounting.infrastructure.web.dto.AccountResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 勘定科目検索 API 統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@DisplayName("勘定科目検索 API 統合テスト")
class AccountSearchApiIntegrationTest {

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

    private String loginAndGetToken(RestClient restClient, String username, String password) {
        LoginResponse response = performLogin(restClient, createLoginRequestBody(username, password));
        return response.accessToken();
    }

    private List<AccountResponse> fetchAccounts(RestClient restClient, String token, String uri) {
        AccountResponse[] responses = restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(AccountResponse[].class);
        return responses == null ? List.of() : Arrays.asList(responses);
    }

    @Test
    @DisplayName("GET /api/accounts?type=ASSET で資産のみ取得")
    void shouldFilterByType() {
        // Given
        RestClient restClient = createRestClient();
        String token = loginAndGetToken(restClient, "admin", "Password123!");
        performCreateAccount(restClient, token, createAccountRequestBody("9101", "現金検索", "ASSET"));
        performCreateAccount(restClient, token, createAccountRequestBody("9102", "買掛金検索", "LIABILITY"));

        // When
        List<AccountResponse> responses = fetchAccounts(restClient, token, "/api/accounts?type=ASSET");

        // Then
        assertThat(responses)
                .isNotEmpty()
                .allMatch(response -> "ASSET".equals(response.accountType()));
        assertThat(responses)
                .extracting(AccountResponse::accountCode)
                .contains("9101");
    }

    @Test
    @DisplayName("GET /api/accounts?keyword=920 でコード前方一致")
    void shouldSearchByKeywordPrefix() {
        // Given
        RestClient restClient = createRestClient();
        String token = loginAndGetToken(restClient, "admin", "Password123!");
        performCreateAccount(restClient, token, createAccountRequestBody("9201", "前方一致", "ASSET"));

        // When
        List<AccountResponse> responses = fetchAccounts(restClient, token, "/api/accounts?keyword=920");

        // Then
        assertThat(responses)
                .extracting(AccountResponse::accountCode)
                .contains("9201");
    }

    @Test
    @DisplayName("GET /api/accounts?type=EXPENSE&keyword=旅費 で複合検索")
    void shouldSearchByTypeAndKeyword() {
        // Given
        RestClient restClient = createRestClient();
        String token = loginAndGetToken(restClient, "admin", "Password123!");
        performCreateAccount(restClient, token, createAccountRequestBody("9301", "旅費交通費S", "EXPENSE"));
        performCreateAccount(restClient, token, createAccountRequestBody("9302", "旅費交通費R", "REVENUE"));

        // When
        List<AccountResponse> responses = fetchAccounts(
                restClient,
                token,
                "/api/accounts?type=EXPENSE&keyword=旅費"
        );

        // Then
        assertThat(responses)
                .isNotEmpty()
                .allMatch(response -> "EXPENSE".equals(response.accountType()));
        assertThat(responses)
                .extracting(AccountResponse::accountCode)
                .contains("9301");
    }

    @Test
    @DisplayName("パラメータなしで全件取得")
    void shouldReturnAllWhenNoParams() {
        // Given
        RestClient restClient = createRestClient();
        String token = loginAndGetToken(restClient, "admin", "Password123!");
        performCreateAccount(restClient, token, createAccountRequestBody("9401", "全件取得", "EQUITY"));

        // When
        List<AccountResponse> responses = fetchAccounts(restClient, token, "/api/accounts");

        // Then
        assertThat(responses)
                .extracting(AccountResponse::accountCode)
                .contains("9401");
    }
}
