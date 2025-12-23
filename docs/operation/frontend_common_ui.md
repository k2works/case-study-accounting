# フロントエンド共通レイアウト・UI コンポーネント実装解説

## 概要

本ドキュメントは、財務会計システムのフロントエンドにおける共通レイアウトおよび UI コンポーネントの実装について解説します。

## ディレクトリ構成

```
src/views/common/
├── layout/                    # レイアウトコンポーネント
│   ├── MainLayout.tsx         # メインレイアウト
│   ├── MainLayout.css
│   ├── Header.tsx             # ヘッダー
│   ├── Header.css
│   ├── Sidebar.tsx            # サイドバーナビゲーション
│   ├── Sidebar.css
│   ├── Breadcrumb.tsx         # パンくずリスト
│   └── Breadcrumb.css
├── Button.tsx                 # ボタン
├── Button.css
├── Modal.tsx                  # モーダル・確認ダイアログ
├── Modal.css
├── Table.tsx                  # テーブル
├── Table.css
├── Pagination.tsx             # ページネーション
├── Pagination.css
├── Loading.tsx                # ローディング
├── Loading.css
├── ErrorMessage.tsx           # エラーメッセージ
├── ErrorMessage.css
├── SuccessNotification.tsx    # 成功通知
├── SuccessNotification.css
├── MoneyDisplay.tsx           # 金額表示
└── MoneyDisplay.css
```

## レイアウトコンポーネント

### MainLayout

メインレイアウトコンポーネント。ヘッダー、サイドバー、メインコンテンツエリアを統合します。

```tsx
import { MainLayout } from '@/views/common/layout/MainLayout';

const MyPage: React.FC = () => {
  const breadcrumbs = [
    { label: 'ホーム', path: '/' },
    { label: '仕訳管理', path: '/journals' },
    { label: '仕訳入力' },
  ];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <h1>仕訳入力</h1>
      {/* ページコンテンツ */}
    </MainLayout>
  );
};
```

**Props:**

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `children` | `ReactNode` | ○ | メインコンテンツ |
| `breadcrumbs` | `BreadcrumbItem[]` | - | パンくずリスト |

**機能:**
- レスポンシブ対応（モバイル用ハンバーガーメニュー）
- サイドバーの開閉制御
- パンくずリストの表示

### Header

アプリケーションヘッダー。アプリ名、ユーザー情報、ログアウトボタンを表示します。

```tsx
import { Header } from '@/views/common/layout/Header';

// MainLayout 内部で自動的に使用される
```

**機能:**
- アプリケーション名の表示（`config.appName` から取得）
- ログインユーザー名の表示
- ログアウトボタン

### Sidebar

サイドバーナビゲーション。ユーザーのロールに基づいてメニュー項目をフィルタリングします。

```tsx
import { Sidebar } from '@/views/common/layout/Sidebar';

// MainLayout 内部で自動的に使用される
```

**メニュー構成:**

| メニュー | サブメニュー | 必要ロール |
|---------|-------------|-----------|
| ダッシュボード | - | 全員 |
| 仕訳管理 | 仕訳一覧、仕訳入力、承認待ち | 承認待ちは ADMIN/MANAGER |
| 元帳・残高 | 総勘定元帳、補助元帳、残高試算表 | 全員 |
| 財務諸表 | 貸借対照表、損益計算書、財務分析 | 財務分析は ADMIN/MANAGER |
| マスタ管理 | 勘定科目 | 全員 |
| システム管理 | ユーザー、監査ログ | ADMIN のみ |

**機能:**
- アコーディオン式サブメニュー
- ロールベースのメニューフィルタリング
- アクティブ状態の視覚的表示
- モバイル対応（スライドイン）

### Breadcrumb

パンくずリストコンポーネント。現在のページ位置を階層的に表示します。

```tsx
import { Breadcrumb, BreadcrumbItem } from '@/views/common/layout/Breadcrumb';

const items: BreadcrumbItem[] = [
  { label: 'ホーム', path: '/' },
  { label: '仕訳管理', path: '/journals' },
  { label: '仕訳詳細' }, // 最後の項目はリンクなし
];

<Breadcrumb items={items} />
```

**BreadcrumbItem:**

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `label` | `string` | ○ | 表示テキスト |
| `path` | `string` | - | リンク先（なければテキストのみ） |

## UI コンポーネント

### Button

統一されたスタイルのボタンコンポーネント。

```tsx
import { Button } from '@/views/common/Button';

<Button variant="primary" size="medium" onClick={handleClick}>
  保存
</Button>

<Button variant="danger" isLoading={isSaving}>
  削除
</Button>

<Button variant="secondary" fullWidth>
  キャンセル
</Button>
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `variant` | `'primary' \| 'secondary' \| 'danger' \| 'text'` | `'primary'` | ボタンのスタイル |
| `size` | `'small' \| 'medium' \| 'large'` | `'medium'` | サイズ |
| `isLoading` | `boolean` | `false` | ローディング状態 |
| `fullWidth` | `boolean` | `false` | 幅いっぱいに広げる |
| `disabled` | `boolean` | `false` | 無効化 |

### Modal / ConfirmModal

モーダルダイアログコンポーネント。

```tsx
import { Modal, ConfirmModal } from '@/views/common/Modal';

// 汎用モーダル
<Modal
  isOpen={isOpen}
  onClose={handleClose}
  title="ユーザー編集"
  actions={
    <>
      <Button variant="secondary" onClick={handleClose}>キャンセル</Button>
      <Button onClick={handleSave}>保存</Button>
    </>
  }
>
  <form>{/* フォーム内容 */}</form>
</Modal>

// 確認モーダル
<ConfirmModal
  isOpen={isOpen}
  onClose={handleClose}
  onConfirm={handleDelete}
  title="削除確認"
  message="この仕訳を削除しますか？"
  confirmLabel="削除"
  isDestructive
/>
```

**Modal Props:**

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `isOpen` | `boolean` | ○ | 表示状態 |
| `onClose` | `() => void` | ○ | 閉じる処理 |
| `title` | `string` | ○ | タイトル |
| `children` | `ReactNode` | ○ | コンテンツ |
| `actions` | `ReactNode` | - | アクションボタン |

**ConfirmModal Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `isOpen` | `boolean` | - | 表示状態 |
| `onClose` | `() => void` | - | 閉じる処理 |
| `onConfirm` | `() => void` | - | 確認処理 |
| `title` | `string` | - | タイトル |
| `message` | `string` | - | メッセージ |
| `confirmLabel` | `string` | `'OK'` | 確認ボタンのラベル |
| `cancelLabel` | `string` | `'キャンセル'` | キャンセルボタンのラベル |
| `isDestructive` | `boolean` | `false` | 破壊的操作（赤いボタン） |

**機能:**
- Escape キーで閉じる
- オーバーレイクリックで閉じる
- スクロールロック

### Table

汎用テーブルコンポーネント。

```tsx
import { Table, TableColumn } from '@/views/common/Table';

interface JournalEntry {
  id: number;
  date: string;
  description: string;
  amount: number;
}

const columns: TableColumn<JournalEntry>[] = [
  { key: 'date', header: '日付', width: '120px' },
  { key: 'description', header: '摘要' },
  {
    key: 'amount',
    header: '金額',
    align: 'right',
    render: (value) => `¥${Number(value).toLocaleString()}`,
  },
];

<Table
  columns={columns}
  data={journals}
  keyField="id"
  onRowClick={(row) => navigate(`/journals/${row.id}`)}
  isLoading={isLoading}
  emptyMessage="仕訳データがありません"
  selectable
  selectedKeys={selectedIds}
  onSelectionChange={setSelectedIds}
/>
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `columns` | `TableColumn<T>[]` | - | カラム定義 |
| `data` | `T[]` | - | データ配列 |
| `keyField` | `keyof T` | - | 一意識別子のフィールド |
| `onRowClick` | `(row, index) => void` | - | 行クリック時の処理 |
| `isLoading` | `boolean` | `false` | ローディング状態 |
| `emptyMessage` | `string` | `'データがありません'` | 空の場合のメッセージ |
| `selectable` | `boolean` | `false` | 行選択を有効化 |
| `selectedKeys` | `Set<string \| number>` | - | 選択中のキー |
| `onSelectionChange` | `(keys) => void` | - | 選択変更時の処理 |

**TableColumn:**

| プロパティ | 型 | 説明 |
|-----------|-----|------|
| `key` | `string` | データのプロパティ名 |
| `header` | `string` | ヘッダーテキスト |
| `width` | `string` | カラム幅 |
| `align` | `'left' \| 'center' \| 'right'` | 配置 |
| `render` | `(value, row, index) => ReactNode` | カスタムレンダラー |

### Pagination

ページネーションコンポーネント。

```tsx
import { Pagination } from '@/views/common/Pagination';

<Pagination
  currentPage={page}
  totalPages={Math.ceil(totalItems / itemsPerPage)}
  totalItems={totalItems}
  itemsPerPage={itemsPerPage}
  onPageChange={setPage}
  onItemsPerPageChange={setItemsPerPage}
  itemsPerPageOptions={[10, 20, 50, 100]}
/>
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `currentPage` | `number` | - | 現在のページ |
| `totalPages` | `number` | - | 総ページ数 |
| `totalItems` | `number` | - | 総アイテム数 |
| `itemsPerPage` | `number` | - | 1ページあたりの件数 |
| `onPageChange` | `(page) => void` | - | ページ変更時の処理 |
| `onItemsPerPageChange` | `(count) => void` | - | 表示件数変更時の処理 |
| `itemsPerPageOptions` | `number[]` | `[10, 20, 50, 100]` | 表示件数オプション |

**機能:**
- ページ番号の省略表示（...）
- 前へ/次へボタン
- 表示件数セレクター
- 総件数表示

### Loading

ローディングコンポーネント。

```tsx
import { Loading } from '@/views/common/Loading';

// 通常のローディング
<Loading message="データを読み込んでいます..." />

// フルスクリーンローディング
<Loading fullScreen size="large" />
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `message` | `string` | `'読み込み中...'` | 表示メッセージ |
| `size` | `'small' \| 'medium' \| 'large'` | `'medium'` | スピナーサイズ |
| `fullScreen` | `boolean` | `false` | 全画面表示 |

### ErrorMessage

エラーメッセージコンポーネント。

```tsx
import { ErrorMessage } from '@/views/common/ErrorMessage';

<ErrorMessage
  message="データの取得に失敗しました"
  onRetry={refetch}
  onDismiss={() => setError(null)}
/>
```

**Props:**

| プロパティ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| `message` | `string` | ○ | エラーメッセージ |
| `onRetry` | `() => void` | - | 再試行ボタンのハンドラー |
| `onDismiss` | `() => void` | - | 閉じるボタンのハンドラー |

### SuccessNotification

成功通知コンポーネント。

```tsx
import { SuccessNotification } from '@/views/common/SuccessNotification';

{showSuccess && (
  <SuccessNotification
    message="保存が完了しました"
    onDismiss={() => setShowSuccess(false)}
    autoHideDuration={3000}
  />
)}
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `message` | `string` | - | 通知メッセージ |
| `onDismiss` | `() => void` | - | 閉じる処理 |
| `autoHideDuration` | `number` | `3000` | 自動非表示までの時間（ms） |

### MoneyDisplay

金額表示コンポーネント。

```tsx
import { MoneyDisplay } from '@/views/common/MoneyDisplay';

// 基本
<MoneyDisplay amount={1234567} />
// 出力: ¥1,234,567

// 符号付き・色付き
<MoneyDisplay amount={-50000} showSign colorize />
// 出力: -¥50,000 (赤色)

// サイズ変更
<MoneyDisplay amount={100000} size="large" />
```

**Props:**

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| `amount` | `number` | - | 金額 |
| `currency` | `string` | `'¥'` | 通貨記号 |
| `showSign` | `boolean` | `false` | 符号を表示 |
| `colorize` | `boolean` | `false` | 正負で色分け |
| `size` | `'small' \| 'medium' \| 'large'` | `'medium'` | フォントサイズ |

## スタイリング規約

### CSS クラス命名規則

BEM（Block Element Modifier）記法を採用しています。

```css
/* Block */
.sidebar { }

/* Element */
.sidebar__item { }
.sidebar__link { }

/* Modifier */
.sidebar__link--active { }

/* State（is- プレフィックス） */
.sidebar__link.is-open { }
.sidebar__link.is-active { }
```

### CSS 変数

共通の CSS 変数を使用してテーマの一貫性を保ちます。

```css
:root {
  /* カラー */
  --color-primary: #1976d2;
  --color-secondary: #424242;
  --color-danger: #d32f2f;
  --color-success: #388e3c;

  /* テキスト */
  --color-text: #333;
  --color-text-secondary: #666;

  /* 背景 */
  --color-background: #f5f5f5;
  --color-surface: #fff;

  /* スペーシング */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  /* ボーダー */
  --border-radius: 4px;
  --border-color: #e0e0e0;
}
```

## レスポンシブ対応

### ブレークポイント

```css
/* モバイル */
@media (max-width: 767px) { }

/* タブレット */
@media (min-width: 768px) and (max-width: 1023px) { }

/* デスクトップ */
@media (min-width: 1024px) { }
```

### モバイル対応のポイント

1. **サイドバー**: スライドイン式、オーバーレイ付き
2. **ヘッダー**: ハンバーガーメニュー表示
3. **テーブル**: 横スクロール対応
4. **モーダル**: 全幅表示

## 使用例

### ページコンポーネントの実装例

```tsx
import React, { useState } from 'react';
import { MainLayout } from '@/views/common/layout/MainLayout';
import { Table, TableColumn } from '@/views/common/Table';
import { Pagination } from '@/views/common/Pagination';
import { Button } from '@/views/common/Button';
import { Loading } from '@/views/common/Loading';
import { ErrorMessage } from '@/views/common/ErrorMessage';
import { ConfirmModal } from '@/views/common/Modal';

interface Journal {
  id: number;
  date: string;
  description: string;
  debitAmount: number;
  creditAmount: number;
}

export const JournalListPage: React.FC = () => {
  const [page, setPage] = useState(1);
  const [deleteTarget, setDeleteTarget] = useState<Journal | null>(null);

  // データ取得（例: TanStack Query 使用）
  const { data, isLoading, error, refetch } = useJournals({ page });

  const breadcrumbs = [
    { label: 'ホーム', path: '/' },
    { label: '仕訳一覧' },
  ];

  const columns: TableColumn<Journal>[] = [
    { key: 'date', header: '日付', width: '120px' },
    { key: 'description', header: '摘要' },
    {
      key: 'debitAmount',
      header: '借方',
      align: 'right',
      render: (value) => <MoneyDisplay amount={Number(value)} />,
    },
    {
      key: 'creditAmount',
      header: '貸方',
      align: 'right',
      render: (value) => <MoneyDisplay amount={Number(value)} />,
    },
    {
      key: 'id',
      header: '操作',
      render: (_, row) => (
        <Button
          variant="danger"
          size="small"
          onClick={(e) => {
            e.stopPropagation();
            setDeleteTarget(row);
          }}
        >
          削除
        </Button>
      ),
    },
  ];

  if (isLoading) {
    return (
      <MainLayout breadcrumbs={breadcrumbs}>
        <Loading message="仕訳データを読み込んでいます..." />
      </MainLayout>
    );
  }

  if (error) {
    return (
      <MainLayout breadcrumbs={breadcrumbs}>
        <ErrorMessage
          message="仕訳データの取得に失敗しました"
          onRetry={refetch}
        />
      </MainLayout>
    );
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <h1>仕訳一覧</h1>

      <Table
        columns={columns}
        data={data.items}
        keyField="id"
        onRowClick={(row) => navigate(`/journals/${row.id}`)}
      />

      <Pagination
        currentPage={page}
        totalPages={data.totalPages}
        totalItems={data.totalItems}
        itemsPerPage={20}
        onPageChange={setPage}
      />

      <ConfirmModal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => {
          handleDelete(deleteTarget!.id);
          setDeleteTarget(null);
        }}
        title="削除確認"
        message={`「${deleteTarget?.description}」を削除しますか？`}
        confirmLabel="削除"
        isDestructive
      />
    </MainLayout>
  );
};
```

## 関連ドキュメント

- [フロントエンド構築手順書](frontend_setup.md)
- [フロントエンドアーキテクチャ](../design/architecture_frontend.md)
- [UI 設計](../design/ui-design.md)
