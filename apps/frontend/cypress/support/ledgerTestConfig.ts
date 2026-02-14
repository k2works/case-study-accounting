export const LEDGER_CREDENTIALS = {
  admin: { username: 'admin', password: 'Password123!' },
  user: { username: 'user', password: 'Password123!' },
  manager: { username: 'manager', password: 'Password123!' },
} as const;

export type LedgerRole = 'admin' | 'user' | 'manager';

export interface LedgerPageConfig {
  page: { path: string; testId: string };
  selectors: { filter: string; [key: string]: string };
}

export const createVisitFunction =
  (config: LedgerPageConfig) =>
  (role: LedgerRole = 'admin') => {
    const { username, password } = LEDGER_CREDENTIALS[role];
    cy.visitLedgerPage(username, password, config.page.path, config.page.testId);
  };

export const describeAccessControl = (
  config: LedgerPageConfig,
  visitFn: (role: LedgerRole) => void,
  pageName: string
) => {
  describe('アクセス制御', () => {
    it(`一般ユーザーも${pageName}にアクセスできる`, () => {
      visitFn('user');
      cy.get(config.selectors.filter).should('be.visible');
    });

    it(`未認証ユーザーは${pageName}にアクセスできない`, () => {
      cy.visit(config.page.path);
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });

    it(`経理責任者も${pageName}にアクセスできる`, () => {
      visitFn('manager');
      cy.get(`[data-testid="${config.page.testId}"]`).should('be.visible');
    });
  });
};

export const describeErrorHandling = (
  visitFn: () => void,
  buttonText = '照会'
) => {
  describe('エラーハンドリング', () => {
    beforeEach(() => {
      visitFn();
    });

    it('勘定科目を選択せずに照会するとエラーメッセージが表示される', () => {
      cy.contains('button', buttonText).click();
      cy.contains('勘定科目を選択してください').should('be.visible');
    });
  });
};

/** 勘定科目ドロップダウンに選択肢があることを検証 */
export const itShouldShowDropdownOptions = (accountSelectTestId: string) => {
  it('勘定科目ドロップダウンに選択肢が表示される', () => {
    cy.get(`#${accountSelectTestId} option`, { timeout: 15000 }).should(
      'have.length.greaterThan',
      1
    );
    cy.get(`#${accountSelectTestId}`)
      .contains('option', '勘定科目を選択')
      .should('exist');
  });
};

/** サマリ情報のラベル群が表示されることを検証 */
export const itShouldShowSummaryLabels = (
  config: LedgerPageConfig,
  labels: string[]
) => {
  it('サマリ情報が表示される', () => {
    cy.selectAccountOption(config.selectors.accountSelect);
    cy.get(config.selectors.summary, { timeout: 15000 }).should('be.visible');
    labels.forEach((label) => {
      cy.get(config.selectors.summary).should('contain', label);
    });
  });
};

/** Recharts チャートの表示を検証する describe ブロック */
export const describeChartDisplay = (
  config: LedgerPageConfig,
  chartName: string
) => {
  describe(`${chartName}をグラフで表示できる`, () => {
    it('勘定科目を選択すると推移グラフが表示される', () => {
      cy.selectAccountOption(config.selectors.accountSelect);
      cy.get(config.selectors.chart!, { timeout: 15000 }).should('be.visible');
    });

    it('グラフには Recharts の LineChart が含まれる', () => {
      cy.selectAccountOption(config.selectors.accountSelect);
      cy.get(config.selectors.chart!, { timeout: 15000 }).should('be.visible');
      cy.get(`${config.selectors.chart!} svg`).should('exist');
    });
  });
};

/** テーブルヘッダーが表示されることを検証 */
export const itShouldShowTableHeaders = (
  config: LedgerPageConfig,
  headers: string[]
) => {
  it('テーブルヘッダーが表示される', () => {
    cy.selectAccountOption(config.selectors.accountSelect);
    cy.get(`[data-testid="${config.selectors.table}"]`, { timeout: 15000 }).should(
      'be.visible'
    );
    headers.forEach((header) => {
      cy.contains('th', header).should('be.visible');
    });
  });
};
