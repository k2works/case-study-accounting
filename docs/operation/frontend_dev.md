# フロントエンド開発手順解説

## 概要

本ドキュメントは、財務会計システムのフロントエンド開発における日常的な開発手順を解説します。
Container / View パターンに基づいた機能追加・修正の具体的な手順を示します。

## 前提条件

- [フロントエンド構築手順書](frontend_setup.md) に従って環境構築が完了していること
- [フロントエンド共通 UI コンポーネント](frontend_common_ui.md) を理解していること

## 開発フロー概要

```
1. API 仕様の確認・生成
   ↓
2. ページコンポーネントの作成
   ↓
3. Container コンポーネントの実装
   ↓
4. View コンポーネントの実装
   ↓
5. テストの作成・実行
   ↓
6. 品質チェック・コミット
```

---

## 1. 開発サーバーの起動

### 通常の開発

```bash
cd apps/frontend
npm run dev
```

ブラウザで http://localhost:3000 が自動的に開きます。

### Guard モード（推奨）

ファイル変更時に自動で lint、format、test を実行:

```bash
npm run guard
```

---

## 2. API クライアントの生成

### バックエンドから OpenAPI 仕様を取得

```bash
# バックエンドが http://localhost:8080 で起動している状態で
npm run api:fetch
```

このコマンドは以下を実行します:
1. バックエンドから `openapi.yaml` を取得
2. Orval で API クライアントを自動生成

### 生成されるファイル

```
src/api/
├── generated/        # API クライアント（編集禁止）
│   ├── 認証/
│   │   └── 認証.ts
│   ├── 勘定科目/
│   │   └── 勘定科目.ts
│   └── ...
├── model/            # 型定義（編集禁止）
│   ├── loginRequest.ts
│   ├── loginResponse.ts
│   └── index.ts
└── axios-instance.ts # カスタム設定（編集可）
```

### 生成コードの使用例

```typescript
import { useGetAccounts, useCreateAccount } from '@/api/generated/勘定科目/勘定科目';
import type { AccountResponse, AccountRequest } from '@/api/model';

// データ取得
const { data: accounts, isLoading, error } = useGetAccounts();

// データ作成
const createMutation = useCreateAccount();
createMutation.mutate({ data: newAccount });
```

---

## 3. 新規ページの追加

### 3.1 ページコンポーネントの作成

ページコンポーネントはルーティングのエントリポイントです。

```typescript
// src/pages/master/AccountPage.tsx
import React from 'react';
import { MainLayout } from '@/views/common/layout/MainLayout';
import { AccountContainer } from '@/components/master/account/AccountContainer';

const AccountPage: React.FC = () => {
  const breadcrumbs = [
    { label: 'ホーム', path: '/' },
    { label: 'マスタ管理' },
    { label: '勘定科目' },
  ];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <AccountContainer />
    </MainLayout>
  );
};

export default AccountPage;
```

### 3.2 ルーティングの追加

```typescript
// src/App.tsx
import { lazy } from 'react';
import { Routes, Route } from 'react-router-dom';

const AccountPage = lazy(() => import('./pages/master/AccountPage'));

function App() {
  return (
    <Routes>
      {/* 既存のルート */}
      <Route path="/master/accounts" element={<AccountPage />} />
    </Routes>
  );
}
```

### 3.3 サイドバーへの追加

```typescript
// src/views/common/layout/Sidebar.tsx
const MENU_ITEMS: MenuItem[] = [
  // ...
  {
    id: 'master',
    label: 'マスタ管理',
    icon: 'settings',
    children: [
      { id: 'master-accounts', label: '勘定科目', path: '/master/accounts' },
      // 新しいメニュー項目を追加
    ],
  },
];
```

---

## 4. Container コンポーネントの実装

Container はデータ取得・状態管理を担当します。

### 4.1 基本構造

```typescript
// src/components/master/account/AccountContainer.tsx
import React, { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  useGetAccounts,
  useCreateAccount,
  useUpdateAccount,
  useDeleteAccount,
  getGetAccountsQueryKey,
} from '@/api/generated/勘定科目/勘定科目';
import type { AccountResponse, AccountRequest } from '@/api/model';
import { AccountCollection } from '@/views/master/account/AccountCollection';
import { AccountEditModal } from '@/views/master/account/AccountEditModal';
import { Loading } from '@/views/common/Loading';
import { ErrorMessage } from '@/views/common/ErrorMessage';

export const AccountContainer: React.FC = () => {
  const queryClient = useQueryClient();

  // 1. サーバー状態（TanStack Query）
  const { data: accounts, isLoading, error, refetch } = useGetAccounts();

  // 2. クライアント状態（useState）
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<AccountResponse | null>(null);

  // 3. Mutations
  const createMutation = useCreateAccount({
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAccountsQueryKey() });
      handleCloseModal();
    },
  });

  const updateMutation = useUpdateAccount({
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAccountsQueryKey() });
      handleCloseModal();
    },
  });

  const deleteMutation = useDeleteAccount({
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: getGetAccountsQueryKey() });
    },
  });

  // 4. イベントハンドラ
  const handleCreateClick = () => {
    setEditingAccount(null);
    setIsModalOpen(true);
  };

  const handleEditClick = (account: AccountResponse) => {
    setEditingAccount(account);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (account: AccountResponse) => {
    deleteMutation.mutate({ accountCode: account.accountCode });
  };

  const handleSave = (data: AccountRequest) => {
    if (editingAccount) {
      updateMutation.mutate({ accountCode: editingAccount.accountCode, data });
    } else {
      createMutation.mutate({ data });
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingAccount(null);
  };

  // 5. 条件分岐レンダリング
  if (isLoading) {
    return <Loading message="勘定科目を読み込み中..." />;
  }

  if (error) {
    return <ErrorMessage message="データの取得に失敗しました" onRetry={refetch} />;
  }

  // 6. 正常表示
  return (
    <>
      <AccountCollection
        accounts={accounts ?? []}
        onCreateClick={handleCreateClick}
        onEditClick={handleEditClick}
        onDeleteClick={handleDeleteClick}
      />
      {isModalOpen && (
        <AccountEditModal
          account={editingAccount}
          onSave={handleSave}
          onCancel={handleCloseModal}
          isSubmitting={createMutation.isPending || updateMutation.isPending}
        />
      )}
    </>
  );
};
```

### 4.2 Container の責務

| 責務 | 説明 |
|------|------|
| データ取得 | TanStack Query でサーバーからデータを取得 |
| 状態管理 | useState でモーダル開閉などの UI 状態を管理 |
| Mutation | 作成・更新・削除の API 呼び出し |
| イベントハンドラ | View から受け取ったイベントを処理 |
| キャッシュ更新 | Mutation 成功時にクエリを無効化 |
| 条件分岐 | ローディング・エラー・正常状態の表示制御 |

---

## 5. View コンポーネントの実装

View は純粋な UI 描画を担当します。

### 5.1 一覧表示（Collection）

```typescript
// src/views/master/account/AccountCollection.tsx
import React from 'react';
import type { AccountResponse } from '@/api/model';
import { AccountTable } from './AccountTable';
import { Button } from '@/views/common/Button';
import './AccountCollection.css';

interface AccountCollectionProps {
  accounts: AccountResponse[];
  onCreateClick: () => void;
  onEditClick: (account: AccountResponse) => void;
  onDeleteClick: (account: AccountResponse) => void;
}

export const AccountCollection: React.FC<AccountCollectionProps> = ({
  accounts,
  onCreateClick,
  onEditClick,
  onDeleteClick,
}) => {
  return (
    <div className="account-collection">
      <div className="account-collection__header">
        <h1>勘定科目一覧</h1>
        <Button variant="primary" onClick={onCreateClick}>
          新規登録
        </Button>
      </div>
      <AccountTable
        accounts={accounts}
        onEdit={onEditClick}
        onDelete={onDeleteClick}
      />
    </div>
  );
};
```

### 5.2 テーブル表示

```typescript
// src/views/master/account/AccountTable.tsx
import React from 'react';
import type { AccountResponse } from '@/api/model';
import { Table, TableColumn } from '@/views/common/Table';
import { Button } from '@/views/common/Button';

interface AccountTableProps {
  accounts: AccountResponse[];
  onEdit: (account: AccountResponse) => void;
  onDelete: (account: AccountResponse) => void;
}

export const AccountTable: React.FC<AccountTableProps> = ({
  accounts,
  onEdit,
  onDelete,
}) => {
  const columns: TableColumn<AccountResponse>[] = [
    { key: 'accountCode', header: 'コード', width: '100px' },
    { key: 'accountName', header: '勘定科目名' },
    {
      key: 'bsplType',
      header: '区分',
      render: (value) => (value === 'B' ? 'B/S' : 'P/L'),
    },
    { key: 'elementType', header: '要素' },
    {
      key: 'accountCode',
      header: '操作',
      render: (_, row) => (
        <div className="account-table__actions">
          <Button size="small" onClick={() => onEdit(row)}>
            編集
          </Button>
          <Button size="small" variant="danger" onClick={() => onDelete(row)}>
            削除
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Table
      columns={columns}
      data={accounts}
      keyField="accountCode"
      emptyMessage="勘定科目が登録されていません"
    />
  );
};
```

### 5.3 フォーム

```typescript
// src/views/master/account/AccountForm.tsx
import React, { useState, FormEvent } from 'react';
import type { AccountRequest } from '@/api/model';
import { Button } from '@/views/common/Button';
import './AccountForm.css';

interface AccountFormProps {
  initialData?: Partial<AccountRequest>;
  onSubmit: (data: AccountRequest) => void;
  onCancel: () => void;
  isSubmitting?: boolean;
}

export const AccountForm: React.FC<AccountFormProps> = ({
  initialData,
  onSubmit,
  onCancel,
  isSubmitting = false,
}) => {
  const [formData, setFormData] = useState<AccountRequest>({
    accountCode: initialData?.accountCode ?? '',
    accountName: initialData?.accountName ?? '',
    bsplType: initialData?.bsplType ?? 'B',
    debitCreditType: initialData?.debitCreditType ?? '借',
    elementType: initialData?.elementType ?? '資産',
    displayOrder: initialData?.displayOrder ?? 0,
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <form className="account-form" onSubmit={handleSubmit}>
      <div className="account-form__field">
        <label htmlFor="accountCode">勘定科目コード</label>
        <input
          id="accountCode"
          name="accountCode"
          type="text"
          value={formData.accountCode}
          onChange={handleChange}
          required
          disabled={!!initialData?.accountCode}
        />
      </div>

      <div className="account-form__field">
        <label htmlFor="accountName">勘定科目名</label>
        <input
          id="accountName"
          name="accountName"
          type="text"
          value={formData.accountName}
          onChange={handleChange}
          required
        />
      </div>

      <div className="account-form__field">
        <label htmlFor="bsplType">B/S・P/L 区分</label>
        <select
          id="bsplType"
          name="bsplType"
          value={formData.bsplType}
          onChange={handleChange}
        >
          <option value="B">B/S（貸借対照表）</option>
          <option value="P">P/L（損益計算書）</option>
        </select>
      </div>

      <div className="account-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel}>
          キャンセル
        </Button>
        <Button type="submit" variant="primary" isLoading={isSubmitting}>
          保存
        </Button>
      </div>
    </form>
  );
};
```

### 5.4 View の責務

| 責務 | 説明 |
|------|------|
| UI 描画 | props に基づいた表示 |
| イベント発火 | ユーザー操作を親に通知 |
| スタイリング | CSS によるデザイン |
| バリデーション表示 | エラーメッセージの表示 |

**View がやってはいけないこと:**
- API 呼び出し
- グローバル状態へのアクセス
- 副作用（useEffect での API コール等）

---

## 6. テストの作成

### 6.1 View コンポーネントのテスト

```typescript
// src/views/master/account/AccountCollection.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AccountCollection } from './AccountCollection';

const mockAccounts = [
  {
    accountCode: '111',
    accountName: '現金預金',
    bsplType: 'B',
    debitCreditType: '借',
    elementType: '資産',
    displayOrder: 1,
    version: 1,
  },
];

describe('AccountCollection', () => {
  it('勘定科目一覧が表示される', () => {
    render(
      <AccountCollection
        accounts={mockAccounts}
        onCreateClick={vi.fn()}
        onEditClick={vi.fn()}
        onDeleteClick={vi.fn()}
      />
    );

    expect(screen.getByText('現金預金')).toBeInTheDocument();
  });

  it('新規登録ボタンをクリックするとonCreateClickが呼ばれる', async () => {
    const user = userEvent.setup();
    const onCreateClick = vi.fn();

    render(
      <AccountCollection
        accounts={mockAccounts}
        onCreateClick={onCreateClick}
        onEditClick={vi.fn()}
        onDeleteClick={vi.fn()}
      />
    );

    await user.click(screen.getByText('新規登録'));
    expect(onCreateClick).toHaveBeenCalled();
  });
});
```

### 6.2 Container コンポーネントのテスト

```typescript
// src/components/master/account/AccountContainer.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClientProvider, QueryClient } from '@tanstack/react-query';
import { AccountContainer } from './AccountContainer';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('AccountContainer', () => {
  it('勘定科目一覧が表示される', async () => {
    render(<AccountContainer />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('現金預金')).toBeInTheDocument();
    });
  });

  it('新規登録ボタンでモーダルが開く', async () => {
    const user = userEvent.setup();
    render(<AccountContainer />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('新規登録')).toBeInTheDocument();
    });

    await user.click(screen.getByText('新規登録'));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });
});
```

### 6.3 MSW ハンドラーの追加

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';
import type { AccountResponse } from '@/api/model';

export const accountHandlers = [
  http.get('*/accounts', () => {
    return HttpResponse.json<AccountResponse[]>([
      {
        accountCode: '111',
        accountName: '現金預金',
        bsplType: 'B',
        debitCreditType: '借',
        elementType: '資産',
        displayOrder: 1,
        version: 1,
      },
    ]);
  }),

  http.post('*/accounts', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json({ ...body, version: 1 }, { status: 201 });
  }),
];

export const handlers = [...authHandlers, ...accountHandlers];
```

### 6.4 テストの実行

```bash
# 単発実行
npm run test:run

# ウォッチモード
npm run test

# カバレッジ付き
npm run test:coverage
```

---

## 7. 品質チェック

### 7.1 全体チェック

```bash
npm run checkAndFix
```

以下を順番に実行:
1. ESLint（自動修正）
2. Prettier（フォーマット）
3. 循環参照チェック
4. テスト

### 7.2 個別チェック

```bash
# ESLint
npm run lint
npm run lint:fix

# フォーマット
npm run format
npm run format:check

# 循環参照
npm run depcruise

# ビルド確認
npm run build
```

---

## 8. デバッグ

### 8.1 React Developer Tools

ブラウザ拡張機能をインストールして、コンポーネントの状態を確認。

### 8.2 TanStack Query Devtools

開発環境では自動的に有効化されています。画面右下のアイコンから開けます。

```typescript
// src/main.tsx
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

<QueryClientProvider client={queryClient}>
  <App />
  <ReactQueryDevtools initialIsOpen={false} />
</QueryClientProvider>
```

### 8.3 API リクエストの確認

ブラウザの開発者ツール → Network タブで API リクエストを確認。

### 8.4 MSW の確認

```typescript
// コンソールでモックが適用されているか確認
console.log('MSW enabled:', import.meta.env.VITE_ENABLE_MSW);
```

---

## 9. よくあるパターン

### 9.1 ページネーション

```typescript
const [page, setPage] = useState(1);
const [itemsPerPage, setItemsPerPage] = useState(20);

const { data } = useGetAccounts({ page, size: itemsPerPage });

<Pagination
  currentPage={page}
  totalPages={data?.totalPages ?? 0}
  totalItems={data?.totalElements ?? 0}
  itemsPerPage={itemsPerPage}
  onPageChange={setPage}
  onItemsPerPageChange={setItemsPerPage}
/>
```

### 9.2 検索・フィルター

```typescript
const [searchTerm, setSearchTerm] = useState('');
const debouncedSearch = useDebounce(searchTerm, 300);

const { data } = useGetAccounts({ keyword: debouncedSearch });
```

### 9.3 確認ダイアログ

```typescript
const [deleteTarget, setDeleteTarget] = useState<AccountResponse | null>(null);

<ConfirmModal
  isOpen={!!deleteTarget}
  onClose={() => setDeleteTarget(null)}
  onConfirm={() => {
    deleteMutation.mutate({ accountCode: deleteTarget!.accountCode });
    setDeleteTarget(null);
  }}
  title="削除確認"
  message={`「${deleteTarget?.accountName}」を削除しますか？`}
  isDestructive
/>
```

### 9.4 成功通知

```typescript
const [showSuccess, setShowSuccess] = useState(false);

const createMutation = useCreateAccount({
  onSuccess: () => {
    setShowSuccess(true);
    queryClient.invalidateQueries({ queryKey: getGetAccountsQueryKey() });
  },
});

{showSuccess && (
  <SuccessNotification
    message="保存が完了しました"
    onDismiss={() => setShowSuccess(false)}
  />
)}
```

---

## 10. コーディング規約

### 10.1 命名規則

| 対象 | 規則 | 例 |
|------|------|-----|
| ページ | 〜Page | AccountPage |
| Container | 〜Container | AccountContainer |
| View（一覧） | 〜Collection | AccountCollection |
| View（詳細） | 〜Single / 〜Detail | AccountDetail |
| View（フォーム） | 〜Form | AccountForm |
| View（テーブル） | 〜Table | AccountTable |
| Hook | use〜 | useAuth |
| CSS | BEM 記法 | .account-form__field |

### 10.2 ファイル配置

```
機能追加時のファイル作成順序:

1. src/pages/xxx/XxxPage.tsx           # ページ
2. src/components/xxx/XxxContainer.tsx # Container
3. src/views/xxx/XxxCollection.tsx     # 一覧 View
4. src/views/xxx/XxxTable.tsx          # テーブル View
5. src/views/xxx/XxxForm.tsx           # フォーム View
6. src/views/xxx/Xxx.css               # スタイル
7. src/mocks/handlers.ts               # モック追加
8. テストファイル                       # テスト
```

### 10.3 インポート順序

```typescript
// 1. React
import React, { useState, useEffect } from 'react';

// 2. 外部ライブラリ
import { useQueryClient } from '@tanstack/react-query';

// 3. API（生成コード）
import { useGetAccounts } from '@/api/generated/勘定科目/勘定科目';

// 4. 型定義
import type { AccountResponse } from '@/api/model';

// 5. コンポーネント
import { AccountCollection } from '@/views/master/account/AccountCollection';
import { Loading } from '@/views/common/Loading';

// 6. フック・ユーティリティ
import { useAuth } from '@/hooks/useAuth';

// 7. スタイル
import './AccountContainer.css';
```

---

## 関連ドキュメント

- [フロントエンド構築手順書](frontend_setup.md) - 環境構築
- [フロントエンド共通 UI コンポーネント](frontend_common_ui.md) - 共通コンポーネント
- [フロントエンドアーキテクチャ](../design/architecture_frontend.md) - 設計思想
- [UI 設計](../design/ui-design.md) - UI 設計
