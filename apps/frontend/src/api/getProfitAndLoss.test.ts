import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  getProfitAndLoss,
  getProfitAndLossErrorMessage,
  exportProfitAndLoss,
} from './getProfitAndLoss';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getProfitAndLoss', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns data with sections', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
        sections: [
          {
            sectionType: 'REVENUE',
            sectionDisplayName: '収益の部',
            entries: [],
            subtotal: 0,
            comparativeSubtotal: null,
          },
        ],
        totalRevenue: 1000000,
        totalExpense: 700000,
        netIncome: 300000,
      },
    });

    const result = await getProfitAndLoss({
      dateFrom: '2024-01-01',
      dateTo: '2024-12-31',
      comparativeDateFrom: '2023-01-01',
      comparativeDateTo: '2023-12-31',
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/profit-and-loss?dateFrom=2024-01-01&dateTo=2024-12-31&comparativeDateFrom=2023-01-01&comparativeDateTo=2023-12-31'
    );
    expect(result.sections).toHaveLength(1);
    expect(result.totalRevenue).toBe(1000000);
    expect(result.netIncome).toBe(300000);
  });

  it('omits empty params and defaults sections to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        dateFrom: null,
        dateTo: null,
        comparativeDateFrom: null,
        comparativeDateTo: null,
        totalRevenue: 0,
        totalExpense: 0,
        netIncome: 0,
      },
    });

    const result = await getProfitAndLoss({});

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/profit-and-loss');
    expect(result.sections).toEqual([]);
  });

  it('builds partial query params correctly', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        dateFrom: '2024-01-01',
        dateTo: null,
        comparativeDateFrom: null,
        comparativeDateTo: null,
        sections: [],
        totalRevenue: 0,
        totalExpense: 0,
        netIncome: 0,
      },
    });

    await getProfitAndLoss({ dateFrom: '2024-01-01' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/profit-and-loss?dateFrom=2024-01-01');
  });
});

describe('exportProfitAndLoss', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('calls the export endpoint with format, dateFrom and dateTo', async () => {
    const mockBlob = new Blob(['test']);
    mockAxiosGet.mockResolvedValue({ data: mockBlob });

    const createObjectURL = vi.fn(() => 'blob:test');
    const revokeObjectURL = vi.fn();
    window.URL.createObjectURL = createObjectURL;
    window.URL.revokeObjectURL = revokeObjectURL;

    const appendChildSpy = vi
      .spyOn(document.body, 'appendChild')
      .mockImplementation((node) => node);
    const clickSpy = vi.fn();
    const removeSpy = vi.fn();
    vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click: clickSpy,
      remove: removeSpy,
      set style(_v: string) {
        /* noop */
      },
    } as unknown as HTMLAnchorElement);

    await exportProfitAndLoss('pdf', '2024-01-01', '2024-12-31');

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/profit-and-loss/export?format=pdf&dateFrom=2024-01-01&dateTo=2024-12-31',
      { responseType: 'blob' }
    );
    expect(clickSpy).toHaveBeenCalled();
    expect(removeSpy).toHaveBeenCalled();
    expect(revokeObjectURL).toHaveBeenCalled();

    appendChildSpy.mockRestore();
  });

  it('calls the export endpoint with excel format and no dates', async () => {
    const mockBlob = new Blob(['test']);
    mockAxiosGet.mockResolvedValue({ data: mockBlob });

    const createObjectURL = vi.fn(() => 'blob:test');
    const revokeObjectURL = vi.fn();
    window.URL.createObjectURL = createObjectURL;
    window.URL.revokeObjectURL = revokeObjectURL;

    const appendChildSpy = vi
      .spyOn(document.body, 'appendChild')
      .mockImplementation((node) => node);
    const clickSpy = vi.fn();
    const removeSpy = vi.fn();
    vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click: clickSpy,
      remove: removeSpy,
      set style(_v: string) {
        /* noop */
      },
    } as unknown as HTMLAnchorElement);

    await exportProfitAndLoss('excel');

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/profit-and-loss/export?format=excel', {
      responseType: 'blob',
    });
    expect(clickSpy).toHaveBeenCalled();

    appendChildSpy.mockRestore();
  });
});

describe('getProfitAndLossErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getProfitAndLossErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getProfitAndLossErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getProfitAndLossErrorMessage('unknown')).toBe('損益計算書の取得に失敗しました');
  });
});
