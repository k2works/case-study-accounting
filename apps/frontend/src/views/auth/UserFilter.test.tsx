import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UserFilter } from './UserFilter';

describe('UserFilter', () => {
  const mockOnChange = vi.fn();
  const mockOnSearch = vi.fn();
  const mockOnReset = vi.fn();

  const defaultValues = {
    role: '',
    keyword: '',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フィルターコンポーネントが正しくレンダリングされる', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    expect(screen.getByTestId('user-filter')).toBeInTheDocument();
    expect(screen.getByTestId('user-filter-role')).toBeInTheDocument();
    expect(screen.getByTestId('user-filter-keyword')).toBeInTheDocument();
    expect(screen.getByTestId('user-filter-search')).toBeInTheDocument();
    expect(screen.getByTestId('user-filter-reset')).toBeInTheDocument();
  });

  it('ロール選択肢が正しく表示される', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    const roleSelect = screen.getByTestId('user-filter-role');
    expect(roleSelect).toHaveValue('');

    const options = roleSelect.querySelectorAll('option');
    expect(options).toHaveLength(5);
    expect(options[0]).toHaveTextContent('すべてのロール');
    expect(options[1]).toHaveTextContent('管理者');
    expect(options[2]).toHaveTextContent('マネージャー');
    expect(options[3]).toHaveTextContent('一般ユーザー');
    expect(options[4]).toHaveTextContent('閲覧者');
  });

  it('ロール変更時にonChangeが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    await user.selectOptions(screen.getByTestId('user-filter-role'), 'ADMIN');

    expect(mockOnChange).toHaveBeenCalledWith({
      role: 'ADMIN',
      keyword: '',
    });
  });

  it('キーワード変更時にonChangeが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    await user.type(screen.getByTestId('user-filter-keyword'), 'admin');

    expect(mockOnChange).toHaveBeenCalledWith({
      role: '',
      keyword: 'a',
    });
  });

  it('検索ボタンクリック時にonSearchが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    await user.click(screen.getByTestId('user-filter-search'));

    expect(mockOnSearch).toHaveBeenCalled();
  });

  it('リセットボタンクリック時にonResetが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    await user.click(screen.getByTestId('user-filter-reset'));

    expect(mockOnReset).toHaveBeenCalled();
  });

  it('Enterキーで検索が実行される', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    const keywordInput = screen.getByTestId('user-filter-keyword');
    fireEvent.keyDown(keywordInput, { key: 'Enter' });

    expect(mockOnSearch).toHaveBeenCalled();
  });

  it('Enter以外のキーでは検索が実行されない', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    const keywordInput = screen.getByTestId('user-filter-keyword');
    fireEvent.keyDown(keywordInput, { key: 'a' });

    expect(mockOnSearch).not.toHaveBeenCalled();
  });

  it('初期値が正しく表示される', () => {
    const values = {
      role: 'USER',
      keyword: 'test',
    };

    render(
      <UserFilter
        values={values}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    expect(screen.getByTestId('user-filter-role')).toHaveValue('USER');
    expect(screen.getByTestId('user-filter-keyword')).toHaveValue('test');
  });

  it('ラベルが正しく表示される', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    expect(screen.getByText('ロール')).toBeInTheDocument();
    expect(screen.getByText('検索キーワード')).toBeInTheDocument();
  });

  it('プレースホルダーが正しく表示される', () => {
    render(
      <UserFilter
        values={defaultValues}
        onChange={mockOnChange}
        onSearch={mockOnSearch}
        onReset={mockOnReset}
      />
    );

    expect(screen.getByPlaceholderText('ユーザーIDまたは氏名')).toBeInTheDocument();
  });
});
