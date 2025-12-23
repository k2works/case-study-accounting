import axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { config } from '../config';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

/**
 * Axios インスタンス
 */
export const axiosInstance = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * リクエストインターセプター
 * - アクセストークンを Authorization ヘッダーに付与
 */
axiosInstance.interceptors.request.use(
  (requestConfig: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (token && requestConfig.headers) {
      requestConfig.headers.Authorization = `Bearer ${token}`;
    }
    return requestConfig;
  },
  (error) => {
    throw error;
  }
);

/**
 * 認証ストレージをクリアしてログインページにリダイレクト
 */
const clearAuthAndRedirect = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem('user');
  window.location.href = '/login';
};

/**
 * トークンをリフレッシュする
 */
const refreshAccessToken = async (): Promise<string> => {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!refreshToken) {
    throw new Error('No refresh token');
  }

  const response = await axios.post('/api/auth/refresh', {
    refreshToken,
  });

  const { accessToken } = response.data;
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  return accessToken;
};

/**
 * 401 エラーかどうかを判定
 */
const isUnauthorizedError = (error: AxiosError): boolean => {
  return error.response?.status === 401;
};

/**
 * ログインエンドポイントかどうかを判定
 */
const isLoginEndpoint = (url?: string): boolean => {
  return url?.includes('/auth/login') ?? false;
};

/**
 * レスポンスインターセプター
 * - 401 エラー時にリフレッシュトークンで再認証を試みる
 */
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean;
    };

    const shouldRetry =
      isUnauthorizedError(error) &&
      !originalRequest._retry &&
      !isLoginEndpoint(originalRequest.url);

    if (!shouldRetry) {
      throw error;
    }

    originalRequest._retry = true;

    try {
      const newToken = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return axiosInstance(originalRequest);
    } catch {
      clearAuthAndRedirect();
      throw error;
    }
  }
);

/**
 * Orval 用カスタムインスタンス
 *
 * Orval が生成する API クライアントから呼び出される。
 * AxiosRequestConfig を受け取り、レスポンスデータを返す。
 *
 * @param config - Axios リクエスト設定
 * @param options - 追加のリクエストオプション
 * @returns レスポンスデータ
 */
export const customInstance = <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig
): Promise<T> => {
  const source = axios.CancelToken.source();
  const promise = axiosInstance({
    ...config,
    ...options,
    cancelToken: source.token,
  }).then(({ data }) => data);

  // @ts-expect-error - Orval が cancel メソッドを使用するため
  promise.cancel = () => {
    source.cancel('Query was cancelled');
  };

  return promise;
};
