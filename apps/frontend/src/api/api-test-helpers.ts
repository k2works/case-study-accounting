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
