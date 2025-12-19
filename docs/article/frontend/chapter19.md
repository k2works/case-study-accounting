# 第19章 キャッシュフロー計算書

本章では、キャッシュフロー計算書（Cash Flow Statement、C/F）の表示機能を実装する。営業・投資・財務活動ごとのキャッシュフロー分析、間接法による表示、ウォーターフォールチャートによる可視化など、資金繰りの把握に必要なコンポーネントを構築していく。

## 19.1 キャッシュフロー計算書の構造

### 3つの活動区分

キャッシュフロー計算書は、企業の現金の流れを以下の3つの活動に区分して表示する：

```
Ⅰ. 営業活動によるキャッシュフロー
    税引前当期純利益
    ＋ 減価償却費
    ＋ 引当金の増減
    ＋ 売上債権の増減
    ＋ 棚卸資産の増減
    ＋ 仕入債務の増減
    ＋ その他
    ─ 法人税等の支払額
    ────────────────
    営業活動によるキャッシュフロー

Ⅱ. 投資活動によるキャッシュフロー
    ＋ 有形固定資産の取得/売却
    ＋ 無形固定資産の取得/売却
    ＋ 投資有価証券の取得/売却
    ＋ 貸付金の増減
    ────────────────
    投資活動によるキャッシュフロー

Ⅲ. 財務活動によるキャッシュフロー
    ＋ 借入金の増減
    ＋ 社債の発行/償還
    ＋ 株式の発行
    ─ 配当金の支払
    ────────────────
    財務活動によるキャッシュフロー

────────────────
現金及び現金同等物の増減額
＋ 現金及び現金同等物の期首残高
────────────────
現金及び現金同等物の期末残高
```

### 型定義

```typescript
// types/cashFlow.ts

/** キャッシュフロー活動区分 */
export type CFActivity =
  | 'operating'   // 営業活動
  | 'investing'   // 投資活動
  | 'financing';  // 財務活動

/** キャッシュフロー項目種別 */
export type CFItemType =
  | 'profitBase'       // 税引前当期純利益（基準）
  | 'depreciation'     // 減価償却費
  | 'provision'        // 引当金増減
  | 'receivable'       // 売上債権増減
  | 'inventory'        // 棚卸資産増減
  | 'payable'          // 仕入債務増減
  | 'interest'         // 利息及び配当金
  | 'tax'              // 法人税等
  | 'fixedAsset'       // 固定資産増減
  | 'investment'       // 投資増減
  | 'loan'             // 貸付金増減
  | 'borrowing'        // 借入金増減
  | 'bond'             // 社債増減
  | 'equity'           // 株式増減
  | 'dividend'         // 配当金
  | 'other';           // その他

/** キャッシュフロー項目 */
export interface CFItem {
  id: string;
  code: string;
  name: string;
  activity: CFActivity;
  itemType: CFItemType;
  amount: number;
  previousAmount?: number;
  children?: CFItem[];
  level: number;
  displayOrder: number;
  description?: string;
}

/** 活動別キャッシュフロー */
export interface ActivityCashFlow {
  activity: CFActivity;
  label: string;
  items: CFItem[];
  subtotal: number;
  previousSubtotal?: number;
}

/** キャッシュフローサマリー */
export interface CFSummary {
  operatingCF: number;
  investingCF: number;
  financingCF: number;
  netChange: number;
  beginningBalance: number;
  endingBalance: number;
  freeCashFlow: number; // 営業CF + 投資CF
}

/** キャッシュフロー計算書データ */
export interface CashFlowData {
  periodStart: string;
  periodEnd: string;
  activities: ActivityCashFlow[];
  summary: CFSummary;
  previousSummary?: CFSummary;
}

/** キャッシュフロー検索条件 */
export interface CFSearchParams {
  periodStart: string;
  periodEnd: string;
  compareWithPrevious: boolean;
  method: 'indirect' | 'direct';
}
```

## 19.2 API 連携

### OpenAPI 定義

```yaml
# openapi/paths/cash-flow.yaml
/api/cash-flow:
  get:
    operationId: getCashFlow
    summary: キャッシュフロー計算書取得
    tags:
      - CashFlow
    parameters:
      - name: periodStart
        in: query
        required: true
        schema:
          type: string
          format: date
      - name: periodEnd
        in: query
        required: true
        schema:
          type: string
          format: date
      - name: compareWithPrevious
        in: query
        schema:
          type: boolean
          default: false
      - name: method
        in: query
        schema:
          type: string
          enum: [indirect, direct]
          default: indirect
    responses:
      '200':
        description: キャッシュフロー計算書
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CashFlowResponse'

/api/cash-flow/monthly-trend:
  get:
    operationId: getCFMonthlyTrend
    summary: キャッシュフロー月次推移取得
    tags:
      - CashFlow
    parameters:
      - name: fiscalYear
        in: query
        required: true
        schema:
          type: integer
    responses:
      '200':
        description: 月次推移データ
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CFMonthlyTrendResponse'

/api/cash-flow/forecast:
  get:
    operationId: getCashFlowForecast
    summary: 資金繰り予測取得
    tags:
      - CashFlow
    parameters:
      - name: baseDate
        in: query
        required: true
        schema:
          type: string
          format: date
      - name: months
        in: query
        schema:
          type: integer
          default: 3
    responses:
      '200':
        description: 資金繰り予測
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CashFlowForecastResponse'
```

### Orval 生成フック

```typescript
// generated/api/cash-flow.ts
import { useQuery } from '@tanstack/react-query';
import type {
  CashFlowResponse,
  CFMonthlyTrendResponse,
  CashFlowForecastResponse,
  GetCashFlowParams,
  GetCFMonthlyTrendParams,
  GetCashFlowForecastParams,
} from '../model';
import { apiClient } from '../client';

export const getCashFlowQueryKey = (params: GetCashFlowParams) =>
  ['cash-flow', params] as const;

export const useGetCashFlow = (
  params: GetCashFlowParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getCashFlowQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<CashFlowResponse>(
        '/api/cash-flow',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};

export const getCFMonthlyTrendQueryKey = (params: GetCFMonthlyTrendParams) =>
  ['cash-flow', 'monthly-trend', params] as const;

export const useGetCFMonthlyTrend = (
  params: GetCFMonthlyTrendParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getCFMonthlyTrendQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<CFMonthlyTrendResponse>(
        '/api/cash-flow/monthly-trend',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};

export const getCashFlowForecastQueryKey = (params: GetCashFlowForecastParams) =>
  ['cash-flow', 'forecast', params] as const;

export const useGetCashFlowForecast = (
  params: GetCashFlowForecastParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getCashFlowForecastQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<CashFlowForecastResponse>(
        '/api/cash-flow/forecast',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};
```

## 19.3 Container 実装

### CashFlowContainer

```typescript
// containers/CashFlowContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetCashFlow } from '../generated/api/cash-flow';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { useMessage } from '../contexts/MessageContext';
import { useCFExport } from '../hooks/useCFExport';
import { CashFlowView } from '../views/CashFlowView';
import type { CFSearchParams } from '../types/cashFlow';
import { formatDate, getFiscalYearRange } from '../utils/dateUtils';

export const CashFlowContainer: React.FC = () => {
  const { currentPeriod } = useAccountingPeriod();
  const { showMessage } = useMessage();
  const { exportToPdf, exportToExcel, isExporting } = useCFExport();

  // 検索条件（デフォルトは今期）
  const [searchParams, setSearchParams] = useState<CFSearchParams>(() => {
    if (currentPeriod) {
      return {
        periodStart: currentPeriod.startDate,
        periodEnd: currentPeriod.endDate,
        compareWithPrevious: false,
        method: 'indirect' as const,
      };
    }
    const { start, end } = getFiscalYearRange(new Date());
    return {
      periodStart: formatDate(start),
      periodEnd: formatDate(end),
      compareWithPrevious: false,
      method: 'indirect' as const,
    };
  });

  // 表示設定
  const [expandedActivities, setExpandedActivities] = useState<Set<string>>(
    new Set(['operating', 'investing', 'financing'])
  );

  // データ取得
  const {
    data: cfData,
    isLoading,
    error,
    refetch,
  } = useGetCashFlow(searchParams);

  // 検索実行
  const handleSearch = useCallback((params: CFSearchParams) => {
    setSearchParams(params);
  }, []);

  // 活動区分の展開/折りたたみ
  const handleToggleActivity = useCallback((activity: string) => {
    setExpandedActivities(prev => {
      const next = new Set(prev);
      if (next.has(activity)) {
        next.delete(activity);
      } else {
        next.add(activity);
      }
      return next;
    });
  }, []);

  // 全展開/全折りたたみ
  const handleExpandAll = useCallback(() => {
    setExpandedActivities(new Set(['operating', 'investing', 'financing']));
  }, []);

  const handleCollapseAll = useCallback(() => {
    setExpandedActivities(new Set());
  }, []);

  // PDF 出力
  const handleExportPdf = useCallback(async () => {
    if (!cfData) return;

    try {
      await exportToPdf(cfData, searchParams);
      showMessage('success', 'PDF を出力しました');
    } catch (e) {
      showMessage('error', 'PDF 出力に失敗しました');
    }
  }, [cfData, searchParams, exportToPdf, showMessage]);

  // Excel 出力
  const handleExportExcel = useCallback(async () => {
    if (!cfData) return;

    try {
      await exportToExcel(cfData, searchParams);
      showMessage('success', 'Excel を出力しました');
    } catch (e) {
      showMessage('error', 'Excel 出力に失敗しました');
    }
  }, [cfData, searchParams, exportToExcel, showMessage]);

  if (error) {
    return (
      <div className="error-container">
        <p>データの取得に失敗しました</p>
        <button onClick={() => refetch()}>再試行</button>
      </div>
    );
  }

  return (
    <CashFlowView
      data={cfData}
      searchParams={searchParams}
      isLoading={isLoading}
      isExporting={isExporting}
      expandedActivities={expandedActivities}
      onSearch={handleSearch}
      onToggleActivity={handleToggleActivity}
      onExpandAll={handleExpandAll}
      onCollapseAll={handleCollapseAll}
      onExportPdf={handleExportPdf}
      onExportExcel={handleExportExcel}
    />
  );
};
```

## 19.4 View 実装

### CashFlowView

```typescript
// views/CashFlowView.tsx
import { CFSearchForm } from '../components/CFSearchForm';
import { CFSummaryPanel } from '../components/CFSummaryPanel';
import { CFStatement } from '../components/CFStatement';
import { CFWaterfallChart } from '../components/CFWaterfallChart';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { CashFlowData, CFSearchParams } from '../types/cashFlow';

interface CashFlowViewProps {
  data?: CashFlowData;
  searchParams: CFSearchParams;
  isLoading: boolean;
  isExporting: boolean;
  expandedActivities: Set<string>;
  onSearch: (params: CFSearchParams) => void;
  onToggleActivity: (activity: string) => void;
  onExpandAll: () => void;
  onCollapseAll: () => void;
  onExportPdf: () => void;
  onExportExcel: () => void;
}

export const CashFlowView: React.FC<CashFlowViewProps> = ({
  data,
  searchParams,
  isLoading,
  isExporting,
  expandedActivities,
  onSearch,
  onToggleActivity,
  onExpandAll,
  onCollapseAll,
  onExportPdf,
  onExportExcel,
}) => {
  return (
    <div className="cash-flow-view">
      <header className="page-header">
        <h1>キャッシュフロー計算書</h1>
        <div className="header-actions">
          <button
            onClick={onExportPdf}
            disabled={!data || isExporting}
            className="btn btn-secondary"
          >
            PDF 出力
          </button>
          <button
            onClick={onExportExcel}
            disabled={!data || isExporting}
            className="btn btn-secondary"
          >
            Excel 出力
          </button>
        </div>
      </header>

      <CFSearchForm
        initialValues={searchParams}
        onSearch={onSearch}
        isLoading={isLoading}
      />

      {isLoading ? (
        <LoadingSpinner />
      ) : data ? (
        <div className="cf-content">
          {/* サマリーパネル */}
          <CFSummaryPanel
            summary={data.summary}
            previousSummary={data.previousSummary}
            compareWithPrevious={searchParams.compareWithPrevious}
          />

          {/* ウォーターフォールチャート */}
          <CFWaterfallChart
            summary={data.summary}
            title="キャッシュフロー構成"
          />

          {/* 表示コントロール */}
          <div className="display-controls">
            <div className="control-group">
              <button onClick={onExpandAll} className="btn btn-text">
                すべて展開
              </button>
              <button onClick={onCollapseAll} className="btn btn-text">
                すべて折りたたむ
              </button>
            </div>
            <div className="method-indicator">
              表示方式: {searchParams.method === 'indirect' ? '間接法' : '直接法'}
            </div>
          </div>

          {/* キャッシュフロー計算書本体 */}
          <CFStatement
            activities={data.activities}
            summary={data.summary}
            compareWithPrevious={searchParams.compareWithPrevious}
            expandedActivities={expandedActivities}
            onToggleActivity={onToggleActivity}
          />
        </div>
      ) : (
        <div className="empty-state">
          <p>検索条件を指定して表示してください</p>
        </div>
      )}
    </div>
  );
};
```

## 19.5 サマリーパネル

### CFSummaryPanel コンポーネント

```typescript
// components/CFSummaryPanel.tsx
import Decimal from 'decimal.js';
import { formatCurrency } from '../utils/formatUtils';
import type { CFSummary } from '../types/cashFlow';

interface CFSummaryPanelProps {
  summary: CFSummary;
  previousSummary?: CFSummary;
  compareWithPrevious: boolean;
}

export const CFSummaryPanel: React.FC<CFSummaryPanelProps> = ({
  summary,
  previousSummary,
  compareWithPrevious,
}) => {
  // 増減を計算
  const calculateChange = (current: number, previous?: number): number | null => {
    if (previous === undefined) return null;
    return new Decimal(current).minus(previous).toNumber();
  };

  const items = [
    {
      key: 'operating',
      label: '営業活動CF',
      amount: summary.operatingCF,
      previousAmount: previousSummary?.operatingCF,
      description: '本業による現金創出力',
      isPositiveGood: true,
    },
    {
      key: 'investing',
      label: '投資活動CF',
      amount: summary.investingCF,
      previousAmount: previousSummary?.investingCF,
      description: '設備投資・資産運用',
      isPositiveGood: false, // 成長企業はマイナスが多い
    },
    {
      key: 'financing',
      label: '財務活動CF',
      amount: summary.financingCF,
      previousAmount: previousSummary?.financingCF,
      description: '資金調達・返済',
      isPositiveGood: null, // 状況による
    },
    {
      key: 'free',
      label: 'フリーCF',
      amount: summary.freeCashFlow,
      previousAmount: previousSummary?.freeCashFlow,
      description: '自由に使える現金',
      isPositiveGood: true,
    },
  ];

  return (
    <div className="cf-summary-panel">
      <h2>キャッシュフロー概要</h2>

      {/* 活動別サマリー */}
      <div className="cf-summary-cards">
        {items.map(item => (
          <div
            key={item.key}
            className={`cf-summary-card ${getCFClass(item.amount, item.isPositiveGood)}`}
          >
            <div className="card-header">
              <span className="card-label">{item.label}</span>
              <span className="card-description">{item.description}</span>
            </div>
            <div className="card-amount">
              {item.amount >= 0 ? '+' : ''}
              {formatCurrency(item.amount)}
            </div>
            {compareWithPrevious && (
              <div className="card-change">
                <ChangeDisplay
                  change={calculateChange(item.amount, item.previousAmount)}
                  isPositiveGood={item.isPositiveGood}
                />
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 現金残高 */}
      <div className="cf-balance-summary">
        <div className="balance-flow">
          <div className="balance-item beginning">
            <span className="balance-label">期首残高</span>
            <span className="balance-amount">
              {formatCurrency(summary.beginningBalance)}
            </span>
          </div>
          <div className="balance-arrow">→</div>
          <div className="balance-item change">
            <span className="balance-label">増減額</span>
            <span className={`balance-amount ${summary.netChange >= 0 ? 'positive' : 'negative'}`}>
              {summary.netChange >= 0 ? '+' : ''}
              {formatCurrency(summary.netChange)}
            </span>
          </div>
          <div className="balance-arrow">→</div>
          <div className="balance-item ending">
            <span className="balance-label">期末残高</span>
            <span className="balance-amount">
              {formatCurrency(summary.endingBalance)}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

// CFの評価クラスを取得
const getCFClass = (amount: number, isPositiveGood: boolean | null): string => {
  if (isPositiveGood === null) return '';
  if (isPositiveGood) {
    return amount >= 0 ? 'positive' : 'negative';
  }
  return '';
};

// 増減表示コンポーネント
interface ChangeDisplayProps {
  change: number | null;
  isPositiveGood: boolean | null;
}

const ChangeDisplay: React.FC<ChangeDisplayProps> = ({
  change,
  isPositiveGood,
}) => {
  if (change === null) return <span>-</span>;

  const isImprovement = isPositiveGood === null
    ? null
    : isPositiveGood
      ? change > 0
      : change < 0;

  return (
    <span className={`change ${isImprovement === true ? 'improved' : isImprovement === false ? 'worsened' : ''}`}>
      前期比: {change >= 0 ? '+' : ''}{formatCurrency(change)}
    </span>
  );
};
```

## 19.6 ウォーターフォールチャート

### CFWaterfallChart コンポーネント

```typescript
// components/CFWaterfallChart.tsx
import { useRef, useEffect, useState } from 'react';
import Decimal from 'decimal.js';
import { formatCurrency } from '../utils/formatUtils';
import type { CFSummary } from '../types/cashFlow';

interface CFWaterfallChartProps {
  summary: CFSummary;
  title: string;
  height?: number;
}

interface WaterfallBar {
  label: string;
  value: number;
  startY: number;
  endY: number;
  color: string;
  isTotal: boolean;
}

export const CFWaterfallChart: React.FC<CFWaterfallChartProps> = ({
  summary,
  title,
  height = 320,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [tooltip, setTooltip] = useState<{
    x: number;
    y: number;
    label: string;
    value: number;
  } | null>(null);

  // バーのデータを計算
  const bars: WaterfallBar[] = [];
  let runningTotal = summary.beginningBalance;

  // 期首残高
  bars.push({
    label: '期首残高',
    value: summary.beginningBalance,
    startY: 0,
    endY: summary.beginningBalance,
    color: '#6b7280',
    isTotal: true,
  });

  // 営業活動CF
  const opStart = runningTotal;
  runningTotal += summary.operatingCF;
  bars.push({
    label: '営業活動CF',
    value: summary.operatingCF,
    startY: opStart,
    endY: runningTotal,
    color: summary.operatingCF >= 0 ? '#10b981' : '#ef4444',
    isTotal: false,
  });

  // 投資活動CF
  const invStart = runningTotal;
  runningTotal += summary.investingCF;
  bars.push({
    label: '投資活動CF',
    value: summary.investingCF,
    startY: invStart,
    endY: runningTotal,
    color: summary.investingCF >= 0 ? '#10b981' : '#f59e0b',
    isTotal: false,
  });

  // 財務活動CF
  const finStart = runningTotal;
  runningTotal += summary.financingCF;
  bars.push({
    label: '財務活動CF',
    value: summary.financingCF,
    startY: finStart,
    endY: runningTotal,
    color: summary.financingCF >= 0 ? '#3b82f6' : '#8b5cf6',
    isTotal: false,
  });

  // 期末残高
  bars.push({
    label: '期末残高',
    value: summary.endingBalance,
    startY: 0,
    endY: summary.endingBalance,
    color: '#111827',
    isTotal: true,
  });

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // キャンバスサイズ設定
    const width = container.clientWidth;
    const dpr = window.devicePixelRatio;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(dpr, dpr);

    // 描画領域
    const padding = { top: 50, right: 30, bottom: 60, left: 100 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // 背景クリア
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);

    // Y軸の範囲を計算
    const allValues = bars.flatMap(b => [b.startY, b.endY]);
    const maxValue = Math.max(...allValues, 0);
    const minValue = Math.min(...allValues, 0);
    const range = maxValue - minValue || 1;
    const yPadding = range * 0.1;
    const yMax = maxValue + yPadding;
    const yMin = minValue - yPadding;
    const yRange = yMax - yMin;

    // Y座標変換関数
    const toY = (value: number): number => {
      return padding.top + ((yMax - value) / yRange) * chartHeight;
    };

    // グリッド線描画
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 1;
    const gridCount = 5;
    for (let i = 0; i <= gridCount; i++) {
      const value = yMax - (yRange * i) / gridCount;
      const y = toY(value);

      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();

      // Y軸ラベル
      ctx.fillStyle = '#6b7280';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(formatCompactCurrency(value), padding.left - 8, y + 4);
    }

    // ゼロライン
    const zeroY = toY(0);
    ctx.strokeStyle = '#374151';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(padding.left, zeroY);
    ctx.lineTo(width - padding.right, zeroY);
    ctx.stroke();

    // バー描画
    const barWidth = chartWidth / bars.length * 0.6;
    const barGap = chartWidth / bars.length * 0.4;

    bars.forEach((bar, index) => {
      const x = padding.left + index * (barWidth + barGap) + barGap / 2;

      // バーの高さ計算
      const y1 = toY(bar.startY);
      const y2 = toY(bar.endY);
      const barTop = Math.min(y1, y2);
      const barHeight = Math.abs(y2 - y1);

      // バー描画
      ctx.fillStyle = bar.color;
      ctx.fillRect(x, barTop, barWidth, barHeight);

      // 接続線（非合計バー間）
      if (index > 0 && index < bars.length - 1 && !bar.isTotal) {
        ctx.strokeStyle = '#9ca3af';
        ctx.lineWidth = 1;
        ctx.setLineDash([4, 2]);
        ctx.beginPath();
        ctx.moveTo(x - barGap, y1);
        ctx.lineTo(x, y1);
        ctx.stroke();
        ctx.setLineDash([]);
      }

      // 値ラベル
      const labelY = bar.value >= 0 ? barTop - 8 : barTop + barHeight + 16;
      ctx.fillStyle = '#111827';
      ctx.font = '12px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(
        (bar.value >= 0 && !bar.isTotal ? '+' : '') + formatCompactCurrency(bar.value),
        x + barWidth / 2,
        labelY
      );

      // X軸ラベル
      ctx.fillStyle = '#374151';
      ctx.font = '12px sans-serif';
      ctx.fillText(bar.label, x + barWidth / 2, height - padding.bottom + 20);
    });

    // タイトル
    ctx.fillStyle = '#111827';
    ctx.font = 'bold 14px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText(title, padding.left, 24);

  }, [bars, title, height]);

  // マウスイベント
  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const width = container.clientWidth;
    const padding = { left: 100, right: 30 };
    const chartWidth = width - padding.left - padding.right;
    const barWidth = chartWidth / bars.length * 0.6;
    const barGap = chartWidth / bars.length * 0.4;

    // どのバーの上にいるか判定
    for (let i = 0; i < bars.length; i++) {
      const barX = padding.left + i * (barWidth + barGap) + barGap / 2;
      if (x >= barX && x <= barX + barWidth) {
        setTooltip({
          x: e.clientX - rect.left,
          y: e.clientY - rect.top - 40,
          label: bars[i].label,
          value: bars[i].value,
        });
        return;
      }
    }
    setTooltip(null);
  };

  const handleMouseLeave = () => {
    setTooltip(null);
  };

  return (
    <div ref={containerRef} className="cf-waterfall-chart">
      <canvas
        ref={canvasRef}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
      />
      {tooltip && (
        <div
          className="chart-tooltip"
          style={{ left: tooltip.x, top: tooltip.y }}
        >
          <div className="tooltip-label">{tooltip.label}</div>
          <div className="tooltip-value">
            {tooltip.value >= 0 ? '+' : ''}
            {formatCurrency(tooltip.value)}
          </div>
        </div>
      )}
    </div>
  );
};

// 金額を短縮表示
const formatCompactCurrency = (value: number): string => {
  const abs = Math.abs(value);
  const sign = value < 0 ? '-' : '';

  if (abs >= 100000000) {
    return `${sign}${new Decimal(abs).dividedBy(100000000).toFixed(1)}億`;
  }
  if (abs >= 10000) {
    return `${sign}${new Decimal(abs).dividedBy(10000).toFixed(0)}万`;
  }
  return `${sign}${abs.toLocaleString()}`;
};
```

## 19.7 キャッシュフロー計算書本体

### CFStatement コンポーネント

```typescript
// components/CFStatement.tsx
import { CFActivitySection } from './CFActivitySection';
import { CFTotalRow } from './CFTotalRow';
import { formatCurrency } from '../utils/formatUtils';
import type { ActivityCashFlow, CFSummary } from '../types/cashFlow';

interface CFStatementProps {
  activities: ActivityCashFlow[];
  summary: CFSummary;
  compareWithPrevious: boolean;
  expandedActivities: Set<string>;
  onToggleActivity: (activity: string) => void;
}

export const CFStatement: React.FC<CFStatementProps> = ({
  activities,
  summary,
  compareWithPrevious,
  expandedActivities,
  onToggleActivity,
}) => {
  return (
    <div className="cf-statement">
      <table className="cf-table">
        <thead>
          <tr>
            <th className="col-name">科目</th>
            <th className="col-amount">当期金額</th>
            {compareWithPrevious && (
              <>
                <th className="col-amount">前期金額</th>
                <th className="col-change">増減</th>
              </>
            )}
          </tr>
        </thead>
        <tbody>
          {/* 各活動区分 */}
          {activities.map(activity => (
            <CFActivitySection
              key={activity.activity}
              activity={activity}
              isExpanded={expandedActivities.has(activity.activity)}
              compareWithPrevious={compareWithPrevious}
              onToggle={() => onToggleActivity(activity.activity)}
            />
          ))}

          {/* 現金増減額 */}
          <tr className="cf-net-change">
            <td className="col-name">
              現金及び現金同等物の増減額
            </td>
            <td className={`col-amount ${summary.netChange >= 0 ? 'positive' : 'negative'}`}>
              {formatCurrency(summary.netChange)}
            </td>
            {compareWithPrevious && (
              <>
                <td className="col-amount">-</td>
                <td className="col-change">-</td>
              </>
            )}
          </tr>

          {/* 期首残高 */}
          <tr className="cf-balance-row">
            <td className="col-name">
              現金及び現金同等物の期首残高
            </td>
            <td className="col-amount">
              {formatCurrency(summary.beginningBalance)}
            </td>
            {compareWithPrevious && (
              <>
                <td className="col-amount">-</td>
                <td className="col-change">-</td>
              </>
            )}
          </tr>

          {/* 期末残高 */}
          <tr className="cf-balance-row ending">
            <td className="col-name">
              現金及び現金同等物の期末残高
            </td>
            <td className="col-amount">
              {formatCurrency(summary.endingBalance)}
            </td>
            {compareWithPrevious && (
              <>
                <td className="col-amount">-</td>
                <td className="col-change">-</td>
              </>
            )}
          </tr>
        </tbody>
      </table>
    </div>
  );
};
```

### CFActivitySection コンポーネント

```typescript
// components/CFActivitySection.tsx
import Decimal from 'decimal.js';
import { formatCurrency } from '../utils/formatUtils';
import type { ActivityCashFlow, CFItem } from '../types/cashFlow';

interface CFActivitySectionProps {
  activity: ActivityCashFlow;
  isExpanded: boolean;
  compareWithPrevious: boolean;
  onToggle: () => void;
}

const ACTIVITY_NUMBERS: Record<string, string> = {
  operating: 'Ⅰ',
  investing: 'Ⅱ',
  financing: 'Ⅲ',
};

export const CFActivitySection: React.FC<CFActivitySectionProps> = ({
  activity,
  isExpanded,
  compareWithPrevious,
  onToggle,
}) => {
  // 増減を計算
  const calculateChange = (current: number, previous?: number): number | null => {
    if (previous === undefined) return null;
    return new Decimal(current).minus(previous).toNumber();
  };

  return (
    <>
      {/* 活動ヘッダー */}
      <tr className={`activity-header ${activity.activity}`}>
        <td className="col-name" colSpan={compareWithPrevious ? 4 : 2}>
          <button
            className="activity-toggle"
            onClick={onToggle}
            aria-expanded={isExpanded}
          >
            <span className={`toggle-icon ${isExpanded ? 'expanded' : ''}`}>
              ▶
            </span>
            {ACTIVITY_NUMBERS[activity.activity]}. {activity.label}
          </button>
        </td>
      </tr>

      {/* 活動内の項目 */}
      {isExpanded && activity.items.map(item => (
        <CFItemRow
          key={item.id}
          item={item}
          compareWithPrevious={compareWithPrevious}
        />
      ))}

      {/* 活動小計 */}
      <tr className={`activity-subtotal ${activity.activity}`}>
        <td className="col-name">
          {activity.label}
        </td>
        <td className={`col-amount ${activity.subtotal >= 0 ? 'positive' : 'negative'}`}>
          {formatCurrency(activity.subtotal)}
        </td>
        {compareWithPrevious && (
          <>
            <td className="col-amount">
              {activity.previousSubtotal !== undefined
                ? formatCurrency(activity.previousSubtotal)
                : '-'}
            </td>
            <td className="col-change">
              {activity.previousSubtotal !== undefined && (
                <ChangeIndicator
                  change={calculateChange(activity.subtotal, activity.previousSubtotal)}
                />
              )}
            </td>
          </>
        )}
      </tr>
    </>
  );
};

// 項目行コンポーネント
interface CFItemRowProps {
  item: CFItem;
  compareWithPrevious: boolean;
}

const CFItemRow: React.FC<CFItemRowProps> = ({
  item,
  compareWithPrevious,
}) => {
  const change = item.previousAmount !== undefined
    ? new Decimal(item.amount).minus(item.previousAmount).toNumber()
    : null;

  return (
    <tr className={`cf-item level-${item.level}`}>
      <td className="col-name">
        <span style={{ paddingLeft: `${(item.level + 1) * 16}px` }}>
          {item.name}
        </span>
      </td>
      <td className="col-amount">
        {formatCurrency(item.amount)}
      </td>
      {compareWithPrevious && (
        <>
          <td className="col-amount">
            {item.previousAmount !== undefined
              ? formatCurrency(item.previousAmount)
              : '-'}
          </td>
          <td className="col-change">
            {change !== null && <ChangeIndicator change={change} />}
          </td>
        </>
      )}
    </tr>
  );
};

// 増減表示コンポーネント
interface ChangeIndicatorProps {
  change: number | null;
}

const ChangeIndicator: React.FC<ChangeIndicatorProps> = ({ change }) => {
  if (change === null) return <span>-</span>;

  return (
    <span className={`change-indicator ${change >= 0 ? 'positive' : 'negative'}`}>
      {change >= 0 ? '+' : ''}
      {formatCurrency(change)}
    </span>
  );
};
```

## 19.8 資金繰り予測

### CashFlowForecastContainer

```typescript
// containers/CashFlowForecastContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetCashFlowForecast } from '../generated/api/cash-flow';
import { CashFlowForecastView } from '../views/CashFlowForecastView';
import { formatDate } from '../utils/dateUtils';

export const CashFlowForecastContainer: React.FC = () => {
  const [baseDate, setBaseDate] = useState(formatDate(new Date()));
  const [months, setMonths] = useState(3);

  const { data, isLoading, error } = useGetCashFlowForecast({
    baseDate,
    months,
  });

  // 資金ショートのリスク判定
  const riskAssessment = useMemo(() => {
    if (!data) return null;

    const minBalance = Math.min(...data.forecast.map(f => f.endingBalance));
    const alerts: string[] = [];

    if (minBalance < 0) {
      alerts.push('資金ショートの可能性があります');
    } else if (minBalance < data.safetyLine) {
      alerts.push('安全ラインを下回る可能性があります');
    }

    // 大口入金・出金の予定
    data.forecast.forEach(month => {
      if (month.largeInflows.length > 0) {
        alerts.push(`${month.monthLabel}: 大口入金予定あり`);
      }
      if (month.largeOutflows.length > 0) {
        alerts.push(`${month.monthLabel}: 大口出金予定あり`);
      }
    });

    return {
      minBalance,
      isRisk: minBalance < data.safetyLine,
      alerts,
    };
  }, [data]);

  const handleBaseDateChange = useCallback((date: string) => {
    setBaseDate(date);
  }, []);

  const handleMonthsChange = useCallback((m: number) => {
    setMonths(m);
  }, []);

  if (error) {
    return <div className="error-state">データの取得に失敗しました</div>;
  }

  return (
    <CashFlowForecastView
      data={data}
      baseDate={baseDate}
      months={months}
      riskAssessment={riskAssessment}
      isLoading={isLoading}
      onBaseDateChange={handleBaseDateChange}
      onMonthsChange={handleMonthsChange}
    />
  );
};
```

### CashFlowForecastChart コンポーネント

```typescript
// components/CashFlowForecastChart.tsx
import { useRef, useEffect } from 'react';
import Decimal from 'decimal.js';
import { formatCurrency } from '../utils/formatUtils';

interface ForecastDataPoint {
  month: string;
  monthLabel: string;
  beginningBalance: number;
  inflows: number;
  outflows: number;
  endingBalance: number;
  isForecast: boolean;
}

interface CashFlowForecastChartProps {
  data: ForecastDataPoint[];
  safetyLine: number;
  height?: number;
}

export const CashFlowForecastChart: React.FC<CashFlowForecastChartProps> = ({
  data,
  safetyLine,
  height = 280,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container || data.length === 0) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // キャンバスサイズ設定
    const width = container.clientWidth;
    const dpr = window.devicePixelRatio;
    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(dpr, dpr);

    // 描画領域
    const padding = { top: 40, right: 30, bottom: 50, left: 80 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // 背景クリア
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);

    // Y軸の範囲を計算
    const balances = data.map(d => d.endingBalance);
    const maxBalance = Math.max(...balances, safetyLine);
    const minBalance = Math.min(...balances, 0);
    const range = maxBalance - minBalance || 1;
    const yPadding = range * 0.1;
    const yMax = maxBalance + yPadding;
    const yMin = minBalance - yPadding;
    const yRange = yMax - yMin;

    // Y座標変換関数
    const toY = (value: number): number => {
      return padding.top + ((yMax - value) / yRange) * chartHeight;
    };

    // グリッド線描画
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 1;
    const gridCount = 5;
    for (let i = 0; i <= gridCount; i++) {
      const value = yMax - (yRange * i) / gridCount;
      const y = toY(value);

      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();

      // Y軸ラベル
      ctx.fillStyle = '#6b7280';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(formatCompactCurrency(value), padding.left - 8, y + 4);
    }

    // 安全ライン
    const safetyY = toY(safetyLine);
    ctx.strokeStyle = '#f59e0b';
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 4]);
    ctx.beginPath();
    ctx.moveTo(padding.left, safetyY);
    ctx.lineTo(width - padding.right, safetyY);
    ctx.stroke();
    ctx.setLineDash([]);

    // 安全ラインラベル
    ctx.fillStyle = '#f59e0b';
    ctx.font = '11px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText('安全ライン', padding.left + 4, safetyY - 6);

    // X軸設定
    const pointGap = chartWidth / (data.length - 1);

    // 残高ラインを描画
    ctx.beginPath();
    data.forEach((point, index) => {
      const x = padding.left + index * pointGap;
      const y = toY(point.endingBalance);

      if (index === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    ctx.strokeStyle = '#3b82f6';
    ctx.lineWidth = 2;
    ctx.stroke();

    // 予測部分を破線で上書き
    const forecastStartIndex = data.findIndex(d => d.isForecast);
    if (forecastStartIndex > 0) {
      ctx.beginPath();
      ctx.setLineDash([6, 4]);
      for (let i = forecastStartIndex; i < data.length; i++) {
        const x = padding.left + i * pointGap;
        const y = toY(data[i].endingBalance);

        if (i === forecastStartIndex) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      }
      ctx.strokeStyle = '#93c5fd';
      ctx.lineWidth = 2;
      ctx.stroke();
      ctx.setLineDash([]);
    }

    // データポイント描画
    data.forEach((point, index) => {
      const x = padding.left + index * pointGap;
      const y = toY(point.endingBalance);

      // 円形マーカー
      ctx.beginPath();
      ctx.arc(x, y, 5, 0, Math.PI * 2);
      ctx.fillStyle = point.isForecast ? '#93c5fd' : '#3b82f6';
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();

      // 残高がマイナスの場合は警告色
      if (point.endingBalance < 0) {
        ctx.beginPath();
        ctx.arc(x, y, 7, 0, Math.PI * 2);
        ctx.strokeStyle = '#ef4444';
        ctx.lineWidth = 2;
        ctx.stroke();
      }

      // X軸ラベル
      ctx.fillStyle = '#374151';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(point.monthLabel, x, height - padding.bottom + 20);
    });

    // 凡例
    const legendY = 18;
    ctx.font = '11px sans-serif';

    // 実績
    ctx.fillStyle = '#3b82f6';
    ctx.fillRect(width - 180, legendY - 8, 12, 12);
    ctx.fillStyle = '#374151';
    ctx.textAlign = 'left';
    ctx.fillText('実績', width - 164, legendY);

    // 予測
    ctx.fillStyle = '#93c5fd';
    ctx.fillRect(width - 110, legendY - 8, 12, 12);
    ctx.fillStyle = '#374151';
    ctx.fillText('予測', width - 94, legendY);

  }, [data, safetyLine, height]);

  return (
    <div ref={containerRef} className="cf-forecast-chart">
      <canvas ref={canvasRef} />
    </div>
  );
};

// 金額を短縮表示
const formatCompactCurrency = (value: number): string => {
  const abs = Math.abs(value);
  const sign = value < 0 ? '-' : '';

  if (abs >= 100000000) {
    return `${sign}${new Decimal(abs).dividedBy(100000000).toFixed(1)}億`;
  }
  if (abs >= 10000) {
    return `${sign}${new Decimal(abs).dividedBy(10000).toFixed(0)}万`;
  }
  return `${sign}${abs.toLocaleString()}`;
};
```

## 19.9 月次推移分析

### CFMonthlyTrendTable コンポーネント

```typescript
// components/CFMonthlyTrendTable.tsx
import { useMemo } from 'react';
import { formatCurrency } from '../utils/formatUtils';

interface MonthlyTrendData {
  month: string;
  monthLabel: string;
  operatingCF: number;
  investingCF: number;
  financingCF: number;
  netChange: number;
  endingBalance: number;
}

interface CFMonthlyTrendTableProps {
  data: MonthlyTrendData[];
  showCumulative: boolean;
}

export const CFMonthlyTrendTable: React.FC<CFMonthlyTrendTableProps> = ({
  data,
  showCumulative,
}) => {
  // 累計データを計算
  const displayData = useMemo(() => {
    if (!showCumulative) return data;

    let cumOperating = 0;
    let cumInvesting = 0;
    let cumFinancing = 0;
    let cumNet = 0;

    return data.map(month => {
      cumOperating += month.operatingCF;
      cumInvesting += month.investingCF;
      cumFinancing += month.financingCF;
      cumNet += month.netChange;

      return {
        ...month,
        operatingCF: cumOperating,
        investingCF: cumInvesting,
        financingCF: cumFinancing,
        netChange: cumNet,
      };
    });
  }, [data, showCumulative]);

  // 年間合計を計算
  const yearTotal = useMemo(() => ({
    operatingCF: data.reduce((sum, m) => sum + m.operatingCF, 0),
    investingCF: data.reduce((sum, m) => sum + m.investingCF, 0),
    financingCF: data.reduce((sum, m) => sum + m.financingCF, 0),
    netChange: data.reduce((sum, m) => sum + m.netChange, 0),
  }), [data]);

  return (
    <div className="cf-monthly-trend-table">
      <table>
        <thead>
          <tr>
            <th className="col-label">項目</th>
            {displayData.map(month => (
              <th key={month.month} className="col-month">
                {month.monthLabel}
              </th>
            ))}
            <th className="col-total">年間合計</th>
          </tr>
        </thead>
        <tbody>
          {/* 営業活動CF */}
          <tr className="row-operating">
            <td className="col-label">営業活動CF</td>
            {displayData.map(month => (
              <td
                key={month.month}
                className={`col-amount ${month.operatingCF >= 0 ? 'positive' : 'negative'}`}
              >
                {formatCurrency(month.operatingCF)}
              </td>
            ))}
            <td className={`col-total ${yearTotal.operatingCF >= 0 ? 'positive' : 'negative'}`}>
              {formatCurrency(yearTotal.operatingCF)}
            </td>
          </tr>

          {/* 投資活動CF */}
          <tr className="row-investing">
            <td className="col-label">投資活動CF</td>
            {displayData.map(month => (
              <td key={month.month} className="col-amount">
                {formatCurrency(month.investingCF)}
              </td>
            ))}
            <td className="col-total">
              {formatCurrency(yearTotal.investingCF)}
            </td>
          </tr>

          {/* 財務活動CF */}
          <tr className="row-financing">
            <td className="col-label">財務活動CF</td>
            {displayData.map(month => (
              <td key={month.month} className="col-amount">
                {formatCurrency(month.financingCF)}
              </td>
            ))}
            <td className="col-total">
              {formatCurrency(yearTotal.financingCF)}
            </td>
          </tr>

          {/* 増減額 */}
          <tr className="row-net-change">
            <td className="col-label">現金増減額</td>
            {displayData.map(month => (
              <td
                key={month.month}
                className={`col-amount ${month.netChange >= 0 ? 'positive' : 'negative'}`}
              >
                {formatCurrency(month.netChange)}
              </td>
            ))}
            <td className={`col-total ${yearTotal.netChange >= 0 ? 'positive' : 'negative'}`}>
              {formatCurrency(yearTotal.netChange)}
            </td>
          </tr>

          {/* 期末残高（累計表示の場合のみ） */}
          {!showCumulative && (
            <tr className="row-ending-balance">
              <td className="col-label">期末残高</td>
              {displayData.map(month => (
                <td key={month.month} className="col-amount">
                  {formatCurrency(month.endingBalance)}
                </td>
              ))}
              <td className="col-total">
                {data.length > 0
                  ? formatCurrency(data[data.length - 1].endingBalance)
                  : '-'}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
```

## 19.10 スタイリング

```css
/* styles/cash-flow.css */

/* キャッシュフロー計算書ページ */
.cash-flow-view {
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
  color: #111827;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* サマリーパネル */
.cf-summary-panel {
  margin-bottom: 24px;
}

.cf-summary-panel h2 {
  font-size: 16px;
  margin-bottom: 16px;
}

.cf-summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.cf-summary-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.cf-summary-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cf-summary-card.positive {
  border-left: 4px solid #10b981;
}

.cf-summary-card.negative {
  border-left: 4px solid #ef4444;
}

.card-header {
  margin-bottom: 8px;
}

.card-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.card-description {
  display: block;
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.card-amount {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.card-change {
  font-size: 12px;
  color: #6b7280;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.card-change .improved {
  color: #10b981;
}

.card-change .worsened {
  color: #ef4444;
}

/* 残高フロー */
.cf-balance-summary {
  background: #f9fafb;
  border-radius: 8px;
  padding: 20px;
}

.balance-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.balance-item {
  text-align: center;
  padding: 12px 20px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.balance-item.ending {
  background: #eff6ff;
  border-color: #3b82f6;
}

.balance-label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.balance-amount {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.balance-amount.positive {
  color: #10b981;
}

.balance-amount.negative {
  color: #ef4444;
}

.balance-arrow {
  font-size: 20px;
  color: #9ca3af;
}

/* ウォーターフォールチャート */
.cf-waterfall-chart {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  position: relative;
}

.chart-tooltip {
  position: absolute;
  background: #1f2937;
  color: #ffffff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  pointer-events: none;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.tooltip-label {
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 2px;
}

.tooltip-value {
  font-size: 14px;
  font-weight: 600;
}

/* 表示コントロール */
.display-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.method-indicator {
  font-size: 13px;
  color: #6b7280;
}

/* キャッシュフロー計算書テーブル */
.cf-statement {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.cf-table {
  width: 100%;
  border-collapse: collapse;
}

.cf-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.cf-table th.col-amount,
.cf-table th.col-change {
  text-align: right;
}

.cf-table td {
  padding: 10px 16px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
}

.cf-table td.col-amount,
.cf-table td.col-change {
  text-align: right;
  font-family: 'SF Mono', monospace;
}

/* 活動ヘッダー */
.activity-header {
  background: #f9fafb;
}

.activity-header td {
  font-weight: 600;
  padding: 14px 16px;
}

.activity-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 15px;
  color: #111827;
}

.toggle-icon {
  font-size: 10px;
  transition: transform 0.2s;
}

.toggle-icon.expanded {
  transform: rotate(90deg);
}

/* 活動小計 */
.activity-subtotal {
  background: #f3f4f6;
  font-weight: 600;
}

.activity-subtotal.operating td.col-amount.positive {
  color: #10b981;
}

.activity-subtotal.operating td.col-amount.negative {
  color: #ef4444;
}

/* 項目行 */
.cf-item {
  transition: background-color 0.15s;
}

.cf-item:hover {
  background: #f9fafb;
}

/* 増減額・残高行 */
.cf-net-change {
  background: #dbeafe;
  font-weight: 600;
}

.cf-net-change td.col-amount.positive {
  color: #10b981;
}

.cf-net-change td.col-amount.negative {
  color: #ef4444;
}

.cf-balance-row {
  background: #f9fafb;
}

.cf-balance-row.ending {
  background: #eff6ff;
  font-weight: 600;
}

/* 増減表示 */
.change-indicator.positive {
  color: #10b981;
}

.change-indicator.negative {
  color: #ef4444;
}

/* 月次推移テーブル */
.cf-monthly-trend-table {
  overflow-x: auto;
}

.cf-monthly-trend-table table {
  width: 100%;
  min-width: 800px;
  border-collapse: collapse;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.cf-monthly-trend-table th,
.cf-monthly-trend-table td {
  padding: 10px 12px;
  text-align: right;
  font-size: 13px;
  border-bottom: 1px solid #e5e7eb;
}

.cf-monthly-trend-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.cf-monthly-trend-table .col-label {
  text-align: left;
  font-weight: 500;
  min-width: 120px;
}

.cf-monthly-trend-table .col-total {
  background: #f3f4f6;
  font-weight: 600;
}

.cf-monthly-trend-table .positive {
  color: #10b981;
}

.cf-monthly-trend-table .negative {
  color: #ef4444;
}

.cf-monthly-trend-table .row-net-change {
  background: #eff6ff;
}

.cf-monthly-trend-table .row-ending-balance {
  background: #f9fafb;
  font-weight: 600;
}

/* 資金繰り予測チャート */
.cf-forecast-chart {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}

/* リスクアラート */
.cf-risk-alert {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.cf-risk-alert.warning {
  background: #fffbeb;
  border-color: #fde68a;
}

.risk-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 8px;
}

.risk-title.warning {
  color: #d97706;
}

.risk-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.risk-list li {
  padding: 4px 0;
  font-size: 13px;
  color: #374151;
}
```

## 19.11 まとめ

本章では、キャッシュフロー計算書の表示機能を実装した。主なポイントは以下の通りである：

1. **3つの活動区分**: 営業・投資・財務活動ごとにキャッシュフローを区分表示
2. **間接法による表示**: 税引前当期純利益からの調整形式で営業CFを計算
3. **ウォーターフォールチャート**: 期首残高から期末残高への資金の流れを可視化
4. **月次推移分析**: 月ごとのキャッシュフロー推移を表形式で表示
5. **資金繰り予測**: 将来の現金残高予測と安全ラインによるリスク判定

キャッシュフロー計算書は、企業の資金繰りを把握する上で不可欠な財務諸表である。貸借対照表・損益計算書と合わせて三表体制を構成し、企業の財務状況を多角的に分析できる。

これで第6部「財務諸表機能」の実装が完了した。次章以降では、発展的なトピックとして、パフォーマンス最適化やセキュリティ対策などについて解説する。
