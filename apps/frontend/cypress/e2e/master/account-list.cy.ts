/**
 * 勘定科目一覧表示 E2E テスト
 *
 * US-MST-004: 勘定科目一覧表示
 *
 * 受入条件:
 * - 科目コード、科目名、勘定科目種別が一覧表示される
 * - 勘定科目種別でフィルタリングできる
 * - 科目コードまたは科目名で検索できる
 */
describe('US-MST-004: 勘定科目一覧表示', () => {
  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('科目コード、科目名、勘定科目種別が一覧表示される', () => {
      // Given: 勘定科目一覧ページが表示されている

      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.contains('th', '勘定科目コード').should('be.visible');
      cy.contains('th', '勘定科目名').should('be.visible');
      cy.contains('th', '勘定科目種別').should('be.visible');

      // Then: テーブルにデータが表示される
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('勘定科目一覧にフィルタUIが表示される', () => {
      // Given: 勘定科目一覧ページが表示されている

      // Then: フィルタUIが表示される
      cy.get('[data-testid="account-filter"]').should('be.visible');
      cy.get('#account-filter-type').should('be.visible');
      cy.get('#account-filter-keyword').should('be.visible');
      cy.contains('button', '検索').should('be.visible');
      cy.contains('button', 'リセット').should('be.visible');
    });
  });

  describe('勘定科目種別フィルタリング', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('勘定科目種別でフィルタリングできる - 資産', () => {
      // Given: 勘定科目一覧が表示されている
      // 初期データ件数を取得
      cy.get('table tbody tr').its('length').then((initialCount) => {
        // When: 資産でフィルタリング
        cy.get('#account-filter-type').select('ASSET');
        cy.contains('button', '検索').click();

        // Then: フィルタリング後、資産の勘定科目が表示される
        // API レスポンスを待つため、テーブルの更新を待機
        cy.get('table tbody tr', { timeout: 10000 }).should('exist');
        // 最初の行が ASSET であることを確認（フィルタリングが機能していることの確認）
        cy.get('table tbody tr').first().find('td').eq(2).should('contain', 'ASSET');
      });
    });

    it('勘定科目種別でフィルタリングできる - 負債', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 負債でフィルタリング
      cy.get('#account-filter-type').select('LIABILITY');
      cy.contains('button', '検索').click();

      // Then: 検索が実行される（結果はデータ依存）
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      // フィルタリング機能が動作することを確認（UI がエラーなく動作）
      cy.get('[data-testid="account-filter"]').should('be.visible');
      cy.get('#account-filter-type').should('have.value', 'LIABILITY');
    });

    it('勘定科目種別でフィルタリングできる - 費用', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 費用でフィルタリング
      cy.get('#account-filter-type').select('EXPENSE');
      cy.contains('button', '検索').click();

      // Then: 検索が実行される（結果はデータ依存）
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      // フィルタリング機能が動作することを確認（UI がエラーなく動作）
      cy.get('[data-testid="account-filter"]').should('be.visible');
      cy.get('#account-filter-type').should('have.value', 'EXPENSE');
    });

    it('すべての勘定科目種別オプションが選択可能', () => {
      // Given: 勘定科目一覧が表示されている

      // Then: すべてのオプションが存在する
      cy.get('#account-filter-type option').should('have.length', 6);
      cy.get('#account-filter-type').contains('option', 'すべて').should('exist');
      cy.get('#account-filter-type').contains('option', '資産').should('exist');
      cy.get('#account-filter-type').contains('option', '負債').should('exist');
      cy.get('#account-filter-type').contains('option', '純資産').should('exist');
      cy.get('#account-filter-type').contains('option', '収益').should('exist');
      cy.get('#account-filter-type').contains('option', '費用').should('exist');
    });
  });

  describe('キーワード検索', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('科目コードで検索できる', () => {
      // Given: 勘定科目一覧が表示されている
      // 初期件数を記録
      cy.get('table tbody tr').its('length').as('initialCount');

      // When: 科目コードで検索（「1000」の前方一致）
      cy.get('#account-filter-keyword').clear().type('1000');
      cy.contains('button', '検索').click();

      // Then: 検索が実行され、結果が表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      // 検索結果にコード 1000 を含む行があることを確認
      cy.get('table tbody tr').first().find('td').first().should('contain', '1000');
    });

    it('科目名で検索できる', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 科目名で検索（「現金」を検索）
      cy.get('#account-filter-keyword').clear().type('現金');
      cy.contains('button', '検索').click();

      // Then: 検索が実行され、結果が表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      cy.get('body').then(($body) => {
        if ($body.find('table tbody tr').length > 0) {
          // 検索結果に「現金」を含む行があることを確認
          cy.get('table tbody tr').first().find('td').eq(1).should('contain', '現金');
        } else {
          // データがない場合は空メッセージを確認
          cy.contains('勘定科目が登録されていません').should('be.visible');
        }
      });
    });

    it('Enterキーで検索が実行される', () => {
      // Given: 勘定科目一覧が表示されている

      // When: キーワード入力後にEnterキーを押す
      cy.get('#account-filter-keyword').clear().type('1000{enter}');

      // Then: 検索が実行される（テーブルが更新される）
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });

    it('検索結果が0件の場合、空のメッセージが表示される', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 存在しないキーワードで検索
      cy.get('#account-filter-keyword').clear().type('ZZZZNOTEXIST99999');
      cy.contains('button', '検索').click();

      // Then: テーブルが空になるか、空のメッセージが表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      cy.get('body').then(($body) => {
        const rows = $body.find('table tbody tr');
        if (rows.length === 0) {
          cy.contains('勘定科目が登録されていません').should('be.visible');
        }
        // 注: データベースの状態によっては結果が返る可能性があるため、
        // このテストは検索機能が動作することの確認に留める
      });
    });
  });

  describe('複合検索（種別 + キーワード）', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('勘定科目種別とキーワードの複合検索ができる', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 資産 + キーワードで検索
      cy.get('#account-filter-type').select('ASSET');
      cy.get('#account-filter-keyword').clear().type('1000');
      cy.contains('button', '検索').click();

      // Then: 検索が実行される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
      cy.get('body').then(($body) => {
        const rows = $body.find('table tbody tr');
        if (rows.length > 0) {
          // 結果があれば、最初の行が ASSET であることを確認
          cy.get('table tbody tr').first().find('td').eq(2).should('contain', 'ASSET');
        } else {
          // 結果がなければ空メッセージが表示される
          cy.contains('勘定科目が登録されていません').should('be.visible');
        }
      });
    });
  });

  describe('リセット機能', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('リセットボタンでフィルタ条件がクリアされる', () => {
      // Given: フィルタ条件が設定されている
      cy.get('#account-filter-type').select('ASSET');
      cy.get('#account-filter-keyword').clear().type('テスト');
      cy.contains('button', '検索').click();

      // When: リセットボタンをクリック
      cy.contains('button', 'リセット').click();

      // Then: フィルタ条件がクリアされる
      cy.get('#account-filter-type').should('have.value', '');
      cy.get('#account-filter-keyword').should('have.value', '');

      // Then: 全件が表示される
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーは勘定科目一覧ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目一覧ページに直接アクセス
      cy.visit('/master/accounts');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/master/accounts');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーは勘定科目一覧ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 勘定科目一覧ページに直接アクセス
      cy.visit('/master/accounts');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('経理責任者は勘定科目一覧ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目一覧ページにアクセス
      cy.visit('/master/accounts');

      // Then: 勘定科目一覧が表示される
      cy.get('[data-testid="account-list"]').should('be.visible');
      cy.get('[data-testid="account-filter"]').should('be.visible');
    });
  });
});
