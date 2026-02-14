import { useCallback, useEffect, useState } from 'react';
import { getAccounts, getAccountsErrorMessage } from '../../api/getAccounts';
import type { Account } from '../../api/getAccounts';

interface UseAccountOptionsResult {
  accounts: Account[];
  isLoading: boolean;
  errorMessage: string | null;
  fetchAccounts: () => Promise<void>;
}

export const useAccountOptions = (): UseAccountOptionsResult => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchAccounts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await getAccounts();
      setAccounts(data);
    } catch (error) {
      setErrorMessage(getAccountsErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  return { accounts, isLoading, errorMessage, fetchAccounts };
};
