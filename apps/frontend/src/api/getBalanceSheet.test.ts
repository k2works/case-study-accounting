import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  getBalanceSheet,
  getBalanceSheetErrorMessage,
  exportBalanceSheet,
} from './getBalanceSheet';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getBalanceSheet', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns data with sections defaulting to []', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        date: '2024-12-31',
        comparativeDate: '2023-12-31',
        sections: [
          {
            sectionType: 'ASSET',
            sectionDisplayName: '資産の部',
            entries: [],
            subtotal: 0,
            comparativeSubtotal: null,
          },
        ],
        totalAssets: 100000,
        totalLiabilities: 50000,
        totalEquity: 50000,
        totalLiabilitiesAndEquity: 100000,
        balanced: true,
        difference: 0,
      },
    });

    const result = await getBalanceSheet({
      date: '2024-12-31',
      comparativeDate: '2023-12-31',
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/balance-sheet?date=2024-12-31&comparativeDate=2023-12-31'
    );
    expect(result.sections).toHaveLength(1);
    expect(result.totalAssets).toBe(100000);
  });

  it('omits empty params and defaults sections to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        date: null,
        comparativeDate: null,
        totalAssets: 0,
        totalLiabilities: 0,
        totalEquity: 0,
        totalLiabilitiesAndEquity: 0,
        balanced: true,
        difference: 0,
      },
    });

    const result = await getBalanceSheet({});

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/balance-sheet');
    expect(result.sections).toEqual([]);
  });
});

describe('exportBalanceSheet', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('calls the export endpoint with format and date', async () => {
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

    await exportBalanceSheet('pdf', '2024-12-31');

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/balance-sheet/export?format=pdf&date=2024-12-31',
      { responseType: 'blob' }
    );
    expect(clickSpy).toHaveBeenCalled();
    expect(removeSpy).toHaveBeenCalled();
    expect(revokeObjectURL).toHaveBeenCalled();

    appendChildSpy.mockRestore();
  });
});

describe('getBalanceSheetErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getBalanceSheetErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getBalanceSheetErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getBalanceSheetErrorMessage('unknown')).toBe('貸借対照表の取得に失敗しました');
  });
});
