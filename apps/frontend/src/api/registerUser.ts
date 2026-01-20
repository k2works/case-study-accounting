import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface RegisterUserRequest {
  username: string;
  email: string;
  password: string;
  displayName: string;
  role: string;
}

export interface RegisterUserResponse {
  success: boolean;
  username?: string;
  email?: string;
  displayName?: string;
  role?: string;
  errorMessage?: string;
}

export const registerUser = async (payload: RegisterUserRequest): Promise<RegisterUserResponse> => {
  const { data } = await axiosInstance.post<RegisterUserResponse>('/api/auth/register', payload);
  return data;
};

export const getRegisterErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as RegisterUserResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'ユーザー登録に失敗しました';
};
