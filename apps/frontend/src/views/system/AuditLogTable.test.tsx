import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { AuditLogTable } from './AuditLogTable';
import type { AuditLogEntry } from '../../api/getAuditLogs';

const mockAuditLogs: AuditLogEntry[] = [
  {
    id: 1,
    userId: 'admin',
    actionType: 'LOGIN',
    actionTypeDisplayName: 'ログイン',
    entityType: null,
    entityTypeDisplayName: null,
    entityId: null,
    description: 'ログイン成功',
    ipAddress: '127.0.0.1',
    createdAt: '2026-01-15T10:30:00',
  },
];

describe('AuditLogTable', () => {
  it('shows empty message when data is empty', () => {
    render(<AuditLogTable auditLogs={[]} />);

    expect(screen.getByTestId('audit-log-empty')).toHaveTextContent('監査ログがありません。');
  });

  it('renders table rows with formatted datetime and fallback values', () => {
    render(<AuditLogTable auditLogs={mockAuditLogs} />);

    expect(screen.getByTestId('audit-log-table')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('ログイン')).toBeInTheDocument();
    expect(screen.getAllByText('-')).toHaveLength(2);
    expect(screen.getByText('2026-01-15 10:30:00')).toBeInTheDocument();
  });
});
