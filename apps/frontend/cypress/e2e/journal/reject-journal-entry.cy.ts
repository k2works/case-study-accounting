/**
 * 仕訳差し戻し E2E テスト
 *
 * US-JNL-009: 仕訳差し戻し
 *
 * 受入条件:
 * - 「承認待ち」ステータスの仕訳のみ差し戻しできる
 * - 差し戻し理由を入力できる
 * - 差し戻し後、ステータスが「下書き」に戻る
 * - 差し戻し成功時、確認メッセージが表示される
 */

describe('US-JNL-009: 仕訳差し戻し', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('差し戻しボタン表示', () => {
    beforeEach(() => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
    });

    it('承認待ちステータスの仕訳に差し戻しボタンが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.checkButtonInFirstRow('差し戻し', true);
    });

    it('下書きステータスの仕訳には差し戻しボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.checkButtonInFirstRow('差し戻し', false);
    });

    it('承認済みステータスの仕訳には差し戻しボタンが表示されない', () => {
      cy.filterJournalEntriesByStatus('APPROVED');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.checkButtonInFirstRow('差し戻し', false);
    });
  });

  describe('差し戻し実行', () => {
    beforeEach(() => {
      // Admin で仕訳作成 → 承認申請して PENDING 状態にする
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.createTestJournalEntry('2024-08-01', '差し戻しテスト仕訳', '20000');
      cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
      cy.filterJournalEntriesByStatus('DRAFT');
      // MSW は unshift で新エントリを先頭に追加するため、先頭行が最新
      cy.clickButtonInFirstRowWithConfirm('承認申請', true);
      cy.contains('仕訳を承認申請しました', { timeout: 10000 }).should('be.visible');
      // Manager で再ログイン
      cy.clearAuth();
      cy.loginAndVisitJournalList('manager', 'Password123!');
    });

    it('差し戻しボタンクリックで理由入力ダイアログが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      // prompt をスタブしてキャンセル（null を返す）
      cy.window().then((win) => {
        cy.stub(win, 'prompt').returns(null);
      });
      cy.get('table tbody tr').first().contains('button', '差し戻し').click();
      cy.window().its('prompt').should('be.called');
    });

    it('理由入力ダイアログでキャンセルすると差し戻しが行われない', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      // prompt をスタブしてキャンセル
      cy.window().then((win) => {
        cy.stub(win, 'prompt').returns(null);
      });
      cy.get('table tbody tr').first().contains('button', '差し戻し').click();
      // ステータスが変わらないこと
      cy.get('table tbody tr').first().should('contain', '承認待ち');
    });

    it('差し戻し理由が空の場合はエラーメッセージが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      // prompt をスタブして空文字を返す
      cy.window().then((win) => {
        cy.stub(win, 'prompt').returns('');
      });
      cy.get('table tbody tr').first().contains('button', '差し戻し').click();
      cy.contains('差し戻し理由は必須です', { timeout: 10000 }).should('be.visible');
    });

    it('差し戻しが成功すると成功メッセージが表示される', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      // prompt をスタブして理由を入力
      cy.window().then((win) => {
        cy.stub(win, 'prompt').returns('金額に誤りがあります');
      });
      cy.get('table tbody tr').first().contains('button', '差し戻し').click();
      cy.contains('仕訳を差し戻しました', { timeout: 10000 }).should('be.visible');
    });

    it('差し戻し後にステータスが下書きに戻る', () => {
      cy.filterJournalEntriesByStatus('PENDING');
      // prompt をスタブして理由を入力
      cy.window().then((win) => {
        cy.stub(win, 'prompt').returns('勘定科目の選択ミス');
      });
      cy.get('table tbody tr').first().contains('button', '差し戻し').click();
      cy.contains('仕訳を差し戻しました', { timeout: 10000 }).should('be.visible');
      // 一覧再読み込み後、下書きにフィルタして確認
      cy.filterJournalEntriesByStatus('DRAFT');
      cy.get('table tbody tr').first().should('contain', '下書き');
    });
  });

  describe('権限確認', () => {
    it('一般ユーザーは差し戻しできない（差し戻しボタンが表示されない）', () => {
      cy.loginAndVisitJournalList('user', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      // 一般ユーザーには差し戻しボタンが表示されないこと
      cy.get('table tbody tr')
        .first()
        .find('button')
        .contains(/^差し戻し$/)
        .should('not.exist');
    });

    it('マネージャーは差し戻しできる', () => {
      cy.loginAndVisitJournalList('manager', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      cy.checkButtonInFirstRow('差し戻し', true);
    });

    it('管理者は差し戻しできる', () => {
      cy.loginAndVisitJournalList('admin', 'Password123!');
      cy.filterJournalEntriesByStatus('PENDING');
      cy.checkButtonInFirstRow('差し戻し', true);
    });
  });
});
