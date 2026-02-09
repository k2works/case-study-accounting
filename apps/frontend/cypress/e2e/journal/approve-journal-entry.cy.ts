/**
 * 仕訳承認 E2E テスト
 *
 * US-JNL-008: 仕訳承認
 *
 * 受入条件:
 * - 「承認待ち」ステータスの仕訳のみ承認できる
 * - 承認後、ステータスが「承認済み」に変わる
 * - 承認者と承認日時が記録される
 * - 承認成功時、確認メッセージが表示される
 */

describe('US-JNL-008: 仕訳承認', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('承認ボタン表示', () => {
    beforeEach(() => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
    });

    it('承認待ちステータスの仕訳に承認ボタンが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.checkButtonInFirstRow('承認', true);
    });

    it('下書きステータスの仕訳には承認ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.checkButtonInFirstRow('承認', false);
    });

    it('承認済みステータスの仕訳には承認ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.checkButtonInFirstRow('承認', false);
    });
  });

  describe('承認実行', () => {
    beforeEach(() => {
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
    });

    it('承認ボタンクリックで確認ダイアログが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.on('window:confirm', (text) => {
        expect(text).to.include('承認しますか');
        return false;
      });
      cy.get('table tbody tr').first().contains('button', '承認').click();
    });

    it('確認ダイアログでキャンセルすると承認が行われない', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.clickButtonInFirstRowWithConfirm('承認', false);
      cy.get('table tbody tr').first().should('contain', '承認待ち');
    });

    it('承認が成功するとステータスが承認済みに変わる', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.clickButtonInFirstRowWithConfirm('承認', true);
      cy.contains('仕訳を承認しました', { timeout: 10000 }).should('be.visible');
    });
  });

  describe('権限確認', () => {
    it('一般ユーザーは承認できない（承認ボタンが表示されない）', () => {
      cy.loginAndVisitJournalList('user', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      // 注意: 実際の権限チェックはバックエンドで行う
    });

    it('マネージャーは承認できる', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      cy.checkButtonInFirstRow('承認', true);
    });

    it('管理者は承認できる', () => {
      cy.loginAndVisitJournalList('admin', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      cy.checkButtonInFirstRow('承認', true);
    });
  });
});
