import React from 'react';
import { MainLayout } from '../views/common';
import './DashboardPage.css';

/**
 * ダッシュボードページ
 *
 * システムの概要と重要な情報を表示する。
 */
const DashboardPage: React.FC = () => {
  // ダミーデータ（将来的に API から取得）
  const stats = {
    todayJournalCount: 25,
    pendingApprovalCount: 5,
  };

  const recentJournals = [
    {
      id: 'J-0001',
      date: '2024/01/15',
      description: '売上計上',
      amount: 100000,
      status: '承認待ち',
    },
    { id: 'J-0002', date: '2024/01/15', description: '仕入計上', amount: 50000, status: '下書き' },
    { id: 'J-0003', date: '2024/01/14', description: '給与支払', amount: 300000, status: '確定' },
  ];

  const breadcrumbs = [{ label: 'ダッシュボード' }];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div className="dashboard">
        <h1 className="dashboard__title">ダッシュボード</h1>

        <div className="dashboard__stats">
          <div className="dashboard__stat-card">
            <h3 className="dashboard__stat-label">本日の仕訳件数</h3>
            <p className="dashboard__stat-value">{stats.todayJournalCount} 件</p>
          </div>
          <div className="dashboard__stat-card">
            <h3 className="dashboard__stat-label">承認待ち件数</h3>
            <p className="dashboard__stat-value dashboard__stat-value--alert">
              {stats.pendingApprovalCount} 件
            </p>
          </div>
        </div>

        <div className="dashboard__section">
          <h2 className="dashboard__section-title">最近の仕訳</h2>
          <div className="dashboard__table-container">
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
                {recentJournals.map((journal) => (
                  <tr key={journal.id}>
                    <td>{journal.date}</td>
                    <td>{journal.description}</td>
                    <td className="dashboard__table-cell--right">
                      ¥{journal.amount.toLocaleString()}
                    </td>
                    <td>
                      <span
                        className={`dashboard__status dashboard__status--${getStatusClass(journal.status)}`}
                      >
                        {journal.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

const getStatusClass = (status: string): string => {
  switch (status) {
    case '承認待ち':
      return 'pending';
    case '下書き':
      return 'draft';
    case '確定':
      return 'confirmed';
    default:
      return 'default';
  }
};

export default DashboardPage;
