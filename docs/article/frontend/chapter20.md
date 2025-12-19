# 第20章 決算処理

本章では、決算処理に関するフロントエンド実装を解説する。月次締め処理、年次決算処理、決算整理仕訳の入力など、会計期間の締め処理に必要なコンポーネントを構築していく。

## 20.1 決算処理の概要

### 決算処理のフロー

財務会計システムにおける決算処理は、以下の流れで行われる：

```
日次処理
    ↓
月次締め処理
    ↓（12ヶ月分）
決算整理仕訳
    ↓
年次決算処理
    ↓
繰越処理
    ↓
次期開始
```

### 型定義

```typescript
// types/closing.ts

/** 月次締めステータス */
export type MonthlyClosingStatus =
  | 'open'       // 未締め
  | 'closing'    // 締め処理中
  | 'closed'     // 締め完了
  | 'reopening'; // 解除処理中

/** 年次決算ステータス */
export type YearlyClosingStatus =
  | 'open'              // 未決算
  | 'adjusting'         // 決算整理中
  | 'closing'           // 決算処理中
  | 'closed'            // 決算完了
  | 'carryingForward';  // 繰越処理中

/** 月次締め情報 */
export interface MonthlyClosing {
  id: string;
  fiscalYear: number;
  month: number;
  status: MonthlyClosingStatus;
  closedAt?: string;
  closedBy?: string;
  reopenedAt?: string;
  reopenedBy?: string;
  journalCount: number;
  totalDebit: number;
  totalCredit: number;
}

/** 年次決算情報 */
export interface YearlyClosing {
  id: string;
  fiscalYear: number;
  status: YearlyClosingStatus;
  periodStart: string;
  periodEnd: string;
  closedAt?: string;
  closedBy?: string;
  monthlyClosings: MonthlyClosing[];
  adjustingEntries: ClosingJournalEntry[];
  carryForwardCompleted: boolean;
}

/** 決算整理仕訳種別 */
export type ClosingJournalType =
  | 'depreciation'      // 減価償却
  | 'allowance'         // 引当金
  | 'accrued'           // 経過勘定（未収・未払）
  | 'deferred'          // 経過勘定（前受・前払）
  | 'inventory'         // 棚卸調整
  | 'revaluation'       // 評価替
  | 'taxProvision'      // 法人税等
  | 'other';            // その他

/** 決算整理仕訳 */
export interface ClosingJournalEntry {
  id: string;
  type: ClosingJournalType;
  description: string;
  amount: number;
  journalEntryId?: string;
  status: 'draft' | 'posted' | 'approved';
  createdAt: string;
  createdBy: string;
}

/** 繰越残高 */
export interface CarryForwardBalance {
  accountCode: string;
  accountName: string;
  closingBalance: number;
  openingBalance: number;
  isCarriedForward: boolean;
}
```

## 20.2 API 連携

### OpenAPI 定義

```yaml
# openapi/paths/closing.yaml
/api/closing/monthly:
  get:
    operationId: getMonthlyClosings
    summary: 月次締め一覧取得
    tags:
      - Closing
    parameters:
      - name: fiscalYear
        in: query
        required: true
        schema:
          type: integer
    responses:
      '200':
        description: 月次締め一覧
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/MonthlyClosing'

/api/closing/monthly/{yearMonth}/close:
  post:
    operationId: closeMonth
    summary: 月次締め実行
    tags:
      - Closing
    parameters:
      - name: yearMonth
        in: path
        required: true
        schema:
          type: string
          pattern: '^\d{4}-\d{2}$'
    responses:
      '200':
        description: 締め完了
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MonthlyClosing'

/api/closing/monthly/{yearMonth}/reopen:
  post:
    operationId: reopenMonth
    summary: 月次締め解除
    tags:
      - Closing
    parameters:
      - name: yearMonth
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: 解除完了
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/MonthlyClosing'

/api/closing/yearly/{fiscalYear}:
  get:
    operationId: getYearlyClosing
    summary: 年次決算情報取得
    tags:
      - Closing
    parameters:
      - name: fiscalYear
        in: path
        required: true
        schema:
          type: integer
    responses:
      '200':
        description: 年次決算情報
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/YearlyClosing'

/api/closing/yearly/{fiscalYear}/close:
  post:
    operationId: closeYear
    summary: 年次決算実行
    tags:
      - Closing
    parameters:
      - name: fiscalYear
        in: path
        required: true
        schema:
          type: integer
    responses:
      '200':
        description: 決算完了

/api/closing/yearly/{fiscalYear}/carry-forward:
  post:
    operationId: carryForward
    summary: 繰越処理実行
    tags:
      - Closing
    parameters:
      - name: fiscalYear
        in: path
        required: true
        schema:
          type: integer
    responses:
      '200':
        description: 繰越完了

/api/closing/adjusting-entries:
  get:
    operationId: getAdjustingEntries
    summary: 決算整理仕訳一覧取得
    tags:
      - Closing
    parameters:
      - name: fiscalYear
        in: query
        required: true
        schema:
          type: integer
      - name: type
        in: query
        schema:
          type: string
    responses:
      '200':
        description: 決算整理仕訳一覧
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/ClosingJournalEntry'

  post:
    operationId: createAdjustingEntry
    summary: 決算整理仕訳作成
    tags:
      - Closing
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ClosingJournalEntryRequest'
    responses:
      '201':
        description: 作成完了
```

### Orval 生成フック

```typescript
// generated/api/closing.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type {
  MonthlyClosing,
  YearlyClosing,
  ClosingJournalEntry,
  ClosingJournalEntryRequest,
} from '../model';
import { apiClient } from '../client';

export const getMonthlyClosingsQueryKey = (fiscalYear: number) =>
  ['closing', 'monthly', fiscalYear] as const;

export const useGetMonthlyClosings = (fiscalYear: number) => {
  return useQuery({
    queryKey: getMonthlyClosingsQueryKey(fiscalYear),
    queryFn: async () => {
      const response = await apiClient.get<MonthlyClosing[]>(
        '/api/closing/monthly',
        { params: { fiscalYear } }
      );
      return response.data;
    },
  });
};

export const useCloseMonth = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (yearMonth: string) => {
      const response = await apiClient.post<MonthlyClosing>(
        `/api/closing/monthly/${yearMonth}/close`
      );
      return response.data;
    },
    onSuccess: (_, yearMonth) => {
      const fiscalYear = parseInt(yearMonth.split('-')[0], 10);
      queryClient.invalidateQueries({
        queryKey: getMonthlyClosingsQueryKey(fiscalYear),
      });
    },
  });
};

export const useReopenMonth = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (yearMonth: string) => {
      const response = await apiClient.post<MonthlyClosing>(
        `/api/closing/monthly/${yearMonth}/reopen`
      );
      return response.data;
    },
    onSuccess: (_, yearMonth) => {
      const fiscalYear = parseInt(yearMonth.split('-')[0], 10);
      queryClient.invalidateQueries({
        queryKey: getMonthlyClosingsQueryKey(fiscalYear),
      });
    },
  });
};

export const getYearlyClosingQueryKey = (fiscalYear: number) =>
  ['closing', 'yearly', fiscalYear] as const;

export const useGetYearlyClosing = (fiscalYear: number) => {
  return useQuery({
    queryKey: getYearlyClosingQueryKey(fiscalYear),
    queryFn: async () => {
      const response = await apiClient.get<YearlyClosing>(
        `/api/closing/yearly/${fiscalYear}`
      );
      return response.data;
    },
  });
};

export const useCloseYear = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (fiscalYear: number) => {
      await apiClient.post(`/api/closing/yearly/${fiscalYear}/close`);
    },
    onSuccess: (_, fiscalYear) => {
      queryClient.invalidateQueries({
        queryKey: getYearlyClosingQueryKey(fiscalYear),
      });
    },
  });
};

export const useCarryForward = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (fiscalYear: number) => {
      await apiClient.post(`/api/closing/yearly/${fiscalYear}/carry-forward`);
    },
    onSuccess: (_, fiscalYear) => {
      queryClient.invalidateQueries({
        queryKey: getYearlyClosingQueryKey(fiscalYear),
      });
      queryClient.invalidateQueries({
        queryKey: getYearlyClosingQueryKey(fiscalYear + 1),
      });
    },
  });
};

export const useGetAdjustingEntries = (
  fiscalYear: number,
  type?: string
) => {
  return useQuery({
    queryKey: ['closing', 'adjusting-entries', fiscalYear, type],
    queryFn: async () => {
      const response = await apiClient.get<ClosingJournalEntry[]>(
        '/api/closing/adjusting-entries',
        { params: { fiscalYear, type } }
      );
      return response.data;
    },
  });
};

export const useCreateAdjustingEntry = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: ClosingJournalEntryRequest) => {
      const response = await apiClient.post<ClosingJournalEntry>(
        '/api/closing/adjusting-entries',
        data
      );
      return response.data;
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({
        queryKey: ['closing', 'adjusting-entries'],
      });
    },
  });
};
```

## 20.3 月次締め処理

### MonthlyClosingContainer

```typescript
// containers/MonthlyClosingContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import {
  useGetMonthlyClosings,
  useCloseMonth,
  useReopenMonth,
} from '../generated/api/closing';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { useMessage } from '../contexts/MessageContext';
import { useAuth } from '../contexts/AuthContext';
import { MonthlyClosingView } from '../views/MonthlyClosingView';
import type { MonthlyClosing } from '../types/closing';

export const MonthlyClosingContainer: React.FC = () => {
  const { currentPeriod } = useAccountingPeriod();
  const { showMessage } = useMessage();
  const { user } = useAuth();

  const [fiscalYear, setFiscalYear] = useState(
    currentPeriod?.year || new Date().getFullYear()
  );
  const [confirmModal, setConfirmModal] = useState<{
    type: 'close' | 'reopen';
    month: MonthlyClosing;
  } | null>(null);

  // 月次締め一覧取得
  const { data: closings, isLoading, refetch } = useGetMonthlyClosings(fiscalYear);

  // 締め処理
  const closeMutation = useCloseMonth();
  const reopenMutation = useReopenMonth();

  // 締め可能な月を判定
  const canClose = useCallback((month: MonthlyClosing): boolean => {
    if (month.status !== 'open') return false;

    // 前月が締まっているか確認
    const closingList = closings || [];
    const prevMonth = closingList.find(c =>
      c.month === month.month - 1 ||
      (month.month === 1 && c.month === 12 && c.fiscalYear === month.fiscalYear - 1)
    );

    if (prevMonth && prevMonth.status !== 'closed') {
      return false;
    }

    return true;
  }, [closings]);

  // 解除可能な月を判定
  const canReopen = useCallback((month: MonthlyClosing): boolean => {
    if (month.status !== 'closed') return false;

    // 管理者権限チェック
    if (!user?.roles.includes('ADMIN')) return false;

    // 後続月が締まっていないか確認
    const closingList = closings || [];
    const nextMonth = closingList.find(c =>
      c.month === month.month + 1 ||
      (month.month === 12 && c.month === 1 && c.fiscalYear === month.fiscalYear + 1)
    );

    if (nextMonth && nextMonth.status === 'closed') {
      return false;
    }

    return true;
  }, [closings, user]);

  // 締め実行
  const handleClose = useCallback(async (month: MonthlyClosing) => {
    const yearMonth = `${month.fiscalYear}-${String(month.month).padStart(2, '0')}`;

    try {
      await closeMutation.mutateAsync(yearMonth);
      showMessage('success', `${month.month}月の締め処理が完了しました`);
      setConfirmModal(null);
    } catch (e) {
      showMessage('error', '締め処理に失敗しました');
    }
  }, [closeMutation, showMessage]);

  // 解除実行
  const handleReopen = useCallback(async (month: MonthlyClosing) => {
    const yearMonth = `${month.fiscalYear}-${String(month.month).padStart(2, '0')}`;

    try {
      await reopenMutation.mutateAsync(yearMonth);
      showMessage('success', `${month.month}月の締め解除が完了しました`);
      setConfirmModal(null);
    } catch (e) {
      showMessage('error', '締め解除に失敗しました');
    }
  }, [reopenMutation, showMessage]);

  // 締め状況サマリー
  const summary = useMemo(() => {
    if (!closings) return null;

    const closed = closings.filter(c => c.status === 'closed').length;
    const open = closings.filter(c => c.status === 'open').length;
    const totalJournals = closings.reduce((sum, c) => sum + c.journalCount, 0);

    return { closed, open, totalJournals };
  }, [closings]);

  const handleYearChange = useCallback((year: number) => {
    setFiscalYear(year);
  }, []);

  const handleConfirmClose = useCallback((month: MonthlyClosing) => {
    setConfirmModal({ type: 'close', month });
  }, []);

  const handleConfirmReopen = useCallback((month: MonthlyClosing) => {
    setConfirmModal({ type: 'reopen', month });
  }, []);

  const handleCancelConfirm = useCallback(() => {
    setConfirmModal(null);
  }, []);

  return (
    <MonthlyClosingView
      fiscalYear={fiscalYear}
      closings={closings || []}
      summary={summary}
      isLoading={isLoading}
      isProcessing={closeMutation.isPending || reopenMutation.isPending}
      confirmModal={confirmModal}
      canClose={canClose}
      canReopen={canReopen}
      onYearChange={handleYearChange}
      onConfirmClose={handleConfirmClose}
      onConfirmReopen={handleConfirmReopen}
      onClose={handleClose}
      onReopen={handleReopen}
      onCancelConfirm={handleCancelConfirm}
    />
  );
};
```

### MonthlyClosingView

```typescript
// views/MonthlyClosingView.tsx
import { MonthlyClosingTable } from '../components/MonthlyClosingTable';
import { MonthlyClosingSummary } from '../components/MonthlyClosingSummary';
import { ClosingConfirmModal } from '../components/ClosingConfirmModal';
import { YearSelector } from '../components/common/YearSelector';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { MonthlyClosing } from '../types/closing';

interface MonthlyClosingViewProps {
  fiscalYear: number;
  closings: MonthlyClosing[];
  summary: { closed: number; open: number; totalJournals: number } | null;
  isLoading: boolean;
  isProcessing: boolean;
  confirmModal: { type: 'close' | 'reopen'; month: MonthlyClosing } | null;
  canClose: (month: MonthlyClosing) => boolean;
  canReopen: (month: MonthlyClosing) => boolean;
  onYearChange: (year: number) => void;
  onConfirmClose: (month: MonthlyClosing) => void;
  onConfirmReopen: (month: MonthlyClosing) => void;
  onClose: (month: MonthlyClosing) => void;
  onReopen: (month: MonthlyClosing) => void;
  onCancelConfirm: () => void;
}

export const MonthlyClosingView: React.FC<MonthlyClosingViewProps> = ({
  fiscalYear,
  closings,
  summary,
  isLoading,
  isProcessing,
  confirmModal,
  canClose,
  canReopen,
  onYearChange,
  onConfirmClose,
  onConfirmReopen,
  onClose,
  onReopen,
  onCancelConfirm,
}) => {
  return (
    <div className="monthly-closing-view">
      <header className="page-header">
        <h1>月次締め処理</h1>
        <YearSelector
          value={fiscalYear}
          onChange={onYearChange}
        />
      </header>

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          {/* サマリー表示 */}
          {summary && (
            <MonthlyClosingSummary
              closed={summary.closed}
              open={summary.open}
              totalJournals={summary.totalJournals}
              fiscalYear={fiscalYear}
            />
          )}

          {/* 月別一覧 */}
          <MonthlyClosingTable
            closings={closings}
            canClose={canClose}
            canReopen={canReopen}
            onClose={onConfirmClose}
            onReopen={onConfirmReopen}
          />
        </>
      )}

      {/* 確認モーダル */}
      {confirmModal && (
        <ClosingConfirmModal
          type={confirmModal.type}
          month={confirmModal.month}
          isProcessing={isProcessing}
          onConfirm={() =>
            confirmModal.type === 'close'
              ? onClose(confirmModal.month)
              : onReopen(confirmModal.month)
          }
          onCancel={onCancelConfirm}
        />
      )}
    </div>
  );
};
```

### MonthlyClosingTable コンポーネント

```typescript
// components/MonthlyClosingTable.tsx
import { formatCurrency, formatDateTime } from '../utils/formatUtils';
import type { MonthlyClosing, MonthlyClosingStatus } from '../types/closing';

interface MonthlyClosingTableProps {
  closings: MonthlyClosing[];
  canClose: (month: MonthlyClosing) => boolean;
  canReopen: (month: MonthlyClosing) => boolean;
  onClose: (month: MonthlyClosing) => void;
  onReopen: (month: MonthlyClosing) => void;
}

const STATUS_LABELS: Record<MonthlyClosingStatus, string> = {
  open: '未締め',
  closing: '締め処理中',
  closed: '締め完了',
  reopening: '解除処理中',
};

const STATUS_CLASSES: Record<MonthlyClosingStatus, string> = {
  open: 'status-open',
  closing: 'status-processing',
  closed: 'status-closed',
  reopening: 'status-processing',
};

export const MonthlyClosingTable: React.FC<MonthlyClosingTableProps> = ({
  closings,
  canClose,
  canReopen,
  onClose,
  onReopen,
}) => {
  // 月順にソート
  const sortedClosings = [...closings].sort((a, b) => a.month - b.month);

  return (
    <div className="monthly-closing-table">
      <table>
        <thead>
          <tr>
            <th className="col-month">月</th>
            <th className="col-status">ステータス</th>
            <th className="col-count">仕訳件数</th>
            <th className="col-debit">借方合計</th>
            <th className="col-credit">貸方合計</th>
            <th className="col-date">締め日時</th>
            <th className="col-user">締め担当者</th>
            <th className="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          {sortedClosings.map(closing => (
            <tr key={closing.id} className={STATUS_CLASSES[closing.status]}>
              <td className="col-month">{closing.month}月</td>
              <td className="col-status">
                <span className={`status-badge ${closing.status}`}>
                  {STATUS_LABELS[closing.status]}
                </span>
              </td>
              <td className="col-count">{closing.journalCount.toLocaleString()}</td>
              <td className="col-debit">{formatCurrency(closing.totalDebit)}</td>
              <td className="col-credit">{formatCurrency(closing.totalCredit)}</td>
              <td className="col-date">
                {closing.closedAt ? formatDateTime(closing.closedAt) : '-'}
              </td>
              <td className="col-user">{closing.closedBy || '-'}</td>
              <td className="col-actions">
                {canClose(closing) && (
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => onClose(closing)}
                  >
                    締め実行
                  </button>
                )}
                {canReopen(closing) && (
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => onReopen(closing)}
                  >
                    解除
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

### ClosingConfirmModal コンポーネント

```typescript
// components/ClosingConfirmModal.tsx
import Modal from 'react-modal';
import { formatCurrency } from '../utils/formatUtils';
import type { MonthlyClosing } from '../types/closing';

interface ClosingConfirmModalProps {
  type: 'close' | 'reopen';
  month: MonthlyClosing;
  isProcessing: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export const ClosingConfirmModal: React.FC<ClosingConfirmModalProps> = ({
  type,
  month,
  isProcessing,
  onConfirm,
  onCancel,
}) => {
  const isClose = type === 'close';

  return (
    <Modal
      isOpen
      onRequestClose={onCancel}
      className="confirm-modal"
      overlayClassName="modal-overlay"
    >
      <div className="modal-header">
        <h2>{isClose ? '月次締め確認' : '月次締め解除確認'}</h2>
      </div>

      <div className="modal-body">
        <div className="confirm-target">
          <span className="target-label">対象月:</span>
          <span className="target-value">
            {month.fiscalYear}年{month.month}月
          </span>
        </div>

        {isClose && (
          <div className="confirm-summary">
            <div className="summary-item">
              <span className="label">仕訳件数:</span>
              <span className="value">{month.journalCount.toLocaleString()}件</span>
            </div>
            <div className="summary-item">
              <span className="label">借方合計:</span>
              <span className="value">{formatCurrency(month.totalDebit)}</span>
            </div>
            <div className="summary-item">
              <span className="label">貸方合計:</span>
              <span className="value">{formatCurrency(month.totalCredit)}</span>
            </div>
          </div>
        )}

        <div className="confirm-message">
          {isClose ? (
            <p>
              この月の締め処理を実行すると、仕訳の追加・変更・削除ができなくなります。
              よろしいですか？
            </p>
          ) : (
            <>
              <p className="warning">
                この月の締めを解除すると、仕訳の追加・変更・削除が可能になります。
              </p>
              <p>
                締め解除は監査証跡に記録されます。
                本当に解除してよろしいですか？
              </p>
            </>
          )}
        </div>
      </div>

      <div className="modal-footer">
        <button
          className="btn btn-secondary"
          onClick={onCancel}
          disabled={isProcessing}
        >
          キャンセル
        </button>
        <button
          className={`btn ${isClose ? 'btn-primary' : 'btn-danger'}`}
          onClick={onConfirm}
          disabled={isProcessing}
        >
          {isProcessing
            ? (isClose ? '締め処理中...' : '解除中...')
            : (isClose ? '締め実行' : '解除実行')
          }
        </button>
      </div>
    </Modal>
  );
};
```

## 20.4 年次決算処理

### YearEndClosingContainer

```typescript
// containers/YearEndClosingContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import {
  useGetYearlyClosing,
  useCloseYear,
  useCarryForward,
} from '../generated/api/closing';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { useMessage } from '../contexts/MessageContext';
import { YearEndClosingView } from '../views/YearEndClosingView';
import type { YearlyClosingStatus } from '../types/closing';

export const YearEndClosingContainer: React.FC = () => {
  const { currentPeriod } = useAccountingPeriod();
  const { showMessage } = useMessage();

  const [fiscalYear, setFiscalYear] = useState(
    currentPeriod?.year || new Date().getFullYear()
  );
  const [activeStep, setActiveStep] = useState<number>(0);
  const [confirmAction, setConfirmAction] = useState<
    'close' | 'carryForward' | null
  >(null);

  // 年次決算情報取得
  const { data: yearlyClosing, isLoading, refetch } = useGetYearlyClosing(fiscalYear);

  // 処理実行
  const closeYearMutation = useCloseYear();
  const carryForwardMutation = useCarryForward();

  // 現在のステップを判定
  const currentStep = useMemo(() => {
    if (!yearlyClosing) return 0;

    const statusSteps: Record<YearlyClosingStatus, number> = {
      open: 0,
      adjusting: 1,
      closing: 2,
      closed: 3,
      carryingForward: 3,
    };

    return statusSteps[yearlyClosing.status];
  }, [yearlyClosing]);

  // 全月締め完了チェック
  const allMonthsClosed = useMemo(() => {
    if (!yearlyClosing) return false;
    return yearlyClosing.monthlyClosings.every(m => m.status === 'closed');
  }, [yearlyClosing]);

  // 決算整理仕訳完了チェック
  const allAdjustingEntriesApproved = useMemo(() => {
    if (!yearlyClosing) return false;
    return yearlyClosing.adjustingEntries.every(e => e.status === 'approved');
  }, [yearlyClosing]);

  // 年次決算実行
  const handleCloseYear = useCallback(async () => {
    try {
      await closeYearMutation.mutateAsync(fiscalYear);
      showMessage('success', `${fiscalYear}年度の決算処理が完了しました`);
      setConfirmAction(null);
    } catch (e) {
      showMessage('error', '決算処理に失敗しました');
    }
  }, [fiscalYear, closeYearMutation, showMessage]);

  // 繰越処理実行
  const handleCarryForward = useCallback(async () => {
    try {
      await carryForwardMutation.mutateAsync(fiscalYear);
      showMessage('success', '繰越処理が完了しました');
      setConfirmAction(null);
    } catch (e) {
      showMessage('error', '繰越処理に失敗しました');
    }
  }, [fiscalYear, carryForwardMutation, showMessage]);

  const handleYearChange = useCallback((year: number) => {
    setFiscalYear(year);
    setActiveStep(0);
  }, []);

  const handleStepClick = useCallback((step: number) => {
    if (step <= currentStep) {
      setActiveStep(step);
    }
  }, [currentStep]);

  return (
    <YearEndClosingView
      fiscalYear={fiscalYear}
      yearlyClosing={yearlyClosing}
      isLoading={isLoading}
      isProcessing={closeYearMutation.isPending || carryForwardMutation.isPending}
      currentStep={currentStep}
      activeStep={activeStep}
      allMonthsClosed={allMonthsClosed}
      allAdjustingEntriesApproved={allAdjustingEntriesApproved}
      confirmAction={confirmAction}
      onYearChange={handleYearChange}
      onStepClick={handleStepClick}
      onConfirmCloseYear={() => setConfirmAction('close')}
      onConfirmCarryForward={() => setConfirmAction('carryForward')}
      onCloseYear={handleCloseYear}
      onCarryForward={handleCarryForward}
      onCancelConfirm={() => setConfirmAction(null)}
    />
  );
};
```

### YearEndClosingView

```typescript
// views/YearEndClosingView.tsx
import { ClosingStepIndicator } from '../components/ClosingStepIndicator';
import { MonthlyClosingStatusPanel } from '../components/MonthlyClosingStatusPanel';
import { AdjustingEntriesPanel } from '../components/AdjustingEntriesPanel';
import { ClosingExecutionPanel } from '../components/ClosingExecutionPanel';
import { CarryForwardPanel } from '../components/CarryForwardPanel';
import { YearSelector } from '../components/common/YearSelector';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { YearlyClosing } from '../types/closing';

interface YearEndClosingViewProps {
  fiscalYear: number;
  yearlyClosing?: YearlyClosing;
  isLoading: boolean;
  isProcessing: boolean;
  currentStep: number;
  activeStep: number;
  allMonthsClosed: boolean;
  allAdjustingEntriesApproved: boolean;
  confirmAction: 'close' | 'carryForward' | null;
  onYearChange: (year: number) => void;
  onStepClick: (step: number) => void;
  onConfirmCloseYear: () => void;
  onConfirmCarryForward: () => void;
  onCloseYear: () => void;
  onCarryForward: () => void;
  onCancelConfirm: () => void;
}

const STEPS = [
  { label: '月次締め', description: '12ヶ月分の月次締め完了' },
  { label: '決算整理', description: '決算整理仕訳の入力・承認' },
  { label: '決算確定', description: '年次決算の実行' },
  { label: '繰越処理', description: '次期への残高繰越' },
];

export const YearEndClosingView: React.FC<YearEndClosingViewProps> = ({
  fiscalYear,
  yearlyClosing,
  isLoading,
  isProcessing,
  currentStep,
  activeStep,
  allMonthsClosed,
  allAdjustingEntriesApproved,
  confirmAction,
  onYearChange,
  onStepClick,
  onConfirmCloseYear,
  onConfirmCarryForward,
  onCloseYear,
  onCarryForward,
  onCancelConfirm,
}) => {
  return (
    <div className="year-end-closing-view">
      <header className="page-header">
        <h1>年次決算処理</h1>
        <YearSelector value={fiscalYear} onChange={onYearChange} />
      </header>

      {isLoading ? (
        <LoadingSpinner />
      ) : yearlyClosing ? (
        <>
          {/* ステップインジケーター */}
          <ClosingStepIndicator
            steps={STEPS}
            currentStep={currentStep}
            activeStep={activeStep}
            onStepClick={onStepClick}
          />

          {/* ステップ別パネル */}
          <div className="step-content">
            {activeStep === 0 && (
              <MonthlyClosingStatusPanel
                monthlyClosings={yearlyClosing.monthlyClosings}
                allClosed={allMonthsClosed}
                fiscalYear={fiscalYear}
              />
            )}

            {activeStep === 1 && (
              <AdjustingEntriesPanel
                adjustingEntries={yearlyClosing.adjustingEntries}
                allApproved={allAdjustingEntriesApproved}
                fiscalYear={fiscalYear}
              />
            )}

            {activeStep === 2 && (
              <ClosingExecutionPanel
                yearlyClosing={yearlyClosing}
                canClose={allMonthsClosed && allAdjustingEntriesApproved}
                isProcessing={isProcessing}
                onClose={onConfirmCloseYear}
              />
            )}

            {activeStep === 3 && (
              <CarryForwardPanel
                yearlyClosing={yearlyClosing}
                canCarryForward={
                  yearlyClosing.status === 'closed' &&
                  !yearlyClosing.carryForwardCompleted
                }
                isProcessing={isProcessing}
                onCarryForward={onConfirmCarryForward}
              />
            )}
          </div>
        </>
      ) : (
        <div className="empty-state">
          <p>年次決算情報が見つかりません</p>
        </div>
      )}

      {/* 確認モーダル */}
      {confirmAction && (
        <YearEndConfirmModal
          action={confirmAction}
          fiscalYear={fiscalYear}
          isProcessing={isProcessing}
          onConfirm={confirmAction === 'close' ? onCloseYear : onCarryForward}
          onCancel={onCancelConfirm}
        />
      )}
    </div>
  );
};
```

### ClosingStepIndicator コンポーネント

```typescript
// components/ClosingStepIndicator.tsx
interface Step {
  label: string;
  description: string;
}

interface ClosingStepIndicatorProps {
  steps: Step[];
  currentStep: number;
  activeStep: number;
  onStepClick: (step: number) => void;
}

export const ClosingStepIndicator: React.FC<ClosingStepIndicatorProps> = ({
  steps,
  currentStep,
  activeStep,
  onStepClick,
}) => {
  return (
    <div className="closing-step-indicator">
      {steps.map((step, index) => {
        const isCompleted = index < currentStep;
        const isCurrent = index === currentStep;
        const isActive = index === activeStep;
        const isClickable = index <= currentStep;

        return (
          <div
            key={index}
            className={`step-item ${isCompleted ? 'completed' : ''} ${isCurrent ? 'current' : ''} ${isActive ? 'active' : ''}`}
          >
            <button
              className="step-button"
              onClick={() => onStepClick(index)}
              disabled={!isClickable}
            >
              <div className="step-number">
                {isCompleted ? (
                  <span className="check-icon">✓</span>
                ) : (
                  index + 1
                )}
              </div>
              <div className="step-content">
                <div className="step-label">{step.label}</div>
                <div className="step-description">{step.description}</div>
              </div>
            </button>
            {index < steps.length - 1 && (
              <div className={`step-connector ${isCompleted ? 'completed' : ''}`} />
            )}
          </div>
        );
      })}
    </div>
  );
};
```

## 20.5 決算整理仕訳

### ClosingJournalContainer

```typescript
// containers/ClosingJournalContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import {
  useGetAdjustingEntries,
  useCreateAdjustingEntry,
} from '../generated/api/closing';
import { useMessage } from '../contexts/MessageContext';
import { ClosingJournalView } from '../views/ClosingJournalView';
import type { ClosingJournalType, ClosingJournalEntry } from '../types/closing';

interface ClosingJournalContainerProps {
  fiscalYear: number;
}

export const ClosingJournalContainer: React.FC<ClosingJournalContainerProps> = ({
  fiscalYear,
}) => {
  const { showMessage } = useMessage();

  const [selectedType, setSelectedType] = useState<ClosingJournalType | 'all'>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState<ClosingJournalEntry | null>(null);

  // 決算整理仕訳一覧取得
  const { data: entries, isLoading } = useGetAdjustingEntries(
    fiscalYear,
    selectedType === 'all' ? undefined : selectedType
  );

  const createMutation = useCreateAdjustingEntry();

  // 種別ごとの件数集計
  const typeCounts = useMemo(() => {
    if (!entries) return {};

    return entries.reduce<Record<string, number>>((acc, entry) => {
      acc[entry.type] = (acc[entry.type] || 0) + 1;
      return acc;
    }, {});
  }, [entries]);

  // 種別フィルタ
  const handleTypeChange = useCallback((type: ClosingJournalType | 'all') => {
    setSelectedType(type);
  }, []);

  // 新規作成
  const handleCreate = useCallback(async (data: any) => {
    try {
      await createMutation.mutateAsync({
        ...data,
        fiscalYear,
      });
      showMessage('success', '決算整理仕訳を作成しました');
      setShowCreateModal(false);
    } catch (e) {
      showMessage('error', '作成に失敗しました');
    }
  }, [fiscalYear, createMutation, showMessage]);

  // 詳細表示
  const handleViewDetail = useCallback((entry: ClosingJournalEntry) => {
    setSelectedEntry(entry);
  }, []);

  return (
    <ClosingJournalView
      entries={entries || []}
      isLoading={isLoading}
      selectedType={selectedType}
      typeCounts={typeCounts}
      showCreateModal={showCreateModal}
      selectedEntry={selectedEntry}
      isCreating={createMutation.isPending}
      onTypeChange={handleTypeChange}
      onOpenCreateModal={() => setShowCreateModal(true)}
      onCloseCreateModal={() => setShowCreateModal(false)}
      onCreate={handleCreate}
      onViewDetail={handleViewDetail}
      onCloseDetail={() => setSelectedEntry(null)}
    />
  );
};
```

### ClosingJournalTypeSelector コンポーネント

```typescript
// components/ClosingJournalTypeSelector.tsx
import type { ClosingJournalType } from '../types/closing';

interface ClosingJournalTypeSelectorProps {
  selectedType: ClosingJournalType | 'all';
  typeCounts: Record<string, number>;
  onChange: (type: ClosingJournalType | 'all') => void;
}

const TYPE_LABELS: Record<ClosingJournalType | 'all', string> = {
  all: 'すべて',
  depreciation: '減価償却',
  allowance: '引当金',
  accrued: '未収・未払',
  deferred: '前受・前払',
  inventory: '棚卸調整',
  revaluation: '評価替',
  taxProvision: '法人税等',
  other: 'その他',
};

const TYPE_ICONS: Record<ClosingJournalType, string> = {
  depreciation: '📉',
  allowance: '🛡️',
  accrued: '📅',
  deferred: '⏳',
  inventory: '📦',
  revaluation: '📊',
  taxProvision: '🏛️',
  other: '📝',
};

export const ClosingJournalTypeSelector: React.FC<ClosingJournalTypeSelectorProps> = ({
  selectedType,
  typeCounts,
  onChange,
}) => {
  const types: (ClosingJournalType | 'all')[] = [
    'all',
    'depreciation',
    'allowance',
    'accrued',
    'deferred',
    'inventory',
    'revaluation',
    'taxProvision',
    'other',
  ];

  return (
    <div className="closing-journal-type-selector">
      {types.map(type => {
        const count = type === 'all'
          ? Object.values(typeCounts).reduce((a, b) => a + b, 0)
          : typeCounts[type] || 0;

        return (
          <button
            key={type}
            className={`type-button ${selectedType === type ? 'active' : ''}`}
            onClick={() => onChange(type)}
          >
            {type !== 'all' && (
              <span className="type-icon">{TYPE_ICONS[type as ClosingJournalType]}</span>
            )}
            <span className="type-label">{TYPE_LABELS[type]}</span>
            <span className="type-count">{count}</span>
          </button>
        );
      })}
    </div>
  );
};
```

### DepreciationEntryForm コンポーネント

```typescript
// components/DepreciationEntryForm.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetFixedAssets } from '../generated/api/fixed-asset';
import { MoneyInput } from './common/MoneyInput';
import { AccountSelector } from './common/AccountSelector';
import { formatCurrency } from '../utils/formatUtils';

interface DepreciationEntryFormProps {
  fiscalYear: number;
  onSubmit: (data: DepreciationEntryData) => void;
  onCancel: () => void;
  isSubmitting: boolean;
}

interface DepreciationEntryData {
  type: 'depreciation';
  description: string;
  fixedAssetId: string;
  depreciationAmount: number;
  expenseAccountCode: string;
  accumulatedAccountCode: string;
}

export const DepreciationEntryForm: React.FC<DepreciationEntryFormProps> = ({
  fiscalYear,
  onSubmit,
  onCancel,
  isSubmitting,
}) => {
  const [selectedAssetId, setSelectedAssetId] = useState<string>('');
  const [depreciationAmount, setDepreciationAmount] = useState<number>(0);
  const [expenseAccountCode, setExpenseAccountCode] = useState<string>('');
  const [accumulatedAccountCode, setAccumulatedAccountCode] = useState<string>('');
  const [description, setDescription] = useState<string>('');

  // 固定資産一覧取得
  const { data: assets } = useGetFixedAssets({ fiscalYear });

  // 選択された資産
  const selectedAsset = useMemo(() => {
    return assets?.find(a => a.id === selectedAssetId);
  }, [assets, selectedAssetId]);

  // 資産選択時に自動設定
  const handleAssetChange = useCallback((assetId: string) => {
    setSelectedAssetId(assetId);
    const asset = assets?.find(a => a.id === assetId);
    if (asset) {
      setDepreciationAmount(asset.calculatedDepreciation);
      setExpenseAccountCode(asset.expenseAccountCode);
      setAccumulatedAccountCode(asset.accumulatedAccountCode);
      setDescription(`${asset.name} 減価償却費`);
    }
  }, [assets]);

  // 送信
  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      type: 'depreciation',
      description,
      fixedAssetId: selectedAssetId,
      depreciationAmount,
      expenseAccountCode,
      accumulatedAccountCode,
    });
  }, [
    description,
    selectedAssetId,
    depreciationAmount,
    expenseAccountCode,
    accumulatedAccountCode,
    onSubmit,
  ]);

  return (
    <form className="depreciation-entry-form" onSubmit={handleSubmit}>
      <div className="form-section">
        <h3>減価償却計上</h3>

        <div className="form-group">
          <label>固定資産</label>
          <select
            value={selectedAssetId}
            onChange={(e) => handleAssetChange(e.target.value)}
            required
          >
            <option value="">選択してください</option>
            {assets?.map(asset => (
              <option key={asset.id} value={asset.id}>
                {asset.name} ({formatCurrency(asset.acquisitionCost)})
              </option>
            ))}
          </select>
        </div>

        {selectedAsset && (
          <div className="asset-info">
            <div className="info-row">
              <span className="label">取得価額:</span>
              <span className="value">{formatCurrency(selectedAsset.acquisitionCost)}</span>
            </div>
            <div className="info-row">
              <span className="label">期首簿価:</span>
              <span className="value">{formatCurrency(selectedAsset.bookValue)}</span>
            </div>
            <div className="info-row">
              <span className="label">償却方法:</span>
              <span className="value">{selectedAsset.depreciationMethod}</span>
            </div>
            <div className="info-row">
              <span className="label">耐用年数:</span>
              <span className="value">{selectedAsset.usefulLife}年</span>
            </div>
          </div>
        )}

        <div className="form-group">
          <label>償却額</label>
          <MoneyInput
            value={depreciationAmount}
            onChange={setDepreciationAmount}
            required
          />
        </div>

        <div className="form-group">
          <label>費用科目</label>
          <AccountSelector
            value={expenseAccountCode}
            onChange={setExpenseAccountCode}
            filter={{ elementType: '費用' }}
            required
          />
        </div>

        <div className="form-group">
          <label>減価償却累計額科目</label>
          <AccountSelector
            value={accumulatedAccountCode}
            onChange={setAccumulatedAccountCode}
            filter={{ accountType: 'accumulated_depreciation' }}
            required
          />
        </div>

        <div className="form-group">
          <label>摘要</label>
          <input
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />
        </div>
      </div>

      <div className="journal-preview">
        <h4>仕訳プレビュー</h4>
        <table className="preview-table">
          <thead>
            <tr>
              <th>借方科目</th>
              <th>借方金額</th>
              <th>貸方科目</th>
              <th>貸方金額</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{expenseAccountCode || '-'}</td>
              <td>{formatCurrency(depreciationAmount)}</td>
              <td>{accumulatedAccountCode || '-'}</td>
              <td>{formatCurrency(depreciationAmount)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="form-actions">
        <button
          type="button"
          className="btn btn-secondary"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          キャンセル
        </button>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={isSubmitting || !selectedAssetId}
        >
          {isSubmitting ? '作成中...' : '作成'}
        </button>
      </div>
    </form>
  );
};
```

## 20.6 繰越処理

### CarryForwardPanel コンポーネント

```typescript
// components/CarryForwardPanel.tsx
import { useMemo } from 'react';
import { useGetCarryForwardBalances } from '../generated/api/closing';
import { formatCurrency } from '../utils/formatUtils';
import type { YearlyClosing, CarryForwardBalance } from '../types/closing';

interface CarryForwardPanelProps {
  yearlyClosing: YearlyClosing;
  canCarryForward: boolean;
  isProcessing: boolean;
  onCarryForward: () => void;
}

export const CarryForwardPanel: React.FC<CarryForwardPanelProps> = ({
  yearlyClosing,
  canCarryForward,
  isProcessing,
  onCarryForward,
}) => {
  // 繰越残高一覧取得
  const { data: balances } = useGetCarryForwardBalances(yearlyClosing.fiscalYear);

  // B/S 科目のみ抽出（P/L 科目は繰越不要）
  const bsBalances = useMemo(() => {
    if (!balances) return [];
    return balances.filter(b => b.closingBalance !== 0);
  }, [balances]);

  // 資産・負債・純資産でグループ化
  const groupedBalances = useMemo(() => {
    const groups: Record<string, CarryForwardBalance[]> = {
      assets: [],
      liabilities: [],
      equity: [],
    };

    bsBalances.forEach(balance => {
      if (balance.accountCode.startsWith('1')) {
        groups.assets.push(balance);
      } else if (balance.accountCode.startsWith('2')) {
        groups.liabilities.push(balance);
      } else if (balance.accountCode.startsWith('3')) {
        groups.equity.push(balance);
      }
    });

    return groups;
  }, [bsBalances]);

  // 繰越完了チェック
  const allCarriedForward = bsBalances.every(b => b.isCarriedForward);

  return (
    <div className="carry-forward-panel">
      <div className="panel-header">
        <h2>繰越処理</h2>
        <p className="description">
          当期の残高を次期の期首残高として繰り越します。
        </p>
      </div>

      {yearlyClosing.carryForwardCompleted ? (
        <div className="status-complete">
          <span className="check-icon">✓</span>
          繰越処理は完了しています
        </div>
      ) : (
        <>
          {/* 繰越残高プレビュー */}
          <div className="carry-forward-preview">
            <h3>繰越残高一覧</h3>

            {/* 資産 */}
            <BalanceGroup
              title="資産"
              balances={groupedBalances.assets}
            />

            {/* 負債 */}
            <BalanceGroup
              title="負債"
              balances={groupedBalances.liabilities}
            />

            {/* 純資産 */}
            <BalanceGroup
              title="純資産"
              balances={groupedBalances.equity}
            />
          </div>

          {/* 実行ボタン */}
          <div className="panel-actions">
            <button
              className="btn btn-primary btn-lg"
              onClick={onCarryForward}
              disabled={!canCarryForward || isProcessing}
            >
              {isProcessing ? '繰越処理中...' : '繰越処理を実行'}
            </button>

            {!canCarryForward && yearlyClosing.status !== 'closed' && (
              <p className="warning-message">
                年次決算が完了していないため、繰越処理を実行できません。
              </p>
            )}
          </div>
        </>
      )}
    </div>
  );
};

// 残高グループ表示
interface BalanceGroupProps {
  title: string;
  balances: CarryForwardBalance[];
}

const BalanceGroup: React.FC<BalanceGroupProps> = ({ title, balances }) => {
  const total = balances.reduce((sum, b) => sum + b.closingBalance, 0);

  if (balances.length === 0) return null;

  return (
    <div className="balance-group">
      <h4>{title}</h4>
      <table className="balance-table">
        <thead>
          <tr>
            <th>科目コード</th>
            <th>科目名</th>
            <th>当期末残高</th>
            <th>次期首残高</th>
          </tr>
        </thead>
        <tbody>
          {balances.map(balance => (
            <tr key={balance.accountCode}>
              <td>{balance.accountCode}</td>
              <td>{balance.accountName}</td>
              <td className="amount">{formatCurrency(balance.closingBalance)}</td>
              <td className="amount">{formatCurrency(balance.openingBalance)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={2}>合計</td>
            <td className="amount">{formatCurrency(total)}</td>
            <td className="amount">{formatCurrency(total)}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
};
```

## 20.7 スタイリング

```css
/* styles/closing.css */

/* 月次締め処理 */
.monthly-closing-view {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

/* サマリー */
.monthly-closing-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.summary-card .value {
  font-size: 32px;
  font-weight: 600;
  color: #111827;
}

.summary-card .label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

/* 月別テーブル */
.monthly-closing-table table {
  width: 100%;
  border-collapse: collapse;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.monthly-closing-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.monthly-closing-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
}

.monthly-closing-table tr.status-closed {
  background: #f0fdf4;
}

.monthly-closing-table tr.status-processing {
  background: #fef3c7;
}

/* ステータスバッジ */
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.open {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.closing,
.status-badge.reopening {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.closed {
  background: #d1fae5;
  color: #059669;
}

/* ステップインジケーター */
.closing-step-indicator {
  display: flex;
  align-items: flex-start;
  margin-bottom: 32px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.step-item {
  flex: 1;
  display: flex;
  align-items: flex-start;
}

.step-button {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
  padding: 0;
}

.step-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f3f4f6;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.step-item.completed .step-number {
  background: #10b981;
  color: #ffffff;
}

.step-item.current .step-number {
  background: #3b82f6;
  color: #ffffff;
}

.step-item.active .step-number {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3);
}

.step-label {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.step-description {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.step-connector {
  flex: 1;
  height: 2px;
  background: #e5e7eb;
  margin: 15px 16px 0;
}

.step-connector.completed {
  background: #10b981;
}

/* 決算整理仕訳種別セレクター */
.closing-journal-type-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.type-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.15s;
}

.type-button:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.type-button.active {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #1d4ed8;
}

.type-icon {
  font-size: 16px;
}

.type-label {
  font-size: 14px;
  font-weight: 500;
}

.type-count {
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  color: #6b7280;
}

.type-button.active .type-count {
  background: #dbeafe;
  color: #1d4ed8;
}

/* 減価償却フォーム */
.depreciation-entry-form {
  max-width: 600px;
}

.asset-info {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.asset-info .info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.asset-info .label {
  color: #6b7280;
  font-size: 13px;
}

.asset-info .value {
  font-weight: 500;
}

/* 仕訳プレビュー */
.journal-preview {
  background: #eff6ff;
  border-radius: 8px;
  padding: 16px;
  margin: 24px 0;
}

.journal-preview h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #1d4ed8;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
}

.preview-table th,
.preview-table td {
  padding: 8px 12px;
  text-align: left;
  border: 1px solid #bfdbfe;
}

.preview-table th {
  background: #dbeafe;
  font-size: 12px;
}

/* 繰越パネル */
.carry-forward-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
}

.panel-header h2 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.panel-header .description {
  color: #6b7280;
  margin: 0;
}

.status-complete {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #d1fae5;
  border-radius: 8px;
  color: #059669;
  font-weight: 500;
  margin-top: 24px;
}

.carry-forward-preview {
  margin-top: 24px;
}

.balance-group {
  margin-bottom: 24px;
}

.balance-group h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #374151;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.balance-table {
  width: 100%;
  border-collapse: collapse;
}

.balance-table th,
.balance-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #f3f4f6;
}

.balance-table th {
  background: #f9fafb;
  font-size: 12px;
  font-weight: 600;
}

.balance-table .amount {
  text-align: right;
  font-family: 'SF Mono', monospace;
}

.balance-table tfoot td {
  font-weight: 600;
  background: #f9fafb;
}

.panel-actions {
  margin-top: 24px;
  text-align: center;
}

.warning-message {
  color: #dc2626;
  font-size: 13px;
  margin-top: 12px;
}

/* 確認モーダル */
.confirm-modal {
  max-width: 480px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
}

.confirm-target {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.target-label {
  color: #6b7280;
}

.target-value {
  font-size: 18px;
  font-weight: 600;
}

.confirm-summary {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 16px;
}

.confirm-summary .summary-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.confirm-message p {
  margin: 12px 0;
  line-height: 1.6;
}

.confirm-message .warning {
  color: #dc2626;
  font-weight: 500;
}
```

## 20.8 まとめ

本章では、決算処理に関するフロントエンド実装を解説した。主なポイントは以下の通りである：

1. **月次締め処理**: 月ごとの締め・解除操作、締め順序の制御、権限による制限
2. **年次決算処理**: ステップインジケーターによる進捗管理、4段階のワークフロー
3. **決算整理仕訳**: 減価償却・引当金など種別ごとの入力フォーム、仕訳プレビュー
4. **繰越処理**: B/S 科目の期首残高への繰越、残高確認プレビュー
5. **確認モーダル**: 重要な操作の確認、処理中状態の表示

決算処理は会計システムの核心的な機能であり、データの整合性と操作の安全性が特に重要である。適切な権限チェックと確認フローにより、誤操作を防止している。次章では、ダウンロード・出力機能について解説する。
