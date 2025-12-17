# 第21章 ダウンロード・出力機能

本章では、各種データのダウンロード・出力機能を実装する。仕訳データ、マスタデータ、財務諸表など、様々な形式でのエクスポート機能と、ダウンロード履歴の管理機能を構築していく。

## 21.1 ダウンロード機能の概要

### 出力対象とフォーマット

財務会計システムでは、以下のデータを出力可能とする：

| カテゴリ | 出力対象 | CSV | Excel | PDF |
|---------|----------|-----|-------|-----|
| 仕訳データ | 仕訳一覧 | ○ | ○ | ○ |
| 仕訳データ | 仕訳帳 | ○ | ○ | ○ |
| マスタ | 勘定科目 | ○ | ○ | - |
| マスタ | 課税区分 | ○ | ○ | - |
| 残高 | 試算表 | ○ | ○ | ○ |
| 残高 | 月次残高 | ○ | ○ | - |
| 財務諸表 | 貸借対照表 | - | ○ | ○ |
| 財務諸表 | 損益計算書 | - | ○ | ○ |
| 財務諸表 | キャッシュフロー | - | ○ | ○ |

### 型定義

```typescript
// types/download.ts

/** ダウンロード対象 */
export type DownloadTarget =
  | 'journal'           // 仕訳一覧
  | 'journalBook'       // 仕訳帳
  | 'account'           // 勘定科目マスタ
  | 'taxType'           // 課税区分マスタ
  | 'trialBalance'      // 試算表
  | 'monthlyBalance'    // 月次残高
  | 'balanceSheet'      // 貸借対照表
  | 'profitLoss'        // 損益計算書
  | 'cashFlow';         // キャッシュフロー計算書

/** 出力フォーマット */
export type ExportFormat = 'csv' | 'excel' | 'pdf';

/** ダウンロード条件 */
export interface DownloadParams {
  target: DownloadTarget;
  format: ExportFormat;
  periodStart?: string;
  periodEnd?: string;
  fiscalYear?: number;
  options?: Record<string, any>;
}

/** ダウンロードジョブ */
export interface DownloadJob {
  id: string;
  target: DownloadTarget;
  format: ExportFormat;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  fileName?: string;
  fileSize?: number;
  downloadUrl?: string;
  expiresAt?: string;
  createdAt: string;
  completedAt?: string;
  errorMessage?: string;
}

/** ダウンロード履歴 */
export interface DownloadHistory {
  id: string;
  target: DownloadTarget;
  format: ExportFormat;
  fileName: string;
  fileSize: number;
  downloadedAt: string;
  downloadedBy: string;
  params: Record<string, any>;
}

/** CSV エクスポートオプション */
export interface CsvExportOptions {
  encoding: 'utf-8' | 'shift-jis';
  delimiter: ',' | '\t';
  includeHeader: boolean;
  dateFormat: string;
}

/** Excel エクスポートオプション */
export interface ExcelExportOptions {
  sheetName?: string;
  includeStyles: boolean;
  freezeHeader: boolean;
}

/** PDF エクスポートオプション */
export interface PdfExportOptions {
  pageSize: 'A4' | 'A3' | 'Letter';
  orientation: 'portrait' | 'landscape';
  includePageNumbers: boolean;
  includeTimestamp: boolean;
}
```

## 21.2 API 連携

### OpenAPI 定義

```yaml
# openapi/paths/download.yaml
/api/download:
  post:
    operationId: createDownloadJob
    summary: ダウンロードジョブ作成
    tags:
      - Download
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/DownloadRequest'
    responses:
      '202':
        description: ジョブ作成完了
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DownloadJob'

/api/download/{jobId}:
  get:
    operationId: getDownloadJob
    summary: ダウンロードジョブ取得
    tags:
      - Download
    parameters:
      - name: jobId
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: ジョブ情報
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DownloadJob'

/api/download/{jobId}/file:
  get:
    operationId: downloadFile
    summary: ファイルダウンロード
    tags:
      - Download
    parameters:
      - name: jobId
        in: path
        required: true
        schema:
          type: string
    responses:
      '200':
        description: ファイル
        content:
          application/octet-stream:
            schema:
              type: string
              format: binary

/api/download/history:
  get:
    operationId: getDownloadHistory
    summary: ダウンロード履歴取得
    tags:
      - Download
    parameters:
      - name: limit
        in: query
        schema:
          type: integer
          default: 20
      - name: offset
        in: query
        schema:
          type: integer
          default: 0
    responses:
      '200':
        description: 履歴一覧
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/DownloadHistoryResponse'

/api/download/quick/{target}:
  get:
    operationId: quickDownload
    summary: 即時ダウンロード（小規模データ用）
    tags:
      - Download
    parameters:
      - name: target
        in: path
        required: true
        schema:
          type: string
      - name: format
        in: query
        required: true
        schema:
          type: string
          enum: [csv, excel]
    responses:
      '200':
        description: ファイル
        content:
          application/octet-stream:
            schema:
              type: string
              format: binary
```

### Orval 生成フック

```typescript
// generated/api/download.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import type {
  DownloadJob,
  DownloadRequest,
  DownloadHistoryResponse,
} from '../model';
import { apiClient } from '../client';

export const useCreateDownloadJob = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (request: DownloadRequest) => {
      const response = await apiClient.post<DownloadJob>(
        '/api/download',
        request
      );
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['download', 'history'] });
    },
  });
};

export const getDownloadJobQueryKey = (jobId: string) =>
  ['download', 'job', jobId] as const;

export const useGetDownloadJob = (
  jobId: string,
  options?: { enabled?: boolean; refetchInterval?: number | false }
) => {
  return useQuery({
    queryKey: getDownloadJobQueryKey(jobId),
    queryFn: async () => {
      const response = await apiClient.get<DownloadJob>(
        `/api/download/${jobId}`
      );
      return response.data;
    },
    ...options,
  });
};

export const useDownloadFile = () => {
  return useMutation({
    mutationFn: async (jobId: string) => {
      const response = await apiClient.get<Blob>(
        `/api/download/${jobId}/file`,
        { responseType: 'blob' }
      );
      return response.data;
    },
  });
};

export const useGetDownloadHistory = (
  params: { limit?: number; offset?: number }
) => {
  return useQuery({
    queryKey: ['download', 'history', params],
    queryFn: async () => {
      const response = await apiClient.get<DownloadHistoryResponse>(
        '/api/download/history',
        { params }
      );
      return response.data;
    },
  });
};

export const useQuickDownload = () => {
  return useMutation({
    mutationFn: async ({
      target,
      format,
      params,
    }: {
      target: string;
      format: 'csv' | 'excel';
      params?: Record<string, any>;
    }) => {
      const response = await apiClient.get<Blob>(
        `/api/download/quick/${target}`,
        {
          params: { format, ...params },
          responseType: 'blob',
        }
      );
      return response.data;
    },
  });
};
```

## 21.3 ダウンロード画面

### DownloadContainer

```typescript
// containers/DownloadContainer.tsx
import { useState, useCallback, useEffect } from 'react';
import {
  useCreateDownloadJob,
  useGetDownloadJob,
  useDownloadFile,
  useGetDownloadHistory,
} from '../generated/api/download';
import { useMessage } from '../contexts/MessageContext';
import { DownloadView } from '../views/DownloadView';
import type {
  DownloadTarget,
  ExportFormat,
  DownloadParams,
  DownloadJob,
} from '../types/download';

export const DownloadContainer: React.FC = () => {
  const { showMessage } = useMessage();

  const [selectedTarget, setSelectedTarget] = useState<DownloadTarget | null>(null);
  const [activeJobId, setActiveJobId] = useState<string | null>(null);

  // ダウンロード履歴取得
  const { data: historyData, isLoading: historyLoading } = useGetDownloadHistory({
    limit: 10,
  });

  // ジョブ作成
  const createJobMutation = useCreateDownloadJob();

  // アクティブジョブの監視
  const {
    data: activeJob,
    refetch: refetchJob,
  } = useGetDownloadJob(activeJobId || '', {
    enabled: !!activeJobId,
    refetchInterval: activeJobId ? 2000 : false, // 2秒ごとにポーリング
  });

  // ファイルダウンロード
  const downloadFileMutation = useDownloadFile();

  // ジョブ完了時の処理
  useEffect(() => {
    if (activeJob?.status === 'completed') {
      showMessage('success', 'ダウンロードの準備が完了しました');
    } else if (activeJob?.status === 'failed') {
      showMessage('error', `ダウンロードに失敗しました: ${activeJob.errorMessage}`);
      setActiveJobId(null);
    }
  }, [activeJob?.status, activeJob?.errorMessage, showMessage]);

  // ダウンロード開始
  const handleStartDownload = useCallback(async (params: DownloadParams) => {
    try {
      const job = await createJobMutation.mutateAsync({
        target: params.target,
        format: params.format,
        periodStart: params.periodStart,
        periodEnd: params.periodEnd,
        fiscalYear: params.fiscalYear,
        options: params.options,
      });
      setActiveJobId(job.id);
      showMessage('info', 'ダウンロードを開始しました');
    } catch (e) {
      showMessage('error', 'ダウンロードの開始に失敗しました');
    }
  }, [createJobMutation, showMessage]);

  // ファイル取得
  const handleDownloadFile = useCallback(async (job: DownloadJob) => {
    if (!job.downloadUrl) return;

    try {
      const blob = await downloadFileMutation.mutateAsync(job.id);

      // ブラウザでダウンロード
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = job.fileName || 'download';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      setActiveJobId(null);
      showMessage('success', 'ダウンロードが完了しました');
    } catch (e) {
      showMessage('error', 'ファイルの取得に失敗しました');
    }
  }, [downloadFileMutation, showMessage]);

  // ジョブキャンセル
  const handleCancelJob = useCallback(() => {
    setActiveJobId(null);
  }, []);

  return (
    <DownloadView
      selectedTarget={selectedTarget}
      activeJob={activeJob}
      history={historyData?.items || []}
      isCreatingJob={createJobMutation.isPending}
      isDownloading={downloadFileMutation.isPending}
      historyLoading={historyLoading}
      onSelectTarget={setSelectedTarget}
      onStartDownload={handleStartDownload}
      onDownloadFile={handleDownloadFile}
      onCancelJob={handleCancelJob}
    />
  );
};
```

### DownloadView

```typescript
// views/DownloadView.tsx
import { DownloadTargetSelector } from '../components/DownloadTargetSelector';
import { DownloadForm } from '../components/DownloadForm';
import { DownloadProgress } from '../components/DownloadProgress';
import { DownloadHistoryTable } from '../components/DownloadHistoryTable';
import type {
  DownloadTarget,
  DownloadParams,
  DownloadJob,
  DownloadHistory,
} from '../types/download';

interface DownloadViewProps {
  selectedTarget: DownloadTarget | null;
  activeJob?: DownloadJob;
  history: DownloadHistory[];
  isCreatingJob: boolean;
  isDownloading: boolean;
  historyLoading: boolean;
  onSelectTarget: (target: DownloadTarget | null) => void;
  onStartDownload: (params: DownloadParams) => void;
  onDownloadFile: (job: DownloadJob) => void;
  onCancelJob: () => void;
}

export const DownloadView: React.FC<DownloadViewProps> = ({
  selectedTarget,
  activeJob,
  history,
  isCreatingJob,
  isDownloading,
  historyLoading,
  onSelectTarget,
  onStartDownload,
  onDownloadFile,
  onCancelJob,
}) => {
  return (
    <div className="download-view">
      <header className="page-header">
        <h1>ダウンロード</h1>
      </header>

      <div className="download-content">
        {/* アクティブジョブがある場合は進捗表示 */}
        {activeJob && (
          <DownloadProgress
            job={activeJob}
            isDownloading={isDownloading}
            onDownload={onDownloadFile}
            onCancel={onCancelJob}
          />
        )}

        {/* ダウンロード対象選択 */}
        {!activeJob && (
          <>
            <section className="download-section">
              <h2>出力対象を選択</h2>
              <DownloadTargetSelector
                selectedTarget={selectedTarget}
                onSelect={onSelectTarget}
              />
            </section>

            {/* ダウンロードフォーム */}
            {selectedTarget && (
              <section className="download-section">
                <h2>出力設定</h2>
                <DownloadForm
                  target={selectedTarget}
                  isSubmitting={isCreatingJob}
                  onSubmit={onStartDownload}
                  onCancel={() => onSelectTarget(null)}
                />
              </section>
            )}
          </>
        )}

        {/* ダウンロード履歴 */}
        <section className="download-section">
          <h2>ダウンロード履歴</h2>
          <DownloadHistoryTable
            history={history}
            isLoading={historyLoading}
          />
        </section>
      </div>
    </div>
  );
};
```

## 21.4 ダウンロード対象選択

### DownloadTargetSelector コンポーネント

```typescript
// components/DownloadTargetSelector.tsx
import type { DownloadTarget } from '../types/download';

interface DownloadTargetSelectorProps {
  selectedTarget: DownloadTarget | null;
  onSelect: (target: DownloadTarget | null) => void;
}

interface TargetGroup {
  label: string;
  targets: {
    key: DownloadTarget;
    label: string;
    description: string;
    formats: ('csv' | 'excel' | 'pdf')[];
  }[];
}

const TARGET_GROUPS: TargetGroup[] = [
  {
    label: '仕訳データ',
    targets: [
      {
        key: 'journal',
        label: '仕訳一覧',
        description: '指定期間の仕訳データを出力',
        formats: ['csv', 'excel', 'pdf'],
      },
      {
        key: 'journalBook',
        label: '仕訳帳',
        description: '仕訳帳形式で出力',
        formats: ['csv', 'excel', 'pdf'],
      },
    ],
  },
  {
    label: 'マスタデータ',
    targets: [
      {
        key: 'account',
        label: '勘定科目マスタ',
        description: '勘定科目の一覧を出力',
        formats: ['csv', 'excel'],
      },
      {
        key: 'taxType',
        label: '課税区分マスタ',
        description: '課税区分の一覧を出力',
        formats: ['csv', 'excel'],
      },
    ],
  },
  {
    label: '残高データ',
    targets: [
      {
        key: 'trialBalance',
        label: '試算表',
        description: '合計残高試算表を出力',
        formats: ['csv', 'excel', 'pdf'],
      },
      {
        key: 'monthlyBalance',
        label: '月次残高',
        description: '月次残高推移を出力',
        formats: ['csv', 'excel'],
      },
    ],
  },
  {
    label: '財務諸表',
    targets: [
      {
        key: 'balanceSheet',
        label: '貸借対照表',
        description: '貸借対照表を出力',
        formats: ['excel', 'pdf'],
      },
      {
        key: 'profitLoss',
        label: '損益計算書',
        description: '損益計算書を出力',
        formats: ['excel', 'pdf'],
      },
      {
        key: 'cashFlow',
        label: 'キャッシュフロー計算書',
        description: 'キャッシュフロー計算書を出力',
        formats: ['excel', 'pdf'],
      },
    ],
  },
];

export const DownloadTargetSelector: React.FC<DownloadTargetSelectorProps> = ({
  selectedTarget,
  onSelect,
}) => {
  return (
    <div className="download-target-selector">
      {TARGET_GROUPS.map(group => (
        <div key={group.label} className="target-group">
          <h3 className="group-label">{group.label}</h3>
          <div className="target-cards">
            {group.targets.map(target => (
              <button
                key={target.key}
                className={`target-card ${selectedTarget === target.key ? 'selected' : ''}`}
                onClick={() => onSelect(target.key)}
              >
                <div className="card-header">
                  <span className="card-title">{target.label}</span>
                  <div className="format-badges">
                    {target.formats.map(format => (
                      <span key={format} className={`format-badge ${format}`}>
                        {format.toUpperCase()}
                      </span>
                    ))}
                  </div>
                </div>
                <p className="card-description">{target.description}</p>
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};
```

## 21.5 ダウンロードフォーム

### DownloadForm コンポーネント

```typescript
// components/DownloadForm.tsx
import { useState, useMemo, useCallback } from 'react';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { PeriodSelector } from './common/PeriodSelector';
import { formatDate, getMonthRange } from '../utils/dateUtils';
import type {
  DownloadTarget,
  ExportFormat,
  DownloadParams,
  CsvExportOptions,
  ExcelExportOptions,
  PdfExportOptions,
} from '../types/download';

interface DownloadFormProps {
  target: DownloadTarget;
  isSubmitting: boolean;
  onSubmit: (params: DownloadParams) => void;
  onCancel: () => void;
}

// 対象ごとの利用可能フォーマット
const AVAILABLE_FORMATS: Record<DownloadTarget, ExportFormat[]> = {
  journal: ['csv', 'excel', 'pdf'],
  journalBook: ['csv', 'excel', 'pdf'],
  account: ['csv', 'excel'],
  taxType: ['csv', 'excel'],
  trialBalance: ['csv', 'excel', 'pdf'],
  monthlyBalance: ['csv', 'excel'],
  balanceSheet: ['excel', 'pdf'],
  profitLoss: ['excel', 'pdf'],
  cashFlow: ['excel', 'pdf'],
};

// 期間指定が必要な対象
const REQUIRES_PERIOD: DownloadTarget[] = [
  'journal',
  'journalBook',
  'trialBalance',
  'monthlyBalance',
  'balanceSheet',
  'profitLoss',
  'cashFlow',
];

export const DownloadForm: React.FC<DownloadFormProps> = ({
  target,
  isSubmitting,
  onSubmit,
  onCancel,
}) => {
  const { currentPeriod } = useAccountingPeriod();

  // フォーマット選択
  const [format, setFormat] = useState<ExportFormat>(
    AVAILABLE_FORMATS[target][0]
  );

  // 期間選択
  const [periodStart, setPeriodStart] = useState(() => {
    if (currentPeriod) return currentPeriod.startDate;
    const { start } = getMonthRange(new Date());
    return formatDate(start);
  });
  const [periodEnd, setPeriodEnd] = useState(() => {
    if (currentPeriod) return currentPeriod.endDate;
    const { end } = getMonthRange(new Date());
    return formatDate(end);
  });

  // CSV オプション
  const [csvOptions, setCsvOptions] = useState<CsvExportOptions>({
    encoding: 'utf-8',
    delimiter: ',',
    includeHeader: true,
    dateFormat: 'YYYY-MM-DD',
  });

  // Excel オプション
  const [excelOptions, setExcelOptions] = useState<ExcelExportOptions>({
    sheetName: undefined,
    includeStyles: true,
    freezeHeader: true,
  });

  // PDF オプション
  const [pdfOptions, setPdfOptions] = useState<PdfExportOptions>({
    pageSize: 'A4',
    orientation: 'portrait',
    includePageNumbers: true,
    includeTimestamp: true,
  });

  // 利用可能なフォーマット
  const availableFormats = useMemo(
    () => AVAILABLE_FORMATS[target],
    [target]
  );

  // 期間指定が必要か
  const requiresPeriod = useMemo(
    () => REQUIRES_PERIOD.includes(target),
    [target]
  );

  // 送信
  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();

    const options =
      format === 'csv'
        ? csvOptions
        : format === 'excel'
          ? excelOptions
          : pdfOptions;

    onSubmit({
      target,
      format,
      periodStart: requiresPeriod ? periodStart : undefined,
      periodEnd: requiresPeriod ? periodEnd : undefined,
      options,
    });
  }, [
    target,
    format,
    periodStart,
    periodEnd,
    requiresPeriod,
    csvOptions,
    excelOptions,
    pdfOptions,
    onSubmit,
  ]);

  return (
    <form className="download-form" onSubmit={handleSubmit}>
      {/* フォーマット選択 */}
      <div className="form-section">
        <h3>出力形式</h3>
        <div className="format-selector">
          {availableFormats.map(f => (
            <label key={f} className="format-option">
              <input
                type="radio"
                name="format"
                value={f}
                checked={format === f}
                onChange={() => setFormat(f)}
              />
              <span className={`format-label ${f}`}>
                {f === 'csv' && 'CSV'}
                {f === 'excel' && 'Excel'}
                {f === 'pdf' && 'PDF'}
              </span>
            </label>
          ))}
        </div>
      </div>

      {/* 期間選択 */}
      {requiresPeriod && (
        <div className="form-section">
          <h3>出力期間</h3>
          <PeriodSelector
            startDate={periodStart}
            endDate={periodEnd}
            onStartChange={setPeriodStart}
            onEndChange={setPeriodEnd}
          />
        </div>
      )}

      {/* CSV オプション */}
      {format === 'csv' && (
        <CsvOptionsForm
          options={csvOptions}
          onChange={setCsvOptions}
        />
      )}

      {/* Excel オプション */}
      {format === 'excel' && (
        <ExcelOptionsForm
          options={excelOptions}
          onChange={setExcelOptions}
        />
      )}

      {/* PDF オプション */}
      {format === 'pdf' && (
        <PdfOptionsForm
          options={pdfOptions}
          onChange={setPdfOptions}
        />
      )}

      {/* 送信ボタン */}
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
          disabled={isSubmitting}
        >
          {isSubmitting ? 'ダウンロード準備中...' : 'ダウンロード開始'}
        </button>
      </div>
    </form>
  );
};

// CSV オプションフォーム
interface CsvOptionsFormProps {
  options: CsvExportOptions;
  onChange: (options: CsvExportOptions) => void;
}

const CsvOptionsForm: React.FC<CsvOptionsFormProps> = ({
  options,
  onChange,
}) => {
  return (
    <div className="form-section options-section">
      <h3>CSV オプション</h3>

      <div className="form-row">
        <label className="form-label">文字コード</label>
        <select
          value={options.encoding}
          onChange={(e) =>
            onChange({ ...options, encoding: e.target.value as 'utf-8' | 'shift-jis' })
          }
        >
          <option value="utf-8">UTF-8</option>
          <option value="shift-jis">Shift-JIS（Excel 互換）</option>
        </select>
      </div>

      <div className="form-row">
        <label className="form-label">区切り文字</label>
        <select
          value={options.delimiter}
          onChange={(e) =>
            onChange({ ...options, delimiter: e.target.value as ',' | '\t' })
          }
        >
          <option value=",">カンマ（,）</option>
          <option value="	">タブ</option>
        </select>
      </div>

      <div className="form-row">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={options.includeHeader}
            onChange={(e) =>
              onChange({ ...options, includeHeader: e.target.checked })
            }
          />
          ヘッダー行を含める
        </label>
      </div>
    </div>
  );
};

// Excel オプションフォーム
interface ExcelOptionsFormProps {
  options: ExcelExportOptions;
  onChange: (options: ExcelExportOptions) => void;
}

const ExcelOptionsForm: React.FC<ExcelOptionsFormProps> = ({
  options,
  onChange,
}) => {
  return (
    <div className="form-section options-section">
      <h3>Excel オプション</h3>

      <div className="form-row">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={options.includeStyles}
            onChange={(e) =>
              onChange({ ...options, includeStyles: e.target.checked })
            }
          />
          書式設定を適用
        </label>
      </div>

      <div className="form-row">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={options.freezeHeader}
            onChange={(e) =>
              onChange({ ...options, freezeHeader: e.target.checked })
            }
          />
          ヘッダー行を固定
        </label>
      </div>
    </div>
  );
};

// PDF オプションフォーム
interface PdfOptionsFormProps {
  options: PdfExportOptions;
  onChange: (options: PdfExportOptions) => void;
}

const PdfOptionsForm: React.FC<PdfOptionsFormProps> = ({
  options,
  onChange,
}) => {
  return (
    <div className="form-section options-section">
      <h3>PDF オプション</h3>

      <div className="form-row">
        <label className="form-label">用紙サイズ</label>
        <select
          value={options.pageSize}
          onChange={(e) =>
            onChange({ ...options, pageSize: e.target.value as 'A4' | 'A3' | 'Letter' })
          }
        >
          <option value="A4">A4</option>
          <option value="A3">A3</option>
          <option value="Letter">Letter</option>
        </select>
      </div>

      <div className="form-row">
        <label className="form-label">印刷の向き</label>
        <select
          value={options.orientation}
          onChange={(e) =>
            onChange({
              ...options,
              orientation: e.target.value as 'portrait' | 'landscape',
            })
          }
        >
          <option value="portrait">縦</option>
          <option value="landscape">横</option>
        </select>
      </div>

      <div className="form-row">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={options.includePageNumbers}
            onChange={(e) =>
              onChange({ ...options, includePageNumbers: e.target.checked })
            }
          />
          ページ番号を表示
        </label>
      </div>

      <div className="form-row">
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={options.includeTimestamp}
            onChange={(e) =>
              onChange({ ...options, includeTimestamp: e.target.checked })
            }
          />
          出力日時を表示
        </label>
      </div>
    </div>
  );
};
```

## 21.6 ダウンロード進捗表示

### DownloadProgress コンポーネント

```typescript
// components/DownloadProgress.tsx
import { useMemo } from 'react';
import { formatFileSize, formatDateTime } from '../utils/formatUtils';
import type { DownloadJob } from '../types/download';

interface DownloadProgressProps {
  job: DownloadJob;
  isDownloading: boolean;
  onDownload: (job: DownloadJob) => void;
  onCancel: () => void;
}

const TARGET_LABELS: Record<string, string> = {
  journal: '仕訳一覧',
  journalBook: '仕訳帳',
  account: '勘定科目マスタ',
  taxType: '課税区分マスタ',
  trialBalance: '試算表',
  monthlyBalance: '月次残高',
  balanceSheet: '貸借対照表',
  profitLoss: '損益計算書',
  cashFlow: 'キャッシュフロー計算書',
};

export const DownloadProgress: React.FC<DownloadProgressProps> = ({
  job,
  isDownloading,
  onDownload,
  onCancel,
}) => {
  const statusMessage = useMemo(() => {
    switch (job.status) {
      case 'pending':
        return 'ダウンロードの準備をしています...';
      case 'processing':
        return 'ファイルを生成しています...';
      case 'completed':
        return 'ダウンロードの準備が完了しました';
      case 'failed':
        return 'ダウンロードに失敗しました';
      default:
        return '';
    }
  }, [job.status]);

  const isInProgress = job.status === 'pending' || job.status === 'processing';

  return (
    <div className={`download-progress ${job.status}`}>
      <div className="progress-header">
        <h3>{TARGET_LABELS[job.target]} のダウンロード</h3>
        <span className={`format-badge ${job.format}`}>
          {job.format.toUpperCase()}
        </span>
      </div>

      <div className="progress-status">
        <span className="status-message">{statusMessage}</span>
        {job.errorMessage && (
          <span className="error-message">{job.errorMessage}</span>
        )}
      </div>

      {/* 進捗バー */}
      {isInProgress && (
        <div className="progress-bar-container">
          <div
            className="progress-bar"
            style={{ width: `${job.progress}%` }}
          />
          <span className="progress-text">{job.progress}%</span>
        </div>
      )}

      {/* 完了時の情報 */}
      {job.status === 'completed' && (
        <div className="completion-info">
          <div className="info-row">
            <span className="label">ファイル名:</span>
            <span className="value">{job.fileName}</span>
          </div>
          <div className="info-row">
            <span className="label">ファイルサイズ:</span>
            <span className="value">{formatFileSize(job.fileSize || 0)}</span>
          </div>
          <div className="info-row">
            <span className="label">有効期限:</span>
            <span className="value">
              {job.expiresAt ? formatDateTime(job.expiresAt) : '-'}
            </span>
          </div>
        </div>
      )}

      {/* アクションボタン */}
      <div className="progress-actions">
        {isInProgress && (
          <button
            className="btn btn-secondary"
            onClick={onCancel}
          >
            キャンセル
          </button>
        )}

        {job.status === 'completed' && (
          <button
            className="btn btn-primary"
            onClick={() => onDownload(job)}
            disabled={isDownloading}
          >
            {isDownloading ? 'ダウンロード中...' : 'ファイルをダウンロード'}
          </button>
        )}

        {job.status === 'failed' && (
          <button
            className="btn btn-secondary"
            onClick={onCancel}
          >
            閉じる
          </button>
        )}
      </div>
    </div>
  );
};
```

## 21.7 ダウンロード履歴

### DownloadHistoryTable コンポーネント

```typescript
// components/DownloadHistoryTable.tsx
import { formatFileSize, formatDateTime } from '../utils/formatUtils';
import type { DownloadHistory } from '../types/download';

interface DownloadHistoryTableProps {
  history: DownloadHistory[];
  isLoading: boolean;
}

const TARGET_LABELS: Record<string, string> = {
  journal: '仕訳一覧',
  journalBook: '仕訳帳',
  account: '勘定科目マスタ',
  taxType: '課税区分マスタ',
  trialBalance: '試算表',
  monthlyBalance: '月次残高',
  balanceSheet: '貸借対照表',
  profitLoss: '損益計算書',
  cashFlow: 'キャッシュフロー計算書',
};

export const DownloadHistoryTable: React.FC<DownloadHistoryTableProps> = ({
  history,
  isLoading,
}) => {
  if (isLoading) {
    return <div className="loading">読み込み中...</div>;
  }

  if (history.length === 0) {
    return (
      <div className="empty-state">
        <p>ダウンロード履歴がありません</p>
      </div>
    );
  }

  return (
    <div className="download-history-table">
      <table>
        <thead>
          <tr>
            <th>出力対象</th>
            <th>形式</th>
            <th>ファイル名</th>
            <th>サイズ</th>
            <th>ダウンロード日時</th>
            <th>ユーザー</th>
          </tr>
        </thead>
        <tbody>
          {history.map(item => (
            <tr key={item.id}>
              <td>{TARGET_LABELS[item.target]}</td>
              <td>
                <span className={`format-badge ${item.format}`}>
                  {item.format.toUpperCase()}
                </span>
              </td>
              <td className="file-name">{item.fileName}</td>
              <td>{formatFileSize(item.fileSize)}</td>
              <td>{formatDateTime(item.downloadedAt)}</td>
              <td>{item.downloadedBy}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
```

## 21.8 即時ダウンロード（小規模データ用）

### QuickDownloadButton コンポーネント

```typescript
// components/QuickDownloadButton.tsx
import { useState, useCallback } from 'react';
import { useQuickDownload } from '../generated/api/download';
import { useMessage } from '../contexts/MessageContext';
import type { DownloadTarget, ExportFormat } from '../types/download';

interface QuickDownloadButtonProps {
  target: DownloadTarget;
  format: ExportFormat;
  params?: Record<string, any>;
  fileName?: string;
  children: React.ReactNode;
  className?: string;
}

export const QuickDownloadButton: React.FC<QuickDownloadButtonProps> = ({
  target,
  format,
  params,
  fileName,
  children,
  className = '',
}) => {
  const { showMessage } = useMessage();
  const [isDownloading, setIsDownloading] = useState(false);
  const quickDownloadMutation = useQuickDownload();

  const handleClick = useCallback(async () => {
    setIsDownloading(true);

    try {
      const blob = await quickDownloadMutation.mutateAsync({
        target,
        format: format as 'csv' | 'excel',
        params,
      });

      // ファイル名を生成
      const extension = format === 'excel' ? 'xlsx' : 'csv';
      const defaultFileName = `${target}_${new Date().toISOString().slice(0, 10)}.${extension}`;

      // ブラウザでダウンロード
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName || defaultFileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      showMessage('success', 'ダウンロードが完了しました');
    } catch (e) {
      showMessage('error', 'ダウンロードに失敗しました');
    } finally {
      setIsDownloading(false);
    }
  }, [target, format, params, fileName, quickDownloadMutation, showMessage]);

  return (
    <button
      className={`quick-download-btn ${className}`}
      onClick={handleClick}
      disabled={isDownloading}
    >
      {isDownloading ? (
        <>
          <span className="spinner" />
          ダウンロード中...
        </>
      ) : (
        children
      )}
    </button>
  );
};
```

### 使用例

```typescript
// 勘定科目マスタ画面での使用例
export const AccountMasterView: React.FC = () => {
  return (
    <div className="account-master-view">
      <header className="page-header">
        <h1>勘定科目マスタ</h1>
        <div className="header-actions">
          <QuickDownloadButton
            target="account"
            format="csv"
            className="btn btn-secondary"
          >
            CSV ダウンロード
          </QuickDownloadButton>
          <QuickDownloadButton
            target="account"
            format="excel"
            className="btn btn-secondary"
          >
            Excel ダウンロード
          </QuickDownloadButton>
        </div>
      </header>
      {/* ... */}
    </div>
  );
};
```

## 21.9 ユーティリティ関数

### formatUtils

```typescript
// utils/formatUtils.ts

/**
 * ファイルサイズを人間が読める形式に変換
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';

  const units = ['B', 'KB', 'MB', 'GB'];
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${units[i]}`;
};

/**
 * 日時をフォーマット
 */
export const formatDateTime = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleString('ja-JP', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};
```

## 21.10 スタイリング

```css
/* styles/download.css */

/* ダウンロードページ */
.download-view {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
}

.download-section {
  margin-bottom: 32px;
}

.download-section h2 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #374151;
}

/* 対象選択 */
.download-target-selector {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.target-group {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}

.group-label {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 12px 0;
}

.target-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.target-card {
  background: #f9fafb;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: all 0.15s;
}

.target-card:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.target-card.selected {
  background: #eff6ff;
  border-color: #3b82f6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-title {
  font-weight: 600;
  color: #111827;
}

.card-description {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

/* フォーマットバッジ */
.format-badges {
  display: flex;
  gap: 4px;
}

.format-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}

.format-badge.csv {
  background: #d1fae5;
  color: #059669;
}

.format-badge.excel {
  background: #dbeafe;
  color: #1d4ed8;
}

.format-badge.pdf {
  background: #fee2e2;
  color: #dc2626;
}

/* フォーマット選択 */
.format-selector {
  display: flex;
  gap: 12px;
}

.format-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.format-option input {
  margin: 0;
}

.format-label {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
}

.format-label.csv {
  background: #d1fae5;
  color: #059669;
}

.format-label.excel {
  background: #dbeafe;
  color: #1d4ed8;
}

.format-label.pdf {
  background: #fee2e2;
  color: #dc2626;
}

/* オプションセクション */
.options-section {
  background: #f9fafb;
  border-radius: 8px;
  padding: 16px;
}

.options-section h3 {
  font-size: 14px;
  margin: 0 0 12px 0;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-label {
  min-width: 100px;
  font-size: 13px;
  color: #374151;
}

.form-row select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
}

/* フォームアクション */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

/* 進捗表示 */
.download-progress {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.download-progress.completed {
  border-color: #10b981;
  background: #f0fdf4;
}

.download-progress.failed {
  border-color: #ef4444;
  background: #fef2f2;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.progress-header h3 {
  margin: 0;
  font-size: 16px;
}

.progress-status {
  margin-bottom: 16px;
}

.status-message {
  font-size: 14px;
  color: #374151;
}

.error-message {
  display: block;
  color: #dc2626;
  font-size: 13px;
  margin-top: 4px;
}

.progress-bar-container {
  position: relative;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 16px;
}

.progress-bar {
  height: 100%;
  background: #3b82f6;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  right: 0;
  top: 12px;
  font-size: 12px;
  color: #6b7280;
}

.completion-info {
  background: #f9fafb;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.completion-info .info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
}

.completion-info .label {
  color: #6b7280;
}

.completion-info .value {
  font-weight: 500;
}

.progress-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 履歴テーブル */
.download-history-table table {
  width: 100%;
  border-collapse: collapse;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.download-history-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.download-history-table td {
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid #f3f4f6;
}

.download-history-table .file-name {
  font-family: 'SF Mono', monospace;
  font-size: 13px;
}

/* クイックダウンロードボタン */
.quick-download-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.quick-download-btn .spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
```

## 21.11 まとめ

本章では、ダウンロード・出力機能を実装した。主なポイントは以下の通りである：

1. **多様な出力対象**: 仕訳データ、マスタ、残高、財務諸表など幅広い対象をサポート
2. **複数フォーマット対応**: CSV、Excel、PDF の3形式に対応
3. **非同期ダウンロード**: 大規模データは非同期処理で生成し、進捗表示とポーリングで完了を監視
4. **即時ダウンロード**: 小規模データ用の即時ダウンロード機能
5. **オプション設定**: フォーマットごとの詳細設定（文字コード、用紙サイズなど）
6. **履歴管理**: ダウンロード履歴の表示と追跡

これらの機能により、ユーザーは必要なデータを適切な形式で取得し、外部システムとの連携や帳票作成に活用できる。次章では、監査・履歴機能について解説する。
