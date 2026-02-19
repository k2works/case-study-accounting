import React from 'react';
import './Table.css';

export interface TableColumn<T> {
  key: string;
  header: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  render?: (value: unknown, row: T, index: number) => React.ReactNode;
}

interface TableProps<T> {
  readonly columns: TableColumn<T>[];
  readonly data: T[];
  readonly keyField: keyof T;
  readonly onRowClick?: (row: T, index: number) => void;
  readonly getRowTestId?: (row: T, index: number) => string | undefined;
  readonly isLoading?: boolean;
  readonly emptyMessage?: string;
  readonly selectable?: boolean;
  readonly selectedKeys?: Set<string | number>;
  readonly onSelectionChange?: (selectedKeys: Set<string | number>) => void;
}

interface TableBodyContentProps<T> {
  columns: TableColumn<T>[];
  data: T[];
  keyField: keyof T;
  selectable: boolean;
  selectedKeys: Set<string | number>;
  onRowClick?: (row: T, index: number) => void;
  getRowTestId?: (row: T, index: number) => string | undefined;
  onSelectRow: (key: string | number) => void;
  isLoading: boolean;
  emptyMessage: string;
}

const TableBodyContent = <T extends Record<string, unknown>>({
  columns,
  data,
  keyField,
  selectable,
  selectedKeys,
  onRowClick,
  getRowTestId,
  onSelectRow,
  isLoading,
  emptyMessage,
}: TableBodyContentProps<T>): React.ReactElement => {
  const colSpan = columns.length + (selectable ? 1 : 0);

  if (isLoading) {
    return (
      <tr>
        <td colSpan={colSpan} className="table__td table__td--loading">
          読み込み中...
        </td>
      </tr>
    );
  }

  if (data.length === 0) {
    return (
      <tr>
        <td colSpan={colSpan} className="table__td table__td--empty">
          {emptyMessage}
        </td>
      </tr>
    );
  }

  return (
    <>
      {data.map((row, rowIndex) => {
        const rowKey = row[keyField] as string | number;
        const isSelected = selectedKeys.has(rowKey);
        const rowClass = buildRowClassName(!!onRowClick, isSelected);

        return (
          <tr
            key={rowKey}
            className={rowClass}
            onClick={() => onRowClick?.(row, rowIndex)}
            data-testid={getRowTestId?.(row, rowIndex)}
          >
            {selectable && (
              <td className="table__td table__td--checkbox">
                <input
                  type="checkbox"
                  checked={isSelected}
                  onChange={() => onSelectRow(rowKey)}
                  onClick={(e) => e.stopPropagation()}
                  aria-label="行を選択"
                />
              </td>
            )}
            {columns.map((column) => renderCell(column, row, rowIndex))}
          </tr>
        );
      })}
    </>
  );
};

const buildRowClassName = (isClickable: boolean, isSelected: boolean): string => {
  const classes = ['table__row'];
  if (isClickable) classes.push('table__row--clickable');
  if (isSelected) classes.push('table__row--selected');
  return classes.join(' ');
};

const formatCellValue = (value: unknown): string => {
  if (value === null || value === undefined) return '';
  if (typeof value === 'object' || typeof value === 'function') {
    return JSON.stringify(value);
  }
  return String(value as string | number | boolean | bigint | symbol);
};

const renderCell = <T extends Record<string, unknown>>(
  column: TableColumn<T>,
  row: T,
  rowIndex: number
): React.ReactElement => {
  const value = row[column.key];
  const content = column.render ? column.render(value, row, rowIndex) : formatCellValue(value);
  return (
    <td key={column.key} className={`table__td table__td--${column.align || 'left'}`}>
      {content}
    </td>
  );
};

const ensureArray = <T,>(data: T[] | undefined | null): T[] => {
  return Array.isArray(data) ? data : [];
};

const createSelectAllHandler = <T extends Record<string, unknown>>(
  data: T[],
  keyField: keyof T,
  onSelectionChange?: (selectedKeys: Set<string | number>) => void
) => {
  return (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!onSelectionChange) return;
    const newKeys = e.target.checked
      ? new Set(data.map((row) => row[keyField] as string | number))
      : new Set<string | number>();
    onSelectionChange(newKeys);
  };
};

const createSelectRowHandler = (
  selectedKeys: Set<string | number>,
  onSelectionChange?: (selectedKeys: Set<string | number>) => void
) => {
  return (key: string | number) => {
    if (!onSelectionChange) return;
    const newSelectedKeys = new Set(selectedKeys);
    if (newSelectedKeys.has(key)) {
      newSelectedKeys.delete(key);
    } else {
      newSelectedKeys.add(key);
    }
    onSelectionChange(newSelectedKeys);
  };
};

/**
 * テーブルコンポーネント
 *
 * データを表形式で表示する汎用コンポーネント。
 */
export function Table<T extends Record<string, unknown>>({
  columns,
  data,
  keyField,
  onRowClick,
  getRowTestId,
  isLoading = false,
  emptyMessage = 'データがありません',
  selectable = false,
  selectedKeys = new Set(),
  onSelectionChange,
}: TableProps<T>): React.ReactElement {
  const safeData = ensureArray(data);
  const handleSelectAll = createSelectAllHandler(safeData, keyField, onSelectionChange);
  const handleSelectRow = createSelectRowHandler(selectedKeys, onSelectionChange);
  const isAllSelected =
    safeData.length > 0 &&
    safeData.every((row) => selectedKeys.has(row[keyField] as string | number));

  return (
    <div className="table-container">
      <table className="table">
        <thead className="table__head">
          <tr>
            {selectable && (
              <th className="table__th table__th--checkbox">
                <input
                  type="checkbox"
                  checked={isAllSelected}
                  onChange={handleSelectAll}
                  aria-label="全選択"
                />
              </th>
            )}
            {columns.map((column) => (
              <th
                key={column.key}
                className={`table__th table__th--${column.align || 'left'}`}
                style={{ width: column.width }}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="table__body">
          <TableBodyContent
            columns={columns}
            data={safeData}
            keyField={keyField}
            selectable={selectable}
            selectedKeys={selectedKeys}
            onRowClick={onRowClick}
            getRowTestId={getRowTestId}
            onSelectRow={handleSelectRow}
            isLoading={isLoading}
            emptyMessage={emptyMessage}
          />
        </tbody>
      </table>
    </div>
  );
}
