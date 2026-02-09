import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface UpdateUserRequest {
  displayName: string;
  password?: string;
  role: string;
}

export interface UpdateUserResponse {
  success: boolean;
  id?: string;
  username?: string;
  email?: string;
  displayName?: string;
  role?: string;
  errorMessage?: string;
}

export const updateUser = async (
  userId: string,
  payload: UpdateUserRequest
): Promise<UpdateUserResponse> => {
  const { data } = await axiosInstance.put<UpdateUserResponse>(`/api/users/${userId}`, payload);
  return data;
};

export const getUpdateErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as UpdateUserResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'ユーザー更新に失敗しました';
};
