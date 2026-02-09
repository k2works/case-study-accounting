// カスタムコマンドの型定義
declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * ログインコマンド
       * @param username - ユーザー名
       * @param password - パスワード
       */
      login(username: string, password: string): Chainable<void>;

      /**
       * ログアウトコマンド
       */
      logout(): Chainable<void>;

      /**
       * ローカルストレージをクリアしてログアウト状態にする
       */
      clearAuth(): Chainable<void>;

      /**
       * 勘定科目を作成するコマンド
       * @param code - 科目コード
       * @param name - 科目名
       * @param type - 勘定科目種別 (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)
       */
      createAccount(code: string, name: string, type: string): Chainable<void>;

      /**
       * テスト用の基本勘定科目をセットアップする
       */
      setupTestAccounts(): Chainable<void>;

      /**
       * テスト用の仕訳を登録する
       * @param date - 仕訳日（YYYY-MM-DD）
       * @param description - 摘要
       * @param amount - 金額（借方・貸方同額）
       */
      createTestJournalEntry(date: string, description: string, amount: string): Chainable<void>;

      /**
       * 仕訳一覧ページに遷移してデータ読み込み完了を待つ
       */
      visitJournalEntryList(): Chainable<void>;

      /**
       * 仕訳一覧ページから編集画面に遷移する
       * @param filterDescription - 摘要でフィルタして特定の仕訳を見つける（省略時は先頭行）
       */
      navigateToEditJournalEntry(filterDescription?: string): Chainable<void>;

      /**
       * 元帳系ページにログインして遷移するコマンド
       * @param username - ユーザー名
       * @param password - パスワード
       * @param path - 遷移先パス
       * @param pageTestId - ページの data-testid
       */
      visitLedgerPage(
        username: string,
        password: string,
        path: string,
        pageTestId: string
      ): Chainable<void>;

      /**
       * 勘定科目ドロップダウンから選択するコマンド
       * @param selectId - select要素のID
       * @param index - 選択するオプションのインデックス（デフォルト: 1）
       */
      selectAccountOption(selectId: string, index?: number): Chainable<void>;

      /**
       * 元帳系ページでの勘定科目選択と結果待機
       * @param selectId - select要素のID
       * @param tableTestId - 結果テーブルのdata-testid
       */
      selectAccountAndWaitForTable(selectId: string, tableTestId: string): Chainable<void>;

      /**
       * 仕訳一覧をステータスでフィルタリング
       * @param status - ステータス値 (DRAFT, PENDING, APPROVED)
       */
      filterJournalEntriesByStatus(status: 'DRAFT' | 'PENDING' | 'APPROVED'): Chainable<void>;

      /**
       * テーブルの先頭行でボタンの存在を確認
       * @param buttonText - ボタンのテキスト
       * @param shouldExist - 存在すべきか
       */
      checkButtonInFirstRow(buttonText: string, shouldExist: boolean): Chainable<void>;

      /**
       * テーブルの先頭行でボタンをクリック（確認ダイアログ付き）
       * @param buttonText - ボタンのテキスト
       * @param confirmAction - true=OK, false=キャンセル
       */
      clickButtonInFirstRowWithConfirm(buttonText: string, confirmAction: boolean): Chainable<void>;

      /**
       * 承認ワークフロー用のログインと仕訳一覧遷移
       * @param username - ユーザー名
       * @param password - パスワード
       */
      loginAndVisitJournalList(username: string, password: string): Chainable<void>;
    }
  }
}

/**
 * ログインコマンド
 * ログインフォームを使用して認証を行う
 */
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.visit('/login');

  // フォームが表示されるまで待機
  cy.get('[data-testid="login-form"]').should('be.visible');

  // 入力フィールドをクリアしてから入力
  cy.get('[data-testid="username-input"]').clear().type(username);
  cy.get('[data-testid="password-input"]').clear().type(password);

  // ログインボタンをクリック
  cy.get('[data-testid="login-submit"]').click();
});

/**
 * ログアウトコマンド
 * ヘッダーのログアウトボタンをクリックしてログアウトする
 */
Cypress.Commands.add('logout', () => {
  cy.get('[data-testid="logout-button"]').click();
});

/**
 * 認証情報クリアコマンド
 * ローカルストレージから認証情報を削除する
 */
Cypress.Commands.add('clearAuth', () => {
  cy.clearLocalStorage('accessToken');
  cy.clearLocalStorage('refreshToken');
  cy.clearLocalStorage('user');
});

/**
 * 勘定科目作成コマンド
 * 管理者権限で勘定科目登録ページから勘定科目を作成する
 */
Cypress.Commands.add('createAccount', (code: string, name: string, type: string) => {
  cy.visit('/master/accounts/new');
  cy.get('[data-testid="create-account-form"]').should('be.visible');

  cy.get('[data-testid="create-account-code-input"]').type(code);
  cy.get('[data-testid="create-account-name-input"]').type(name);
  cy.get('[data-testid="create-account-type-select"]').select(type);
  cy.get('[data-testid="create-account-submit"]').click();

  // 成功メッセージまたはエラーメッセージを待機（最大3秒）
  // 既存の科目の場合はエラーが表示されるが、テストは継続
  cy.wait(1000);
});

/**
 * テスト用の基本勘定科目をセットアップする
 * 借方・貸方それぞれで使える勘定科目を作成
 */
Cypress.Commands.add('setupTestAccounts', () => {
  // 管理者でログイン
  cy.login('admin', 'Password123!');
  cy.get('[data-testid="dashboard"]').should('be.visible');

  // 資産科目（借方で使用）
  cy.createAccount('1001', '現金', 'ASSET');

  // 収益科目（貸方で使用）
  cy.createAccount('4001', '売上高', 'REVENUE');

  // 費用科目
  cy.createAccount('5001', '仕入高', 'EXPENSE');

  // 負債科目
  cy.createAccount('2001', '買掛金', 'LIABILITY');
});

/**
 * テスト用仕訳登録コマンド
 * 勘定科目リストから動的に選択して仕訳を作成する
 */
Cypress.Commands.add('createTestJournalEntry', (date: string, description: string, amount: string) => {
  cy.visit('/journal/entries/new');
  cy.get('[data-testid="journal-entry-form"]').should('be.visible');

  // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
  cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 }).should(
    'have.length.greaterThan',
    1
  );

  cy.get('[data-testid="journal-entry-date-input"]').type(date);
  cy.get('[data-testid="journal-entry-description-input"]').type(description);

  // 最初の有効な勘定科目を選択（index 1 = 最初の option 以外）
  cy.get('[data-testid="journal-entry-account-0"]').select(1);
  cy.get('[data-testid="journal-entry-debit-0"]').type(amount);

  // 行を追加して 2 番目の勘定科目を選択
  cy.get('[data-testid="journal-entry-add-line"]').click();
  cy.get('[data-testid="journal-entry-account-1"]').select(2);
  cy.get('[data-testid="journal-entry-credit-1"]').type(amount);

  cy.get('[data-testid="journal-entry-submit"]').click();
});

/**
 * 仕訳一覧ページ遷移コマンド
 * 一覧ページに遷移しデータ読み込み完了を待機する
 */
Cypress.Commands.add('visitJournalEntryList', () => {
  cy.visit('/journal/entries');
  cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
  cy.get('table tbody', { timeout: 15000 }).should('exist');
});

/**
 * 仕訳編集画面遷移コマンド
 * 仕訳一覧ページから編集画面に遷移する
 */
Cypress.Commands.add('navigateToEditJournalEntry', (filterDescription?: string) => {
  cy.visit('/journal/entries');
  cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
  cy.get('table tbody tr', { timeout: 15000 }).should('have.length.at.least', 1);

  if (filterDescription) {
    cy.get('#journal-entry-filter-description').type(filterDescription);
    cy.contains('button', '検索').click();
    cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);
  }

  cy.get('table tbody tr').first().contains('button', '編集').click();
  cy.get('[data-testid="journal-entry-edit-form"]', { timeout: 15000 }).should('be.visible');
});

/**
 * 元帳系ページにログインして遷移するコマンド
 * ログイン → ダッシュボード待機 → ページ遷移 → ページ表示待機
 */
Cypress.Commands.add(
  'visitLedgerPage',
  (username: string, password: string, path: string, pageTestId: string) => {
    cy.login(username, password);
    cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
    cy.visit(path);
    cy.get(`[data-testid="${pageTestId}"]`, { timeout: 15000 }).should('be.visible');
  }
);

/**
 * 勘定科目ドロップダウンから選択するコマンド
 * オプションが読み込まれるのを待ってから選択
 */
Cypress.Commands.add('selectAccountOption', (selectId: string, index: number = 1) => {
  cy.get(`#${selectId} option`, { timeout: 15000 }).should('have.length.greaterThan', 1);
  cy.get(`#${selectId}`).select(index);
});

/**
 * 元帳系ページでの勘定科目選択と結果待機
 * 勘定科目選択後、テーブル表示を待機
 */
Cypress.Commands.add('selectAccountAndWaitForTable', (selectId: string, tableTestId: string) => {
  cy.selectAccountOption(selectId);
  cy.wait(1000);
  cy.get(`[data-testid="${tableTestId}"]`, { timeout: 15000 }).should('be.visible');
});

/**
 * 仕訳一覧をステータスでフィルタリング
 */
Cypress.Commands.add('filterJournalEntriesByStatus', (status: 'DRAFT' | 'PENDING' | 'APPROVED') => {
  cy.get('#journal-entry-filter-status').select(status);
  cy.contains('button', '検索').click();
  cy.get('table tbody tr', { timeout: 10000 }).should('exist');
});

/**
 * テーブルの先頭行でボタンの存在を確認
 */
Cypress.Commands.add('checkButtonInFirstRow', (buttonText: string, shouldExist: boolean) => {
  if (shouldExist) {
    cy.get('table tbody tr').first().contains('button', buttonText).should('be.visible');
  } else {
    // 完全一致で検索（「承認」と「承認申請」を区別）
    cy.get('table tbody tr')
      .first()
      .find('button')
      .contains(new RegExp(`^${buttonText}$`))
      .should('not.exist');
  }
});

/**
 * テーブルの先頭行でボタンをクリック（確認ダイアログ付き）
 */
Cypress.Commands.add(
  'clickButtonInFirstRowWithConfirm',
  (buttonText: string, confirmAction: boolean) => {
    cy.on('window:confirm', () => confirmAction);
    cy.get('table tbody tr').first().contains('button', buttonText).click();
  }
);

/**
 * 承認ワークフロー用のログインと仕訳一覧遷移
 */
Cypress.Commands.add('loginAndVisitJournalList', (username: string, password: string) => {
  cy.login(username, password);
  cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
  cy.visitJournalEntryList();
});

export {};
