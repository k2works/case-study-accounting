import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import type { User } from './getUsers';

export type GetUserResponse = User;

export const getUser = async (userId: string): Promise<GetUserResponse> => {
  const { data } = await axiosInstance.get<GetUserResponse>(`/api/users/${userId}`);
  return data;
};

const extractNotFoundMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return 'ユーザーが見つかりませんでした';
  }
  const data = error.response?.data as { errorMessage?: string } | undefined;
  return data?.errorMessage ?? null;
};

export const getUserErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractNotFoundMessage(error) ?? 'ユーザー情報の取得に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'ユーザー情報の取得に失敗しました';
};
