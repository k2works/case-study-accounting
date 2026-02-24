import React, { useEffect, useState } from 'react';
import { MainLayout } from '../views/common';
import { getDashboardStats, DashboardStats } from '../api/getDashboardStats';
import { getJournalEntries, JournalEntrySummary } from '../api/getJournalEntries';
import './DashboardPage.css';

/**
 * ダッシュボードページ
 *
 * システムの概要と重要な情報を表示する。
 */
interface Notice {
  id: string;
  type: 'info' | 'warning' | 'important';
  title: string;
  content: string;
  date: string;
}

const STATUS_LABELS: Record<string, string> = {
  DRAFT: '下書き',
  PENDING: '承認待ち',
  APPROVED: '承認済み',
  CONFIRMED: '確定',
};

const NOTICE_TYPE_LABELS: Record<string, string> = {
  important: '重要',
  warning: '注意',
  info: 'お知らせ',
};

const getStatusLabel = (status: string): string => STATUS_LABELS[status] ?? status;

const getStatusClass = (status: string): string => {
  const classMap: Record<string, string> = {
    PENDING: 'pending',
    DRAFT: 'draft',
    CONFIRMED: 'confirmed',
    APPROVED: 'approved',
  };
  return classMap[status] ?? 'default';
};

const notices: Notice[] = [
  {
    id: 'N-001',
    type: 'important',
    title: '決算期末のお知らせ',
    content: '3月31日は決算期末です。すべての仕訳を確定してください。',
    date: '2024/03/01',
  },
  {
    id: 'N-002',
    type: 'warning',
    title: 'システムメンテナンス',
    content: '3月15日 22:00〜24:00 にシステムメンテナンスを実施します。',
    date: '2024/03/05',
  },
  {
    id: 'N-003',
    type: 'info',
    title: '新機能リリース',
    content: '仕訳一括インポート機能が追加されました。',
    date: '2024/03/10',
  },
];

const NoticeSection: React.FC = () => (
  <div className="dashboard__notices">
    <h2 className="dashboard__section-title">お知らせ</h2>
    <div className="dashboard__notice-list">
      {notices.map((notice) => (
        <div key={notice.id} className={`dashboard__notice dashboard__notice--${notice.type}`}>
          <div className="dashboard__notice-header">
            <span className={`dashboard__notice-badge dashboard__notice-badge--${notice.type}`}>
              {NOTICE_TYPE_LABELS[notice.type]}
            </span>
            <span className="dashboard__notice-date">{notice.date}</span>
          </div>
          <h3 className="dashboard__notice-title">{notice.title}</h3>
          <p className="dashboard__notice-content">{notice.content}</p>
        </div>
      ))}
    </div>
  </div>
);

const formatStatValue = (loading: boolean, value: number | undefined): string =>
  loading ? '...' : `${value ?? 0} 件`;

const StatsSection: React.FC<{ loading: boolean; stats: DashboardStats | null }> = ({
  loading,
  stats,
}) => (
  <div className="dashboard__stats">
    <div className="dashboard__stat-card">
      <h3 className="dashboard__stat-label">本日の仕訳件数</h3>
      <p className="dashboard__stat-value">{formatStatValue(loading, stats?.todayJournalCount)}</p>
    </div>
    <div className="dashboard__stat-card">
      <h3 className="dashboard__stat-label">承認待ち件数</h3>
      <p className="dashboard__stat-value dashboard__stat-value--alert">
        {formatStatValue(loading, stats?.pendingApprovalCount)}
      </p>
    </div>
  </div>
);

const RecentJournalsTable: React.FC<{ journals: JournalEntrySummary[] }> = ({ journals }) => (
  <table className="dashboard__table">
    <thead>
      <tr>
        <th>日付</th>
        <th>摘要</th>
        <th className="dashboard__table-cell--right">金額</th>
        <th>ステータス</th>
      </tr>
    </thead>
    <tbody>
      {journals.map((journal) => (
        <tr key={journal.journalEntryId}>
          <td>{journal.journalDate}</td>
          <td>{journal.description}</td>
          <td className="dashboard__table-cell--right">
            ¥{journal.totalDebitAmount.toLocaleString()}
          </td>
          <td>
            <span
              className={`dashboard__status dashboard__status--${getStatusClass(journal.status)}`}
            >
              {getStatusLabel(journal.status)}
            </span>
          </td>
        </tr>
      ))}
    </tbody>
  </table>
);

const RecentJournalsContent: React.FC<{
  loading: boolean;
  journals: JournalEntrySummary[];
}> = ({ loading, journals }) => {
  if (loading) {
    return (
      <p className="dashboard__loading" data-testid="dashboard-loading">
        読み込み中...
      </p>
    );
  }
  if (journals.length === 0) {
    return <p className="dashboard__empty">仕訳データがありません</p>;
  }
  return <RecentJournalsTable journals={journals} />;
};

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentJournals, setRecentJournals] = useState<JournalEntrySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [dashboardStats, journalsResult] = await Promise.all([
          getDashboardStats(),
          getJournalEntries({ size: 5 }),
        ]);
        setStats(dashboardStats);
        setRecentJournals(journalsResult.content);
      } catch {
        setError('データの取得に失敗しました');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const breadcrumbs = [{ label: 'ダッシュボード' }];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div className="dashboard" data-testid="dashboard">
        <h1 className="dashboard__title">ダッシュボード</h1>
        <NoticeSection />
        {error && (
          <div className="dashboard__error" data-testid="dashboard-error">
            {error}
          </div>
        )}
        <StatsSection loading={loading} stats={stats} />
        <div className="dashboard__section">
          <h2 className="dashboard__section-title">最近の仕訳</h2>
          <div className="dashboard__table-container">
            <RecentJournalsContent loading={loading} journals={recentJournals} />
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

export default DashboardPage;
