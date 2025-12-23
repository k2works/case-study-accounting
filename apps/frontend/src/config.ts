export const config = {
  // OpenAPI spec のパスに /api が含まれているため、baseURL は空
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '',
  appName: import.meta.env.VITE_APP_NAME || '財務会計システム',
  isDev: import.meta.env.VITE_DEV_MODE === 'true',
};
