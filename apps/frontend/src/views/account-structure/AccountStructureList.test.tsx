import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AccountStructureList } from './AccountStructureList';
import { deleteAccountStructure } from '../../api/deleteAccountStructure';
import type { AccountStructure } from '../../api/getAccountStructures';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ hasRole: (role: string) => role === 'MANAGER' }),
}));

vi.mock('../../api/deleteAccountStructure', () => ({
  deleteAccountStructure: vi.fn(),
  getDeleteAccountStructureErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '削除エラー',
}));

const mockDelete = vi.mocked(deleteAccountStructure);

const mockStructures: AccountStructure[] = [
  {
    accountCode: '1000',
    accountName: '現金',
    accountPath: '/1000',
    hierarchyLevel: 1,
    parentAccountCode: null,
    displayOrder: 1,
  },
  {
    accountCode: '2000',
    accountName: '普通預金',
    accountPath: '/2000',
    hierarchyLevel: 1,
    parentAccountCode: '1000',
    displayOrder: 2,
  },
];

describe('AccountStructureList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('構成一覧を表示する', () => {
    render(
      <AccountStructureList structures={mockStructures} onEdit={vi.fn()} onDelete={vi.fn()} />
    );

    expect(screen.getAllByText('1000').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('現金')).toBeInTheDocument();
    expect(screen.getByText('普通預金')).toBeInTheDocument();
  });

  it('テーブルヘッダーを表示する', () => {
    render(
      <AccountStructureList structures={mockStructures} onEdit={vi.fn()} onDelete={vi.fn()} />
    );

    expect(screen.getByText('勘定科目コード')).toBeInTheDocument();
    expect(screen.getByText('勘定科目名')).toBeInTheDocument();
    expect(screen.getByText('パス')).toBeInTheDocument();
    expect(screen.getByText('階層')).toBeInTheDocument();
    expect(screen.getByText('操作')).toBeInTheDocument();
  });

  it('編集ボタンクリックで onEdit が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(<AccountStructureList structures={mockStructures} onEdit={onEdit} onDelete={vi.fn()} />);

    const editButtons = screen.getAllByText('編集');
    await user.click(editButtons[0]);
    expect(onEdit).toHaveBeenCalledWith(mockStructures[0]);
  });

  it('削除確認で OK した場合 API を呼び出す', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDelete.mockResolvedValue({ success: true, message: '削除しました' });
    render(
      <AccountStructureList structures={mockStructures} onEdit={vi.fn()} onDelete={onDelete} />
    );

    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    await waitFor(() => {
      expect(mockDelete).toHaveBeenCalledWith('1000');
      expect(onDelete).toHaveBeenCalled();
    });
  });

  it('削除確認でキャンセルした場合 API を呼び出さない', async () => {
    const user = userEvent.setup();
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    render(
      <AccountStructureList structures={mockStructures} onEdit={vi.fn()} onDelete={vi.fn()} />
    );

    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    expect(mockDelete).not.toHaveBeenCalled();
  });

  it('空一覧の場合メッセージを表示する', () => {
    render(<AccountStructureList structures={[]} onEdit={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByText('勘定科目構成が登録されていません')).toBeInTheDocument();
  });
});
