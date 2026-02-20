import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AutoJournalPatternList } from './AutoJournalPatternList';
import { deleteAutoJournalPattern } from '../../api/deleteAutoJournalPattern';
import type { AutoJournalPattern } from '../../api/getAutoJournalPatterns';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ hasRole: (role: string) => role === 'ADMIN' || role === 'MANAGER' }),
}));

vi.mock('../../api/deleteAutoJournalPattern', () => ({
  deleteAutoJournalPattern: vi.fn(),
  getDeleteAutoJournalPatternErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '削除エラー',
}));

const mockDelete = vi.mocked(deleteAutoJournalPattern);

const mockPatterns: AutoJournalPattern[] = [
  {
    patternId: 1,
    patternCode: 'P001',
    patternName: '売上パターン',
    sourceTableName: 'sales',
    isActive: true,
    items: [],
  },
  {
    patternId: 2,
    patternCode: 'P002',
    patternName: '仕入パターン',
    sourceTableName: 'purchases',
    isActive: false,
    items: [],
  },
];

describe('AutoJournalPatternList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('パターン一覧を表示する', () => {
    render(<AutoJournalPatternList patterns={mockPatterns} onEdit={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByText('P001')).toBeInTheDocument();
    expect(screen.getByText('売上パターン')).toBeInTheDocument();
    expect(screen.getByText('P002')).toBeInTheDocument();
    expect(screen.getByText('仕入パターン')).toBeInTheDocument();
  });

  it('有効/無効バッジを表示する', () => {
    render(<AutoJournalPatternList patterns={mockPatterns} onEdit={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByText('有効')).toBeInTheDocument();
    expect(screen.getByText('無効')).toBeInTheDocument();
  });

  it('編集ボタンクリックで onEdit が呼ばれる', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    render(<AutoJournalPatternList patterns={mockPatterns} onEdit={onEdit} onDelete={vi.fn()} />);

    await user.click(screen.getByTestId('auto-journal-pattern-edit-1'));
    expect(onEdit).toHaveBeenCalledWith(mockPatterns[0]);
  });

  it('削除確認で OK した場合 API を呼び出す', async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn();
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDelete.mockResolvedValue({ success: true, message: '削除しました' });
    render(<AutoJournalPatternList patterns={mockPatterns} onEdit={vi.fn()} onDelete={onDelete} />);

    await user.click(screen.getByTestId('auto-journal-pattern-delete-1'));

    await waitFor(() => {
      expect(mockDelete).toHaveBeenCalledWith(1);
      expect(onDelete).toHaveBeenCalled();
    });
  });

  it('削除確認でキャンセルした場合 API を呼び出さない', async () => {
    const user = userEvent.setup();
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    render(<AutoJournalPatternList patterns={mockPatterns} onEdit={vi.fn()} onDelete={vi.fn()} />);

    await user.click(screen.getByTestId('auto-journal-pattern-delete-1'));

    expect(mockDelete).not.toHaveBeenCalled();
  });

  it('空一覧の場合メッセージを表示する', () => {
    render(<AutoJournalPatternList patterns={[]} onEdit={vi.fn()} onDelete={vi.fn()} />);

    expect(screen.getByText('自動仕訳パターンが登録されていません')).toBeInTheDocument();
  });
});
