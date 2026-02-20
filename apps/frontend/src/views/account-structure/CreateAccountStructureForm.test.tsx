import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateAccountStructureForm } from './CreateAccountStructureForm';
import { createAccountStructure } from '../../api/createAccountStructure';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock('../../api/createAccountStructure', () => ({
  createAccountStructure: vi.fn(),
  getCreateAccountStructureErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目構成の登録に失敗しました',
}));

const mockCreate = vi.mocked(createAccountStructure);

const setupUser = () => userEvent.setup();

describe('CreateAccountStructureForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームフィールドが表示される', () => {
    render(<CreateAccountStructureForm />);

    expect(screen.getByLabelText('勘定科目コード')).toBeInTheDocument();
    expect(screen.getByLabelText('親科目コード')).toBeInTheDocument();
    expect(screen.getByLabelText('表示順')).toBeInTheDocument();
    expect(screen.getByText('登録')).toBeInTheDocument();
    expect(screen.getByText('戻る')).toBeInTheDocument();
  });

  it('勘定科目コードが空の場合バリデーションエラーを表示する', async () => {
    const user = setupUser();
    render(<CreateAccountStructureForm />);

    await user.click(screen.getByText('登録'));

    expect(screen.getByText('勘定科目コードを入力してください')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('登録成功時にナビゲートする', async () => {
    const user = setupUser();
    mockCreate.mockResolvedValue({ success: true, accountCode: '1000' });
    render(<CreateAccountStructureForm />);

    await user.type(screen.getByLabelText('勘定科目コード'), '1000');
    await user.click(screen.getByText('登録'));

    await waitFor(() => {
      expect(mockCreate).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/master/account-structures', {
        state: { successMessage: '勘定科目構成を登録しました' },
      });
    });
  });

  it('登録失敗時にエラーメッセージを表示する', async () => {
    const user = setupUser();
    mockCreate.mockRejectedValue(new Error('重複エラー'));
    render(<CreateAccountStructureForm />);

    await user.type(screen.getByLabelText('勘定科目コード'), '1000');
    await user.click(screen.getByText('登録'));

    await waitFor(() => {
      expect(screen.getByText('重複エラー')).toBeInTheDocument();
    });
  });

  it('戻るボタンで一覧ページにナビゲートする', async () => {
    const user = setupUser();
    render(<CreateAccountStructureForm />);

    await user.click(screen.getByText('戻る'));

    expect(mockNavigate).toHaveBeenCalledWith('/master/account-structures');
  });
});
