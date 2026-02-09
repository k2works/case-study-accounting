import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DeleteUserResponse {
  success: boolean;
  errorMessage?: string;
}

export const deleteUser = async (userId: string): Promise<DeleteUserResponse> => {
  const { data } = await axiosInstance.delete<DeleteUserResponse>(`/api/users/${userId}`);
  return data;
};

export const getDeleteUserErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as DeleteUserResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'ユーザー削除に失敗しました';
};
