/**
 * ユーザーロール
 */
export type Role = 'ADMIN' | 'MANAGER' | 'USER' | 'VIEWER';

/**
 * ユーザー情報
 */
export interface User {
  username: string;
  role: Role;
}

/**
 * ログインリクエスト
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * ログインレスポンス
 */
export interface LoginResponse {
  success: boolean;
  accessToken: string | null;
  refreshToken: string | null;
  username: string | null;
  role: string | null;
  errorMessage: string | null;
}

/**
 * 認証コンテキストの型
 */
export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: Role) => boolean;
}
