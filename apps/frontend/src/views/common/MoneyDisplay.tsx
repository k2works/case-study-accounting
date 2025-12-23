import React from 'react';
import './MoneyDisplay.css';

interface MoneyDisplayProps {
  amount: number;
  currency?: string;
  showSign?: boolean;
  colorize?: boolean;
  size?: 'small' | 'medium' | 'large';
}

/**
 * 金額フォーマット
 */
const formatMoney = (amount: number, currency: string): string => {
  const formatted = Math.abs(amount).toLocaleString('ja-JP');
  return `${currency}${formatted}`;
};

/**
 * 符号を取得
 */
const getSign = (amount: number): string => {
  if (amount > 0) return '+';
  if (amount < 0) return '-';
  return '';
};

/**
 * 色クラスを取得
 */
const getColorClass = (amount: number, colorize: boolean): string => {
  if (!colorize) return '';
  if (amount > 0) return 'money-display--positive';
  if (amount < 0) return 'money-display--negative';
  return '';
};

/**
 * 金額表示コンポーネント
 *
 * 金額を適切なフォーマットで表示する。
 */
export const MoneyDisplay: React.FC<MoneyDisplayProps> = ({
  amount,
  currency = '¥',
  showSign = false,
  colorize = false,
  size = 'medium',
}) => {
  const formattedAmount = formatMoney(amount, currency);
  const sign = getSign(amount);
  const displayValue = showSign && amount !== 0 ? `${sign}${formattedAmount}` : formattedAmount;
  const colorClass = getColorClass(amount, colorize);

  return (
    <span className={`money-display money-display--${size} ${colorClass}`}>{displayValue}</span>
  );
};
