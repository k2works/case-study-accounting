import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // キャッシュの有効期間（5分）
      staleTime: 5 * 60 * 1000,
      // ガベージコレクション期間（30分）
      gcTime: 30 * 60 * 1000,
      // リトライ回数
      retry: 1,
      // フォーカス時の再取得を無効化
      refetchOnWindowFocus: false,
      // マウント時の再取得
      refetchOnMount: true,
    },
    mutations: {
      // ミューテーションのリトライ
      retry: 0,
    },
  },
});
