import React, { createContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { login as apiLogin } from '../api/generated/認証/認証';
import { User, Role, AuthContextType } from '../types/auth';
import type { LoginResponse } from '../api/model';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

/**
 * JWT ペイロードのデコード
 */
const decodeJwtPayload = (token: string): { exp: number; sub: string } | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replaceAll('-', '+').replaceAll('_', '/');
    const payload = JSON.parse(window.atob(base64));
    return payload;
  } catch {
    return null;
  }
};

/**
 * トークンの有効期限チェック
 */
const isTokenExpired = (token: string, bufferSeconds = 60): boolean => {
  const payload = decodeJwtPayload(token);
  if (!payload) return true;

  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();
  const bufferTime = bufferSeconds * 1000;

  return currentTime >= expirationTime - bufferTime;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const queryClient = useQueryClient();

  /**
   * ストレージからの認証情報読み込み
   */
  const loadAuthFromStorage = useCallback((): User | null => {
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    const savedUser = localStorage.getItem(USER_KEY);

    if (!accessToken || !savedUser) {
      return null;
    }

    if (isTokenExpired(accessToken)) {
      return null;
    }

    try {
      return JSON.parse(savedUser) as User;
    } catch {
      return null;
    }
  }, []);

  /**
   * 認証情報の保存
   */
  const saveAuthToStorage = useCallback(
    (accessToken: string, refreshToken: string, userData: User) => {
      localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
    },
    []
  );

  /**
   * 認証情報のクリア
   */
  const clearAuthStorage = useCallback(() => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }, []);

  /**
   * 初期化
   */
  useEffect(() => {
    const initialize = () => {
      const savedUser = loadAuthFromStorage();

      if (savedUser) {
        setUser(savedUser);
      } else {
        clearAuthStorage();
      }

      setIsLoading(false);
    };

    initialize();
  }, [loadAuthFromStorage, clearAuthStorage]);

  /**
   * Axios エラーからエラーメッセージを抽出
   */
  const extractErrorMessage = (error: unknown): string | null => {
    if (error instanceof AxiosError && error.response?.data) {
      const responseData = error.response.data as LoginResponse;
      return responseData.errorMessage || null;
    }
    return null;
  };

  /**
   * ログインレスポンスを検証
   */
  const validateLoginResponse = (data: LoginResponse): void => {
    if (!data.success) {
      throw new Error(data.errorMessage || '認証に失敗しました');
    }
    if (!data.accessToken || !data.refreshToken || !data.username || !data.role) {
      throw new Error('認証レスポンスが不正です');
    }
  };

  /**
   * ログイン
   */
  const login = useCallback(
    async (username: string, password: string) => {
      try {
        const data = await apiLogin({ username, password });
        validateLoginResponse(data);

        const userData: User = {
          username: data.username!,
          role: data.role as Role,
        };

        saveAuthToStorage(data.accessToken!, data.refreshToken!, userData);
        setUser(userData);
        queryClient.clear();
      } catch (error) {
        const errorMessage = extractErrorMessage(error);
        if (errorMessage) {
          throw new Error(errorMessage);
        }
        throw error;
      }
    },
    [saveAuthToStorage, queryClient]
  );

  /**
   * ログアウト
   */
  const logout = useCallback(() => {
    clearAuthStorage();
    setUser(null);
    queryClient.clear();
  }, [clearAuthStorage, queryClient]);

  /**
   * ロールチェック
   */
  const hasRole = useCallback(
    (role: Role): boolean => {
      if (!user) return false;
      if (user.role === 'ADMIN') return true;
      return user.role === role;
    },
    [user]
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext };
export type { AuthContextType };
