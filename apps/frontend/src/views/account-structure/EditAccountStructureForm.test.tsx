import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditAccountStructureForm } from './EditAccountStructureForm';
import { getAccountStructure } from '../../api/getAccountStructures';
import { updateAccountStructure } from '../../api/updateAccountStructure';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../../api/getAccountStructures', () => ({
  getAccountStructure: vi.fn(),
}));

vi.mock('../../api/updateAccountStructure', () => ({
  updateAccountStructure: vi.fn(),
  getUpdateAccountStructureErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目構成の更新に失敗しました',
}));

const mockGetStructure = vi.mocked(getAccountStructure);
const mockUpdate = vi.mocked(updateAccountStructure);

const mockStructure = {
  accountCode: '1000',
  accountName: '現金',
  accountPath: '/1000',
  hierarchyLevel: 1,
  parentAccountCode: null,
  displayOrder: 1,
};

describe('EditAccountStructureForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('読み込み中にローディングを表示する', () => {
    mockGetStructure.mockImplementation(() => new Promise(() => {}));
    render(<EditAccountStructureForm code="1000" />);

    expect(screen.getByText('勘定科目構成を読み込み中...')).toBeInTheDocument();
  });

  it('データ取得後にフォームを表示する', async () => {
    mockGetStructure.mockResolvedValue(mockStructure);
    render(<EditAccountStructureForm code="1000" />);

    await waitFor(() => {
      expect(screen.getByLabelText('勘定科目コード')).toHaveValue('1000');
      expect(screen.getByLabelText('勘定科目コード')).toBeDisabled();
      expect(screen.getByLabelText('表示順')).toHaveValue(1);
    });
  });

  it('データ取得失敗時にエラーメッセージを表示する', async () => {
    mockGetStructure.mockRejectedValue(new Error('取得エラー'));
    render(<EditAccountStructureForm code="1000" />);

    await waitFor(() => {
      expect(screen.getByText('勘定科目構成の取得に失敗しました')).toBeInTheDocument();
    });
  });

  it('更新成功時にナビゲートする', async () => {
    const user = userEvent.setup();
    mockGetStructure.mockResolvedValue(mockStructure);
    mockUpdate.mockResolvedValue({ success: true, message: '更新しました' });
    render(<EditAccountStructureForm code="1000" />);

    await waitFor(() => {
      expect(screen.getByLabelText('表示順')).toBeInTheDocument();
    });

    await user.clear(screen.getByLabelText('表示順'));
    await user.type(screen.getByLabelText('表示順'), '5');
    await user.click(screen.getByText('更新'));

    await waitFor(() => {
      expect(mockUpdate).toHaveBeenCalledWith('1000', {
        parentAccountCode: null,
        displayOrder: 5,
      });
      expect(mockNavigate).toHaveBeenCalledWith('/master/account-structures', {
        replace: true,
        state: { successMessage: '更新しました' },
      });
    });
  });

  it('戻るボタンで一覧ページにナビゲートする', async () => {
    const user = userEvent.setup();
    mockGetStructure.mockResolvedValue(mockStructure);
    render(<EditAccountStructureForm code="1000" />);

    await waitFor(() => {
      expect(screen.getByText('戻る')).toBeInTheDocument();
    });

    await user.click(screen.getByText('戻る'));
    expect(mockNavigate).toHaveBeenCalledWith('/master/account-structures');
  });
});
