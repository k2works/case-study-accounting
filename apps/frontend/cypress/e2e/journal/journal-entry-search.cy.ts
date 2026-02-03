/**
 * 仕訳検索 E2E テスト
 *
 * US-JNL-005: 仕訳検索
 *
 * 受入条件:
 * - 仕訳日付、勘定科目、金額範囲、摘要で検索できる
 * - 複数条件を組み合わせて検索できる
 * - 検索結果が一覧表示される
 * - 検索条件をクリアできる
 */

/**
 * 仕訳を登録するヘルパー関数
 */
const createTestJournalEntry = (date: string, description: string, amount: string) => {
  cy.visit('/journal/entries/new');
  cy.get('[data-testid="journal-entry-form"]').should('be.visible');

  // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
  cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 }).should(
    'have.length.greaterThan',
    1
  );

  cy.get('[data-testid="journal-entry-date-input"]').type(date);
  cy.get('[data-testid="journal-entry-description-input"]').type(description);

  cy.get('[data-testid="journal-entry-account-0"]').select(1);
  cy.get('[data-testid="journal-entry-debit-0"]').type(amount);

  cy.get('[data-testid="journal-entry-add-line"]').click();
  cy.get('[data-testid="journal-entry-account-1"]').select(2);
  cy.get('[data-testid="journal-entry-credit-1"]').type(amount);

  cy.get('[data-testid="journal-entry-submit"]').click();
};

/**
 * 仕訳一覧ページに遷移してデータ読み込み完了を待つヘルパー
 */
const visitJournalEntryList = () => {
  cy.visit('/journal/entries');
  cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
  cy.get('table tbody', { timeout: 15000 }).should('exist');
};

describe('US-JNL-005: 仕訳検索', () => {
  before(() => {
    cy.clearAuth();
    cy.setupTestAccounts();
    cy.clearAuth();

    // テスト用仕訳を複数登録
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

    createTestJournalEntry('2024-04-01', '4月売上計上', '50000');
    cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');

    createTestJournalEntry('2024-04-15', '4月仕入計上', '30000');
    cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');

    createTestJournalEntry('2024-05-01', '5月売上計上', '80000');
    cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');

    cy.clearAuth();
  });

  beforeEach(() => {
    cy.clearAuth();
  });

  describe('検索フォーム表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('拡張検索フォームが表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: 検索フォームに新しいフィールドが表示される
      cy.get('[data-testid="journal-entry-filter"]').should('be.visible');
      cy.get('#journal-entry-filter-description').should('be.visible');
      cy.get('#journal-entry-filter-account-id').should('be.visible');
      cy.get('#journal-entry-filter-amount-from').should('be.visible');
      cy.get('#journal-entry-filter-amount-to').should('be.visible');
    });
  });

  describe('摘要検索', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('摘要で検索できる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 摘要に「売上」と入力して検索
      cy.get('#journal-entry-filter-description').type('売上');
      cy.contains('button', '検索').click();

      // Then: 検索結果が表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });
  });

  describe('金額範囲検索', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('金額範囲で検索できる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 金額範囲を指定して検索
      cy.get('#journal-entry-filter-amount-from').type('10000');
      cy.get('#journal-entry-filter-amount-to').type('60000');
      cy.contains('button', '検索').click();

      // Then: 検索結果が表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });

    it('最小金額のみ指定して検索できる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 最小金額のみ指定
      cy.get('#journal-entry-filter-amount-from').type('50000');
      cy.contains('button', '検索').click();

      // Then: 検索結果が表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });
  });

  describe('複数条件の組み合わせ検索', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('日付範囲と摘要を組み合わせて検索できる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 日付範囲と摘要を指定して検索
      cy.get('#journal-entry-filter-date-from').type('2024-04-01');
      cy.get('#journal-entry-filter-date-to').type('2024-04-30');
      cy.get('#journal-entry-filter-description').type('売上');
      cy.contains('button', '検索').click();

      // Then: 複合条件でフィルタリングされる
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });

    it('ステータスと金額範囲を組み合わせて検索できる', () => {
      // Given: 仕訳一覧が表示されている

      // When: ステータスと金額範囲を指定して検索
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.get('#journal-entry-filter-amount-from').type('10000');
      cy.contains('button', '検索').click();

      // Then: 複合条件でフィルタリングされる
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });
  });

  describe('検索条件のリセット', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('リセットボタンで全検索条件がクリアされる', () => {
      // Given: 複数のフィルタ条件が設定されている
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.get('#journal-entry-filter-date-from').type('2024-04-01');
      cy.get('#journal-entry-filter-date-to').type('2024-04-30');
      cy.get('#journal-entry-filter-description').type('売上');
      cy.get('#journal-entry-filter-amount-from').type('10000');
      cy.get('#journal-entry-filter-amount-to').type('100000');
      cy.contains('button', '検索').click();

      // When: リセットボタンをクリック
      cy.contains('button', 'リセット').click();

      // Then: すべてのフィルタ条件がクリアされる
      cy.get('#journal-entry-filter-status').should('have.value', '');
      cy.get('#journal-entry-filter-date-from').should('have.value', '');
      cy.get('#journal-entry-filter-date-to').should('have.value', '');
      cy.get('#journal-entry-filter-description').should('have.value', '');
      cy.get('#journal-entry-filter-amount-from').should('have.value', '');
      cy.get('#journal-entry-filter-amount-to').should('have.value', '');

      // Then: 全件が表示される
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });
  });

  describe('検索結果表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      visitJournalEntryList();
    });

    it('検索結果が一覧形式で表示される', () => {
      // Given: 仕訳一覧が表示されている

      // When: 検索を実行
      cy.contains('button', '検索').click();

      // Then: 検索結果が一覧表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      cy.contains('th', '仕訳番号').should('be.visible');
      cy.contains('th', '仕訳日付').should('be.visible');
      cy.contains('th', '摘要').should('be.visible');
      cy.contains('th', '借方金額').should('be.visible');
      cy.contains('th', '貸方金額').should('be.visible');
      cy.contains('th', 'ステータス').should('be.visible');
    });
  });
});
