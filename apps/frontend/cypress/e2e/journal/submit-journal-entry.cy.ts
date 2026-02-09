/**
 * 仕訳承認申請 E2E テスト
 *
 * US-JNL-007: 仕訳承認申請
 *
 * 受入条件:
 * - 「下書き」ステータスの仕訳のみ承認申請できる
 * - 承認申請後、ステータスが「承認待ち」に変わる
 * - 承認申請成功時、確認メッセージが表示される
 */

describe('US-JNL-007: 仕訳承認申請', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('承認申請ボタン表示', () => {
    beforeEach(() => {
      cy.loginAndVisitJournalList('admin', 'Password123!');
    });

    it('下書きステータスの仕訳に承認申請ボタンが表示される', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.checkButtonInFirstRow('承認申請', true);
    });

    it('承認待ちステータスの仕訳には承認申請ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.checkButtonInFirstRow('承認申請', false);
    });

    it('承認済みステータスの仕訳には承認申請ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.checkButtonInFirstRow('承認申請', false);
    });
  });

  describe('承認申請実行', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.createTestJournalEntry('2024-07-01', '承認申請テスト仕訳', '12000');
      cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('承認申請ボタンクリックで確認ダイアログが表示される', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.on('window:confirm', (text) => {
        expect(text).to.include('承認申請しますか');
        return false;
      });
      cy.get('table tbody tr').first().contains('button', '承認申請').click();
    });

    it('確認ダイアログでキャンセルすると承認申請が行われない', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.clickButtonInFirstRowWithConfirm('承認申請', false);
      cy.get('table tbody tr').first().should('contain', '下書き');
    });

    it('承認申請が成功するとステータスが承認待ちに変わる', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.clickButtonInFirstRowWithConfirm('承認申請', true);
      cy.contains('仕訳を承認申請しました', { timeout: 10000 }).should('be.visible');
    });
  });

  describe('権限確認', () => {
    it('一般ユーザーでも承認申請ができる', () => {
      cy.loginAndVisitJournalList('user', 'Password123!');
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.checkButtonInFirstRow('承認申請', true);
    });
  });
});
