// Orval 生成型を再エクスポート
export type { LoginRequest, LoginResponse } from '../api/model';

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
