import React from 'react';
import './Pagination.css';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  itemsPerPage: number;
  onPageChange: (page: number) => void;
  onItemsPerPageChange?: (itemsPerPage: number) => void;
  itemsPerPageOptions?: number[];
}

const MAX_VISIBLE_PAGES = 5;
const ELLIPSIS = '...';

/**
 * ページ番号の配列を生成
 */
const generatePageNumbers = (currentPage: number, totalPages: number): (number | string)[] => {
  if (totalPages <= MAX_VISIBLE_PAGES + 2) {
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  return generateTruncatedPageNumbers(currentPage, totalPages);
};

const generateTruncatedPageNumbers = (
  currentPage: number,
  totalPages: number
): (number | string)[] => {
  const pages: (number | string)[] = [1];

  if (currentPage <= 3) {
    pages.push(...Array.from({ length: MAX_VISIBLE_PAGES - 1 }, (_, i) => i + 2), ELLIPSIS);
  } else if (currentPage >= totalPages - 2) {
    pages.push(
      ELLIPSIS,
      ...Array.from(
        { length: MAX_VISIBLE_PAGES - 1 },
        (_, i) => totalPages - MAX_VISIBLE_PAGES + i + 1
      )
    );
  } else {
    pages.push(ELLIPSIS, currentPage - 1, currentPage, currentPage + 1, ELLIPSIS);
  }

  pages.push(totalPages);
  return pages;
};

interface PageButtonProps {
  page: number | string;
  currentPage: number;
  onPageChange: (page: number) => void;
}

const PageButton: React.FC<PageButtonProps> = ({ page, currentPage, onPageChange }) => {
  if (typeof page === 'string') {
    return <span className="pagination__ellipsis">{page}</span>;
  }

  const isActive = page === currentPage;
  return (
    <button
      className={`pagination__page ${isActive ? 'is-active' : ''}`}
      onClick={() => onPageChange(page)}
    >
      {page}
    </button>
  );
};

/**
 * ページネーションコンポーネント
 *
 * ページ切り替えと表示件数の変更を提供する。
 */
export const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  totalItems,
  itemsPerPage,
  onPageChange,
  onItemsPerPageChange,
  itemsPerPageOptions = [10, 20, 50, 100],
}) => {
  const pageNumbers = generatePageNumbers(currentPage, totalPages);
  const hasPrevious = currentPage > 1;
  const hasNext = currentPage < totalPages;

  if (totalPages <= 1 && !onItemsPerPageChange) {
    return null;
  }

  return (
    <div className="pagination">
      <div className="pagination__nav">
        <button
          className="pagination__btn"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!hasPrevious}
        >
          &lt; 前へ
        </button>

        <div className="pagination__pages">
          {pageNumbers.map((page, index) => (
            <PageButton
              key={index}
              page={page}
              currentPage={currentPage}
              onPageChange={onPageChange}
            />
          ))}
        </div>

        <button
          className="pagination__btn"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!hasNext}
        >
          次へ &gt;
        </button>
      </div>

      <div className="pagination__info">
        {onItemsPerPageChange && (
          <div className="pagination__per-page">
            表示件数:
            <select
              className="pagination__select"
              value={itemsPerPage}
              onChange={(e) => onItemsPerPageChange(Number(e.target.value))}
            >
              {itemsPerPageOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
        )}
        <span className="pagination__total">全 {totalItems.toLocaleString()} 件</span>
      </div>
    </div>
  );
};
