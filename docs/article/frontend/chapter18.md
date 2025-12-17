# 第18章 損益計算書

本章では、損益計算書（Profit and Loss Statement、P/L）の表示機能を実装する。収益・費用の区分表示、段階利益の算出、期間比較分析など、経営判断に必要な情報を提供するコンポーネントを構築していく。

## 18.1 損益計算書の構造

### 損益計算書の区分

損益計算書は、企業の一定期間における経営成績を示す財務諸表である。日本の会計基準では、以下の5つの段階利益を表示する：

```
売上高
－ 売上原価
────────────────
売上総利益（粗利）

－ 販売費及び一般管理費
────────────────
営業利益

＋ 営業外収益
－ 営業外費用
────────────────
経常利益

＋ 特別利益
－ 特別損失
────────────────
税引前当期純利益

－ 法人税等
────────────────
当期純利益
```

### 型定義

```typescript
// types/profitAndLoss.ts

/** 損益計算書区分 */
export type PLSection =
  | 'sales'           // 売上高
  | 'costOfSales'     // 売上原価
  | 'sgAndA'          // 販売費及び一般管理費
  | 'nonOperatingIncome'   // 営業外収益
  | 'nonOperatingExpense'  // 営業外費用
  | 'extraordinaryIncome'  // 特別利益
  | 'extraordinaryLoss'    // 特別損失
  | 'incomeTax';      // 法人税等

/** 段階利益種別 */
export type ProfitType =
  | 'grossProfit'     // 売上総利益
  | 'operatingProfit' // 営業利益
  | 'ordinaryProfit'  // 経常利益
  | 'profitBeforeTax' // 税引前当期純利益
  | 'netProfit';      // 当期純利益

/** 損益計算書科目 */
export interface PLItem {
  id: string;
  code: string;
  name: string;
  section: PLSection;
  amount: number;
  previousAmount?: number;
  budgetAmount?: number;
  children?: PLItem[];
  level: number;
  displayOrder: number;
}

/** 段階利益 */
export interface StagedProfit {
  type: ProfitType;
  label: string;
  amount: number;
  previousAmount?: number;
  budgetAmount?: number;
  ratio?: number;  // 売上高比率
}

/** 損益計算書データ */
export interface ProfitAndLossData {
  periodStart: string;
  periodEnd: string;
  items: PLItem[];
  profits: StagedProfit[];
  totalSales: number;
  previousTotalSales?: number;
}

/** 損益計算書検索条件 */
export interface PLSearchParams {
  periodStart: string;
  periodEnd: string;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
  departmentId?: string;
  projectId?: string;
}
```

## 18.2 API 連携

### OpenAPI 定義

```yaml
# openapi/paths/profit-and-loss.yaml
/api/profit-and-loss:
  get:
    operationId: getProfitAndLoss
    summary: 損益計算書取得
    tags:
      - ProfitAndLoss
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
      - name: compareWithBudget
        in: query
        schema:
          type: boolean
          default: false
      - name: departmentId
        in: query
        schema:
          type: string
      - name: projectId
        in: query
        schema:
          type: string
    responses:
      '200':
        description: 損益計算書
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProfitAndLossResponse'

/api/profit-and-loss/monthly-trend:
  get:
    operationId: getPLMonthlyTrend
    summary: 損益月次推移取得
    tags:
      - ProfitAndLoss
    parameters:
      - name: fiscalYear
        in: query
        required: true
        schema:
          type: integer
      - name: departmentId
        in: query
        schema:
          type: string
    responses:
      '200':
        description: 月次推移データ
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PLMonthlyTrendResponse'

/api/profit-and-loss/expense-breakdown:
  get:
    operationId: getExpenseBreakdown
    summary: 費用内訳取得
    tags:
      - ProfitAndLoss
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
      - name: section
        in: query
        required: true
        schema:
          type: string
          enum: [costOfSales, sgAndA]
    responses:
      '200':
        description: 費用内訳
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ExpenseBreakdownResponse'
```

### Orval 生成フック

```typescript
// generated/api/profit-and-loss.ts
import { useQuery } from '@tanstack/react-query';
import type {
  ProfitAndLossResponse,
  PLMonthlyTrendResponse,
  ExpenseBreakdownResponse,
  GetProfitAndLossParams,
  GetPLMonthlyTrendParams,
  GetExpenseBreakdownParams,
} from '../model';
import { apiClient } from '../client';

export const getProfitAndLossQueryKey = (params: GetProfitAndLossParams) =>
  ['profit-and-loss', params] as const;

export const useGetProfitAndLoss = (
  params: GetProfitAndLossParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getProfitAndLossQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<ProfitAndLossResponse>(
        '/api/profit-and-loss',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};

export const getPLMonthlyTrendQueryKey = (params: GetPLMonthlyTrendParams) =>
  ['profit-and-loss', 'monthly-trend', params] as const;

export const useGetPLMonthlyTrend = (
  params: GetPLMonthlyTrendParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getPLMonthlyTrendQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<PLMonthlyTrendResponse>(
        '/api/profit-and-loss/monthly-trend',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};

export const getExpenseBreakdownQueryKey = (params: GetExpenseBreakdownParams) =>
  ['profit-and-loss', 'expense-breakdown', params] as const;

export const useGetExpenseBreakdown = (
  params: GetExpenseBreakdownParams,
  options?: { enabled?: boolean }
) => {
  return useQuery({
    queryKey: getExpenseBreakdownQueryKey(params),
    queryFn: async () => {
      const response = await apiClient.get<ExpenseBreakdownResponse>(
        '/api/profit-and-loss/expense-breakdown',
        { params }
      );
      return response.data;
    },
    ...options,
  });
};
```

## 18.3 Container 実装

### ProfitAndLossContainer

```typescript
// containers/ProfitAndLossContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetProfitAndLoss } from '../generated/api/profit-and-loss';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { useMessage } from '../contexts/MessageContext';
import { usePLExport } from '../hooks/usePLExport';
import { ProfitAndLossView } from '../views/ProfitAndLossView';
import type { PLSearchParams } from '../types/profitAndLoss';
import { formatDate, getMonthRange } from '../utils/dateUtils';

export const ProfitAndLossContainer: React.FC = () => {
  const { currentPeriod } = useAccountingPeriod();
  const { showMessage } = useMessage();
  const { exportToPdf, exportToExcel, isExporting } = usePLExport();

  // 検索条件
  const [searchParams, setSearchParams] = useState<PLSearchParams>(() => {
    const { start, end } = getMonthRange(new Date());
    return {
      periodStart: formatDate(start),
      periodEnd: formatDate(end),
      compareWithPrevious: false,
      compareWithBudget: false,
    };
  });

  // 表示設定
  const [showRatio, setShowRatio] = useState(true);
  const [expandedSections, setExpandedSections] = useState<Set<string>>(
    new Set(['sales', 'costOfSales', 'sgAndA'])
  );

  // データ取得
  const {
    data: plData,
    isLoading,
    error,
    refetch,
  } = useGetProfitAndLoss(searchParams);

  // 検索実行
  const handleSearch = useCallback((params: PLSearchParams) => {
    setSearchParams(params);
  }, []);

  // セクション展開/折りたたみ
  const handleToggleSection = useCallback((section: string) => {
    setExpandedSections(prev => {
      const next = new Set(prev);
      if (next.has(section)) {
        next.delete(section);
      } else {
        next.add(section);
      }
      return next;
    });
  }, []);

  // 全展開/全折りたたみ
  const handleExpandAll = useCallback(() => {
    const allSections = [
      'sales', 'costOfSales', 'sgAndA',
      'nonOperatingIncome', 'nonOperatingExpense',
      'extraordinaryIncome', 'extraordinaryLoss', 'incomeTax'
    ];
    setExpandedSections(new Set(allSections));
  }, []);

  const handleCollapseAll = useCallback(() => {
    setExpandedSections(new Set());
  }, []);

  // PDF 出力
  const handleExportPdf = useCallback(async () => {
    if (!plData) return;

    try {
      await exportToPdf(plData, searchParams);
      showMessage('success', 'PDF を出力しました');
    } catch (e) {
      showMessage('error', 'PDF 出力に失敗しました');
    }
  }, [plData, searchParams, exportToPdf, showMessage]);

  // Excel 出力
  const handleExportExcel = useCallback(async () => {
    if (!plData) return;

    try {
      await exportToExcel(plData, searchParams);
      showMessage('success', 'Excel を出力しました');
    } catch (e) {
      showMessage('error', 'Excel 出力に失敗しました');
    }
  }, [plData, searchParams, exportToExcel, showMessage]);

  // 売上高比率計算
  const itemsWithRatio = useMemo(() => {
    if (!plData) return [];

    const totalSales = plData.totalSales;
    if (totalSales === 0) return plData.items;

    return plData.items.map(item => ({
      ...item,
      ratio: (item.amount / totalSales) * 100,
      previousRatio: item.previousAmount && plData.previousTotalSales
        ? (item.previousAmount / plData.previousTotalSales) * 100
        : undefined,
    }));
  }, [plData]);

  if (error) {
    return (
      <div className="error-container">
        <p>データの取得に失敗しました</p>
        <button onClick={() => refetch()}>再試行</button>
      </div>
    );
  }

  return (
    <ProfitAndLossView
      data={plData ? { ...plData, items: itemsWithRatio } : undefined}
      searchParams={searchParams}
      isLoading={isLoading}
      isExporting={isExporting}
      showRatio={showRatio}
      expandedSections={expandedSections}
      onSearch={handleSearch}
      onToggleSection={handleToggleSection}
      onExpandAll={handleExpandAll}
      onCollapseAll={handleCollapseAll}
      onToggleRatio={() => setShowRatio(prev => !prev)}
      onExportPdf={handleExportPdf}
      onExportExcel={handleExportExcel}
    />
  );
};
```

## 18.4 View 実装

### ProfitAndLossView

```typescript
// views/ProfitAndLossView.tsx
import { PLSearchForm } from '../components/PLSearchForm';
import { PLStatement } from '../components/PLStatement';
import { StagedProfitSummary } from '../components/StagedProfitSummary';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import type { ProfitAndLossData, PLSearchParams } from '../types/profitAndLoss';

interface ProfitAndLossViewProps {
  data?: ProfitAndLossData;
  searchParams: PLSearchParams;
  isLoading: boolean;
  isExporting: boolean;
  showRatio: boolean;
  expandedSections: Set<string>;
  onSearch: (params: PLSearchParams) => void;
  onToggleSection: (section: string) => void;
  onExpandAll: () => void;
  onCollapseAll: () => void;
  onToggleRatio: () => void;
  onExportPdf: () => void;
  onExportExcel: () => void;
}

export const ProfitAndLossView: React.FC<ProfitAndLossViewProps> = ({
  data,
  searchParams,
  isLoading,
  isExporting,
  showRatio,
  expandedSections,
  onSearch,
  onToggleSection,
  onExpandAll,
  onCollapseAll,
  onToggleRatio,
  onExportPdf,
  onExportExcel,
}) => {
  return (
    <div className="profit-and-loss-view">
      <header className="page-header">
        <h1>損益計算書</h1>
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

      <PLSearchForm
        initialValues={searchParams}
        onSearch={onSearch}
        isLoading={isLoading}
      />

      {isLoading ? (
        <LoadingSpinner />
      ) : data ? (
        <div className="pl-content">
          {/* 段階利益サマリー */}
          <StagedProfitSummary
            profits={data.profits}
            totalSales={data.totalSales}
            compareWithPrevious={searchParams.compareWithPrevious}
            compareWithBudget={searchParams.compareWithBudget}
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
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={showRatio}
                onChange={onToggleRatio}
              />
              売上高比率を表示
            </label>
          </div>

          {/* 損益計算書本体 */}
          <PLStatement
            items={data.items}
            profits={data.profits}
            showRatio={showRatio}
            compareWithPrevious={searchParams.compareWithPrevious}
            compareWithBudget={searchParams.compareWithBudget}
            expandedSections={expandedSections}
            onToggleSection={onToggleSection}
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

## 18.5 段階利益サマリー

### StagedProfitSummary コンポーネント

```typescript
// components/StagedProfitSummary.tsx
import Decimal from 'decimal.js';
import { formatCurrency, formatPercent } from '../utils/formatUtils';
import type { StagedProfit } from '../types/profitAndLoss';

interface StagedProfitSummaryProps {
  profits: StagedProfit[];
  totalSales: number;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
}

const PROFIT_LABELS: Record<string, string> = {
  grossProfit: '売上総利益',
  operatingProfit: '営業利益',
  ordinaryProfit: '経常利益',
  profitBeforeTax: '税引前当期純利益',
  netProfit: '当期純利益',
};

export const StagedProfitSummary: React.FC<StagedProfitSummaryProps> = ({
  profits,
  totalSales,
  compareWithPrevious,
  compareWithBudget,
}) => {
  // 売上高比率を計算
  const calculateRatio = (amount: number): number => {
    if (totalSales === 0) return 0;
    return new Decimal(amount)
      .dividedBy(totalSales)
      .times(100)
      .toNumber();
  };

  // 増減率を計算
  const calculateChangeRate = (
    current: number,
    previous?: number
  ): number | null => {
    if (previous === undefined || previous === 0) return null;
    return new Decimal(current)
      .minus(previous)
      .dividedBy(Math.abs(previous))
      .times(100)
      .toNumber();
  };

  // 予算達成率を計算
  const calculateAchievementRate = (
    actual: number,
    budget?: number
  ): number | null => {
    if (budget === undefined || budget === 0) return null;
    return new Decimal(actual)
      .dividedBy(budget)
      .times(100)
      .toNumber();
  };

  return (
    <div className="staged-profit-summary">
      <h2>段階利益サマリー</h2>

      <div className="profit-cards">
        {profits.map(profit => {
          const ratio = calculateRatio(profit.amount);
          const changeRate = calculateChangeRate(
            profit.amount,
            profit.previousAmount
          );
          const achievementRate = calculateAchievementRate(
            profit.amount,
            profit.budgetAmount
          );

          return (
            <div
              key={profit.type}
              className={`profit-card ${profit.amount < 0 ? 'negative' : ''}`}
            >
              <div className="profit-label">
                {PROFIT_LABELS[profit.type]}
              </div>

              <div className="profit-amount">
                {formatCurrency(profit.amount)}
              </div>

              <div className="profit-ratio">
                売上高比率: {formatPercent(ratio)}
              </div>

              {compareWithPrevious && profit.previousAmount !== undefined && (
                <div className="profit-comparison">
                  <span className="label">前期比:</span>
                  <span className={`value ${getChangeClass(changeRate)}`}>
                    {changeRate !== null
                      ? `${changeRate >= 0 ? '+' : ''}${formatPercent(changeRate)}`
                      : '-'}
                  </span>
                </div>
              )}

              {compareWithBudget && profit.budgetAmount !== undefined && (
                <div className="profit-achievement">
                  <span className="label">予算達成率:</span>
                  <span className={`value ${getAchievementClass(achievementRate)}`}>
                    {achievementRate !== null
                      ? formatPercent(achievementRate)
                      : '-'}
                  </span>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

// 増減率のクラス判定
const getChangeClass = (rate: number | null): string => {
  if (rate === null) return '';
  if (rate > 0) return 'positive';
  if (rate < 0) return 'negative';
  return '';
};

// 達成率のクラス判定
const getAchievementClass = (rate: number | null): string => {
  if (rate === null) return '';
  if (rate >= 100) return 'achieved';
  if (rate >= 80) return 'warning';
  return 'below';
};
```

## 18.6 損益計算書本体

### PLStatement コンポーネント

```typescript
// components/PLStatement.tsx
import { useMemo } from 'react';
import { PLSectionGroup } from './PLSectionGroup';
import { PLProfitRow } from './PLProfitRow';
import { formatCurrency, formatPercent } from '../utils/formatUtils';
import type { PLItem, StagedProfit, PLSection } from '../types/profitAndLoss';

interface PLStatementProps {
  items: PLItem[];
  profits: StagedProfit[];
  showRatio: boolean;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
  expandedSections: Set<string>;
  onToggleSection: (section: string) => void;
}

// セクションの表示順序と構造定義
const PL_STRUCTURE = [
  { section: 'sales' as PLSection, label: '売上高', isRevenue: true },
  { section: 'costOfSales' as PLSection, label: '売上原価', isExpense: true },
  { type: 'profit', profitType: 'grossProfit' },
  { section: 'sgAndA' as PLSection, label: '販売費及び一般管理費', isExpense: true },
  { type: 'profit', profitType: 'operatingProfit' },
  { section: 'nonOperatingIncome' as PLSection, label: '営業外収益', isRevenue: true },
  { section: 'nonOperatingExpense' as PLSection, label: '営業外費用', isExpense: true },
  { type: 'profit', profitType: 'ordinaryProfit' },
  { section: 'extraordinaryIncome' as PLSection, label: '特別利益', isRevenue: true },
  { section: 'extraordinaryLoss' as PLSection, label: '特別損失', isExpense: true },
  { type: 'profit', profitType: 'profitBeforeTax' },
  { section: 'incomeTax' as PLSection, label: '法人税等', isExpense: true },
  { type: 'profit', profitType: 'netProfit' },
];

export const PLStatement: React.FC<PLStatementProps> = ({
  items,
  profits,
  showRatio,
  compareWithPrevious,
  compareWithBudget,
  expandedSections,
  onToggleSection,
}) => {
  // セクションごとにアイテムをグループ化
  const groupedItems = useMemo(() => {
    return items.reduce<Record<PLSection, PLItem[]>>((acc, item) => {
      if (!acc[item.section]) {
        acc[item.section] = [];
      }
      acc[item.section].push(item);
      return acc;
    }, {} as Record<PLSection, PLItem[]>);
  }, [items]);

  // 段階利益をマップ化
  const profitMap = useMemo(() => {
    return profits.reduce<Record<string, StagedProfit>>((acc, profit) => {
      acc[profit.type] = profit;
      return acc;
    }, {});
  }, [profits]);

  // 列数を計算
  const columnCount = useMemo(() => {
    let count = 2; // 科目名 + 当期金額
    if (showRatio) count++;
    if (compareWithPrevious) count += 2; // 前期金額 + 増減
    if (compareWithBudget) count += 2; // 予算 + 達成率
    return count;
  }, [showRatio, compareWithPrevious, compareWithBudget]);

  return (
    <div className="pl-statement">
      <table className="pl-table">
        <thead>
          <tr>
            <th className="col-name">科目</th>
            <th className="col-amount">当期金額</th>
            {showRatio && <th className="col-ratio">構成比</th>}
            {compareWithPrevious && (
              <>
                <th className="col-amount">前期金額</th>
                <th className="col-change">増減</th>
              </>
            )}
            {compareWithBudget && (
              <>
                <th className="col-amount">予算</th>
                <th className="col-rate">達成率</th>
              </>
            )}
          </tr>
        </thead>
        <tbody>
          {PL_STRUCTURE.map((entry, index) => {
            if (entry.type === 'profit') {
              const profit = profitMap[entry.profitType!];
              if (!profit) return null;

              return (
                <PLProfitRow
                  key={entry.profitType}
                  profit={profit}
                  showRatio={showRatio}
                  compareWithPrevious={compareWithPrevious}
                  compareWithBudget={compareWithBudget}
                  columnCount={columnCount}
                />
              );
            }

            const sectionItems = groupedItems[entry.section!] || [];

            return (
              <PLSectionGroup
                key={entry.section}
                section={entry.section!}
                label={entry.label!}
                items={sectionItems}
                isExpanded={expandedSections.has(entry.section!)}
                showRatio={showRatio}
                compareWithPrevious={compareWithPrevious}
                compareWithBudget={compareWithBudget}
                isRevenue={entry.isRevenue}
                isExpense={entry.isExpense}
                onToggle={() => onToggleSection(entry.section!)}
              />
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
```

### PLSectionGroup コンポーネント

```typescript
// components/PLSectionGroup.tsx
import Decimal from 'decimal.js';
import { formatCurrency, formatPercent } from '../utils/formatUtils';
import type { PLItem, PLSection } from '../types/profitAndLoss';

interface PLSectionGroupProps {
  section: PLSection;
  label: string;
  items: PLItem[];
  isExpanded: boolean;
  showRatio: boolean;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
  isRevenue?: boolean;
  isExpense?: boolean;
  onToggle: () => void;
}

export const PLSectionGroup: React.FC<PLSectionGroupProps> = ({
  section,
  label,
  items,
  isExpanded,
  showRatio,
  compareWithPrevious,
  compareWithBudget,
  isRevenue,
  isExpense,
  onToggle,
}) => {
  // セクション合計を計算
  const sectionTotal = items.reduce(
    (sum, item) => sum + item.amount,
    0
  );
  const previousTotal = items.reduce(
    (sum, item) => sum + (item.previousAmount || 0),
    0
  );
  const budgetTotal = items.reduce(
    (sum, item) => sum + (item.budgetAmount || 0),
    0
  );

  // 増減を計算
  const calculateChange = (current: number, previous: number): number => {
    return new Decimal(current).minus(previous).toNumber();
  };

  // 達成率を計算
  const calculateRate = (actual: number, budget: number): number | null => {
    if (budget === 0) return null;
    return new Decimal(actual)
      .dividedBy(budget)
      .times(100)
      .toNumber();
  };

  // アイテムを階層表示用にソート・フィルタ
  const displayItems = items
    .filter(item => item.level <= 2 || isExpanded)
    .sort((a, b) => a.displayOrder - b.displayOrder);

  return (
    <>
      {/* セクションヘッダー */}
      <tr className={`section-header ${section}`}>
        <td className="col-name">
          <button
            className="section-toggle"
            onClick={onToggle}
            aria-expanded={isExpanded}
          >
            <span className={`toggle-icon ${isExpanded ? 'expanded' : ''}`}>
              ▶
            </span>
            {label}
          </button>
        </td>
        <td className="col-amount section-total">
          {formatCurrency(sectionTotal)}
        </td>
        {showRatio && (
          <td className="col-ratio">
            {/* セクション合計の比率は段階利益行で表示 */}
          </td>
        )}
        {compareWithPrevious && (
          <>
            <td className="col-amount">
              {formatCurrency(previousTotal)}
            </td>
            <td className="col-change">
              <ChangeIndicator
                change={calculateChange(sectionTotal, previousTotal)}
                isExpense={isExpense}
              />
            </td>
          </>
        )}
        {compareWithBudget && (
          <>
            <td className="col-amount">
              {formatCurrency(budgetTotal)}
            </td>
            <td className="col-rate">
              <RateIndicator
                rate={calculateRate(sectionTotal, budgetTotal)}
                isExpense={isExpense}
              />
            </td>
          </>
        )}
      </tr>

      {/* セクション内アイテム */}
      {isExpanded && displayItems.map(item => (
        <PLItemRow
          key={item.id}
          item={item}
          showRatio={showRatio}
          compareWithPrevious={compareWithPrevious}
          compareWithBudget={compareWithBudget}
          isExpense={isExpense}
        />
      ))}
    </>
  );
};

// アイテム行コンポーネント
interface PLItemRowProps {
  item: PLItem;
  showRatio: boolean;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
  isExpense?: boolean;
}

const PLItemRow: React.FC<PLItemRowProps> = ({
  item,
  showRatio,
  compareWithPrevious,
  compareWithBudget,
  isExpense,
}) => {
  const change = item.previousAmount !== undefined
    ? new Decimal(item.amount).minus(item.previousAmount).toNumber()
    : null;

  const rate = item.budgetAmount && item.budgetAmount !== 0
    ? new Decimal(item.amount)
        .dividedBy(item.budgetAmount)
        .times(100)
        .toNumber()
    : null;

  return (
    <tr className={`pl-item level-${item.level}`}>
      <td className="col-name">
        <span style={{ paddingLeft: `${item.level * 16}px` }}>
          {item.name}
        </span>
      </td>
      <td className="col-amount">
        {formatCurrency(item.amount)}
      </td>
      {showRatio && (
        <td className="col-ratio">
          {(item as any).ratio !== undefined
            ? formatPercent((item as any).ratio)
            : '-'}
        </td>
      )}
      {compareWithPrevious && (
        <>
          <td className="col-amount">
            {item.previousAmount !== undefined
              ? formatCurrency(item.previousAmount)
              : '-'}
          </td>
          <td className="col-change">
            {change !== null && (
              <ChangeIndicator change={change} isExpense={isExpense} />
            )}
          </td>
        </>
      )}
      {compareWithBudget && (
        <>
          <td className="col-amount">
            {item.budgetAmount !== undefined
              ? formatCurrency(item.budgetAmount)
              : '-'}
          </td>
          <td className="col-rate">
            {rate !== null && (
              <RateIndicator rate={rate} isExpense={isExpense} />
            )}
          </td>
        </>
      )}
    </tr>
  );
};

// 増減表示コンポーネント
interface ChangeIndicatorProps {
  change: number;
  isExpense?: boolean;
}

const ChangeIndicator: React.FC<ChangeIndicatorProps> = ({
  change,
  isExpense,
}) => {
  // 費用の場合、増加は悪い（赤）、減少は良い（緑）
  // 収益の場合、増加は良い（緑）、減少は悪い（赤）
  const isPositive = isExpense ? change < 0 : change > 0;
  const isNegative = isExpense ? change > 0 : change < 0;

  return (
    <span
      className={`change-indicator ${
        isPositive ? 'positive' : isNegative ? 'negative' : ''
      }`}
    >
      {change >= 0 ? '+' : ''}
      {formatCurrency(change)}
    </span>
  );
};

// 達成率表示コンポーネント
interface RateIndicatorProps {
  rate: number | null;
  isExpense?: boolean;
}

const RateIndicator: React.FC<RateIndicatorProps> = ({ rate, isExpense }) => {
  if (rate === null) return <span>-</span>;

  // 費用の場合、100%以下が良い
  // 収益の場合、100%以上が良い
  const isGood = isExpense ? rate <= 100 : rate >= 100;
  const isBad = isExpense ? rate > 100 : rate < 100;

  return (
    <span
      className={`rate-indicator ${
        isGood ? 'good' : isBad ? 'bad' : ''
      }`}
    >
      {formatPercent(rate)}
    </span>
  );
};
```

### PLProfitRow コンポーネント

```typescript
// components/PLProfitRow.tsx
import Decimal from 'decimal.js';
import { formatCurrency, formatPercent } from '../utils/formatUtils';
import type { StagedProfit } from '../types/profitAndLoss';

interface PLProfitRowProps {
  profit: StagedProfit;
  showRatio: boolean;
  compareWithPrevious: boolean;
  compareWithBudget: boolean;
  columnCount: number;
}

const PROFIT_LABELS: Record<string, string> = {
  grossProfit: '売上総利益',
  operatingProfit: '営業利益',
  ordinaryProfit: '経常利益',
  profitBeforeTax: '税引前当期純利益',
  netProfit: '当期純利益',
};

export const PLProfitRow: React.FC<PLProfitRowProps> = ({
  profit,
  showRatio,
  compareWithPrevious,
  compareWithBudget,
  columnCount,
}) => {
  // 増減を計算
  const change = profit.previousAmount !== undefined
    ? new Decimal(profit.amount).minus(profit.previousAmount).toNumber()
    : null;

  // 増減率を計算
  const changeRate = profit.previousAmount && profit.previousAmount !== 0
    ? new Decimal(profit.amount)
        .minus(profit.previousAmount)
        .dividedBy(Math.abs(profit.previousAmount))
        .times(100)
        .toNumber()
    : null;

  // 達成率を計算
  const achievementRate = profit.budgetAmount && profit.budgetAmount !== 0
    ? new Decimal(profit.amount)
        .dividedBy(profit.budgetAmount)
        .times(100)
        .toNumber()
    : null;

  return (
    <tr className={`profit-row ${profit.type} ${profit.amount < 0 ? 'negative' : ''}`}>
      <td className="col-name profit-name">
        {PROFIT_LABELS[profit.type]}
      </td>
      <td className="col-amount profit-amount">
        {formatCurrency(profit.amount)}
      </td>
      {showRatio && (
        <td className="col-ratio profit-ratio">
          {profit.ratio !== undefined
            ? formatPercent(profit.ratio)
            : '-'}
        </td>
      )}
      {compareWithPrevious && (
        <>
          <td className="col-amount">
            {profit.previousAmount !== undefined
              ? formatCurrency(profit.previousAmount)
              : '-'}
          </td>
          <td className="col-change">
            {change !== null && (
              <span className={`change ${change >= 0 ? 'positive' : 'negative'}`}>
                {change >= 0 ? '+' : ''}
                {formatCurrency(change)}
                {changeRate !== null && (
                  <span className="change-rate">
                    ({changeRate >= 0 ? '+' : ''}{formatPercent(changeRate)})
                  </span>
                )}
              </span>
            )}
          </td>
        </>
      )}
      {compareWithBudget && (
        <>
          <td className="col-amount">
            {profit.budgetAmount !== undefined
              ? formatCurrency(profit.budgetAmount)
              : '-'}
          </td>
          <td className="col-rate">
            {achievementRate !== null && (
              <span
                className={`achievement-rate ${
                  achievementRate >= 100 ? 'achieved' : 'not-achieved'
                }`}
              >
                {formatPercent(achievementRate)}
              </span>
            )}
          </td>
        </>
      )}
    </tr>
  );
};
```

## 18.7 月次推移分析

### PLMonthlyTrendContainer

```typescript
// containers/PLMonthlyTrendContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetPLMonthlyTrend } from '../generated/api/profit-and-loss';
import { useAccountingPeriod } from '../contexts/AccountingPeriodContext';
import { PLMonthlyTrendView } from '../views/PLMonthlyTrendView';
import type { ProfitType } from '../types/profitAndLoss';

export const PLMonthlyTrendContainer: React.FC = () => {
  const { currentPeriod } = useAccountingPeriod();
  const [fiscalYear, setFiscalYear] = useState(currentPeriod?.year || new Date().getFullYear());
  const [selectedProfitType, setSelectedProfitType] = useState<ProfitType>('operatingProfit');
  const [showCumulative, setShowCumulative] = useState(false);

  const { data, isLoading, error } = useGetPLMonthlyTrend({ fiscalYear });

  // 選択した利益タイプの月次データを抽出
  const monthlyData = useMemo(() => {
    if (!data) return [];

    return data.months.map(month => {
      const profit = month.profits.find(p => p.type === selectedProfitType);
      return {
        month: month.month,
        monthLabel: month.monthLabel,
        amount: profit?.amount || 0,
        budgetAmount: profit?.budgetAmount || 0,
        previousYearAmount: profit?.previousYearAmount || 0,
      };
    });
  }, [data, selectedProfitType]);

  // 累計データを計算
  const cumulativeData = useMemo(() => {
    if (!showCumulative) return monthlyData;

    let cumAmount = 0;
    let cumBudget = 0;
    let cumPrevious = 0;

    return monthlyData.map(month => {
      cumAmount += month.amount;
      cumBudget += month.budgetAmount;
      cumPrevious += month.previousYearAmount;

      return {
        ...month,
        amount: cumAmount,
        budgetAmount: cumBudget,
        previousYearAmount: cumPrevious,
      };
    });
  }, [monthlyData, showCumulative]);

  const handleYearChange = useCallback((year: number) => {
    setFiscalYear(year);
  }, []);

  const handleProfitTypeChange = useCallback((type: ProfitType) => {
    setSelectedProfitType(type);
  }, []);

  const handleToggleCumulative = useCallback(() => {
    setShowCumulative(prev => !prev);
  }, []);

  if (error) {
    return <div className="error-state">データの取得に失敗しました</div>;
  }

  return (
    <PLMonthlyTrendView
      fiscalYear={fiscalYear}
      selectedProfitType={selectedProfitType}
      showCumulative={showCumulative}
      data={cumulativeData}
      salesData={data?.salesMonthly || []}
      isLoading={isLoading}
      onYearChange={handleYearChange}
      onProfitTypeChange={handleProfitTypeChange}
      onToggleCumulative={handleToggleCumulative}
    />
  );
};
```

### PLTrendChart コンポーネント

```typescript
// components/PLTrendChart.tsx
import { useRef, useEffect } from 'react';
import Decimal from 'decimal.js';
import { formatCurrency } from '../utils/formatUtils';

interface MonthlyDataPoint {
  month: number;
  monthLabel: string;
  amount: number;
  budgetAmount: number;
  previousYearAmount: number;
}

interface PLTrendChartProps {
  data: MonthlyDataPoint[];
  title: string;
  height?: number;
  showBudget?: boolean;
  showPreviousYear?: boolean;
}

export const PLTrendChart: React.FC<PLTrendChartProps> = ({
  data,
  title,
  height = 300,
  showBudget = true,
  showPreviousYear = true,
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
    canvas.width = width * window.devicePixelRatio;
    canvas.height = height * window.devicePixelRatio;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

    // 描画領域
    const padding = { top: 40, right: 20, bottom: 40, left: 80 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    // 背景クリア
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, width, height);

    // データ範囲を計算
    const allValues = data.flatMap(d => [
      d.amount,
      showBudget ? d.budgetAmount : 0,
      showPreviousYear ? d.previousYearAmount : 0,
    ]);
    const maxValue = Math.max(...allValues, 0);
    const minValue = Math.min(...allValues, 0);
    const range = maxValue - minValue || 1;
    const yScale = chartHeight / (range * 1.1);
    const yOffset = maxValue + range * 0.05;

    // グリッド線描画
    ctx.strokeStyle = '#e5e7eb';
    ctx.lineWidth = 1;
    const gridCount = 5;
    for (let i = 0; i <= gridCount; i++) {
      const y = padding.top + (chartHeight * i) / gridCount;
      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();

      // Y軸ラベル
      const value = yOffset - (range * 1.1 * i) / gridCount;
      ctx.fillStyle = '#6b7280';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText(formatCompactCurrency(value), padding.left - 8, y + 4);
    }

    // ゼロライン
    if (minValue < 0) {
      const zeroY = padding.top + (yOffset - 0) * yScale;
      ctx.strokeStyle = '#374151';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(padding.left, zeroY);
      ctx.lineTo(width - padding.right, zeroY);
      ctx.stroke();
    }

    // X軸設定
    const barGroupWidth = chartWidth / data.length;
    const barWidth = barGroupWidth * 0.2;

    // データ描画
    data.forEach((point, index) => {
      const x = padding.left + barGroupWidth * index + barGroupWidth / 2;

      // X軸ラベル
      ctx.fillStyle = '#6b7280';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(point.monthLabel, x, height - padding.bottom + 20);

      // 棒グラフ描画関数
      const drawBar = (value: number, offsetX: number, color: string) => {
        const barHeight = Math.abs(value) * yScale;
        const barY = value >= 0
          ? padding.top + (yOffset - value) * yScale
          : padding.top + yOffset * yScale;

        ctx.fillStyle = color;
        ctx.fillRect(x + offsetX - barWidth / 2, barY, barWidth, barHeight);
      };

      // 当期実績
      drawBar(point.amount, 0, '#3b82f6');

      // 予算
      if (showBudget) {
        drawBar(point.budgetAmount, barWidth + 4, '#10b981');
      }

      // 前年実績
      if (showPreviousYear) {
        drawBar(point.previousYearAmount, (barWidth + 4) * 2, '#9ca3af');
      }
    });

    // 凡例
    const legendY = 20;
    const legendItems = [
      { label: '当期', color: '#3b82f6' },
      ...(showBudget ? [{ label: '予算', color: '#10b981' }] : []),
      ...(showPreviousYear ? [{ label: '前年', color: '#9ca3af' }] : []),
    ];

    let legendX = width - padding.right - 20;
    legendItems.reverse().forEach(item => {
      ctx.font = '12px sans-serif';
      const textWidth = ctx.measureText(item.label).width;
      legendX -= textWidth + 24;

      ctx.fillStyle = item.color;
      ctx.fillRect(legendX, legendY - 8, 12, 12);

      ctx.fillStyle = '#374151';
      ctx.textAlign = 'left';
      ctx.fillText(item.label, legendX + 16, legendY);
    });

    // タイトル
    ctx.fillStyle = '#111827';
    ctx.font = 'bold 14px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText(title, padding.left, 20);

  }, [data, title, height, showBudget, showPreviousYear]);

  return (
    <div ref={containerRef} className="pl-trend-chart">
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

## 18.8 費用内訳分析

### ExpenseBreakdownContainer

```typescript
// containers/ExpenseBreakdownContainer.tsx
import { useState, useCallback, useMemo } from 'react';
import { useGetExpenseBreakdown } from '../generated/api/profit-and-loss';
import { ExpenseBreakdownView } from '../views/ExpenseBreakdownView';
import type { PLSection } from '../types/profitAndLoss';

interface ExpenseBreakdownContainerProps {
  periodStart: string;
  periodEnd: string;
}

export const ExpenseBreakdownContainer: React.FC<ExpenseBreakdownContainerProps> = ({
  periodStart,
  periodEnd,
}) => {
  const [selectedSection, setSelectedSection] = useState<'costOfSales' | 'sgAndA'>('sgAndA');
  const [sortBy, setSortBy] = useState<'amount' | 'ratio'>('amount');
  const [showTop, setShowTop] = useState<number>(10);

  const { data, isLoading } = useGetExpenseBreakdown({
    periodStart,
    periodEnd,
    section: selectedSection,
  });

  // ソート・フィルタリング
  const processedData = useMemo(() => {
    if (!data) return [];

    const sorted = [...data.items].sort((a, b) => {
      if (sortBy === 'amount') {
        return b.amount - a.amount;
      }
      return b.ratio - a.ratio;
    });

    return sorted.slice(0, showTop);
  }, [data, sortBy, showTop]);

  // その他の合計
  const othersTotal = useMemo(() => {
    if (!data) return 0;

    const topTotal = processedData.reduce((sum, item) => sum + item.amount, 0);
    return data.total - topTotal;
  }, [data, processedData]);

  const handleSectionChange = useCallback((section: 'costOfSales' | 'sgAndA') => {
    setSelectedSection(section);
  }, []);

  const handleSortChange = useCallback((sort: 'amount' | 'ratio') => {
    setSortBy(sort);
  }, []);

  const handleTopChange = useCallback((top: number) => {
    setShowTop(top);
  }, []);

  return (
    <ExpenseBreakdownView
      selectedSection={selectedSection}
      sortBy={sortBy}
      showTop={showTop}
      data={processedData}
      total={data?.total || 0}
      othersTotal={othersTotal}
      isLoading={isLoading}
      onSectionChange={handleSectionChange}
      onSortChange={handleSortChange}
      onTopChange={handleTopChange}
    />
  );
};
```

### ExpenseBreakdownChart（円グラフ）

```typescript
// components/ExpenseBreakdownChart.tsx
import { useRef, useEffect, useState } from 'react';
import Decimal from 'decimal.js';
import { formatCurrency, formatPercent } from '../utils/formatUtils';

interface ExpenseItem {
  id: string;
  name: string;
  amount: number;
  ratio: number;
}

interface ExpenseBreakdownChartProps {
  data: ExpenseItem[];
  othersAmount: number;
  total: number;
  title: string;
}

const COLORS = [
  '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6',
  '#ec4899', '#06b6d4', '#84cc16', '#f97316', '#6366f1',
];

export const ExpenseBreakdownChart: React.FC<ExpenseBreakdownChartProps> = ({
  data,
  othersAmount,
  total,
  title,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const [tooltip, setTooltip] = useState<{
    x: number;
    y: number;
    content: string;
  } | null>(null);

  // 描画データを準備（その他を含む）
  const chartData = [
    ...data.map((item, index) => ({
      ...item,
      color: COLORS[index % COLORS.length],
    })),
    ...(othersAmount > 0 ? [{
      id: 'others',
      name: 'その他',
      amount: othersAmount,
      ratio: total > 0 ? (othersAmount / total) * 100 : 0,
      color: '#9ca3af',
    }] : []),
  ];

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const size = 280;
    const dpr = window.devicePixelRatio;
    canvas.width = size * dpr;
    canvas.height = size * dpr;
    canvas.style.width = `${size}px`;
    canvas.style.height = `${size}px`;
    ctx.scale(dpr, dpr);

    const centerX = size / 2;
    const centerY = size / 2;
    const radius = 100;
    const innerRadius = 50;

    // 背景クリア
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);

    // 円グラフ描画
    let currentAngle = -Math.PI / 2; // 12時の位置から開始

    chartData.forEach((item, index) => {
      const sliceAngle = (item.ratio / 100) * Math.PI * 2;
      const isHovered = hoveredIndex === index;

      ctx.beginPath();
      ctx.moveTo(centerX, centerY);

      // ホバー時は少し拡大
      const r = isHovered ? radius + 5 : radius;
      const ir = isHovered ? innerRadius - 2 : innerRadius;

      ctx.arc(centerX, centerY, r, currentAngle, currentAngle + sliceAngle);
      ctx.arc(centerX, centerY, ir, currentAngle + sliceAngle, currentAngle, true);
      ctx.closePath();

      ctx.fillStyle = item.color;
      ctx.fill();

      // ホバー時は境界線を追加
      if (isHovered) {
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2;
        ctx.stroke();
      }

      currentAngle += sliceAngle;
    });

    // 中央テキスト
    ctx.fillStyle = '#111827';
    ctx.font = 'bold 16px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('合計', centerX, centerY - 10);
    ctx.font = '14px sans-serif';
    ctx.fillText(formatCompactAmount(total), centerX, centerY + 10);

  }, [chartData, total, hoveredIndex]);

  // マウスイベントハンドラ
  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const centerX = 140;
    const centerY = 140;
    const dx = x - centerX;
    const dy = y - centerY;
    const distance = Math.sqrt(dx * dx + dy * dy);

    // ドーナツの範囲内かチェック
    if (distance < 50 || distance > 100) {
      setHoveredIndex(null);
      setTooltip(null);
      return;
    }

    // 角度を計算
    let angle = Math.atan2(dy, dx) + Math.PI / 2;
    if (angle < 0) angle += Math.PI * 2;
    const percentage = (angle / (Math.PI * 2)) * 100;

    // どのセグメントかを判定
    let cumulative = 0;
    for (let i = 0; i < chartData.length; i++) {
      cumulative += chartData[i].ratio;
      if (percentage <= cumulative) {
        setHoveredIndex(i);
        setTooltip({
          x: e.clientX - rect.left + 10,
          y: e.clientY - rect.top - 30,
          content: `${chartData[i].name}: ${formatCurrency(chartData[i].amount)} (${formatPercent(chartData[i].ratio)})`,
        });
        return;
      }
    }
  };

  const handleMouseLeave = () => {
    setHoveredIndex(null);
    setTooltip(null);
  };

  return (
    <div className="expense-breakdown-chart">
      <h3>{title}</h3>

      <div className="chart-container">
        <div className="chart-canvas-wrapper">
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
              {tooltip.content}
            </div>
          )}
        </div>

        <div className="chart-legend">
          {chartData.map((item, index) => (
            <div
              key={item.id}
              className={`legend-item ${hoveredIndex === index ? 'hovered' : ''}`}
              onMouseEnter={() => setHoveredIndex(index)}
              onMouseLeave={() => setHoveredIndex(null)}
            >
              <span
                className="legend-color"
                style={{ backgroundColor: item.color }}
              />
              <span className="legend-name">{item.name}</span>
              <span className="legend-ratio">{formatPercent(item.ratio)}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

const formatCompactAmount = (value: number): string => {
  if (value >= 100000000) {
    return `${new Decimal(value).dividedBy(100000000).toFixed(1)}億円`;
  }
  if (value >= 10000) {
    return `${new Decimal(value).dividedBy(10000).toFixed(0)}万円`;
  }
  return `${value.toLocaleString()}円`;
};
```

## 18.9 スタイリング

```css
/* styles/profit-and-loss.css */

/* 損益計算書ページ */
.profit-and-loss-view {
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

/* 段階利益サマリー */
.staged-profit-summary {
  margin-bottom: 24px;
}

.staged-profit-summary h2 {
  font-size: 16px;
  margin-bottom: 16px;
}

.profit-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.profit-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.profit-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profit-card.negative {
  background: #fef2f2;
  border-color: #fecaca;
}

.profit-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.profit-amount {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.profit-card.negative .profit-amount {
  color: #dc2626;
}

.profit-ratio {
  font-size: 12px;
  color: #6b7280;
}

.profit-comparison,
.profit-achievement {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #e5e7eb;
}

.profit-comparison .value.positive {
  color: #10b981;
}

.profit-comparison .value.negative {
  color: #dc2626;
}

.profit-achievement .value.achieved {
  color: #10b981;
}

.profit-achievement .value.warning {
  color: #f59e0b;
}

.profit-achievement .value.below {
  color: #dc2626;
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

.control-group {
  display: flex;
  gap: 16px;
}

.btn-text {
  background: none;
  border: none;
  color: #3b82f6;
  cursor: pointer;
  font-size: 14px;
}

.btn-text:hover {
  text-decoration: underline;
}

/* 損益計算書テーブル */
.pl-statement {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.pl-table {
  width: 100%;
  border-collapse: collapse;
}

.pl-table th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.pl-table th.col-amount,
.pl-table th.col-ratio,
.pl-table th.col-change,
.pl-table th.col-rate {
  text-align: right;
}

.pl-table td {
  padding: 10px 16px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
}

.pl-table td.col-amount,
.pl-table td.col-ratio,
.pl-table td.col-change,
.pl-table td.col-rate {
  text-align: right;
  font-family: 'SF Mono', monospace;
}

/* セクションヘッダー */
.section-header {
  background: #f9fafb;
}

.section-header td {
  font-weight: 600;
}

.section-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  color: #111827;
}

.toggle-icon {
  font-size: 10px;
  transition: transform 0.2s;
}

.toggle-icon.expanded {
  transform: rotate(90deg);
}

/* アイテム行 */
.pl-item {
  transition: background-color 0.15s;
}

.pl-item:hover {
  background: #f9fafb;
}

.pl-item.level-0 .col-name {
  font-weight: 600;
}

.pl-item.level-1 .col-name {
  color: #374151;
}

.pl-item.level-2 .col-name {
  color: #6b7280;
  font-size: 13px;
}

/* 段階利益行 */
.profit-row {
  background: #eff6ff;
  font-weight: 600;
}

.profit-row.netProfit {
  background: #dbeafe;
}

.profit-row.negative {
  background: #fef2f2;
}

.profit-name {
  color: #1d4ed8;
}

.profit-row.negative .profit-name {
  color: #dc2626;
}

/* 増減表示 */
.change-indicator.positive {
  color: #10b981;
}

.change-indicator.negative {
  color: #dc2626;
}

/* 達成率表示 */
.rate-indicator.good {
  color: #10b981;
}

.rate-indicator.bad {
  color: #dc2626;
}

/* 月次推移チャート */
.pl-trend-chart {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
}

/* 費用内訳チャート */
.expense-breakdown-chart {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.expense-breakdown-chart h3 {
  font-size: 16px;
  margin: 0 0 16px 0;
}

.chart-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.chart-canvas-wrapper {
  position: relative;
}

.chart-tooltip {
  position: absolute;
  background: #1f2937;
  color: #ffffff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.chart-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.legend-item:hover,
.legend-item.hovered {
  background: #f3f4f6;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-name {
  flex: 1;
  font-size: 13px;
  color: #374151;
}

.legend-ratio {
  font-size: 13px;
  color: #6b7280;
  font-family: 'SF Mono', monospace;
}
```

## 18.10 まとめ

本章では、損益計算書の表示機能を実装した。主なポイントは以下の通りである：

1. **段階利益の表示**: 売上総利益から当期純利益まで、5つの段階利益を明確に表示
2. **売上高比率分析**: 各科目の売上高に対する構成比を表示し、収益性を分析
3. **期間比較**: 前期比較・予算比較により、業績の推移と達成状況を把握
4. **費用内訳分析**: 円グラフによる費用構成の可視化
5. **月次推移**: 棒グラフによる月次業績推移の表示

損益計算書は、企業の収益力を示す重要な財務諸表であり、経営判断に欠かせない情報を提供する。次章では、キャッシュフロー計算書の実装について解説する。
