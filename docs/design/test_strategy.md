# テスト戦略

## 概要

本ドキュメントは、財務会計システムのテスト戦略を定義します。ヘキサゴナルアーキテクチャと CQRS パターンを採用したシステムに適した、ピラミッド形テスト戦略を採用します。

## テスト戦略の基本方針

### 目的

- **品質保証**: システムが要件を満たし、期待通りに動作することを保証する
- **早期バグ発見**: 開発サイクルの早い段階でバグを発見し、修正コストを最小化する
- **リグレッション防止**: 変更による既存機能への影響を防止する
- **ドキュメンテーション**: テストコードが仕様の生きたドキュメントとして機能する

### テストピラミッド

```
        /\
       /  \        E2E テスト (5%)
      /----\       - ユーザーシナリオ検証
     /      \      - クリティカルパス
    /--------\     統合テスト (15%)
   /          \    - API 結合
  /            \   - DB アクセス
 /--------------\  ユニットテスト (80%)
/                \ - ドメインロジック
------------------  - 純粋関数
```

| テストレベル | 割合 | 目的 | 実行速度 |
|------------|------|------|---------|
| ユニットテスト | 80% | ドメインロジックの検証 | 高速（ミリ秒） |
| 統合テスト | 15% | コンポーネント間連携の検証 | 中速（秒） |
| E2E テスト | 5% | ユーザーシナリオの検証 | 低速（分） |

### テスト駆動開発（TDD）サイクル

```
┌─────────────────────────────────────────┐
│                                         │
│    ┌─────┐    ┌─────┐    ┌──────────┐  │
│    │ Red │───►│Green│───►│Refactor  │  │
│    └─────┘    └─────┘    └──────────┘  │
│        ▲                       │        │
│        └───────────────────────┘        │
│                                         │
└─────────────────────────────────────────┘
```

1. **Red**: 失敗するテストを書く
2. **Green**: テストを通す最小限のコードを実装する
3. **Refactor**: コードを改善する（テストは通ったまま）

## アーキテクチャとテストの対応

### ヘキサゴナルアーキテクチャにおけるテスト配置

```
┌─────────────────────────────────────────────────────────────┐
│                      E2E テスト                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                   統合テスト                           │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │              ユニットテスト                      │  │  │
│  │  │  ┌─────────────────────────────────────────┐   │  │  │
│  │  │  │           Domain Layer                   │   │  │  │
│  │  │  │  Entity, ValueObject, DomainService     │   │  │  │
│  │  │  └─────────────────────────────────────────┘   │  │  │
│  │  │  ┌─────────────────────────────────────────┐   │  │  │
│  │  │  │         Application Layer                │   │  │  │
│  │  │  │    UseCase, ApplicationService          │   │  │  │
│  │  │  └─────────────────────────────────────────┘   │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │           Infrastructure Layer                  │  │  │
│  │  │      Repository, External Services             │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Presentation Layer                       │  │
│  │          Controller, View, API                       │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### レイヤー別テスト戦略

| レイヤー | テスト種別 | カバレッジ目標 | テスト対象 |
|---------|-----------|---------------|-----------|
| Domain | ユニットテスト | 90% | Entity, ValueObject, DomainService |
| Application | ユニット/統合テスト | 85% | UseCase, ApplicationService |
| Infrastructure | 統合テスト | 70% | Repository, ExternalService |
| Presentation | 統合/E2E テスト | 70% | Controller, API |

## バックエンドテスト戦略

### 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| テストフレームワーク | JUnit 5 | 5.11+ |
| モック | Mockito | 5.14+ |
| アサーション | AssertJ | 3.26+ |
| コンテナ | Testcontainers | 1.20+ |
| BDD | Cucumber | 7.20+ |
| API テスト | REST Assured | 5.5+ |

### ディレクトリ構造

```
src/
├── main/java/com/example/accounting/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── journalentry/
│   │   │   ├── account/
│   │   │   └── fiscalperiod/
│   │   └── service/
│   ├── application/
│   │   ├── usecase/
│   │   └── service/
│   ├── infrastructure/
│   │   ├── persistence/
│   │   └── external/
│   └── presentation/
│       ├── api/
│       └── dto/
└── test/
    ├── java/com/example/accounting/
    │   ├── domain/
    │   │   ├── model/          # ドメインモデルのユニットテスト
    │   │   └── service/        # ドメインサービスのユニットテスト
    │   ├── application/
    │   │   ├── usecase/        # ユースケースのユニットテスト
    │   │   └── service/        # アプリケーションサービスのテスト
    │   ├── infrastructure/
    │   │   ├── persistence/    # リポジトリの統合テスト
    │   │   └── external/       # 外部サービスの統合テスト
    │   ├── presentation/
    │   │   └── api/            # API の統合テスト
    │   ├── e2e/                # E2E テスト
    │   └── support/            # テストユーティリティ
    │       ├── factory/        # テストデータファクトリ
    │       ├── fixture/        # テストフィクスチャ
    │       └── container/      # Testcontainers 設定
    └── resources/
        ├── features/           # Cucumber フィーチャファイル
        └── testdata/           # テストデータ
```

### ユニットテスト

#### ドメインモデルのテスト

```java
@DisplayName("仕訳エンティティのテスト")
class JournalEntryTest {

    @Nested
    @DisplayName("仕訳作成")
    class Creation {

        @Test
        @DisplayName("有効なパラメータで仕訳を作成できる")
        void shouldCreateJournalEntryWithValidParameters() {
            // Given
            var debitLine = new JournalLine(
                new AccountId("1001"),
                Money.of(10000),
                DebitCredit.DEBIT
            );
            var creditLine = new JournalLine(
                new AccountId("2001"),
                Money.of(10000),
                DebitCredit.CREDIT
            );

            // When
            var journalEntry = JournalEntry.create(
                LocalDate.of(2024, 1, 15),
                "売上計上",
                List.of(debitLine, creditLine)
            );

            // Then
            assertThat(journalEntry.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(journalEntry.getDescription()).isEqualTo("売上計上");
            assertThat(journalEntry.getLines()).hasSize(2);
            assertThat(journalEntry.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("貸借が不均衡な場合は例外をスローする")
        void shouldThrowExceptionWhenUnbalanced() {
            // Given
            var debitLine = new JournalLine(
                new AccountId("1001"),
                Money.of(10000),
                DebitCredit.DEBIT
            );
            var creditLine = new JournalLine(
                new AccountId("2001"),
                Money.of(5000),
                DebitCredit.CREDIT
            );

            // When/Then
            assertThatThrownBy(() -> JournalEntry.create(
                LocalDate.now(),
                "不均衡な仕訳",
                List.of(debitLine, creditLine)
            ))
            .isInstanceOf(UnbalancedJournalEntryException.class)
            .hasMessageContaining("貸借が一致しません");
        }
    }
}
```

#### 値オブジェクトのテスト

```java
@DisplayName("金額値オブジェクトのテスト")
class MoneyTest {

    @Nested
    @DisplayName("生成")
    class Creation {

        @ParameterizedTest
        @DisplayName("有効な金額で生成できる")
        @ValueSource(longs = {0, 1, 100, 999999999})
        void shouldCreateWithValidAmount(long amount) {
            var money = Money.of(amount);
            assertThat(money.getValue()).isEqualTo(BigDecimal.valueOf(amount));
        }

        @Test
        @DisplayName("負の金額では例外をスローする")
        void shouldThrowExceptionForNegativeAmount() {
            assertThatThrownBy(() -> Money.of(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("金額は0以上である必要があります");
        }
    }

    @Nested
    @DisplayName("演算")
    class Operations {

        @Test
        @DisplayName("加算が正しく動作する")
        void shouldAddCorrectly() {
            var money1 = Money.of(100);
            var money2 = Money.of(200);

            var result = money1.add(money2);

            assertThat(result.getValue()).isEqualByComparingTo("300");
        }

        @Test
        @DisplayName("減算が正しく動作する")
        void shouldSubtractCorrectly() {
            var money1 = Money.of(300);
            var money2 = Money.of(100);

            var result = money1.subtract(money2);

            assertThat(result.getValue()).isEqualByComparingTo("200");
        }
    }
}
```

### 統合テスト

#### リポジトリテスト（Testcontainers 使用）

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("仕訳リポジトリの統合テスト")
class JournalEntryRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("accounting_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JournalEntryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("仕訳を保存して取得できる")
    void shouldSaveAndRetrieveJournalEntry() {
        // Given
        var journalEntry = TestDataFactory.createJournalEntry();

        // When
        var saved = repository.save(journalEntry);
        entityManager.flush();
        entityManager.clear();
        var found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo(journalEntry.getDescription());
    }

    @Test
    @DisplayName("期間で仕訳を検索できる")
    void shouldFindJournalEntriesByPeriod() {
        // Given
        var entry1 = TestDataFactory.createJournalEntry(LocalDate.of(2024, 1, 15));
        var entry2 = TestDataFactory.createJournalEntry(LocalDate.of(2024, 2, 15));
        var entry3 = TestDataFactory.createJournalEntry(LocalDate.of(2024, 3, 15));
        repository.saveAll(List.of(entry1, entry2, entry3));
        entityManager.flush();

        // When
        var results = repository.findByDateBetween(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 2, 28)
        );

        // Then
        assertThat(results).hasSize(2);
    }
}
```

#### API テスト

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("仕訳 API の統合テスト")
class JournalEntryApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    private int port;

    @Autowired
    private JournalEntryRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        repository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/journal-entries - 仕訳を作成できる")
    void shouldCreateJournalEntry() {
        var request = """
            {
                "date": "2024-01-15",
                "description": "売上計上",
                "lines": [
                    {"accountId": "1001", "amount": 10000, "type": "DEBIT"},
                    {"accountId": "2001", "amount": 10000, "type": "CREDIT"}
                ]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/journal-entries")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("description", equalTo("売上計上"));
    }

    @Test
    @DisplayName("GET /api/journal-entries/{id} - 仕訳を取得できる")
    void shouldGetJournalEntry() {
        // Given
        var saved = repository.save(TestDataFactory.createJournalEntry());

        // When/Then
        given()
            .pathParam("id", saved.getId())
        .when()
            .get("/api/journal-entries/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(saved.getId().toString()));
    }
}
```

### 受け入れテスト（BDD）

#### Cucumber フィーチャファイル

```gherkin
# language: ja
@仕訳管理
機能: 仕訳の登録と管理
  経理担当者として
  日々の取引を仕訳として記録したい
  正確な財務記録を維持するために

  背景:
    前提 ユーザー "経理担当者" としてログインしている
    かつ 以下の勘定科目が存在する:
      | コード | 名称     | 区分   |
      | 1001   | 現金     | 資産   |
      | 1002   | 普通預金 | 資産   |
      | 4001   | 売上高   | 収益   |
      | 5001   | 仕入高   | 費用   |

  @正常系
  シナリオ: 有効な仕訳を登録する
    前提 会計期間 "2024年度" が開いている
    もし 以下の仕訳を登録する:
      | 日付       | 摘要     | 借方科目 | 貸方科目 | 金額   |
      | 2024-01-15 | 売上計上 | 1001     | 4001     | 10,000 |
    ならば 仕訳が正常に登録される
    かつ 仕訳番号が発行される
    かつ "現金" の残高が 10,000 増加する
    かつ "売上高" の残高が 10,000 増加する

  @異常系
  シナリオ: 貸借不均衡な仕訳は登録できない
    前提 会計期間 "2024年度" が開いている
    もし 以下の不均衡な仕訳を登録しようとする:
      | 日付       | 摘要       | 借方科目 | 借方金額 | 貸方科目 | 貸方金額 |
      | 2024-01-15 | 不正な仕訳 | 1001     | 10,000   | 4001     | 5,000    |
    ならば エラーメッセージ "貸借が一致しません" が表示される
    かつ 仕訳は登録されない

  @境界値
  シナリオアウトライン: 金額の境界値テスト
    前提 会計期間 "2024年度" が開いている
    もし 金額 <金額> で仕訳を登録する
    ならば <結果> となる

    例:
      | 金額          | 結果               |
      | 1             | 正常に登録される   |
      | 999,999,999   | 正常に登録される   |
      | 0             | エラーになる       |
      | -1            | エラーになる       |
      | 1,000,000,000 | エラーになる       |
```

#### Cucumber ステップ定義

```java
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JournalEntrySteps {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private AccountRepository accountRepository;

    private JournalEntryDto createdEntry;
    private Exception lastException;

    @Given("ユーザー {string} としてログインしている")
    public void userIsLoggedIn(String role) {
        // 認証コンテキストの設定
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(role, null, "ROLE_" + role)
        );
    }

    @Given("以下の勘定科目が存在する:")
    public void accountsExist(DataTable dataTable) {
        dataTable.asMaps().forEach(row -> {
            accountRepository.save(new Account(
                row.get("コード"),
                row.get("名称"),
                AccountType.valueOf(row.get("区分"))
            ));
        });
    }

    @When("以下の仕訳を登録する:")
    public void registerJournalEntry(DataTable dataTable) {
        var data = dataTable.asMaps().get(0);
        var request = new CreateJournalEntryRequest(
            LocalDate.parse(data.get("日付")),
            data.get("摘要"),
            List.of(
                new JournalLineRequest(data.get("借方科目"), parseAmount(data.get("金額")), "DEBIT"),
                new JournalLineRequest(data.get("貸方科目"), parseAmount(data.get("金額")), "CREDIT")
            )
        );
        createdEntry = journalEntryService.create(request);
    }

    @Then("仕訳が正常に登録される")
    public void journalEntryIsCreated() {
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getId()).isNotNull();
    }

    @Then("{string} の残高が {long} 増加する")
    public void balanceIncreases(String accountName, long amount) {
        var account = accountRepository.findByName(accountName).orElseThrow();
        assertThat(account.getBalance().getValue())
            .isEqualByComparingTo(BigDecimal.valueOf(amount));
    }
}
```

### テストデータファクトリ

```java
public class TestDataFactory {

    public static JournalEntry createJournalEntry() {
        return createJournalEntry(LocalDate.now());
    }

    public static JournalEntry createJournalEntry(LocalDate date) {
        return JournalEntry.create(
            date,
            "テスト仕訳",
            List.of(
                new JournalLine(new AccountId("1001"), Money.of(10000), DebitCredit.DEBIT),
                new JournalLine(new AccountId("2001"), Money.of(10000), DebitCredit.CREDIT)
            )
        );
    }

    public static Account createAccount(String code, String name, AccountType type) {
        return new Account(new AccountId(code), name, type);
    }

    public static FiscalPeriod createFiscalPeriod(int year) {
        return FiscalPeriod.create(
            year + "年度",
            LocalDate.of(year, 4, 1),
            LocalDate.of(year + 1, 3, 31)
        );
    }

    // Builder パターンによる柔軟なテストデータ生成
    public static JournalEntryBuilder journalEntry() {
        return new JournalEntryBuilder();
    }

    public static class JournalEntryBuilder {
        private LocalDate date = LocalDate.now();
        private String description = "テスト仕訳";
        private List<JournalLine> lines = new ArrayList<>();

        public JournalEntryBuilder withDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public JournalEntryBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public JournalEntryBuilder withLine(String accountId, long amount, DebitCredit type) {
            lines.add(new JournalLine(new AccountId(accountId), Money.of(amount), type));
            return this;
        }

        public JournalEntry build() {
            if (lines.isEmpty()) {
                lines = List.of(
                    new JournalLine(new AccountId("1001"), Money.of(10000), DebitCredit.DEBIT),
                    new JournalLine(new AccountId("2001"), Money.of(10000), DebitCredit.CREDIT)
                );
            }
            return JournalEntry.create(date, description, lines);
        }
    }
}
```

## フロントエンドテスト戦略

### 技術スタック

| カテゴリ | 技術 | バージョン |
|---------|------|-----------|
| テストフレームワーク | Vitest | 2.0+ |
| コンポーネントテスト | Testing Library | 16.0+ |
| API モック | MSW | 2.0+ |
| E2E テスト | Cypress | 14.5+ |
| カバレッジ | V8 Coverage | - |

### ディレクトリ構造

```
src/
├── components/
│   ├── journalEntry/
│   │   ├── JournalEntryForm.tsx
│   │   ├── JournalEntryForm.test.tsx
│   │   ├── JournalEntryList.tsx
│   │   └── JournalEntryList.test.tsx
│   └── common/
│       ├── MoneyInput.tsx
│       └── MoneyInput.test.tsx
├── hooks/
│   ├── useJournalEntry.ts
│   └── useJournalEntry.test.ts
├── pages/
│   └── journal/
│       ├── JournalPage.tsx
│       └── JournalPage.test.tsx
├── services/
│   └── api/
│       └── journalEntryApi.ts
├── __tests__/
│   ├── setup.ts
│   └── utils/
│       ├── testUtils.tsx
│       └── mocks/
│           └── handlers.ts
└── e2e/
    ├── support/
    │   └── commands.ts
    └── specs/
        └── journalEntry.cy.ts
```

### Vitest 設定

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/__tests__/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/__tests__/',
        '**/*.d.ts',
        '**/*.config.ts',
      ],
      thresholds: {
        branches: 80,
        functions: 80,
        lines: 80,
        statements: 80,
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

### テストセットアップ

```typescript
// src/__tests__/setup.ts
import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll } from 'vitest';
import { server } from './utils/mocks/server';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  cleanup();
  server.resetHandlers();
});
afterAll(() => server.close());
```

### MSW ハンドラー

```typescript
// src/__tests__/utils/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  // 仕訳一覧取得
  http.get('/api/journal-entries', () => {
    return HttpResponse.json([
      {
        id: '1',
        date: '2024-01-15',
        description: '売上計上',
        lines: [
          { accountId: '1001', amount: 10000, type: 'DEBIT' },
          { accountId: '4001', amount: 10000, type: 'CREDIT' },
        ],
      },
    ]);
  }),

  // 仕訳作成
  http.post('/api/journal-entries', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json(
      {
        id: crypto.randomUUID(),
        ...body,
      },
      { status: 201 }
    );
  }),

  // 仕訳取得
  http.get('/api/journal-entries/:id', ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      date: '2024-01-15',
      description: '売上計上',
      lines: [
        { accountId: '1001', amount: 10000, type: 'DEBIT' },
        { accountId: '4001', amount: 10000, type: 'CREDIT' },
      ],
    });
  }),
];
```

### コンポーネントテスト

```typescript
// src/components/journalEntry/JournalEntryForm.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { JournalEntryForm } from './JournalEntryForm';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('JournalEntryForm', () => {
  it('フォームが正しくレンダリングされる', () => {
    render(<JournalEntryForm />, { wrapper: createWrapper() });

    expect(screen.getByLabelText('日付')).toBeInTheDocument();
    expect(screen.getByLabelText('摘要')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '登録' })).toBeInTheDocument();
  });

  it('有効な入力で仕訳を登録できる', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();

    render(<JournalEntryForm onSuccess={onSuccess} />, {
      wrapper: createWrapper(),
    });

    await user.type(screen.getByLabelText('日付'), '2024-01-15');
    await user.type(screen.getByLabelText('摘要'), '売上計上');
    await user.type(screen.getByLabelText('借方金額'), '10000');
    await user.type(screen.getByLabelText('貸方金額'), '10000');

    await user.click(screen.getByRole('button', { name: '登録' }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
    });
  });

  it('貸借不均衡の場合エラーを表示する', async () => {
    const user = userEvent.setup();

    render(<JournalEntryForm />, { wrapper: createWrapper() });

    await user.type(screen.getByLabelText('日付'), '2024-01-15');
    await user.type(screen.getByLabelText('摘要'), '不正な仕訳');
    await user.type(screen.getByLabelText('借方金額'), '10000');
    await user.type(screen.getByLabelText('貸方金額'), '5000');

    await user.click(screen.getByRole('button', { name: '登録' }));

    await waitFor(() => {
      expect(screen.getByText('貸借が一致しません')).toBeInTheDocument();
    });
  });
});
```

### カスタムフックのテスト

```typescript
// src/hooks/useJournalEntry.test.ts
import { describe, it, expect } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useJournalEntries, useCreateJournalEntry } from './useJournalEntry';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('useJournalEntries', () => {
  it('仕訳一覧を取得できる', async () => {
    const { result } = renderHook(() => useJournalEntries(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toHaveLength(1);
    expect(result.current.data?.[0].description).toBe('売上計上');
  });
});

describe('useCreateJournalEntry', () => {
  it('仕訳を作成できる', async () => {
    const { result } = renderHook(() => useCreateJournalEntry(), {
      wrapper: createWrapper(),
    });

    result.current.mutate({
      date: '2024-01-15',
      description: '新規仕訳',
      lines: [
        { accountId: '1001', amount: 10000, type: 'DEBIT' },
        { accountId: '4001', amount: 10000, type: 'CREDIT' },
      ],
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });
  });
});
```

### 金額計算のテスト

```typescript
// src/utils/money.test.ts
import { describe, it, expect } from 'vitest';
import Decimal from 'decimal.js';
import { Money } from './money';

describe('Money', () => {
  describe('生成', () => {
    it.each([
      [0, '0'],
      [100, '100'],
      [999999999, '999999999'],
    ])('有効な金額 %i で生成できる', (input, expected) => {
      const money = Money.of(input);
      expect(money.toString()).toBe(expected);
    });

    it('負の金額では例外をスローする', () => {
      expect(() => Money.of(-1)).toThrow('金額は0以上である必要があります');
    });
  });

  describe('演算', () => {
    it('加算が正しく動作する', () => {
      const a = Money.of(100);
      const b = Money.of(200);
      expect(a.add(b).value.equals(new Decimal(300))).toBe(true);
    });

    it('浮動小数点誤差なく計算できる', () => {
      const a = Money.of(0.1);
      const b = Money.of(0.2);
      expect(a.add(b).value.equals(new Decimal('0.3'))).toBe(true);
    });
  });

  describe('フォーマット', () => {
    it('カンマ区切りでフォーマットできる', () => {
      const money = Money.of(1234567);
      expect(money.format()).toBe('¥1,234,567');
    });
  });
});
```

### E2E テスト（Cypress）

```typescript
// e2e/specs/journalEntry.cy.ts
describe('仕訳管理', () => {
  beforeEach(() => {
    cy.login('accountant@example.com', 'password');
    cy.visit('/journal-entries');
  });

  describe('仕訳一覧', () => {
    it('仕訳一覧が表示される', () => {
      cy.get('[data-testid="journal-entry-list"]').should('be.visible');
      cy.get('[data-testid="journal-entry-row"]').should('have.length.at.least', 1);
    });

    it('仕訳を検索できる', () => {
      cy.get('[data-testid="search-input"]').type('売上');
      cy.get('[data-testid="search-button"]').click();
      cy.get('[data-testid="journal-entry-row"]').each(($row) => {
        cy.wrap($row).should('contain', '売上');
      });
    });
  });

  describe('仕訳登録', () => {
    beforeEach(() => {
      cy.get('[data-testid="create-button"]').click();
    });

    it('有効な仕訳を登録できる', () => {
      cy.get('[data-testid="date-input"]').type('2024-01-15');
      cy.get('[data-testid="description-input"]').type('売上計上');
      cy.get('[data-testid="debit-account"]').select('現金');
      cy.get('[data-testid="debit-amount"]').type('10000');
      cy.get('[data-testid="credit-account"]').select('売上高');
      cy.get('[data-testid="credit-amount"]').type('10000');

      cy.get('[data-testid="submit-button"]').click();

      cy.get('[data-testid="success-message"]').should('be.visible');
      cy.get('[data-testid="journal-entry-list"]').should('contain', '売上計上');
    });

    it('貸借不均衡の場合エラーを表示する', () => {
      cy.get('[data-testid="date-input"]').type('2024-01-15');
      cy.get('[data-testid="description-input"]').type('不正な仕訳');
      cy.get('[data-testid="debit-amount"]').type('10000');
      cy.get('[data-testid="credit-amount"]').type('5000');

      cy.get('[data-testid="submit-button"]').click();

      cy.get('[data-testid="error-message"]')
        .should('be.visible')
        .and('contain', '貸借が一致しません');
    });
  });
});
```

### Cypress カスタムコマンド

```typescript
// e2e/support/commands.ts
declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>;
      createJournalEntry(data: JournalEntryData): Chainable<void>;
    }
  }
}

Cypress.Commands.add('login', (email: string, password: string) => {
  cy.session([email, password], () => {
    cy.visit('/login');
    cy.get('[data-testid="email-input"]').type(email);
    cy.get('[data-testid="password-input"]').type(password);
    cy.get('[data-testid="login-button"]').click();
    cy.url().should('not.include', '/login');
  });
});

Cypress.Commands.add('createJournalEntry', (data: JournalEntryData) => {
  cy.request({
    method: 'POST',
    url: '/api/journal-entries',
    body: data,
    headers: {
      Authorization: `Bearer ${Cypress.env('authToken')}`,
    },
  });
});
```

## 要件とテストのトレーサビリティ

### マッピング構造

```
ビジネスユースケース (BUC)
    └── システムユースケース (UC)
        └── ユーザーストーリー (US)
            └── 受け入れ基準 (AC)
                └── テストケース (TC)
```

### トレーサビリティマトリクス

| BUC | UC | US | テストタイプ | テストファイル |
|-----|----|----|-------------|---------------|
| BUC01 日次経理業務 | UC001 仕訳登録 | US001 仕訳入力 | ユニット | `JournalEntryTest.java` |
| | | | 統合 | `JournalEntryApiIntegrationTest.java` |
| | | | E2E | `journalEntry.cy.ts` |
| | | | 受け入れ | `journalEntry.feature` |
| | UC002 仕訳照会 | US002 仕訳検索 | ユニット | `JournalEntryQueryTest.java` |
| | | | 統合 | `JournalEntrySearchApiTest.java` |
| BUC02 月次決算業務 | UC005 試算表作成 | US010 試算表生成 | ユニット | `TrialBalanceServiceTest.java` |
| | | | 統合 | `TrialBalanceApiTest.java` |
| | UC006 月次締め | US011 月次締め処理 | ユニット | `MonthlyCloseServiceTest.java` |
| | | | 受け入れ | `monthlyClose.feature` |

### テストケースの命名規約

```
[テスト対象]_[条件]_[期待結果]

例:
- createJournalEntry_withValidData_shouldSucceed
- createJournalEntry_withUnbalancedAmount_shouldThrowException
- findJournalEntries_byDateRange_shouldReturnMatchingEntries
```

## テスト実行環境

### CI/CD パイプライン

```yaml
# .github/workflows/test.yml
name: Test

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Run unit tests
        run: ./gradlew test

      - name: Upload coverage report
        uses: codecov/codecov-action@v4

  integration-test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: accounting_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports:
          - 5432:5432
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'

      - name: Run integration tests
        run: ./gradlew integrationTest

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'

      - name: Install dependencies
        run: npm ci

      - name: Run tests
        run: npm test -- --coverage

      - name: Upload coverage
        uses: codecov/codecov-action@v4

  e2e-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'

      - name: Install dependencies
        run: npm ci

      - name: Start application
        run: npm run start:test &

      - name: Run Cypress tests
        uses: cypress-io/github-action@v6
        with:
          wait-on: 'http://localhost:3000'
```

### ローカル実行コマンド

```bash
# バックエンド
./gradlew test                    # ユニットテスト
./gradlew integrationTest         # 統合テスト
./gradlew cucumberTest            # 受け入れテスト
./gradlew test jacocoTestReport   # カバレッジレポート

# フロントエンド
npm test                          # ユニットテスト
npm run test:coverage             # カバレッジ付きテスト
npm run test:watch                # ウォッチモード
npm run cypress:open              # Cypress GUI
npm run cypress:run               # Cypress ヘッドレス
```

## 品質メトリクス

### カバレッジ目標

| レイヤー | 行カバレッジ | 分岐カバレッジ |
|---------|------------|--------------|
| Domain | 90% | 85% |
| Application | 85% | 80% |
| Infrastructure | 70% | 65% |
| Presentation | 70% | 65% |
| 全体 | 80% | 75% |

### 品質ゲート

- ✅ 全テストがパス
- ✅ カバレッジ目標を達成
- ✅ 静的解析（SonarQube）で重大な問題がない
- ✅ セキュリティスキャンで脆弱性がない

## テスト分類

### 正常系テスト

正常な入力で期待通りの結果が得られることを検証する。

```java
@Test
@DisplayName("正常な仕訳データで登録できる")
void shouldCreateJournalEntry() {
    // 正常なデータでの動作確認
}
```

### 境界値テスト

境界値での動作を検証する。

```java
@ParameterizedTest
@DisplayName("金額の境界値テスト")
@ValueSource(longs = {1, Long.MAX_VALUE})
void shouldHandleBoundaryValues(long amount) {
    // 境界値での動作確認
}
```

### 異常系テスト

異常な入力でエラー処理が正しく動作することを検証する。

```java
@Test
@DisplayName("不正なデータでは例外をスローする")
void shouldThrowExceptionForInvalidData() {
    assertThatThrownBy(() -> service.create(invalidData))
        .isInstanceOf(ValidationException.class);
}
```

### 極端値テスト

極端な値での動作を検証する。

```java
@Test
@DisplayName("大量データでもタイムアウトしない")
void shouldHandleLargeDataSet() {
    // パフォーマンステスト
}
```

## 付録

### テストダブルの使い分け

| 種類 | 用途 | 例 |
|------|------|-----|
| Stub | 固定値を返す | リポジトリの戻り値 |
| Mock | 呼び出しを検証 | メール送信の確認 |
| Fake | 簡易実装 | インメモリリポジトリ |
| Spy | 実オブジェクトの一部をモック | 部分モック |

### 参考資料

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers Documentation](https://testcontainers.com/)
- [Vitest Documentation](https://vitest.dev/)
- [Cypress Documentation](https://docs.cypress.io/)
- [Testing Library](https://testing-library.com/)
