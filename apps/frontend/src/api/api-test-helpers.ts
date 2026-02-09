import { AxiosError } from 'axios';

/**
 * API テスト用のユーティリティ関数
 */

/**
 * 404 エラーレスポンスを作成
 */
export const create404Error = (): AxiosError => {
  const error = new AxiosError('Not found');
  error.response = {
    data: {},
    status: 404,
    statusText: 'Not Found',
    headers: {},
    config: {} as never,
  };
  return error;
};

/**
 * カスタムエラーメッセージ付きのエラーレスポンスを作成
 */
export const createErrorWithMessage = (errorMessage: string, status = 400): AxiosError => {
  const error = new AxiosError('Bad Request');
  error.response = {
    data: { errorMessage },
    status,
    statusText: 'Bad Request',
    headers: {},
    config: {} as never,
  };
  return error;
};

/**
 * エラーメッセージ抽出関数のテストケース設定
 */
export interface ErrorMessageTestOptions {
  notFoundMessage: string;
  customErrorMessage: string;
  defaultErrorMessage: string;
}

/**
 * エラーメッセージテストケースを生成
 */
export const createErrorMessageTestCases = (
  errorMessageFn: (error: unknown) => string,
  options: ErrorMessageTestOptions
) => {
  const { notFoundMessage, customErrorMessage, defaultErrorMessage } = options;

  return [
    {
      name: '404 の場合は仕訳が見つかりませんを返す',
      input: create404Error(),
      expected: notFoundMessage,
    },
    {
      name: 'API の errorMessage を優先して返す',
      input: createErrorWithMessage(customErrorMessage),
      expected: customErrorMessage,
    },
    {
      name: '汎用 Error の message を返す',
      input: new Error('failure'),
      expected: 'failure',
    },
    {
      name: '未知のエラーはデフォルトメッセージを返す',
      input: 'unknown',
      expected: defaultErrorMessage,
    },
  ].map(({ name, input, expected }) => ({
    name,
    run: () => {
      const result = errorMessageFn(input);
      if (result !== expected) {
        throw new Error(`Expected "${expected}" but got "${result}"`);
      }
    },
  }));
};

/**
 * 仕訳ステータス変更 API テスト設定
 */
export interface JournalStatusApiTestConfig {
  /** API 関数名（テスト説明用） */
  apiName: string;
  /** API エンドポイントのアクション部分（submit または approve） */
  action: string;
  /** 期待するステータス */
  expectedStatus: string;
  /** 成功メッセージ */
  successMessage: string;
  /** エラーメッセージ設定 */
  errorMessages: ErrorMessageTestOptions;
}

/**
 * 仕訳ステータス変更 API のテストケースを生成
 */
export const createJournalStatusApiTestCases = (config: JournalStatusApiTestConfig) => {
  const { apiName, action, expectedStatus, successMessage } = config;

  return {
    apiTestName: apiName,
    apiTestDescription: `${apiName} API を呼び出してレスポンスを返す`,
    expectedEndpoint: (id: number) => `/api/journal-entries/${id}/${action}`,
    mockResponse: {
      success: true,
      journalEntryId: 1,
      status: expectedStatus,
      message: successMessage,
    },
    expectedStatus,
  };
};

/**
 * axios post mock のファクトリを作成
 */
export const createAxiosPostMockFactory = () => ({
  axiosInstance: {
    post: () => Promise.resolve({ data: {} }),
  },
});
