import React from 'react';
import { Link } from 'react-router-dom';
import './Breadcrumb.css';

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

/**
 * パンくずリストコンポーネント
 *
 * 現在のページ位置を階層的に表示する。
 */
export const Breadcrumb: React.FC<BreadcrumbProps> = ({ items }) => {
  if (items.length === 0) {
    return null;
  }

  return (
    <nav className="breadcrumb" aria-label="パンくずリスト">
      <ol className="breadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;

          return (
            <li key={index} className={`breadcrumb__item ${isLast ? 'is-current' : ''}`}>
              {isLast || !item.path ? (
                <span className="breadcrumb__text">{item.label}</span>
              ) : (
                <Link to={item.path} className="breadcrumb__link">
                  {item.label}
                </Link>
              )}
              {!isLast && <span className="breadcrumb__separator">&gt;</span>}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
