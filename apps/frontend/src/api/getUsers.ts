import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface User extends Record<string, unknown> {
  id: string;
  username: string;
  email: string;
  displayName: string;
  role: string;
  lastLoginAt: string | null;
}

export interface GetUsersParams {
  role?: string;
  keyword?: string;
}

export const getUsers = async (params?: GetUsersParams): Promise<User[]> => {
  const searchParams = new URLSearchParams();
  if (params?.role) searchParams.append('role', params.role);
  if (params?.keyword) searchParams.append('keyword', params.keyword);

  const query = searchParams.toString();
  const url = query ? `/api/users?${query}` : '/api/users';
  const { data } = await axiosInstance.get<User[]>(url);
  return data;
};

export const getUsersErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'ユーザー一覧の取得に失敗しました';
};
