import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
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
    return Promise.reject(error);
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

  const response = await axios.post(`${config.apiBaseUrl}/auth/refresh`, {
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
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const newToken = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return axiosInstance(originalRequest);
    } catch {
      clearAuthAndRedirect();
      return Promise.reject(error);
    }
  }
);

export default axiosInstance;
