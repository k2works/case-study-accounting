import React, { ChangeEvent, FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { AccountStructure } from '../../api/getAccountStructures';
import { getAccountStructure } from '../../api/getAccountStructures';
import {
  updateAccountStructure,
  getUpdateAccountStructureErrorMessage,
} from '../../api/updateAccountStructure';
import { Button, ErrorMessage, Loading, SuccessNotification } from '../common';
import './EditAccountStructureForm.css';

interface EditAccountStructureFormProps {
  code: string;
}

interface FormData {
  parentAccountCode: string;
  displayOrder: number;
}

const createFormData = (structure: AccountStructure): FormData => ({
  parentAccountCode: structure.parentAccountCode ?? '',
  displayOrder: structure.displayOrder,
});

export const EditAccountStructureForm: React.FC<EditAccountStructureFormProps> = ({ code }) => {
  const navigate = useNavigate();
  const [structure, setStructure] = useState<AccountStructure | null>(null);
  const [formData, setFormData] = useState<FormData>({ parentAccountCode: '', displayOrder: 0 });
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const fetchStructure = async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const data = await getAccountStructure(code);
        setStructure(data);
        setFormData(createFormData(data));
      } catch {
        setErrorMessage('勘定科目構成の取得に失敗しました');
      } finally {
        setIsLoading(false);
      }
    };

    void fetchStructure();
  }, [code]);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'displayOrder' ? Number(value) : value,
    }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    setIsSubmitting(true);

    try {
      const response = await updateAccountStructure(code, {
        parentAccountCode: formData.parentAccountCode.trim() || null,
        displayOrder: formData.displayOrder,
      });

      if (!response.success) {
        throw new Error(response.errorMessage || '勘定科目構成の更新に失敗しました');
      }

      const message = response.message || '勘定科目構成を更新しました';
      setSuccessMessage(message);
      navigate('/master/account-structures', { replace: true, state: { successMessage: message } });
    } catch (error) {
      setErrorMessage(getUpdateAccountStructureErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <Loading message="勘定科目構成を読み込み中..." />;
  }

  if (!structure) {
    return <ErrorMessage message={errorMessage || '勘定科目構成が見つかりません'} />;
  }

  return (
    <form className="edit-account-structure-form" onSubmit={handleSubmit} noValidate>
      {errorMessage && (
        <ErrorMessage message={errorMessage} onDismiss={() => setErrorMessage(null)} />
      )}
      {successMessage && (
        <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
      )}

      <div className="edit-account-structure-form__field">
        <label htmlFor="accountCode" className="edit-account-structure-form__label">
          勘定科目コード
        </label>
        <input
          id="accountCode"
          name="accountCode"
          type="text"
          className="edit-account-structure-form__input"
          value={structure.accountCode}
          readOnly
          disabled
        />
      </div>

      <div className="edit-account-structure-form__field">
        <label htmlFor="parentAccountCode" className="edit-account-structure-form__label">
          親科目コード
        </label>
        <input
          id="parentAccountCode"
          name="parentAccountCode"
          type="text"
          className="edit-account-structure-form__input"
          value={formData.parentAccountCode}
          onChange={handleChange}
          disabled={isSubmitting}
        />
      </div>

      <div className="edit-account-structure-form__field">
        <label htmlFor="displayOrder" className="edit-account-structure-form__label">
          表示順
        </label>
        <input
          id="displayOrder"
          name="displayOrder"
          type="number"
          className="edit-account-structure-form__input"
          value={formData.displayOrder}
          onChange={handleChange}
          disabled={isSubmitting}
        />
      </div>

      <div className="edit-account-structure-form__actions">
        <Button
          type="button"
          variant="secondary"
          onClick={() => navigate('/master/account-structures')}
          disabled={isSubmitting}
        >
          戻る
        </Button>
        <Button type="submit" isLoading={isSubmitting}>
          更新
        </Button>
      </div>
    </form>
  );
};
