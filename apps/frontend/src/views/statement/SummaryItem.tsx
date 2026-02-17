import React from 'react';

interface SummaryItemProps {
  label: string;
  value: React.ReactNode;
}

export const SummaryItem: React.FC<SummaryItemProps> = ({ label, value }) => (
  <div>
    <div style={{ fontSize: '12px', color: '#666' }}>{label}</div>
    <div style={{ fontSize: '18px', fontWeight: 600 }}>{value}</div>
  </div>
);
