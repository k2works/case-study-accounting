/**
 * 仕訳確定 E2E テスト（MSW モック環境）
 *
 * US-JNL-010: 仕訳確定
 *
 * 受入条件:
 * 1. APPROVED ステータスの仕訳のみ確定可能
 * 2. 確定後、ステータスが CONFIRMED に変わる
 * 3. 確定済み仕訳は編集・削除不可
 * 4. 確定成功時、確認メッセージが表示される
 */

describe('US-JNL-010: 仕訳確定', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('受入条件1: APPROVED ステータスの仕訳のみ確定可能', () => {
    beforeEach(() => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
    });

    it('承認済みステータスの仕訳に確定ボタンが表示される', () => {
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.checkButtonInFirstRow('確定', true);
    });

    it('下書きステータスの仕訳には確定ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.checkButtonInFirstRow('確定', false);
    });

    it('承認待ちステータスの仕訳には確定ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.checkButtonInFirstRow('確定', false);
    });

    it('確定済みステータスの仕訳には確定ボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('CONFIRMED');
      cy.get('table tbody tr').first().should('contain', '確定');
      cy.checkButtonInFirstRow('確定', false);
    });
  });

  describe('受入条件2: 確定後、ステータスが CONFIRMED に変わる', () => {
    it('確定実行後にステータスが確定済みに変わる', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.clickButtonInFirstRowWithConfirm('確定', true);
      cy.contains('仕訳を確定しました', { timeout: 10000 }).should('be.visible');
      // 確定後、CONFIRMED フィルタで確認
      cy.filterJournalEntriesByStatus('CONFIRMED');
      cy.get('table tbody tr').first().should('contain', '確定');
    });
  });

  describe('受入条件3: 確定済み仕訳は編集・削除不可', () => {
    beforeEach(() => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('CONFIRMED');
    });

    it('確定済み仕訳には操作ボタンが表示されない', () => {
      cy.get('table tbody tr')
        .first()
        .then(($row) => {
          // 確定済みステータスでは編集・削除・ワークフローボタンがすべて非表示
          expect($row.find('button').length).to.equal(0);
        });
    });
  });

  describe('受入条件4: 確定成功時、確認メッセージが表示される', () => {
    it('確定ボタンクリックで確認ダイアログが表示される', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.on('window:confirm', (text) => {
        expect(text).to.include('確定しますか');
        return false;
      });
      cy.get('table tbody tr').first().contains('button', '確定').click();
    });

    it('確認ダイアログでキャンセルすると確定が行われない', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.clickButtonInFirstRowWithConfirm('確定', false);
      cy.get('table tbody tr').first().should('contain', '承認済み');
    });

    it('確定が成功すると成功メッセージが表示される', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.clickButtonInFirstRowWithConfirm('確定', true);
      cy.contains('仕訳を確定しました', { timeout: 10000 }).should('be.visible');
    });
  });

  describe('権限確認', () => {
    it('マネージャーは確定ボタンが表示される', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.checkButtonInFirstRow('確定', true);
    });

    it('管理者は確定ボタンが表示される', () => {
      cy.loginAndVisitJournalList('admin', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.checkButtonInFirstRow('確定', true);
    });

    it('一般ユーザーには確定ボタンが表示されない（フロントエンド権限チェック）', () => {
      cy.loginAndVisitJournalList('user', 'Password123!');
      cy.filterJournalEntriesByStatus('APPROVED');
      // COMMON-24: フロントエンドで canApprove = hasRole('MANAGER') により制御
      cy.checkButtonInFirstRow('確定', false);
    });
  });
});
