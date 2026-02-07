import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface User extends Record<string, unknown> {
  id: string;
  username: string;
  email: string;
  displayName: string;
  role: string;
}

export const getUsers = async (): Promise<User[]> => {
  const { data } = await axiosInstance.get<User[]>('/api/users');
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
