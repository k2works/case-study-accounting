import { getJournalEntries } from './getJournalEntries';

export interface DashboardStats {
  todayJournalCount: number;
  pendingApprovalCount: number;
}

export const getDashboardStats = async (): Promise<DashboardStats> => {
  const today = new Date().toISOString().split('T')[0];
  const [todayResult, pendingResult] = await Promise.all([
    getJournalEntries({ dateFrom: today, dateTo: today, size: 1 }),
    getJournalEntries({ status: ['PENDING'], size: 1 }),
  ]);
  return {
    todayJournalCount: todayResult.totalElements,
    pendingApprovalCount: pendingResult.totalElements,
  };
};
