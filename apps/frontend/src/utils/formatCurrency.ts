export const formatCurrency = (amount: number): string => {
  return amount.toLocaleString('ja-JP', { maximumFractionDigits: 0 });
};
