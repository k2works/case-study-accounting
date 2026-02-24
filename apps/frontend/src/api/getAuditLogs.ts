import { axiosInstance } from './axios-instance';
import { getStatementErrorMessage } from './statementShared';

export interface AuditLogEntry {
  id: number;
  userId: string;
  actionType: string;
  actionTypeDisplayName: string;
  entityType: string | null;
  entityTypeDisplayName: string | null;
  entityId: string | null;
  description: string;
  ipAddress: string;
  createdAt: string;
}

export interface GetAuditLogsResult {
  auditLogs: AuditLogEntry[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
}

export interface AuditLogSearchParams {
  userId?: string;
  actionType?: string;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
}

const buildSearchParams = (params: AuditLogSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (params.userId) searchParams.append('userId', params.userId);
  if (params.actionType) searchParams.append('actionType', params.actionType);
  if (params.dateFrom) searchParams.append('dateFrom', params.dateFrom);
  if (params.dateTo) searchParams.append('dateTo', params.dateTo);
  if (params.page !== undefined) searchParams.append('page', String(params.page));
  if (params.size !== undefined) searchParams.append('size', String(params.size));
  return searchParams;
};

export const getAuditLogs = async (params: AuditLogSearchParams): Promise<GetAuditLogsResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/audit-logs?${queryString}` : '/api/audit-logs';
  const { data } = await axiosInstance.get<GetAuditLogsResult>(url);
  return { ...data, auditLogs: data.auditLogs ?? [] };
};

export const getAuditLogsErrorMessage = (error: unknown): string =>
  getStatementErrorMessage(error, '監査ログの取得に失敗しました');
