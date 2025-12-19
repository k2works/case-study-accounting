package com.example.accounting.infrastructure.persistence;

import com.example.accounting.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testcontainers を使用したデータベース接続テスト
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("データベース接続テスト")
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("データベースに接続できる")
    void shouldConnectToDatabase() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("accountsテーブルが作成されている")
    void shouldHaveAccountsTableCreated() {
        String sql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_name = 'accounts'
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Flywayマイグレーション履歴が存在する")
    void shouldHaveFlywaySchemaHistory() {
        String sql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_name = 'flyway_schema_history'
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("accountsテーブルに必要なカラムが存在する")
    void shouldHaveRequiredColumnsInAccountsTable() {
        String sql = """
            SELECT column_name FROM information_schema.columns
            WHERE table_schema = 'public'
            AND table_name = 'accounts'
            ORDER BY ordinal_position
            """;

        var columns = jdbcTemplate.queryForList(sql, String.class);

        assertThat(columns).containsExactly(
                "id",
                "code",
                "name",
                "account_type",
                "created_at",
                "updated_at"
        );
    }
}
