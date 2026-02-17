/* eslint-disable react-refresh/only-export-components */
import React from 'react';

interface TooltipState {
  payload: Array<{ payload: unknown }>;
  label: string;
}

const state: TooltipState = { payload: [], label: '' };

export const setTooltipPayload = (payload: Array<{ payload: unknown }>) => {
  state.payload = payload;
};

export const setTooltipLabel = (label: string) => {
  state.label = label;
};

export const resetTooltipState = (defaultLabel = '') => {
  state.payload = [];
  state.label = defaultLabel;
};

export const ResponsiveContainer = ({ children }: { children: React.ReactNode }) => (
  <div data-testid="responsive-container">{children}</div>
);

export const LineChart = ({ children }: { children: React.ReactNode }) => (
  <div data-testid="line-chart">{children}</div>
);

export const CartesianGrid = () => <div data-testid="cartesian-grid" />;

export const XAxis = ({
  dataKey,
  tickFormatter,
}: {
  dataKey: string;
  tickFormatter?: (value: number) => string;
}) => (
  <div data-testid="x-axis" data-key={dataKey} data-tick={tickFormatter ? tickFormatter(1) : ''} />
);

export const YAxis = ({ tickFormatter }: { tickFormatter?: (value: number) => string }) => (
  <div data-testid="y-axis" data-value={tickFormatter ? tickFormatter(1000) : ''} />
);

export const Tooltip = ({ content }: { content?: React.ReactElement }) => (
  <div data-testid="tooltip">
    {content
      ? React.cloneElement(content, {
          active: true,
          payload: state.payload,
          label: state.label,
        })
      : null}
  </div>
);

export const Line = ({ dataKey }: { dataKey: string }) => (
  <div data-testid="line" data-key={dataKey} />
);
