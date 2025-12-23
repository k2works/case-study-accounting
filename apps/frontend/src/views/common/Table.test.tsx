import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Table, TableColumn } from './Table';

interface TestData extends Record<string, unknown> {
  id: number;
  name: string;
  amount: number;
}

const testColumns: TableColumn<TestData>[] = [
  { key: 'id', header: 'ID' },
  { key: 'name', header: '名前' },
  { key: 'amount', header: '金額', align: 'right' },
];

const testData: TestData[] = [
  { id: 1, name: '項目A', amount: 1000 },
  { id: 2, name: '項目B', amount: 2000 },
  { id: 3, name: '項目C', amount: 3000 },
];

describe('Table', () => {
  it('renders table with headers', () => {
    render(<Table columns={testColumns} data={testData} keyField="id" />);

    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('名前')).toBeInTheDocument();
    expect(screen.getByText('金額')).toBeInTheDocument();
  });

  it('renders data rows', () => {
    render(<Table columns={testColumns} data={testData} keyField="id" />);

    expect(screen.getByText('項目A')).toBeInTheDocument();
    expect(screen.getByText('項目B')).toBeInTheDocument();
    expect(screen.getByText('項目C')).toBeInTheDocument();
    expect(screen.getByText('1000')).toBeInTheDocument();
    expect(screen.getByText('2000')).toBeInTheDocument();
    expect(screen.getByText('3000')).toBeInTheDocument();
  });

  it('renders empty message when no data', () => {
    render(<Table columns={testColumns} data={[]} keyField="id" />);
    expect(screen.getByText('データがありません')).toBeInTheDocument();
  });

  it('renders custom empty message', () => {
    render(
      <Table
        columns={testColumns}
        data={[]}
        keyField="id"
        emptyMessage="表示するデータがありません"
      />
    );
    expect(screen.getByText('表示するデータがありません')).toBeInTheDocument();
  });

  it('renders loading state', () => {
    render(<Table columns={testColumns} data={testData} keyField="id" isLoading />);
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();
  });

  it('calls onRowClick when row is clicked', async () => {
    const user = userEvent.setup();
    const handleRowClick = vi.fn();
    render(
      <Table columns={testColumns} data={testData} keyField="id" onRowClick={handleRowClick} />
    );

    await user.click(screen.getByText('項目A'));
    expect(handleRowClick).toHaveBeenCalledWith(testData[0], 0);
  });

  describe('column alignment', () => {
    it('applies left alignment by default', () => {
      const { container } = render(<Table columns={testColumns} data={testData} keyField="id" />);
      expect(container.querySelector('.table__th--left')).toBeInTheDocument();
    });

    it('applies right alignment', () => {
      const { container } = render(<Table columns={testColumns} data={testData} keyField="id" />);
      expect(container.querySelector('.table__th--right')).toBeInTheDocument();
    });
  });

  describe('custom render', () => {
    it('uses custom render function', () => {
      const columnsWithRender: TableColumn<TestData>[] = [
        ...testColumns.slice(0, 2),
        {
          key: 'amount',
          header: '金額',
          render: (value) => `¥${(value as number).toLocaleString()}`,
        },
      ];

      render(<Table columns={columnsWithRender} data={testData} keyField="id" />);
      expect(screen.getByText('¥1,000')).toBeInTheDocument();
    });
  });

  describe('selectable', () => {
    it('renders select all checkbox when selectable', () => {
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          onSelectionChange={() => {}}
        />
      );
      expect(screen.getByRole('checkbox', { name: '全選択' })).toBeInTheDocument();
    });

    it('renders row checkboxes when selectable', () => {
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          onSelectionChange={() => {}}
        />
      );
      const checkboxes = screen.getAllByRole('checkbox', { name: '行を選択' });
      expect(checkboxes).toHaveLength(3);
    });

    it('calls onSelectionChange when row checkbox is clicked', async () => {
      const user = userEvent.setup();
      const handleSelectionChange = vi.fn();
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set()}
          onSelectionChange={handleSelectionChange}
        />
      );

      const checkboxes = screen.getAllByRole('checkbox', { name: '行を選択' });
      await user.click(checkboxes[0]);
      expect(handleSelectionChange).toHaveBeenCalledWith(new Set([1]));
    });

    it('calls onSelectionChange when select all is clicked', async () => {
      const user = userEvent.setup();
      const handleSelectionChange = vi.fn();
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set()}
          onSelectionChange={handleSelectionChange}
        />
      );

      await user.click(screen.getByRole('checkbox', { name: '全選択' }));
      expect(handleSelectionChange).toHaveBeenCalledWith(new Set([1, 2, 3]));
    });

    it('deselects all when select all is clicked on fully selected table', async () => {
      const user = userEvent.setup();
      const handleSelectionChange = vi.fn();
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set([1, 2, 3])}
          onSelectionChange={handleSelectionChange}
        />
      );

      await user.click(screen.getByRole('checkbox', { name: '全選択' }));
      expect(handleSelectionChange).toHaveBeenCalledWith(new Set());
    });

    it('shows selected row as checked', () => {
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set([2])}
          onSelectionChange={() => {}}
        />
      );

      const checkboxes = screen.getAllByRole('checkbox', { name: '行を選択' });
      expect(checkboxes[0]).not.toBeChecked();
      expect(checkboxes[1]).toBeChecked();
      expect(checkboxes[2]).not.toBeChecked();
    });

    it('deselects row when checkbox is clicked on selected row', async () => {
      const user = userEvent.setup();
      const handleSelectionChange = vi.fn();
      render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set([1, 2])}
          onSelectionChange={handleSelectionChange}
        />
      );

      const checkboxes = screen.getAllByRole('checkbox', { name: '行を選択' });
      await user.click(checkboxes[0]);
      expect(handleSelectionChange).toHaveBeenCalledWith(new Set([2]));
    });
  });

  describe('row styling', () => {
    it('applies clickable class when onRowClick is provided', () => {
      const { container } = render(
        <Table columns={testColumns} data={testData} keyField="id" onRowClick={() => {}} />
      );
      expect(container.querySelector('.table__row--clickable')).toBeInTheDocument();
    });

    it('applies selected class when row is selected', () => {
      const { container } = render(
        <Table
          columns={testColumns}
          data={testData}
          keyField="id"
          selectable
          selectedKeys={new Set([1])}
          onSelectionChange={() => {}}
        />
      );
      expect(container.querySelector('.table__row--selected')).toBeInTheDocument();
    });
  });
});
