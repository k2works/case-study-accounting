import React from 'react';
import type { AuditLogEntry } from '../../api/getAuditLogs';

interface AuditLogTableProps {
  auditLogs: AuditLogEntry[];
}

const formatDateTime = (dateTime: string): string => dateTime.replace('T', ' ').substring(0, 19);

export const AuditLogTable: React.FC<AuditLogTableProps> = ({ auditLogs }) => {
  if (auditLogs.length === 0) {
    return <p data-testid="audit-log-empty">監査ログがありません。</p>;
  }

  return (
    <div data-testid="audit-log-table">
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              ID
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              ユーザーID
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              アクション
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              対象種別
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              対象ID
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              説明
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              IPアドレス
            </th>
            <th style={{ textAlign: 'left', padding: '8px', borderBottom: '2px solid #ddd' }}>
              日時
            </th>
          </tr>
        </thead>
        <tbody>
          {auditLogs.map((log) => (
            <tr key={log.id}>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{log.id}</td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{log.userId}</td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                {log.actionTypeDisplayName}
              </td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                {log.entityTypeDisplayName ?? '-'}
              </td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                {log.entityId ?? '-'}
              </td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{log.description}</td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{log.ipAddress}</td>
              <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                {formatDateTime(log.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
