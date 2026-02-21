/**
 * 自動仕訳生成 E2E テスト
 *
 * US-JNL-006: 自動仕訳生成
 *
 * 受入条件:
 * - 自動仕訳設定を選択して仕訳を生成できる
 * - 生成された仕訳は編集可能
 * - 金額は手動で変更できる
 * - 生成成功時、確認メッセージが表示される
 */
describe('US-JNL-006: 自動仕訳生成', () => {
  before(() => {
    cy.clearAuth();
    cy.setupTestAccounts();
    cy.clearAuth();
  });

  beforeEach(() => {
    cy.clearAuth();
  });

  /**
   * 管理者でログインして仕訳入力ページにアクセスするヘルパー
   */
  const loginAndVisitNewJournalEntry = () => {
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit('/journal/entries/new');
    cy.get('[data-testid="journal-entry-form"]').should('be.visible');
  };

  /**
   * 自動仕訳ダイアログを開いてパターン読み込みを待つヘルパー
   */
  const openAutoJournalDialog = () => {
    cy.get('[data-testid="auto-journal-button"]').click();
    cy.get('.auto-journal-dialog-overlay').should('be.visible');
    cy.get('#auto-journal-pattern').should('be.visible');
    cy.get('#auto-journal-pattern option').should('have.length.greaterThan', 1);
  };

  /** ダイアログ内の生成ボタンをクリック */
  const clickGenerateButton = () => {
    cy.get('.auto-journal-dialog').within(() => {
      cy.contains('button', '生成').click();
    });
  };

  /** パターンを選択して金額・日付・摘要を入力し生成する */
  const selectPatternAndGenerate = (
    patternId: string,
    amount: string,
    date: string,
    description?: string,
  ) => {
    cy.get('#auto-journal-pattern').select(patternId);
    cy.get('#amount-amount').clear().type(amount);
    cy.get('#auto-journal-date').clear().type(date);
    if (description) {
      cy.get('#auto-journal-description').clear().type(description);
    }
    clickGenerateButton();
  };

  /** 生成後のダイアログ閉じ + ダッシュボード遷移を確認 */
  const assertDialogClosedAndDashboard = () => {
    cy.get('.auto-journal-dialog-overlay').should('not.exist');
    cy.get('[data-testid="dashboard"]').should('be.visible');
  };

  describe('アクセス制御', () => {
    const loginAndVisitForm = (role: string) => {
      cy.login(role, 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    };

    it('一般ユーザーには自動仕訳ボタンが表示されない', () => {
      loginAndVisitForm('user');
      cy.get('[data-testid="auto-journal-button"]').should('not.exist');
    });

    it('管理者には自動仕訳ボタンが表示される', () => {
      loginAndVisitForm('admin');
      cy.get('[data-testid="auto-journal-button"]').should('be.visible');
    });
  });

  describe('自動仕訳ダイアログ', () => {
    beforeEach(() => {
      loginAndVisitNewJournalEntry();
    });

    it('自動仕訳ボタンをクリックするとダイアログが開く', () => {
      // When: 自動仕訳ボタンをクリック
      cy.get('[data-testid="auto-journal-button"]').click();

      // Then: ダイアログが表示される
      cy.get('.auto-journal-dialog-overlay').should('be.visible');
      cy.contains('自動仕訳生成').should('be.visible');
      cy.get('#auto-journal-pattern').should('be.visible');
    });

    it('キャンセルボタンでダイアログを閉じることができる', () => {
      // Given: ダイアログを開く
      openAutoJournalDialog();

      // When: キャンセルボタンをクリック
      cy.get('.auto-journal-dialog').within(() => {
        cy.contains('button', 'キャンセル').click();
      });

      // Then: ダイアログが閉じる
      cy.get('.auto-journal-dialog-overlay').should('not.exist');
    });

    it('パターンが未選択の場合は生成できない', () => {
      openAutoJournalDialog();
      clickGenerateButton();
      cy.contains('自動仕訳パターンを選択してください').should('be.visible');
    });
  });

  describe('受入条件: 自動仕訳設定を選択して仕訳を生成できる', () => {
    beforeEach(() => {
      loginAndVisitNewJournalEntry();
    });

    it('パターンを選択すると明細行が表示される', () => {
      // Given: ダイアログを開く
      openAutoJournalDialog();

      // When: 売上仕訳パターンを選択
      cy.get('#auto-journal-pattern').select('1');

      // Then: ダイアログ内にパターンの明細行が表示される
      cy.get('.auto-journal-dialog').within(() => {
        cy.contains('明細行').should('be.visible');
        cy.get('.journal-entry-form__table').should('be.visible');
        cy.get('.journal-entry-form__table tbody tr').should('have.length', 2);
        cy.get('.journal-entry-form__table tbody tr').eq(0).within(() => {
          cy.contains('1100').should('be.visible');
          cy.contains('借方').should('be.visible');
          cy.contains('amount').should('be.visible');
        });
        cy.get('.journal-entry-form__table tbody tr').eq(1).within(() => {
          cy.contains('4100').should('be.visible');
          cy.contains('貸方').should('be.visible');
          cy.contains('amount').should('be.visible');
        });
      });
    });

    it('パターンを選択して金額を入力し仕訳を生成できる', () => {
      openAutoJournalDialog();
      selectPatternAndGenerate('1', '50000', '2026-01-15', 'テスト売上計上');
      assertDialogClosedAndDashboard();
    });

    it('別のパターン（仕入）を選択して仕訳を生成できる', () => {
      openAutoJournalDialog();
      cy.get('#auto-journal-pattern').select('2');

      // 仕入パターンの明細行が表示される
      cy.get('.auto-journal-dialog').within(() => {
        cy.get('.journal-entry-form__table tbody tr').eq(0).should('contain', '5100').and('contain', '借方');
        cy.get('.journal-entry-form__table tbody tr').eq(1).should('contain', '2100').and('contain', '貸方');
      });

      selectPatternAndGenerate('2', '30000', '2026-01-20');
      assertDialogClosedAndDashboard();
    });
  });

  describe('受入条件: 金額は手動で変更できる', () => {
    beforeEach(() => {
      loginAndVisitNewJournalEntry();
    });

    it('金額入力フィールドはパターン選択後に表示される', () => {
      // Given: ダイアログを開く
      openAutoJournalDialog();

      // パターン未選択時は金額入力フィールドが表示されない
      cy.get('#amount-amount').should('not.exist');

      // When: パターンを選択
      cy.get('#auto-journal-pattern').select('1');

      // Then: 金額入力フィールドが表示される
      cy.get('#amount-amount').should('be.visible');
    });

    it('金額を自由に入力・変更できる', () => {
      // Given: ダイアログを開いてパターンを選択
      openAutoJournalDialog();
      cy.get('#auto-journal-pattern').select('1');

      // When: 金額を入力
      cy.get('#amount-amount').clear().type('100000');
      cy.get('#amount-amount').should('have.value', '100000');

      // Then: 金額を変更できる
      cy.get('#amount-amount').clear().type('250000');
      cy.get('#amount-amount').should('have.value', '250000');
    });

    it('金額未入力の場合はエラーメッセージが表示される', () => {
      openAutoJournalDialog();
      cy.get('#auto-journal-pattern').select('1');
      cy.get('#amount-amount').clear();
      clickGenerateButton();
      cy.contains('amount の金額を入力してください').should('be.visible');
    });
  });

  describe('受入条件: 生成成功時、確認メッセージが表示される', () => {
    beforeEach(() => {
      loginAndVisitNewJournalEntry();
    });

    it('仕訳生成成功後にダッシュボードに遷移し成功が確認できる', () => {
      openAutoJournalDialog();
      selectPatternAndGenerate('1', '80000', '2026-02-01', '確認メッセージテスト');
      cy.get('.auto-journal-dialog-overlay').should('not.exist');
      cy.url().should('match', /\/$/);
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });
  });

  describe('受入条件: 生成された仕訳は編集可能', () => {
    it('生成された仕訳が仕訳一覧に下書きステータスで表示される', () => {
      loginAndVisitNewJournalEntry();
      openAutoJournalDialog();
      selectPatternAndGenerate('1', '120000', '2026-02-10', '編集可能テスト');
      assertDialogClosedAndDashboard();

      // When: サイドバーから仕訳一覧に遷移（クライアントサイドナビゲーションで MSW 状態を保持）
      cy.contains('.sidebar__link-text', '仕訳').click();
      cy.contains('.sidebar__sublink', '仕訳一覧').click();
      cy.get('[data-testid="journal-entry-list"]').should('be.visible');

      // Then: 生成された仕訳が一覧に表示される（下書きステータス）
      cy.contains('td', '編集可能テスト').should('be.visible');
      cy.contains('td', '編集可能テスト')
        .closest('tr')
        .within(() => {
          cy.contains('下書き').should('be.visible');
        });
    });
  });
});
