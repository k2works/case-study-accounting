export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/api',
  appName: import.meta.env.VITE_APP_NAME || '財務会計システム',
  isDev: import.meta.env.VITE_DEV_MODE === 'true',
};
