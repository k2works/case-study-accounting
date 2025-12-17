# 第22章 監査・履歴機能

本章では、システムの監査証跡と履歴管理機能を実装する。操作履歴の表示、仕訳変更履歴の差分表示、ログイン履歴の追跡など、内部統制とコンプライアンスに必要なコンポーネントを構築していく。

## 22.1 監査機能の概要

### 監査ログの重要性

財務会計システムにおいて、監査ログは以下の目的で必要とされる：

- **内部統制**: 不正アクセスや操作の検知
- **コンプライアンス**: 法的要件への対応（J-SOX など）
- **トレーサビリティ**: 問題発生時の原因追跡
- **説明責任**: 操作の正当性の証明

### 型定義

```typescript
// types/audit.ts

/** 操作種別 */
export type OperationType =
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'APPROVE'
  | 'REJECT'
  | 'CLOSE'
  | 'REOPEN'
  | 'EXPORT'
  | 'LOGIN'
  | 'LOGOUT';

/** 対象エンティティ */
export type EntityType =
  | 'JOURNAL_ENTRY'
  | 'ACCOUNT'
  | 'ACCOUNTING_PERIOD'
  | 'TAX_TYPE'
  | 'USER'
  | 'MONTHLY_CLOSING'
  | 'YEARLY_CLOSING';

/** 監査ログエントリ */
export interface AuditLogEntry {
  id: string;
  timestamp: string;
  userId: string;
  userName: string;
  ipAddress: string;
  userAgent: string;
  operationType: OperationType;
  entityType: EntityType;
  entityId: string;
  entityDescription?: string;
  previousValue?: Record<string, any>;
  newValue?: Record<string, any>;
  additionalInfo?: Record<string, any>;
}

/** 監査ログ検索条件 */
export interface AuditLogSearchParams {
  startDate?: string;
  endDate?: string;
  userId?: string;
  operationType?: OperationType;
  entityType?: EntityType;
  entityId?: string;
  keyword?: string;
  page?: number;
  pageSize?: number;
}

/** 仕訳変更履歴 */
export interface JournalChangeHistory {
  id: string;
  journalEntryId: string;
  version: number;
  changedAt: string;
  changedBy: string;
  changeType: 'CREATE' | 'UPDATE' | 'DELETE';
  previousData?: JournalEntrySnapshot;
  currentData?: JournalEntrySnapshot;
  changes: FieldChange[];
}

/** 仕訳スナップショット */
export interface JournalEntrySnapshot {
  journalDate: string;
  description: string;
  details: JournalDetailSnapshot[];
  status: string;
}

/** 仕訳明細スナップショット */
export interface JournalDetailSnapshot {
  accountCode: string;
  accountName: string;
  debitAmount: number;
  creditAmount: number;
  description?: string;
}

/** フィールド変更 */
export interface FieldChange {
  field: string;
  fieldLabel: string;
  previousValue: any;
  newValue: any;
}

/** ログイン履歴 */
export interface LoginHistory {
  id: string;
  userId: string;
  userName: string;
  eventType: 'LOGIN' | 'LOGOUT' | 'LOGIN_FAILED';
  timestamp: string;
  ipAddress: string;
  userAgent: string;
  location?: string;
  failureReason?: string;
}
```

## 22.2 API 連携

### OpenAPI 定義

```yaml
# openapi/paths/audit.yaml
/api/audit/logs:
  get:
    operationId: getAuditLogs
    summary: 監査ログ一覧取得
    tags:
      - Audit
    parameters:
      - name: startDate
        in: query
        schema:
          type: string
          format: date
      - name: endDate
        in: query
        schema:
          type: string
          format: date
      - name: userId
        in: query
        schema:
          type: string
      - name: operationType
        in: query
        schema:
          type: string
      - name: entityType
        in: query
        schema:
          type: string
      - name: entityId
        in: query
        schema:
          type: string
      - name: keyword
        in: query
        schema:
          type: string
      - name: page
        in: query
        schema:
          type: integer
          default: 0
      - name: pageSize
        in: query
        schema:
          type: integer
          default: 50
    responses:
      '200':
        description: 監査ログ一覧
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuditLogResponse'

/api/audit/logs/{id}:
  get:
    operationId: getAuditLogDetail
    summary: 監査ログ詳細取得
    tags:
      - Audit
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: 監査ログ詳細
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuditLogEntry'

/api/audit/journal-history/{journalEntryId}:
  get:
    operationId: getJournalHistory
    summary: 仕訳変更履歴取得
    tags:
      - Audit
    parameters:
      - name: journalEntryId
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: 仕訳変更履歴
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/JournalChangeHistory'

/api/audit/login-history:
  get:
    operationId: getLoginHistory
    summary: ログイン履歴取得
    tags:
      - Audit
    parameters:
      - name: userId
        in: query
        schema:
          type: string
      - name: startDate
        in: query
        schema:
          type: string
          format: date
      - name: endDate
        in: query
        schema:
          type: string
          format: date
      - name: page
        in: query
        schema:
          type: integer
          default: 0
      - name: pageSize
        in: query
        schema:
          type: integer
          default: 50
    responses:
      '200':
        description: ログイン履歴
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginHistoryResponse'
```

### Orval 生成フック

```typescript
// generated/api/audit.ts
import { useQuery } from '@tanstack/react-query';
import type {
  AuditLogResponse,
  AuditLogEntry,
  JournalChangeHistory,
  LoginHistoryResponse,
  GetAuditLogsParams,
  GetLoginHistoryParams,
} from '../model';
import { apiClient } from '../client';

export const getAuditLogsQueryKey = (params: GetAuditLogsParams) =>
  ['audit', 'logs', params] as const;

export const useGetAuditLogs = (params: GetAuditLogsParams) => {
  return useQuery({
    queryKey: getAuditLogsQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<AuditLogResponse>(
        '/api/audit/logs',
        { params }
      );
      return response.data;
    },
    keepPreviousData: true,
  });
};

export const getAuditLogDetailQueryKey = (id: string) =>
  ['audit', 'logs', id] as const;

export const useGetAuditLogDetail = (
  id: string,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getAuditLogDetailQueryKey(id),
    queryFn: async () => {
      const response = await apiClient.get<AuditLogEntry>(
        `/api/audit/logs/${id}`
      );
      return response.data;
    },
    ...options,
  });
};

export const getJournalHistoryQueryKey = (journalEntryId: string) =>
  ['audit', 'journal-history', journalEntryId] as const;

export const useGetJournalHistory = (journalEntryId: string) => {
  return useQuery({
    queryKey: getJournalHistoryQueryKey(journalEntryId),
    queryFn: async () => {
      const response = await apiClient.get<JournalChangeHistory[]>(
        `/api/audit/journal-history/${journalEntryId}`
      );
      return response.data;
    },
  });
};

export const getLoginHistoryQueryKey = (params: GetLoginHistoryParams) =>
  ['audit', 'login-history', params] as const;

export const useGetLoginHistory = (params: GetLoginHistoryParams) => {
  return useQuery({
    queryKey: getLoginHistoryQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<LoginHistoryResponse>(
        '/api/audit/login-history',
        { params }
      );
      return response.data;
    },
    keepPreviousData: true,
  });
};
```

## 22.3 操作履歴画面

### AuditLogContainer

```typescript
// containers/AuditLogContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetAuditLogs } from '../generated/api/audit';
import { AuditLogView } from '../views/AuditLogView';
import type { AuditLogSearchParams, AuditLogEntry } from '../types/audit';
import { formatDate, getLastMonthRange } from '../utils/dateUtils';

export const AuditLogContainer: React.FC = () => {
  // 検索条件（デフォルトは過去1ヶ月）
  const [searchParams, setSearchParams] = useState<AuditLogSearchParams>(() => {
    const { start, end } = getLastMonthRange();
    return {
      startDate: formatDate(start),
      endDate: formatDate(end),
      page: 0,
      pageSize: 50,
    };
  });

  // 詳細表示
  const [selectedLog, setSelectedLog] = useState<AuditLogEntry | null>(null);

  // データ取得
  const { data, isLoading, error } = useGetAuditLogs(searchParams);

  // 検索実行
  const handleSearch = useCallback((params: Partial<AuditLogSearchParams>) => {
    setSearchParams(prev => ({
      ...prev,
      ...params,
      page: 0, // 検索条件変更時は先頭ページに戻る
    }));
  }, []);

  // ページ変更
  const handlePageChange = useCallback((page: number) => {
    setSearchParams(prev => ({ ...prev, page }));
  }, []);

  // 詳細表示
  const handleViewDetail = useCallback((log: AuditLogEntry) => {
    setSelectedLog(log);
  }, []);

  // 詳細閉じる
  const handleCloseDetail = useCallback(() => {
    setSelectedLog(null);
  }, []);

  // 操作種別ごとの件数
  const operationCounts = useMemo(() => {
    if (!data?.items) return {};
    return data.items.reduce<Record<string, number>>((acc, log) => {
      acc[log.operationType] = (acc[log.operationType] || 0) + 1;
      return acc;
    }, {});
  }, [data?.items]);

  if (error) {
    return <div className="error-state">データの取得に失敗しました</div>;
  }

  return (
    <AuditLogView
      logs={data?.items || []}
      totalCount={data?.totalCount || 0}
      searchParams={searchParams}
      isLoading={isLoading}
      selectedLog={selectedLog}
      operationCounts={operationCounts}
      onSearch={handleSearch}
      onPageChange={handlePageChange}
      onViewDetail={handleViewDetail}
      onCloseDetail={handleCloseDetail}
    />
  );
};
```

### AuditLogView

```typescript
// views/AuditLogView.tsx
import { AuditLogSearchForm } from '../components/AuditLogSearchForm';
import { AuditLogTable } from '../components/AuditLogTable';
import { AuditLogDetailModal } from '../components/AuditLogDetailModal';
import { AuditLogStats } from '../components/AuditLogStats';
import { Pagination } from '../components/common/Pagination';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { AuditLogEntry, AuditLogSearchParams } from '../types/audit';

interface AuditLogViewProps {
  logs: AuditLogEntry[];
  totalCount: number;
  searchParams: AuditLogSearchParams;
  isLoading: boolean;
  selectedLog: AuditLogEntry | null;
  operationCounts: Record<string, number>;
  onSearch: (params: Partial<AuditLogSearchParams>) => void;
  onPageChange: (page: number) => void;
  onViewDetail: (log: AuditLogEntry) => void;
  onCloseDetail: () => void;
}

export const AuditLogView: React.FC<AuditLogViewProps> = ({
  logs,
  totalCount,
  searchParams,
  isLoading,
  selectedLog,
  operationCounts,
  onSearch,
  onPageChange,
  onViewDetail,
  onCloseDetail,
}) => {
  const pageCount = Math.ceil(totalCount / (searchParams.pageSize || 50));

  return (
    <div className="audit-log-view">
      <header className="page-header">
        <h1>操作履歴</h1>
      </header>

      {/* 検索フォーム */}
      <AuditLogSearchForm
        searchParams={searchParams}
        onSearch={onSearch}
      />

      {/* 統計情報 */}
      <AuditLogStats
        operationCounts={operationCounts}
        totalCount={totalCount}
      />

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          {/* 操作履歴テーブル */}
          <AuditLogTable
            logs={logs}
            onViewDetail={onViewDetail}
          />

          {/* ページネーション */}
          {pageCount > 1 && (
            <Pagination
              currentPage={searchParams.page || 0}
              pageCount={pageCount}
              onPageChange={onPageChange}
            />
          )}
        </>
      )}

      {/* 詳細モーダル */}
      {selectedLog && (
        <AuditLogDetailModal
          log={selectedLog}
          onClose={onCloseDetail}
        />
      )}
    </div>
  );
};
```

### AuditLogTable コンポーネント

```typescript
// components/AuditLogTable.tsx
import { formatDateTime } from '../utils/formatUtils';
import type { AuditLogEntry, OperationType, EntityType } from '../types/audit';

interface AuditLogTableProps {
  logs: AuditLogEntry[];
  onViewDetail: (log: AuditLogEntry) => void;
}

const OPERATION_LABELS: Record<OperationType, string> = {
  CREATE: '作成',
  UPDATE: '更新',
  DELETE: '削除',
  APPROVE: '承認',
  REJECT: '差戻し',
  CLOSE: '締め',
  REOPEN: '解除',
  EXPORT: '出力',
  LOGIN: 'ログイン',
  LOGOUT: 'ログアウト',
};

const OPERATION_CLASSES: Record<OperationType, string> = {
  CREATE: 'op-create',
  UPDATE: 'op-update',
  DELETE: 'op-delete',
  APPROVE: 'op-approve',
  REJECT: 'op-reject',
  CLOSE: 'op-close',
  REOPEN: 'op-reopen',
  EXPORT: 'op-export',
  LOGIN: 'op-login',
  LOGOUT: 'op-logout',
};

const ENTITY_LABELS: Record<EntityType, string> = {
  JOURNAL_ENTRY: '仕訳',
  ACCOUNT: '勘定科目',
  ACCOUNTING_PERIOD: '会計期間',
  TAX_TYPE: '課税区分',
  USER: 'ユーザー',
  MONTHLY_CLOSING: '月次締め',
  YEARLY_CLOSING: '年次決算',
};

export const AuditLogTable: React.FC<AuditLogTableProps> = ({
  logs,
  onViewDetail,
}) => {
  if (logs.length === 0) {
    return (
      <div className="empty-state">
        <p>該当する操作履歴がありません</p>
      </div>
    );
  }

  return (
    <div className="audit-log-table">
      <table>
        <thead>
          <tr>
            <th className="col-timestamp">日時</th>
            <th className="col-user">ユーザー</th>
            <th className="col-operation">操作</th>
            <th className="col-entity">対象</th>
            <th className="col-description">内容</th>
            <th className="col-ip">IPアドレス</th>
            <th className="col-actions">詳細</th>
          </tr>
        </thead>
        <tbody>
          {logs.map(log => (
            <tr key={log.id}>
              <td className="col-timestamp">
                {formatDateTime(log.timestamp)}
              </td>
              <td className="col-user">
                <span className="user-name">{log.userName}</span>
              </td>
              <td className="col-operation">
                <span className={`operation-badge ${OPERATION_CLASSES[log.operationType]}`}>
                  {OPERATION_LABELS[log.operationType]}
                </span>
              </td>
              <td className="col-entity">
                <span className="entity-type">
                  {ENTITY_LABELS[log.entityType]}
                </span>
                <span className="entity-id">{log.entityId}</span>
              </td>
              <td className="col-description">
                {log.entityDescription || '-'}
              </td>
              <td className="col-ip">
                {log.ipAddress}
              </td>
              <td className="col-actions">
                <button
                  className="btn btn-text btn-sm"
                  onClick={() => onViewDetail(log)}
                >
                  詳細
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

### AuditLogDetailModal コンポーネント

```typescript
// components/AuditLogDetailModal.tsx
import Modal from 'react-modal';
import { formatDateTime } from '../utils/formatUtils';
import { JsonDiff } from './JsonDiff';
import type { AuditLogEntry } from '../types/audit';

interface AuditLogDetailModalProps {
  log: AuditLogEntry;
  onClose: () => void;
}

const OPERATION_LABELS: Record<string, string> = {
  CREATE: '作成',
  UPDATE: '更新',
  DELETE: '削除',
  APPROVE: '承認',
  REJECT: '差戻し',
  CLOSE: '締め',
  REOPEN: '解除',
  EXPORT: '出力',
  LOGIN: 'ログイン',
  LOGOUT: 'ログアウト',
};

const ENTITY_LABELS: Record<string, string> = {
  JOURNAL_ENTRY: '仕訳',
  ACCOUNT: '勘定科目',
  ACCOUNTING_PERIOD: '会計期間',
  TAX_TYPE: '課税区分',
  USER: 'ユーザー',
  MONTHLY_CLOSING: '月次締め',
  YEARLY_CLOSING: '年次決算',
};

export const AuditLogDetailModal: React.FC<AuditLogDetailModalProps> = ({
  log,
  onClose,
}) => {
  const hasChanges = log.previousValue || log.newValue;

  return (
    <Modal
      isOpen
      onRequestClose={onClose}
      className="audit-detail-modal"
      overlayClassName="modal-overlay"
    >
      <div className="modal-header">
        <h2>操作詳細</h2>
        <button className="close-btn" onClick={onClose}>×</button>
      </div>

      <div className="modal-body">
        {/* 基本情報 */}
        <section className="detail-section">
          <h3>基本情報</h3>
          <dl className="detail-grid">
            <dt>日時</dt>
            <dd>{formatDateTime(log.timestamp)}</dd>

            <dt>ユーザー</dt>
            <dd>{log.userName} ({log.userId})</dd>

            <dt>操作</dt>
            <dd>
              <span className={`operation-badge op-${log.operationType.toLowerCase()}`}>
                {OPERATION_LABELS[log.operationType]}
              </span>
            </dd>

            <dt>対象</dt>
            <dd>
              {ENTITY_LABELS[log.entityType]} - {log.entityId}
            </dd>

            <dt>内容</dt>
            <dd>{log.entityDescription || '-'}</dd>
          </dl>
        </section>

        {/* アクセス情報 */}
        <section className="detail-section">
          <h3>アクセス情報</h3>
          <dl className="detail-grid">
            <dt>IPアドレス</dt>
            <dd>{log.ipAddress}</dd>

            <dt>ユーザーエージェント</dt>
            <dd className="user-agent">{log.userAgent}</dd>
          </dl>
        </section>

        {/* 変更内容 */}
        {hasChanges && (
          <section className="detail-section">
            <h3>変更内容</h3>
            <JsonDiff
              previous={log.previousValue}
              current={log.newValue}
            />
          </section>
        )}

        {/* 追加情報 */}
        {log.additionalInfo && Object.keys(log.additionalInfo).length > 0 && (
          <section className="detail-section">
            <h3>追加情報</h3>
            <pre className="additional-info">
              {JSON.stringify(log.additionalInfo, null, 2)}
            </pre>
          </section>
        )}
      </div>

      <div className="modal-footer">
        <button className="btn btn-secondary" onClick={onClose}>
          閉じる
        </button>
      </div>
    </Modal>
  );
};
```

## 22.4 仕訳変更履歴

### JournalHistoryContainer

```typescript
// containers/JournalHistoryContainer.tsx
import { useState, useCallback } from 'react';
import { useGetJournalHistory } from '../generated/api/audit';
import { JournalHistoryView } from '../views/JournalHistoryView';
import type { JournalChangeHistory } from '../types/audit';

interface JournalHistoryContainerProps {
  journalEntryId: string;
}

export const JournalHistoryContainer: React.FC<JournalHistoryContainerProps> = ({
  journalEntryId,
}) => {
  const [selectedVersions, setSelectedVersions] = useState<[number, number] | null>(null);

  // 変更履歴取得
  const { data: history, isLoading, error } = useGetJournalHistory(journalEntryId);

  // バージョン比較
  const handleCompare = useCallback((v1: number, v2: number) => {
    setSelectedVersions([Math.min(v1, v2), Math.max(v1, v2)]);
  }, []);

  // 比較解除
  const handleClearCompare = useCallback(() => {
    setSelectedVersions(null);
  }, []);

  if (error) {
    return <div className="error-state">履歴の取得に失敗しました</div>;
  }

  return (
    <JournalHistoryView
      history={history || []}
      isLoading={isLoading}
      selectedVersions={selectedVersions}
      onCompare={handleCompare}
      onClearCompare={handleClearCompare}
    />
  );
};
```

### JournalHistoryTimeline コンポーネント

```typescript
// components/JournalHistoryTimeline.tsx
import { formatDateTime } from '../utils/formatUtils';
import type { JournalChangeHistory } from '../types/audit';

interface JournalHistoryTimelineProps {
  history: JournalChangeHistory[];
  selectedVersions: [number, number] | null;
  onVersionClick: (version: number) => void;
  onCompare: (v1: number, v2: number) => void;
}

const CHANGE_TYPE_LABELS: Record<string, string> = {
  CREATE: '作成',
  UPDATE: '更新',
  DELETE: '削除',
};

const CHANGE_TYPE_ICONS: Record<string, string> = {
  CREATE: '✨',
  UPDATE: '✏️',
  DELETE: '🗑️',
};

export const JournalHistoryTimeline: React.FC<JournalHistoryTimelineProps> = ({
  history,
  selectedVersions,
  onVersionClick,
  onCompare,
}) => {
  // 比較用のバージョン選択
  const handleVersionSelect = (version: number) => {
    if (!selectedVersions) {
      onVersionClick(version);
    } else if (selectedVersions.length === 1) {
      onCompare(selectedVersions[0], version);
    }
  };

  return (
    <div className="journal-history-timeline">
      <div className="timeline-header">
        <h3>変更履歴</h3>
        <p className="timeline-instruction">
          バージョンをクリックして比較できます
        </p>
      </div>

      <div className="timeline-content">
        {history.map((entry, index) => {
          const isSelected = selectedVersions?.includes(entry.version);
          const isFirst = index === 0;
          const isLast = index === history.length - 1;

          return (
            <div
              key={entry.id}
              className={`timeline-item ${entry.changeType.toLowerCase()} ${isSelected ? 'selected' : ''}`}
            >
              {/* タイムライン線 */}
              <div className="timeline-line">
                {!isFirst && <div className="line-top" />}
                <div className="timeline-dot">
                  <span className="dot-icon">
                    {CHANGE_TYPE_ICONS[entry.changeType]}
                  </span>
                </div>
                {!isLast && <div className="line-bottom" />}
              </div>

              {/* コンテンツ */}
              <div
                className="timeline-card"
                onClick={() => handleVersionSelect(entry.version)}
              >
                <div className="card-header">
                  <span className="version-badge">v{entry.version}</span>
                  <span className={`change-type ${entry.changeType.toLowerCase()}`}>
                    {CHANGE_TYPE_LABELS[entry.changeType]}
                  </span>
                </div>

                <div className="card-meta">
                  <span className="meta-date">
                    {formatDateTime(entry.changedAt)}
                  </span>
                  <span className="meta-user">{entry.changedBy}</span>
                </div>

                {/* 変更サマリー */}
                {entry.changes.length > 0 && (
                  <div className="change-summary">
                    <span className="change-count">
                      {entry.changes.length}件の変更
                    </span>
                    <ul className="change-list">
                      {entry.changes.slice(0, 3).map((change, i) => (
                        <li key={i}>
                          {change.fieldLabel}
                        </li>
                      ))}
                      {entry.changes.length > 3 && (
                        <li className="more">
                          他 {entry.changes.length - 3} 件
                        </li>
                      )}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
```

### JournalDiffView コンポーネント

```typescript
// components/JournalDiffView.tsx
import { useMemo } from 'react';
import { formatCurrency, formatDate } from '../utils/formatUtils';
import type {
  JournalChangeHistory,
  JournalEntrySnapshot,
  FieldChange,
} from '../types/audit';

interface JournalDiffViewProps {
  beforeVersion: JournalChangeHistory;
  afterVersion: JournalChangeHistory;
  onClose: () => void;
}

export const JournalDiffView: React.FC<JournalDiffViewProps> = ({
  beforeVersion,
  afterVersion,
  onClose,
}) => {
  const beforeData = beforeVersion.currentData;
  const afterData = afterVersion.currentData;

  // 変更点を抽出
  const changes = useMemo(() => {
    if (!beforeData || !afterData) return [];
    return afterVersion.changes;
  }, [beforeData, afterData, afterVersion.changes]);

  return (
    <div className="journal-diff-view">
      <div className="diff-header">
        <h3>バージョン比較</h3>
        <div className="version-labels">
          <span className="version-label before">
            v{beforeVersion.version} ({formatDate(beforeVersion.changedAt)})
          </span>
          <span className="arrow">→</span>
          <span className="version-label after">
            v{afterVersion.version} ({formatDate(afterVersion.changedAt)})
          </span>
        </div>
        <button className="btn btn-text" onClick={onClose}>
          比較を解除
        </button>
      </div>

      {/* 変更一覧 */}
      <div className="diff-changes">
        <h4>変更内容</h4>
        {changes.length === 0 ? (
          <p className="no-changes">変更はありません</p>
        ) : (
          <table className="changes-table">
            <thead>
              <tr>
                <th>項目</th>
                <th>変更前</th>
                <th>変更後</th>
              </tr>
            </thead>
            <tbody>
              {changes.map((change, index) => (
                <tr key={index}>
                  <td className="field-name">{change.fieldLabel}</td>
                  <td className="value before">
                    <DiffValue value={change.previousValue} field={change.field} />
                  </td>
                  <td className="value after">
                    <DiffValue value={change.newValue} field={change.field} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* 仕訳明細の比較 */}
      {beforeData && afterData && (
        <div className="diff-details">
          <h4>仕訳明細</h4>
          <div className="details-comparison">
            <div className="details-before">
              <h5>変更前</h5>
              <JournalDetailTable details={beforeData.details} />
            </div>
            <div className="details-after">
              <h5>変更後</h5>
              <JournalDetailTable details={afterData.details} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// 値の表示
interface DiffValueProps {
  value: any;
  field: string;
}

const DiffValue: React.FC<DiffValueProps> = ({ value, field }) => {
  if (value === null || value === undefined) {
    return <span className="null-value">(なし)</span>;
  }

  // 金額フィールド
  if (field.includes('Amount') || field.includes('amount')) {
    return <span>{formatCurrency(value)}</span>;
  }

  // 日付フィールド
  if (field.includes('Date') || field.includes('date')) {
    return <span>{formatDate(value)}</span>;
  }

  // その他
  return <span>{String(value)}</span>;
};

// 仕訳明細テーブル
interface JournalDetailTableProps {
  details: {
    accountCode: string;
    accountName: string;
    debitAmount: number;
    creditAmount: number;
    description?: string;
  }[];
}

const JournalDetailTable: React.FC<JournalDetailTableProps> = ({ details }) => {
  return (
    <table className="detail-table">
      <thead>
        <tr>
          <th>勘定科目</th>
          <th>借方</th>
          <th>貸方</th>
        </tr>
      </thead>
      <tbody>
        {details.map((detail, index) => (
          <tr key={index}>
            <td>
              <span className="account-code">{detail.accountCode}</span>
              <span className="account-name">{detail.accountName}</span>
            </td>
            <td className="amount debit">
              {detail.debitAmount > 0 ? formatCurrency(detail.debitAmount) : '-'}
            </td>
            <td className="amount credit">
              {detail.creditAmount > 0 ? formatCurrency(detail.creditAmount) : '-'}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};
```

## 22.5 ログイン履歴

### LoginHistoryContainer

```typescript
// containers/LoginHistoryContainer.tsx
import { useState, useCallback } from 'react';
import { useGetLoginHistory } from '../generated/api/audit';
import { LoginHistoryView } from '../views/LoginHistoryView';
import type { LoginHistory } from '../types/audit';
import { formatDate, getLastWeekRange } from '../utils/dateUtils';

export const LoginHistoryContainer: React.FC = () => {
  // 検索条件（デフォルトは過去1週間）
  const [searchParams, setSearchParams] = useState(() => {
    const { start, end } = getLastWeekRange();
    return {
      startDate: formatDate(start),
      endDate: formatDate(end),
      page: 0,
      pageSize: 50,
    };
  });

  // データ取得
  const { data, isLoading, error } = useGetLoginHistory(searchParams);

  // 検索実行
  const handleSearch = useCallback((params: Partial<typeof searchParams>) => {
    setSearchParams(prev => ({
      ...prev,
      ...params,
      page: 0,
    }));
  }, []);

  // ページ変更
  const handlePageChange = useCallback((page: number) => {
    setSearchParams(prev => ({ ...prev, page }));
  }, []);

  if (error) {
    return <div className="error-state">データの取得に失敗しました</div>;
  }

  return (
    <LoginHistoryView
      history={data?.items || []}
      totalCount={data?.totalCount || 0}
      searchParams={searchParams}
      isLoading={isLoading}
      onSearch={handleSearch}
      onPageChange={handlePageChange}
    />
  );
};
```

### LoginHistoryTable コンポーネント

```typescript
// components/LoginHistoryTable.tsx
import { formatDateTime } from '../utils/formatUtils';
import type { LoginHistory } from '../types/audit';

interface LoginHistoryTableProps {
  history: LoginHistory[];
}

const EVENT_LABELS: Record<string, string> = {
  LOGIN: 'ログイン',
  LOGOUT: 'ログアウト',
  LOGIN_FAILED: 'ログイン失敗',
};

const EVENT_CLASSES: Record<string, string> = {
  LOGIN: 'event-login',
  LOGOUT: 'event-logout',
  LOGIN_FAILED: 'event-failed',
};

export const LoginHistoryTable: React.FC<LoginHistoryTableProps> = ({
  history,
}) => {
  if (history.length === 0) {
    return (
      <div className="empty-state">
        <p>該当するログイン履歴がありません</p>
      </div>
    );
  }

  return (
    <div className="login-history-table">
      <table>
        <thead>
          <tr>
            <th className="col-timestamp">日時</th>
            <th className="col-user">ユーザー</th>
            <th className="col-event">イベント</th>
            <th className="col-ip">IPアドレス</th>
            <th className="col-location">場所</th>
            <th className="col-browser">ブラウザ</th>
            <th className="col-reason">詳細</th>
          </tr>
        </thead>
        <tbody>
          {history.map(entry => (
            <tr key={entry.id} className={EVENT_CLASSES[entry.eventType]}>
              <td className="col-timestamp">
                {formatDateTime(entry.timestamp)}
              </td>
              <td className="col-user">
                <span className="user-name">{entry.userName}</span>
              </td>
              <td className="col-event">
                <span className={`event-badge ${entry.eventType.toLowerCase()}`}>
                  {EVENT_LABELS[entry.eventType]}
                </span>
              </td>
              <td className="col-ip">{entry.ipAddress}</td>
              <td className="col-location">{entry.location || '-'}</td>
              <td className="col-browser">
                <BrowserInfo userAgent={entry.userAgent} />
              </td>
              <td className="col-reason">
                {entry.failureReason || '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// ブラウザ情報の解析・表示
interface BrowserInfoProps {
  userAgent: string;
}

const BrowserInfo: React.FC<BrowserInfoProps> = ({ userAgent }) => {
  // 簡易的なブラウザ判定
  const getBrowserName = (ua: string): string => {
    if (ua.includes('Chrome')) return 'Chrome';
    if (ua.includes('Firefox')) return 'Firefox';
    if (ua.includes('Safari')) return 'Safari';
    if (ua.includes('Edge')) return 'Edge';
    if (ua.includes('MSIE') || ua.includes('Trident')) return 'IE';
    return 'その他';
  };

  const getOSName = (ua: string): string => {
    if (ua.includes('Windows')) return 'Windows';
    if (ua.includes('Mac')) return 'macOS';
    if (ua.includes('Linux')) return 'Linux';
    if (ua.includes('Android')) return 'Android';
    if (ua.includes('iOS')) return 'iOS';
    return 'その他';
  };

  return (
    <span className="browser-info" title={userAgent}>
      {getBrowserName(userAgent)} / {getOSName(userAgent)}
    </span>
  );
};
```

### LoginSecurityAlert コンポーネント

```typescript
// components/LoginSecurityAlert.tsx
import { useMemo } from 'react';
import type { LoginHistory } from '../types/audit';

interface LoginSecurityAlertProps {
  history: LoginHistory[];
}

interface SecurityAlert {
  type: 'warning' | 'danger';
  message: string;
  details: string[];
}

export const LoginSecurityAlert: React.FC<LoginSecurityAlertProps> = ({
  history,
}) => {
  // セキュリティアラートを検出
  const alerts = useMemo((): SecurityAlert[] => {
    const result: SecurityAlert[] = [];

    // ログイン失敗の検出
    const failedAttempts = history.filter(h => h.eventType === 'LOGIN_FAILED');
    if (failedAttempts.length >= 3) {
      const uniqueIps = new Set(failedAttempts.map(f => f.ipAddress));
      result.push({
        type: 'warning',
        message: `${failedAttempts.length}回のログイン失敗を検出`,
        details: [
          `対象IP: ${Array.from(uniqueIps).join(', ')}`,
          `期間: ${failedAttempts[failedAttempts.length - 1].timestamp} 〜 ${failedAttempts[0].timestamp}`,
        ],
      });
    }

    // 異なるIPからの同時ログインを検出
    const loginsByUser = history
      .filter(h => h.eventType === 'LOGIN')
      .reduce<Record<string, LoginHistory[]>>((acc, h) => {
        if (!acc[h.userId]) acc[h.userId] = [];
        acc[h.userId].push(h);
        return acc;
      }, {});

    Object.entries(loginsByUser).forEach(([userId, logins]) => {
      const uniqueIps = new Set(logins.map(l => l.ipAddress));
      if (uniqueIps.size >= 3) {
        result.push({
          type: 'warning',
          message: `${logins[0].userName} が ${uniqueIps.size} 箇所からログイン`,
          details: Array.from(uniqueIps),
        });
      }
    });

    return result;
  }, [history]);

  if (alerts.length === 0) {
    return null;
  }

  return (
    <div className="login-security-alerts">
      {alerts.map((alert, index) => (
        <div key={index} className={`alert alert-${alert.type}`}>
          <div className="alert-icon">
            {alert.type === 'danger' ? '🚨' : '⚠️'}
          </div>
          <div className="alert-content">
            <div className="alert-message">{alert.message}</div>
            <ul className="alert-details">
              {alert.details.map((detail, i) => (
                <li key={i}>{detail}</li>
              ))}
            </ul>
          </div>
        </div>
      ))}
    </div>
  );
};
```

## 22.6 JSON 差分表示

### JsonDiff コンポーネント

```typescript
// components/JsonDiff.tsx
import { useMemo } from 'react';

interface JsonDiffProps {
  previous?: Record<string, any>;
  current?: Record<string, any>;
}

interface DiffLine {
  type: 'unchanged' | 'added' | 'removed' | 'modified';
  key: string;
  previousValue?: any;
  currentValue?: any;
}

export const JsonDiff: React.FC<JsonDiffProps> = ({
  previous,
  current,
}) => {
  const diffLines = useMemo((): DiffLine[] => {
    const result: DiffLine[] = [];
    const allKeys = new Set([
      ...Object.keys(previous || {}),
      ...Object.keys(current || {}),
    ]);

    allKeys.forEach(key => {
      const prevVal = previous?.[key];
      const currVal = current?.[key];

      if (prevVal === undefined && currVal !== undefined) {
        result.push({
          type: 'added',
          key,
          currentValue: currVal,
        });
      } else if (prevVal !== undefined && currVal === undefined) {
        result.push({
          type: 'removed',
          key,
          previousValue: prevVal,
        });
      } else if (JSON.stringify(prevVal) !== JSON.stringify(currVal)) {
        result.push({
          type: 'modified',
          key,
          previousValue: prevVal,
          currentValue: currVal,
        });
      } else {
        result.push({
          type: 'unchanged',
          key,
          previousValue: prevVal,
          currentValue: currVal,
        });
      }
    });

    return result;
  }, [previous, current]);

  return (
    <div className="json-diff">
      <table className="diff-table">
        <thead>
          <tr>
            <th>キー</th>
            <th>変更前</th>
            <th>変更後</th>
          </tr>
        </thead>
        <tbody>
          {diffLines.map(line => (
            <tr key={line.key} className={`diff-line diff-${line.type}`}>
              <td className="diff-key">{line.key}</td>
              <td className="diff-value previous">
                {line.type !== 'added' && (
                  <JsonValue value={line.previousValue} />
                )}
              </td>
              <td className="diff-value current">
                {line.type !== 'removed' && (
                  <JsonValue value={line.currentValue} />
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

// JSON 値の表示
interface JsonValueProps {
  value: any;
}

const JsonValue: React.FC<JsonValueProps> = ({ value }) => {
  if (value === null) {
    return <span className="value-null">null</span>;
  }

  if (value === undefined) {
    return <span className="value-undefined">-</span>;
  }

  if (typeof value === 'boolean') {
    return <span className="value-boolean">{value ? 'true' : 'false'}</span>;
  }

  if (typeof value === 'number') {
    return <span className="value-number">{value}</span>;
  }

  if (typeof value === 'string') {
    return <span className="value-string">"{value}"</span>;
  }

  if (Array.isArray(value)) {
    return (
      <span className="value-array">
        [{value.map((v, i) => (
          <span key={i}>
            <JsonValue value={v} />
            {i < value.length - 1 && ', '}
          </span>
        ))}]
      </span>
    );
  }

  if (typeof value === 'object') {
    return (
      <span className="value-object">
        {'{'}
        {Object.entries(value).map(([k, v], i, arr) => (
          <span key={k}>
            {k}: <JsonValue value={v} />
            {i < arr.length - 1 && ', '}
          </span>
        ))}
        {'}'}
      </span>
    );
  }

  return <span>{String(value)}</span>;
};
```

## 22.7 スタイリング

```css
/* styles/audit.css */

/* 監査ログビュー */
.audit-log-view {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

/* 検索フォーム */
.audit-log-search-form {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.search-row:last-child {
  margin-bottom: 0;
}

.search-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-field label {
  font-size: 12px;
  color: #6b7280;
}

/* 統計情報 */
.audit-log-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #111827;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

/* 操作履歴テーブル */
.audit-log-table table {
  width: 100%;
  border-collapse: collapse;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.audit-log-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.audit-log-table td {
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid #f3f4f6;
  vertical-align: top;
}

/* 操作バッジ */
.operation-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.operation-badge.op-create { background: #d1fae5; color: #059669; }
.operation-badge.op-update { background: #dbeafe; color: #1d4ed8; }
.operation-badge.op-delete { background: #fee2e2; color: #dc2626; }
.operation-badge.op-approve { background: #d1fae5; color: #059669; }
.operation-badge.op-reject { background: #fef3c7; color: #d97706; }
.operation-badge.op-close { background: #e0e7ff; color: #4338ca; }
.operation-badge.op-reopen { background: #fce7f3; color: #be185d; }
.operation-badge.op-export { background: #f3f4f6; color: #374151; }
.operation-badge.op-login { background: #d1fae5; color: #059669; }
.operation-badge.op-logout { background: #f3f4f6; color: #374151; }

/* 対象エンティティ */
.entity-type {
  display: block;
  font-size: 12px;
  color: #6b7280;
}

.entity-id {
  font-family: 'SF Mono', monospace;
  font-size: 13px;
}

/* 詳細モーダル */
.audit-detail-modal {
  max-width: 720px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  max-height: 90vh;
  overflow-y: auto;
}

.detail-section {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #374151;
}

.detail-grid {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 8px 16px;
}

.detail-grid dt {
  color: #6b7280;
  font-size: 13px;
}

.detail-grid dd {
  margin: 0;
  font-size: 14px;
}

.user-agent {
  font-size: 12px;
  word-break: break-all;
  color: #6b7280;
}

.additional-info {
  background: #f9fafb;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  font-family: 'SF Mono', monospace;
  overflow-x: auto;
}

/* タイムライン */
.journal-history-timeline {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.timeline-header {
  margin-bottom: 20px;
}

.timeline-header h3 {
  margin: 0 0 4px 0;
}

.timeline-instruction {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.timeline-item {
  display: flex;
  gap: 16px;
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 32px;
}

.line-top,
.line-bottom {
  flex: 1;
  width: 2px;
  background: #e5e7eb;
}

.timeline-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.timeline-item.create .timeline-dot { background: #d1fae5; }
.timeline-item.update .timeline-dot { background: #dbeafe; }
.timeline-item.delete .timeline-dot { background: #fee2e2; }

.timeline-card {
  flex: 1;
  background: #f9fafb;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.15s;
}

.timeline-card:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.timeline-item.selected .timeline-card {
  border-color: #3b82f6;
  background: #eff6ff;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.version-badge {
  background: #3b82f6;
  color: #ffffff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.change-type {
  font-size: 12px;
  font-weight: 500;
}

.change-type.create { color: #059669; }
.change-type.update { color: #1d4ed8; }
.change-type.delete { color: #dc2626; }

.card-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6b7280;
}

.change-summary {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.change-count {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

.change-list {
  margin: 4px 0 0 0;
  padding-left: 16px;
  font-size: 12px;
  color: #6b7280;
}

/* 差分表示 */
.journal-diff-view {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.diff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.version-labels {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-label {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.version-label.before {
  background: #fee2e2;
  color: #dc2626;
}

.version-label.after {
  background: #d1fae5;
  color: #059669;
}

.changes-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 24px;
}

.changes-table th,
.changes-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.changes-table th {
  background: #f9fafb;
  font-size: 12px;
  font-weight: 600;
}

.changes-table .value.before {
  background: #fef2f2;
}

.changes-table .value.after {
  background: #f0fdf4;
}

/* JSON 差分 */
.json-diff {
  font-family: 'SF Mono', monospace;
  font-size: 12px;
}

.diff-table {
  width: 100%;
  border-collapse: collapse;
}

.diff-table th,
.diff-table td {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
}

.diff-table th {
  background: #f9fafb;
  font-weight: 600;
  text-align: left;
}

.diff-line.diff-added { background: #f0fdf4; }
.diff-line.diff-removed { background: #fef2f2; }
.diff-line.diff-modified .previous { background: #fef2f2; }
.diff-line.diff-modified .current { background: #f0fdf4; }

.value-null { color: #6b7280; font-style: italic; }
.value-undefined { color: #9ca3af; }
.value-boolean { color: #8b5cf6; }
.value-number { color: #0891b2; }
.value-string { color: #059669; }

/* ログイン履歴 */
.login-history-table table {
  width: 100%;
  border-collapse: collapse;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.login-history-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
}

.login-history-table td {
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid #f3f4f6;
}

.login-history-table tr.event-failed {
  background: #fef2f2;
}

.event-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.event-badge.login { background: #d1fae5; color: #059669; }
.event-badge.logout { background: #f3f4f6; color: #374151; }
.event-badge.login_failed { background: #fee2e2; color: #dc2626; }

/* セキュリティアラート */
.login-security-alerts {
  margin-bottom: 24px;
}

.alert {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.alert-warning {
  background: #fffbeb;
  border: 1px solid #fde68a;
}

.alert-danger {
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.alert-icon {
  font-size: 20px;
}

.alert-message {
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.alert-details {
  margin: 0;
  padding-left: 16px;
  font-size: 13px;
  color: #6b7280;
}
```

## 22.8 まとめ

本章では、監査・履歴機能を実装した。主なポイントは以下の通りである：

1. **操作履歴**: すべての重要な操作を記録し、検索・閲覧可能
2. **仕訳変更履歴**: 仕訳のバージョン管理と差分表示
3. **ログイン履歴**: アクセス履歴の追跡とセキュリティ監視
4. **差分表示**: JSON 形式での変更前後比較
5. **セキュリティアラート**: 不審なアクセスパターンの検出

これらの機能により、システムの操作に対するトレーサビリティを確保し、内部統制とコンプライアンスの要件に対応できる。

これで第7部「システム機能」の実装が完了した。次章以降では、第8部「テストと品質」として、単体テストと E2E テストの実装について解説する。
