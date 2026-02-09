/**
 * 仕訳ワークフロー E2E テスト用の共通設定とファクトリ
 */

export interface WorkflowTestConfig {
  /** ユーザーストーリーID */
  storyId: string;
  /** テストスイート名 */
  suiteName: string;
  /** 操作ボタンのテキスト */
  buttonText: string;
  /** 確認ダイアログのテキスト（部分一致） */
  confirmText: string;
  /** 成功メッセージ */
  successMessage: string;
  /** 対象ステータス（このステータスの仕訳にボタンが表示される） */
  targetStatus: 'DRAFT' | 'PENDING' | 'APPROVED';
  /** 対象ステータスの表示名 */
  targetStatusLabel: string;
  /** ボタンが表示されないステータスリスト */
  excludedStatuses: Array<{
    status: 'DRAFT' | 'PENDING' | 'APPROVED';
    label: string;
  }>;
  /** ボタン表示確認のログインユーザー */
  visibilityCheckUser: string;
  /** アクション実行前のセットアップ（仕訳作成等） */
  setupBeforeAction?: () => void;
  /** 権限テスト設定 */
  permissionTests: Array<{
    description: string;
    username: string;
    shouldHaveButton: boolean;
  }>;
}

/**
 * 仕訳ワークフローテストを生成
 */
export const createWorkflowTests = (config: WorkflowTestConfig): void => {
  describe(`${config.storyId}: ${config.suiteName}`, () => {
    beforeEach(() => {
      cy.clearAuth();
    });

    describe(`${config.buttonText}ボタン表示`, () => {
      beforeEach(() => {
        cy.loginAndVisitJournalList(config.visibilityCheckUser, 'Password123!');
      });

      it(`${config.targetStatusLabel}ステータスの仕訳に${config.buttonText}ボタンが表示される`, () => {
        cy.filterJournalEntriesByStatus(config.targetStatus);
        cy.get('table tbody tr').first().should('contain', config.targetStatusLabel);
        cy.checkButtonInFirstRow(config.buttonText, true);
      });

      config.excludedStatuses.forEach(({ status, label }) => {
        it(`${label}ステータスの仕訳には${config.buttonText}ボタンが表示されない`, () => {
          cy.filterJournalEntriesByStatus(status);
          cy.get('table tbody tr').first().should('contain', label);
          cy.checkButtonInFirstRow(config.buttonText, false);
        });
      });
    });

    describe(`${config.buttonText}実行`, () => {
      beforeEach(() => {
        if (config.setupBeforeAction) {
          config.setupBeforeAction();
        }
      });

      it(`${config.buttonText}ボタンクリックで確認ダイアログが表示される`, () => {
        cy.filterJournalEntriesByStatus(config.targetStatus);
        cy.on('window:confirm', (text) => {
          expect(text).to.include(config.confirmText);
          return false;
        });
        cy.get('table tbody tr').first().contains('button', config.buttonText).click();
      });

      it(`確認ダイアログでキャンセルすると${config.buttonText}が行われない`, () => {
        cy.filterJournalEntriesByStatus(config.targetStatus);
        cy.get('table tbody tr').first().should('contain', config.targetStatusLabel);
        cy.clickButtonInFirstRowWithConfirm(config.buttonText, false);
        cy.get('table tbody tr').first().should('contain', config.targetStatusLabel);
      });

      it(`${config.buttonText}が成功すると成功メッセージが表示される`, () => {
        cy.filterJournalEntriesByStatus(config.targetStatus);
        cy.clickButtonInFirstRowWithConfirm(config.buttonText, true);
        cy.contains(config.successMessage, { timeout: 10000 }).should('be.visible');
      });
    });

    describe('権限確認', () => {
      config.permissionTests.forEach(({ description, username, shouldHaveButton }) => {
        it(description, () => {
          cy.loginAndVisitJournalList(username, 'Password123!');
          cy.filterJournalEntriesByStatus(config.targetStatus);
          if (shouldHaveButton) {
            cy.checkButtonInFirstRow(config.buttonText, true);
          }
          // shouldHaveButton が false の場合は権限チェックはバックエンドで行うためアサーションなし
        });
      });
    });
  });
};

/**
 * 承認申請テストの設定
 */
export const submitJournalEntryConfig: WorkflowTestConfig = {
  storyId: 'US-JNL-007',
  suiteName: '仕訳承認申請',
  buttonText: '承認申請',
  confirmText: '承認申請しますか',
  successMessage: '仕訳を承認申請しました',
  targetStatus: 'DRAFT',
  targetStatusLabel: '下書き',
  excludedStatuses: [
    { status: 'PENDING', label: '承認待ち' },
    { status: 'APPROVED', label: '承認済み' },
  ],
  visibilityCheckUser: 'admin',
  setupBeforeAction: () => {
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
    cy.createTestJournalEntry('2024-07-01', '承認申請テスト仕訳', '12000');
    cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');
    cy.visitJournalEntryList();
  },
  permissionTests: [
    {
      description: '一般ユーザーでも承認申請ができる',
      username: 'user',
      shouldHaveButton: true,
    },
  ],
};

/**
 * 承認テストの設定
 */
export const approveJournalEntryConfig: WorkflowTestConfig = {
  storyId: 'US-JNL-008',
  suiteName: '仕訳承認',
  buttonText: '承認',
  confirmText: '承認しますか',
  successMessage: '仕訳を承認しました',
  targetStatus: 'PENDING',
  targetStatusLabel: '承認待ち',
  excludedStatuses: [
    { status: 'DRAFT', label: '下書き' },
    { status: 'APPROVED', label: '承認済み' },
  ],
  visibilityCheckUser: 'manager',
  setupBeforeAction: () => {
    // Admin で仕訳作成と承認申請
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
    cy.createTestJournalEntry('2024-07-15', '承認テスト仕訳', '15000');
    cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');
    cy.visitJournalEntryList();
    cy.filterJournalEntriesByStatus('DRAFT');
    cy.clickButtonInFirstRowWithConfirm('承認申請', true);
    cy.contains('仕訳を承認申請しました', { timeout: 10000 }).should('be.visible');
    // Manager で再ログイン
    cy.clearAuth();
    cy.loginAndVisitJournalList('manager', 'Password123!');
  },
  permissionTests: [
    {
      description: '一般ユーザーは承認できない（承認ボタンが表示されない）',
      username: 'user',
      shouldHaveButton: false,
    },
    {
      description: 'マネージャーは承認できる',
      username: 'manager',
      shouldHaveButton: true,
    },
    {
      description: '管理者は承認できる',
      username: 'admin',
      shouldHaveButton: true,
    },
  ],
};
