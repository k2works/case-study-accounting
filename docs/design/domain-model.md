# ドメインモデル設計書

## 1. 概要

本書は、財務会計システムのドメインモデル設計を定義します。ドメイン駆動設計（DDD）のパターンに基づき、ビジネスロジックをドメインモデルに集約します。

### 1.1 システム概要

| 項目 | 内容 |
|------|------|
| システム名 | 財務会計システム |
| 対象領域 | 複式簿記による財務会計処理 |
| 主要機能 | 仕訳管理、元帳管理、残高管理、財務諸表生成 |

### 1.2 設計原則

1. **リッチドメインモデル**: データとビジネスロジックを一体化
2. **依存性逆転の原則**: ドメイン層は外部技術に依存しない
3. **不変条件の保証**: コンストラクタで制約を強制
4. **複式簿記の原理**: 借方合計 = 貸方合計をモデルで保証

---

## 2. ドメインモデル全体像

### 2.1 パッケージ構造

```
com.example.accounting
├── domain/                          # ドメイン層
│   ├── model/                       # ドメインモデル
│   │   ├── account/                 # 勘定科目集約
│   │   │   ├── Account.java         # 集約ルート
│   │   │   ├── AccountCode.java     # 値オブジェクト
│   │   │   ├── AccountType.java     # 値オブジェクト（enum）
│   │   │   ├── AccountStructure.java # エンティティ
│   │   │   └── AccountList.java     # コレクション
│   │   ├── journal/                 # 仕訳集約
│   │   │   ├── JournalEntry.java    # 集約ルート
│   │   │   ├── JournalDetail.java   # エンティティ
│   │   │   ├── JournalItem.java     # エンティティ
│   │   │   ├── JournalEntryId.java  # 値オブジェクト
│   │   │   └── DebitCreditType.java # 値オブジェクト（enum）
│   │   ├── balance/                 # 残高集約
│   │   │   ├── DailyBalance.java    # 集約ルート
│   │   │   └── MonthlyBalance.java  # 集約ルート
│   │   └── statement/               # 財務諸表
│   │       ├── TrialBalance.java
│   │       ├── BalanceSheet.java
│   │       └── IncomeStatement.java
│   ├── service/                     # ドメインサービス
│   │   ├── BalanceCalculator.java
│   │   └── JournalValidator.java
│   ├── exception/                   # ドメイン例外
│   │   ├── InvalidJournalEntryException.java
│   │   └── AccountNotFoundException.java
│   └── type/                        # 共通型（値オブジェクト）
│       ├── Money.java
│       └── AccountingPeriod.java
│
├── application/                     # アプリケーション層
│   ├── port/
│   │   ├── in/                      # Input Port（ユースケース）
│   │   │   ├── JournalEntryUseCase.java
│   │   │   └── AccountUseCase.java
│   │   └── out/                     # Output Port（リポジトリ）
│   │       ├── JournalEntryRepository.java
│   │       ├── AccountRepository.java
│   │       └── BalanceRepository.java
│   └── service/                     # Application Service
│       ├── JournalEntryService.java
│       └── AccountService.java
│
└── infrastructure/                  # インフラストラクチャ層
    └── persistence/                 # 永続化
        ├── entity/                  # MyBatis Entity
        ├── mapper/                  # MyBatis Mapper
        └── repository/              # Repository 実装
```

### 2.2 集約の構成

```plantuml
@startuml
title ドメインモデル - 集約の構成

package "勘定科目集約" {
  class Account <<Aggregate Root>> {
    - accountCode: AccountCode
    - accountName: String
    - accountType: AccountType
    - bsplType: BSPLType
    - structure: AccountStructure
    + isBalanceSheetAccount(): boolean
    + isProfitLossAccount(): boolean
    + isDebitBalance(): boolean
  }

  class AccountCode <<Value Object>> {
    - code: String
    + validate(): void
  }

  class AccountStructure <<Entity>> {
    - accountPath: String
    - hierarchyLevel: int
    - parentCode: AccountCode
  }

  enum AccountType <<Value Object>> {
    ASSET
    LIABILITY
    EQUITY
    REVENUE
    EXPENSE
  }
}

package "仕訳集約" {
  class JournalEntry <<Aggregate Root>> {
    - journalId: JournalEntryId
    - journalDate: LocalDate
    - description: String
    - details: List<JournalDetail>
    + isBalanced(): boolean
    + getTotalDebit(): Money
    + getTotalCredit(): Money
    + validate(): void
  }

  class JournalDetail <<Entity>> {
    - lineNo: int
    - description: String
    - items: List<JournalItem>
  }

  class JournalItem <<Entity>> {
    - debitCreditType: DebitCreditType
    - accountCode: AccountCode
    - amount: Money
  }

  class JournalEntryId <<Value Object>> {
    - value: String
  }

  enum DebitCreditType <<Value Object>> {
    DEBIT
    CREDIT
  }
}

package "残高集約" {
  class DailyBalance <<Aggregate Root>> {
    - entryDate: LocalDate
    - accountCode: AccountCode
    - debitAmount: Money
    - creditAmount: Money
    + getNetBalance(): Money
  }

  class MonthlyBalance <<Aggregate Root>> {
    - fiscalYear: int
    - month: int
    - accountCode: AccountCode
    - openingBalance: Money
    - closingBalance: Money
  }
}

package "共通型" {
  class Money <<Value Object>> {
    - amount: BigDecimal
    + add(Money): Money
    + subtract(Money): Money
    + isZero(): boolean
  }

  class AccountingPeriod <<Value Object>> {
    - fiscalYear: int
    - startDate: LocalDate
    - endDate: LocalDate
  }
}

Account *-- AccountCode
Account *-- AccountStructure
Account *-- AccountType

JournalEntry *-- JournalEntryId
JournalEntry *-- "1..*" JournalDetail
JournalDetail *-- "1..*" JournalItem
JournalItem *-- DebitCreditType
JournalItem *-- AccountCode
JournalItem *-- Money

DailyBalance *-- AccountCode
DailyBalance *-- Money
MonthlyBalance *-- AccountCode
MonthlyBalance *-- Money

@enduml
```

---

## 3. 値オブジェクト（Value Object）

### 3.1 Money（金額）

金額を表現する値オブジェクト。不変で、値による等価性を持ちます。

```plantuml
@startuml
class Money <<Value Object>> {
  - amount: BigDecimal
  --
  + Money(amount: BigDecimal)
  + add(other: Money): Money
  + subtract(other: Money): Money
  + multiply(multiplier: BigDecimal): Money
  + negate(): Money
  + isZero(): boolean
  + isPositive(): boolean
  + isNegative(): boolean
  + compareTo(other: Money): int
  + equals(other: Object): boolean
  + hashCode(): int
}

note right of Money
  **不変条件**
  - amount は null 不可
  - 小数点以下2桁に丸め

  **使用箇所**
  - 仕訳金額
  - 残高金額
  - 財務諸表の金額
end note
@enduml
```

**実装例**:

```java
@Value
public class Money implements Comparable<Money> {
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    BigDecimal amount;

    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("金額は必須です");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money negate() {
        return new Money(this.amount.negate());
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }
}
```

### 3.2 AccountCode（勘定科目コード）

勘定科目を一意に識別するコード。フォーマット検証を含みます。

```plantuml
@startuml
class AccountCode <<Value Object>> {
  - code: String
  --
  + AccountCode(code: String)
  + getValue(): String
  + equals(other: Object): boolean
  + hashCode(): int
  + toString(): String
}

note right of AccountCode
  **不変条件**
  - null 不可
  - 空文字不可
  - 4〜10桁の数字

  **例**
  - "1010"（現金）
  - "11000"（流動資産）
end note
@enduml
```

**実装例**:

```java
@Value
public class AccountCode {
    String code;

    public AccountCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("勘定科目コードは必須です");
        }
        if (!code.matches("^[0-9]{4,10}$")) {
            throw new IllegalArgumentException("勘定科目コードは4〜10桁の数字です: " + code);
        }
        this.code = code;
    }
}
```

### 3.3 JournalEntryId（仕訳伝票番号）

仕訳を一意に識別する伝票番号。採番ルールを含みます。

```plantuml
@startuml
class JournalEntryId <<Value Object>> {
  - value: String
  --
  + JournalEntryId(value: String)
  + getValue(): String
  + {static} generate(date: LocalDate, sequence: int): JournalEntryId
}

note right of JournalEntryId
  **フォーマット**
  JE-YYYYMMDD-NNN

  **例**
  JE-20250101-001
  JE-20250101-002
end note
@enduml
```

**実装例**:

```java
@Value
public class JournalEntryId {
    String value;

    public JournalEntryId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("仕訳伝票番号は必須です");
        }
        if (!value.matches("^JE-\\d{8}-\\d{3}$")) {
            throw new IllegalArgumentException("仕訳伝票番号のフォーマットが不正です: " + value);
        }
        this.value = value;
    }

    public static JournalEntryId generate(LocalDate date, int sequence) {
        String value = String.format("JE-%s-%03d",
            date.format(DateTimeFormatter.BASIC_ISO_DATE),
            sequence);
        return new JournalEntryId(value);
    }
}
```

### 3.4 AccountType（勘定科目種別）

会計の5要素を表現する列挙型。

```plantuml
@startuml
enum AccountType <<Value Object>> {
  ASSET
  LIABILITY
  EQUITY
  REVENUE
  EXPENSE
  --
  - japaneseLabel: String
  - bsplType: BSPLType
  - normalBalance: DebitCreditType
  --
  + getJapaneseLabel(): String
  + getBsplType(): BSPLType
  + getNormalBalance(): DebitCreditType
  + isBalanceSheetType(): boolean
  + isProfitLossType(): boolean
}

note right of AccountType
  **対応表**
  ASSET: 資産 / B/S / 借方
  LIABILITY: 負債 / B/S / 貸方
  EQUITY: 純資産 / B/S / 貸方
  REVENUE: 収益 / P/L / 貸方
  EXPENSE: 費用 / P/L / 借方
end note
@enduml
```

**実装例**:

```java
public enum AccountType {
    ASSET("資産", BSPLType.BALANCE_SHEET, DebitCreditType.DEBIT),
    LIABILITY("負債", BSPLType.BALANCE_SHEET, DebitCreditType.CREDIT),
    EQUITY("純資産", BSPLType.BALANCE_SHEET, DebitCreditType.CREDIT),
    REVENUE("収益", BSPLType.PROFIT_LOSS, DebitCreditType.CREDIT),
    EXPENSE("費用", BSPLType.PROFIT_LOSS, DebitCreditType.DEBIT);

    private final String japaneseLabel;
    private final BSPLType bsplType;
    private final DebitCreditType normalBalance;

    AccountType(String japaneseLabel, BSPLType bsplType, DebitCreditType normalBalance) {
        this.japaneseLabel = japaneseLabel;
        this.bsplType = bsplType;
        this.normalBalance = normalBalance;
    }

    public boolean isBalanceSheetType() {
        return this.bsplType == BSPLType.BALANCE_SHEET;
    }

    public boolean isProfitLossType() {
        return this.bsplType == BSPLType.PROFIT_LOSS;
    }
}
```

### 3.5 DebitCreditType（貸借区分）

借方・貸方を表現する列挙型。

```plantuml
@startuml
enum DebitCreditType <<Value Object>> {
  DEBIT
  CREDIT
  --
  - code: String
  - japaneseLabel: String
  --
  + getCode(): String
  + getJapaneseLabel(): String
  + opposite(): DebitCreditType
}

note right of DebitCreditType
  DEBIT: D / 借方
  CREDIT: C / 貸方
end note
@enduml
```

**実装例**:

```java
public enum DebitCreditType {
    DEBIT("D", "借方"),
    CREDIT("C", "貸方");

    private final String code;
    private final String japaneseLabel;

    DebitCreditType(String code, String japaneseLabel) {
        this.code = code;
        this.japaneseLabel = japaneseLabel;
    }

    public DebitCreditType opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }
}
```

### 3.6 AccountingPeriod（会計期間）

会計年度と期間を表現する値オブジェクト。

```plantuml
@startuml
class AccountingPeriod <<Value Object>> {
  - fiscalYear: int
  - startDate: LocalDate
  - endDate: LocalDate
  --
  + AccountingPeriod(fiscalYear: int, startDate: LocalDate, endDate: LocalDate)
  + contains(date: LocalDate): boolean
  + isCurrentPeriod(): boolean
  + isClosed(): boolean
}

note right of AccountingPeriod
  **例**
  fiscalYear: 2025
  startDate: 2025-04-01
  endDate: 2026-03-31
end note
@enduml
```

---

## 4. エンティティ（Entity）

### 4.1 Account（勘定科目）

勘定科目を表現するエンティティ。集約ルートとして機能します。

```plantuml
@startuml
class Account <<Entity>> <<Aggregate Root>> {
  - accountCode: AccountCode
  - accountName: String
  - accountAbbr: String
  - accountKana: String
  - accountType: AccountType
  - bsplType: BSPLType
  - debitCreditType: DebitCreditType
  - isSummaryAccount: boolean
  - displayOrder: int
  - taxCode: String
  - structure: AccountStructure
  --
  + isBalanceSheetAccount(): boolean
  + isProfitLossAccount(): boolean
  + isDebitBalance(): boolean
  + isCreditBalance(): boolean
  + isAsset(): boolean
  + isLiability(): boolean
  + isEquity(): boolean
  + isRevenue(): boolean
  + isExpense(): boolean
  + validateForJournal(): void
}

note right of Account
  **識別子**
  accountCode（勘定科目コード）

  **不変条件**
  - accountCode は必須
  - accountName は必須
  - accountType は必須
end note
@enduml
```

**実装例**:

```java
@Value
@With
public class Account {
    AccountCode accountCode;
    String accountName;
    String accountAbbr;
    String accountKana;
    AccountType accountType;
    BSPLType bsplType;
    DebitCreditType debitCreditType;
    boolean isSummaryAccount;
    Integer displayOrder;
    String taxCode;
    AccountStructure structure;

    public Account(AccountCode accountCode, String accountName, AccountType accountType) {
        if (accountCode == null) {
            throw new IllegalArgumentException("勘定科目コードは必須です");
        }
        if (accountName == null || accountName.isBlank()) {
            throw new IllegalArgumentException("勘定科目名は必須です");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("勘定科目種別は必須です");
        }
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.bsplType = accountType.getBsplType();
        this.debitCreditType = accountType.getNormalBalance();
        // その他のフィールドは WithBuilder で設定
    }

    public boolean isBalanceSheetAccount() {
        return bsplType == BSPLType.BALANCE_SHEET;
    }

    public boolean isProfitLossAccount() {
        return bsplType == BSPLType.PROFIT_LOSS;
    }

    public boolean isDebitBalance() {
        return debitCreditType == DebitCreditType.DEBIT;
    }

    public boolean isCreditBalance() {
        return debitCreditType == DebitCreditType.CREDIT;
    }

    public void validateForJournal() {
        if (isSummaryAccount) {
            throw new IllegalStateException("集計科目は仕訳に使用できません: " + accountCode);
        }
    }
}
```

### 4.2 AccountStructure（勘定科目構成）

勘定科目の階層構造を表現するエンティティ。チルダ連結方式を採用します。

```plantuml
@startuml
class AccountStructure <<Entity>> {
  - accountCode: AccountCode
  - accountPath: String
  - hierarchyLevel: int
  - parentCode: AccountCode
  - displayOrder: int
  --
  + getPathSegments(): List<String>
  + isDescendantOf(ancestorCode: AccountCode): boolean
  + isAncestorOf(descendantCode: AccountCode): boolean
}

note right of AccountStructure
  **チルダ連結方式**

  パス例: "11~11000~11190~11110"
  ├─ 11: 資産の部
  ├─ 11000: 流動資産
  ├─ 11190: 現金及び預金
  └─ 11110: 現金

  hierarchyLevel: 4
  parentCode: 11190
end note
@enduml
```

**実装例**:

```java
@Value
public class AccountStructure {
    AccountCode accountCode;
    String accountPath;
    int hierarchyLevel;
    AccountCode parentCode;
    int displayOrder;

    public List<String> getPathSegments() {
        return Arrays.asList(accountPath.split("~"));
    }

    public boolean isDescendantOf(AccountCode ancestorCode) {
        return accountPath.contains("~" + ancestorCode.getCode() + "~") ||
               accountPath.startsWith(ancestorCode.getCode() + "~");
    }

    public boolean isAncestorOf(AccountCode descendantCode) {
        // 子孫のパスがこのアカウントのパスで始まるかチェック
        return descendantCode != null;
    }
}
```

### 4.3 JournalEntry（仕訳）

仕訳伝票を表現するエンティティ。集約ルートとして機能し、複式簿記の原理を保証します。

```plantuml
@startuml
class JournalEntry <<Entity>> <<Aggregate Root>> {
  - journalId: JournalEntryId
  - journalDate: LocalDate
  - inputDate: LocalDate
  - description: String
  - isSettlementJournal: boolean
  - isSimpleJournal: boolean
  - isReversalEntry: boolean
  - reversalJournalId: JournalEntryId
  - details: List<JournalDetail>
  --
  + isBalanced(): boolean
  + getTotalDebit(): Money
  + getTotalCredit(): Money
  + getDebitItems(): List<JournalItem>
  + getCreditItems(): List<JournalItem>
  + validate(): void
  + addDetail(detail: JournalDetail): void
}

note right of JournalEntry
  **識別子**
  journalId（仕訳伝票番号）

  **不変条件**
  - journalDate は必須
  - details は2件以上
  - 借方合計 = 貸方合計

  **赤黒処理**
  isReversalEntry が true の場合
  reversalJournalId は必須
end note
@enduml
```

**実装例**:

```java
@Value
@With
public class JournalEntry {
    JournalEntryId journalId;
    LocalDate journalDate;
    LocalDate inputDate;
    String description;
    boolean isSettlementJournal;
    boolean isSimpleJournal;
    boolean isReversalEntry;
    JournalEntryId reversalJournalId;
    List<JournalDetail> details;

    public JournalEntry(JournalEntryId journalId, LocalDate journalDate,
                        String description, List<JournalDetail> details) {
        if (journalId == null) {
            throw new IllegalArgumentException("仕訳伝票番号は必須です");
        }
        if (journalDate == null) {
            throw new IllegalArgumentException("起票日は必須です");
        }
        if (details == null || details.size() < 1) {
            throw new IllegalArgumentException("仕訳明細は1件以上必要です");
        }

        this.journalId = journalId;
        this.journalDate = journalDate;
        this.inputDate = LocalDate.now();
        this.description = description;
        this.details = List.copyOf(details);
        this.isSettlementJournal = false;
        this.isSimpleJournal = details.size() == 1;
        this.isReversalEntry = false;
        this.reversalJournalId = null;
    }

    /**
     * 複式簿記の原則: 借方合計 = 貸方合計
     */
    public boolean isBalanced() {
        return getTotalDebit().compareTo(getTotalCredit()) == 0;
    }

    public Money getTotalDebit() {
        return details.stream()
                .flatMap(d -> d.getItems().stream())
                .filter(JournalItem::isDebit)
                .map(JournalItem::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public Money getTotalCredit() {
        return details.stream()
                .flatMap(d -> d.getItems().stream())
                .filter(JournalItem::isCredit)
                .map(JournalItem::getAmount)
                .reduce(Money.ZERO, Money::add);
    }

    public void validate() {
        if (!isBalanced()) {
            throw new InvalidJournalEntryException(
                "貸借が一致していません。借方: " + getTotalDebit() + ", 貸方: " + getTotalCredit());
        }
        if (isReversalEntry && reversalJournalId == null) {
            throw new InvalidJournalEntryException("赤伝票には元伝票番号が必須です");
        }
    }
}
```

### 4.4 JournalDetail（仕訳明細）

仕訳の明細行を表現するエンティティ。行摘要を持ちます。

```plantuml
@startuml
class JournalDetail <<Entity>> {
  - lineNo: int
  - description: String
  - items: List<JournalItem>
  --
  + getDebitItem(): JournalItem
  + getCreditItem(): JournalItem
  + isBalanced(): boolean
}

note right of JournalDetail
  **識別子**
  journalId + lineNo

  **3層構造**
  仕訳 → 仕訳明細 → 仕訳貸借明細
end note
@enduml
```

**実装例**:

```java
@Value
public class JournalDetail {
    int lineNo;
    String description;
    List<JournalItem> items;

    public JournalDetail(int lineNo, String description, List<JournalItem> items) {
        if (lineNo < 1) {
            throw new IllegalArgumentException("行番号は1以上です");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("貸借明細は1件以上必要です");
        }
        this.lineNo = lineNo;
        this.description = description;
        this.items = List.copyOf(items);
    }

    public Optional<JournalItem> getDebitItem() {
        return items.stream()
                .filter(JournalItem::isDebit)
                .findFirst();
    }

    public Optional<JournalItem> getCreditItem() {
        return items.stream()
                .filter(JournalItem::isCredit)
                .findFirst();
    }
}
```

### 4.5 JournalItem（仕訳貸借明細）

借方または貸方の詳細を表現するエンティティ。

```plantuml
@startuml
class JournalItem <<Entity>> {
  - debitCreditType: DebitCreditType
  - accountCode: AccountCode
  - amount: Money
  - currencyCode: String
  - exchangeRate: BigDecimal
  - baseAmount: Money
  - departmentCode: String
  - projectCode: String
  - taxType: String
  - taxRate: BigDecimal
  --
  + isDebit(): boolean
  + isCredit(): boolean
}

note right of JournalItem
  **識別子**
  journalId + lineNo + debitCreditType

  **多次元管理**
  - 部門コード
  - プロジェクトコード
  - セグメントコード

  **多通貨対応**
  - 通貨コード
  - 為替レート
  - 基軸換算金額
end note
@enduml
```

**実装例**:

```java
@Value
@With
public class JournalItem {
    DebitCreditType debitCreditType;
    AccountCode accountCode;
    Money amount;
    String currencyCode;
    BigDecimal exchangeRate;
    Money baseAmount;
    String departmentCode;
    String projectCode;
    String taxType;
    BigDecimal taxRate;

    public JournalItem(DebitCreditType debitCreditType, AccountCode accountCode, Money amount) {
        if (debitCreditType == null) {
            throw new IllegalArgumentException("貸借区分は必須です");
        }
        if (accountCode == null) {
            throw new IllegalArgumentException("勘定科目コードは必須です");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("金額は0より大きい値が必要です");
        }
        this.debitCreditType = debitCreditType;
        this.accountCode = accountCode;
        this.amount = amount;
        this.currencyCode = "JPY";
        this.exchangeRate = BigDecimal.ONE;
        this.baseAmount = amount;
        // その他はWithBuilderで設定
    }

    public boolean isDebit() {
        return debitCreditType == DebitCreditType.DEBIT;
    }

    public boolean isCredit() {
        return debitCreditType == DebitCreditType.CREDIT;
    }
}
```

---

## 5. 集約（Aggregate）

### 5.1 勘定科目集約

```plantuml
@startuml
title 勘定科目集約

package "Account Aggregate" {
  class Account <<Aggregate Root>> #FFE0E0 {
    - accountCode: AccountCode
    - accountName: String
    - accountType: AccountType
    - structure: AccountStructure
  }

  class AccountCode <<Value Object>> #E0FFE0
  class AccountType <<Value Object>> #E0FFE0
  class AccountStructure <<Entity>> #E0E0FF
}

Account *-- AccountCode
Account *-- AccountType
Account *-- AccountStructure

note bottom of Account
  **集約ルート**
  - 外部からのアクセスポイント
  - 整合性の境界
  - トランザクション単位
end note
@enduml
```

### 5.2 仕訳集約

```plantuml
@startuml
title 仕訳集約（3層構造）

package "Journal Aggregate" {
  class JournalEntry <<Aggregate Root>> #FFE0E0 {
    - journalId: JournalEntryId
    - journalDate: LocalDate
    - details: List<JournalDetail>
    + isBalanced(): boolean
    + validate(): void
  }

  class JournalEntryId <<Value Object>> #E0FFE0

  class JournalDetail <<Entity>> #E0E0FF {
    - lineNo: int
    - description: String
    - items: List<JournalItem>
  }

  class JournalItem <<Entity>> #E0E0FF {
    - debitCreditType: DebitCreditType
    - accountCode: AccountCode
    - amount: Money
  }

  class DebitCreditType <<Value Object>> #E0FFE0
  class AccountCode <<Value Object>> #E0FFE0
  class Money <<Value Object>> #E0FFE0
}

JournalEntry *-- JournalEntryId
JournalEntry *-- "1..*" JournalDetail
JournalDetail *-- "1..*" JournalItem
JournalItem *-- DebitCreditType
JournalItem *-- AccountCode
JournalItem *-- Money

note bottom of JournalEntry
  **複式簿記の原理**
  借方合計 = 貸方合計

  この不変条件は
  集約ルートで保証
end note
@enduml
```

### 5.3 残高集約

```plantuml
@startuml
title 残高集約

package "Daily Balance Aggregate" {
  class DailyBalance <<Aggregate Root>> #FFE0E0 {
    - entryDate: LocalDate
    - accountCode: AccountCode
    - subAccountCode: String
    - departmentCode: String
    - projectCode: String
    - debitAmount: Money
    - creditAmount: Money
    + getNetBalance(): Money
    + addDebit(amount: Money): void
    + addCredit(amount: Money): void
  }
}

package "Monthly Balance Aggregate" {
  class MonthlyBalance <<Aggregate Root>> #FFE0E0 {
    - fiscalYear: int
    - month: int
    - accountCode: AccountCode
    - openingBalance: Money
    - debitAmount: Money
    - creditAmount: Money
    - closingBalance: Money
    + calculateClosingBalance(): Money
  }
}

note bottom of DailyBalance
  **複合主キー（6項目）**
  - 起票日
  - 勘定科目コード
  - 補助科目コード
  - 部門コード
  - プロジェクトコード
  - 決算仕訳フラグ
end note

note bottom of MonthlyBalance
  **月末残高計算**
  月末残高 = 月初残高 + 借方金額 - 貸方金額
end note
@enduml
```

---

## 6. ドメインサービス（Domain Service）

### 6.1 BalanceCalculator（残高計算サービス）

複数の残高エンティティを跨ぐ計算ロジックを提供します。

```plantuml
@startuml
class BalanceCalculator <<Domain Service>> {
  + calculateTrialBalance(period: AccountingPeriod): TrialBalance
  + calculateAccountBalance(accountCode: AccountCode, date: LocalDate): Money
  + calculateHierarchyBalance(parentCode: AccountCode, date: LocalDate): Money
  + aggregateMonthlyBalance(dailyBalances: List<DailyBalance>): MonthlyBalance
}

note right of BalanceCalculator
  **状態を持たない**
  - 計算ロジックのみ
  - エンティティに属さない処理

  **使用箇所**
  - 試算表生成
  - 財務諸表生成
  - 階層別残高集計
end note
@enduml
```

**実装例**:

```java
@Service
public class BalanceCalculator {

    public Money calculateAccountBalance(
            List<DailyBalance> balances,
            AccountCode accountCode,
            DebitCreditType normalBalance) {

        Money totalDebit = balances.stream()
                .filter(b -> b.getAccountCode().equals(accountCode))
                .map(DailyBalance::getDebitAmount)
                .reduce(Money.ZERO, Money::add);

        Money totalCredit = balances.stream()
                .filter(b -> b.getAccountCode().equals(accountCode))
                .map(DailyBalance::getCreditAmount)
                .reduce(Money.ZERO, Money::add);

        if (normalBalance == DebitCreditType.DEBIT) {
            return totalDebit.subtract(totalCredit);
        } else {
            return totalCredit.subtract(totalDebit);
        }
    }

    public Money calculateHierarchyBalance(
            List<DailyBalance> balances,
            List<AccountStructure> structures,
            AccountCode parentCode) {

        List<AccountCode> descendants = structures.stream()
                .filter(s -> s.isDescendantOf(parentCode))
                .map(AccountStructure::getAccountCode)
                .collect(Collectors.toList());

        return balances.stream()
                .filter(b -> descendants.contains(b.getAccountCode()))
                .map(b -> b.getDebitAmount().subtract(b.getCreditAmount()))
                .reduce(Money.ZERO, Money::add);
    }
}
```

### 6.2 JournalValidator（仕訳検証サービス）

仕訳登録時の業務ルール検証を行います。

```plantuml
@startuml
class JournalValidator <<Domain Service>> {
  - accountRepository: AccountRepository
  --
  + validate(entry: JournalEntry): void
  + validateDoubleEntry(entry: JournalEntry): void
  + validateAccountExistence(entry: JournalEntry): void
  + validateAccountingPeriod(entry: JournalEntry, period: AccountingPeriod): void
  + validateNotSummaryAccount(entry: JournalEntry): void
}

note right of JournalValidator
  **検証項目**
  - 複式簿記の原則（貸借一致）
  - 勘定科目の存在確認
  - 会計期間のチェック
  - 集計科目の使用禁止
end note
@enduml
```

**実装例**:

```java
@Service
@RequiredArgsConstructor
public class JournalValidator {

    private final AccountRepository accountRepository;

    public void validate(JournalEntry entry) {
        validateDoubleEntry(entry);
        validateAccountExistence(entry);
        validateNotSummaryAccount(entry);
    }

    public void validateDoubleEntry(JournalEntry entry) {
        if (!entry.isBalanced()) {
            throw new InvalidJournalEntryException(
                "貸借が一致していません。借方: " + entry.getTotalDebit() +
                ", 貸方: " + entry.getTotalCredit());
        }
    }

    public void validateAccountExistence(JournalEntry entry) {
        for (JournalDetail detail : entry.getDetails()) {
            for (JournalItem item : detail.getItems()) {
                accountRepository.findByCode(item.getAccountCode())
                    .orElseThrow(() -> new AccountNotFoundException(
                        "勘定科目が存在しません: " + item.getAccountCode()));
            }
        }
    }

    public void validateNotSummaryAccount(JournalEntry entry) {
        for (JournalDetail detail : entry.getDetails()) {
            for (JournalItem item : detail.getItems()) {
                Account account = accountRepository.findByCode(item.getAccountCode())
                    .orElseThrow();
                account.validateForJournal();
            }
        }
    }
}
```

---

## 7. リポジトリ（Repository）

### 7.1 AccountRepository

```plantuml
@startuml
interface AccountRepository <<Output Port>> {
  + findAll(): List<Account>
  + findByCode(code: AccountCode): Optional<Account>
  + findByType(type: AccountType): List<Account>
  + findByBsplType(bsplType: BSPLType): List<Account>
  + findDescendants(parentCode: AccountCode): List<Account>
  + save(account: Account): Account
  + delete(code: AccountCode): void
}

note right of AccountRepository
  **配置**
  インターフェース: application/port/out
  実装: infrastructure/persistence/repository

  **戻り値**
  Domain Model（Account）を返す
end note
@enduml
```

### 7.2 JournalEntryRepository

```plantuml
@startuml
interface JournalEntryRepository <<Output Port>> {
  + findById(id: JournalEntryId): Optional<JournalEntry>
  + findByDateRange(startDate: LocalDate, endDate: LocalDate): List<JournalEntry>
  + findByAccountCode(accountCode: AccountCode): List<JournalEntry>
  + save(entry: JournalEntry): JournalEntry
  + delete(id: JournalEntryId): void
  + nextIdentity(date: LocalDate): JournalEntryId
}

note right of JournalEntryRepository
  **3層構造の保存**
  - 仕訳
  - 仕訳明細
  - 仕訳貸借明細
  を一括で保存
end note
@enduml
```

### 7.3 BalanceRepository

```plantuml
@startuml
interface DailyBalanceRepository <<Output Port>> {
  + findByDateAndAccount(date: LocalDate, accountCode: AccountCode): Optional<DailyBalance>
  + findByDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyBalance>
  + upsert(balance: DailyBalance): DailyBalance
}

interface MonthlyBalanceRepository <<Output Port>> {
  + findByPeriod(fiscalYear: int, month: int): List<MonthlyBalance>
  + findByAccount(accountCode: AccountCode, fiscalYear: int): List<MonthlyBalance>
  + save(balance: MonthlyBalance): MonthlyBalance
}

note right of DailyBalanceRepository
  **UPSERT パターン**
  ON CONFLICT ... DO UPDATE
  で効率的に残高更新
end note
@enduml
```

---

## 8. ファクトリ（Factory）

### 8.1 JournalEntryFactory

```plantuml
@startuml
interface JournalEntryFactory <<Factory>> {
  + create(date: LocalDate, description: String, details: List<JournalDetail>): JournalEntry
  + createReversalEntry(original: JournalEntry): JournalEntry
  + createSettlementEntry(date: LocalDate, details: List<JournalDetail>): JournalEntry
}

class JournalEntryFactoryImpl implements JournalEntryFactory {
  - journalEntryRepository: JournalEntryRepository
  --
  + create(...): JournalEntry
  + createReversalEntry(...): JournalEntry
  + createSettlementEntry(...): JournalEntry
}

note right of JournalEntryFactory
  **責務**
  - 仕訳伝票番号の採番
  - 赤伝票の生成
  - 決算仕訳の生成
end note
@enduml
```

**実装例**:

```java
@Service
@RequiredArgsConstructor
public class JournalEntryFactoryImpl implements JournalEntryFactory {

    private final JournalEntryRepository repository;

    @Override
    public JournalEntry create(LocalDate date, String description, List<JournalDetail> details) {
        JournalEntryId id = repository.nextIdentity(date);
        return new JournalEntry(id, date, description, details);
    }

    @Override
    public JournalEntry createReversalEntry(JournalEntry original) {
        JournalEntryId id = repository.nextIdentity(original.getJournalDate());

        List<JournalDetail> reversedDetails = original.getDetails().stream()
                .map(this::reverseDetail)
                .collect(Collectors.toList());

        return new JournalEntry(id, original.getJournalDate(),
                "【取消】" + original.getDescription(), reversedDetails)
                .withReversalEntry(true)
                .withReversalJournalId(original.getJournalId());
    }

    private JournalDetail reverseDetail(JournalDetail original) {
        List<JournalItem> reversedItems = original.getItems().stream()
                .map(item -> new JournalItem(
                        item.getDebitCreditType().opposite(),
                        item.getAccountCode(),
                        item.getAmount()))
                .collect(Collectors.toList());

        return new JournalDetail(original.getLineNo(), original.getDescription(), reversedItems);
    }
}
```

---

## 9. アプリケーションサービス

### 9.1 JournalEntryService

```plantuml
@startuml
class JournalEntryService <<Application Service>> {
  - journalEntryRepository: JournalEntryRepository
  - journalEntryFactory: JournalEntryFactory
  - journalValidator: JournalValidator
  - dailyBalanceRepository: DailyBalanceRepository
  --
  + createJournalEntry(command: CreateJournalEntryCommand): JournalEntry
  + getJournalEntry(id: JournalEntryId): JournalEntry
  + getJournalEntries(startDate: LocalDate, endDate: LocalDate): List<JournalEntry>
  + reverseJournalEntry(id: JournalEntryId): JournalEntry
}

note right of JournalEntryService
  **責務**
  - トランザクション管理
  - ドメインロジックの調整
  - 残高更新の連携

  **@Transactional**
  仕訳登録と残高更新を
  同一トランザクションで実行
end note
@enduml
```

**実装例**:

```java
@Service
@RequiredArgsConstructor
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryFactory journalEntryFactory;
    private final JournalValidator journalValidator;
    private final DailyBalanceRepository dailyBalanceRepository;

    public JournalEntry createJournalEntry(CreateJournalEntryCommand command) {
        // 1. ファクトリで仕訳を生成
        JournalEntry entry = journalEntryFactory.create(
                command.getJournalDate(),
                command.getDescription(),
                command.toDetails());

        // 2. ドメインサービスで検証
        journalValidator.validate(entry);

        // 3. 仕訳を保存
        JournalEntry saved = journalEntryRepository.save(entry);

        // 4. 日次残高を更新
        updateDailyBalances(saved);

        return saved;
    }

    private void updateDailyBalances(JournalEntry entry) {
        for (JournalDetail detail : entry.getDetails()) {
            for (JournalItem item : detail.getItems()) {
                Money debitAmount = item.isDebit() ? item.getAmount() : Money.ZERO;
                Money creditAmount = item.isCredit() ? item.getAmount() : Money.ZERO;

                DailyBalance balance = DailyBalance.of(
                        entry.getJournalDate(),
                        item.getAccountCode(),
                        debitAmount,
                        creditAmount);

                dailyBalanceRepository.upsert(balance);
            }
        }
    }

    @Transactional(readOnly = true)
    public JournalEntry getJournalEntry(JournalEntryId id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new JournalNotFoundException("仕訳が見つかりません: " + id));
    }
}
```

---

## 10. ドメインモデル・データモデル対応表

### 10.1 勘定科目

| ドメインモデル | データモデル（テーブル） | 備考 |
|---------------|------------------------|------|
| Account | 勘定科目マスタ | 集約ルート |
| AccountCode | 勘定科目コード | 値オブジェクト |
| AccountStructure | 勘定科目構成マスタ | チルダ連結方式 |

### 10.2 仕訳

| ドメインモデル | データモデル（テーブル） | 備考 |
|---------------|------------------------|------|
| JournalEntry | 仕訳 | 集約ルート、ヘッダー |
| JournalDetail | 仕訳明細 | 行単位 |
| JournalItem | 仕訳貸借明細 | 借方・貸方明細 |
| JournalEntryId | 仕訳伝票番号 | 値オブジェクト |
| Money | 仕訳金額 | 値オブジェクト |

### 10.3 残高

| ドメインモデル | データモデル（テーブル） | 備考 |
|---------------|------------------------|------|
| DailyBalance | 日次勘定科目残高 | 集約ルート |
| MonthlyBalance | 月次勘定科目残高 | 集約ルート |

---

## 11. トレーサビリティ

### 11.1 ユースケースとの対応

| ユースケース | ドメインモデル | アプリケーションサービス |
|-------------|---------------|----------------------|
| UC001 勘定科目を登録する | Account, AccountCode | AccountService |
| UC002 勘定科目階層を設定する | AccountStructure | AccountService |
| UC005 仕訳を入力する | JournalEntry, JournalDetail, JournalItem | JournalEntryService |
| UC006 複合仕訳を入力する | JournalEntry（複数Detail） | JournalEntryService |
| UC010 総勘定元帳を照会する | DailyBalance | LedgerService |
| UC011 試算表を照会する | TrialBalance | FinancialStatementService |

### 11.2 ビジネスユースケースとの対応

| BUC | 関連ドメインモデル |
|-----|------------------|
| BUC01 日次経理業務 | JournalEntry, DailyBalance |
| BUC02 月次決算 | MonthlyBalance, TrialBalance |
| BUC03 財務状況把握 | BalanceSheet, IncomeStatement |
| BUC04 マスタ管理 | Account, AccountStructure |

---

## 12. 参考資料

- [第4章: データモデル設計の基礎](../article/backend/chapter04.md)
- [第5章: マスタデータモデル](../article/backend/chapter05.md)
- [第6章: トランザクションデータモデル](../article/backend/chapter06.md)
- [第7章: ドメインモデルとデータモデルの対応](../article/backend/chapter07.md)
- [ドメインモデル設計ガイド](../reference/ドメインモデル設計ガイド.md)
- [バックエンドアーキテクチャ設計書](architecture_backend.md)
- [データモデル設計書](data-model.md)
