import React, { ChangeEvent, FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  createAccountStructure,
  getCreateAccountStructureErrorMessage,
} from '../../api/createAccountStructure';
import { Button, ErrorMessage, SuccessNotification } from '../common';
import './CreateAccountStructureForm.css';

interface FormData {
  accountCode: string;
  parentAccountCode: string;
  displayOrder: number;
}

interface FormErrors {
  accountCode?: string;
}

const initialFormData: FormData = {
  accountCode: '',
  parentAccountCode: '',
  displayOrder: 0,
};

const validateFormData = (formData: FormData): FormErrors => {
  const errors: FormErrors = {};
  if (!formData.accountCode.trim()) {
    errors.accountCode = '勘定科目コードを入力してください';
  }
  return errors;
};

export const CreateAccountStructureForm: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<FormData>(initialFormData);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: name === 'displayOrder' ? Number(value) : value,
    }));

    if (name === 'accountCode' && errors.accountCode) {
      setErrors((prev) => ({ ...prev, accountCode: undefined }));
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await createAccountStructure({
        accountCode: formData.accountCode.trim(),
        parentAccountCode: formData.parentAccountCode.trim() || null,
        displayOrder: formData.displayOrder,
      });

      if (!response.success) {
        throw new Error(response.errorMessage || '勘定科目構成の登録に失敗しました');
      }

      const message = '勘定科目構成を登録しました';
      setSuccessMessage(message);
      setFormData(initialFormData);
      navigate('/master/account-structures', { state: { successMessage: message } });
    } catch (error) {
      setErrorMessage(getCreateAccountStructureErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="create-account-structure-form" onSubmit={handleSubmit} noValidate>
      {errorMessage && (
        <ErrorMessage message={errorMessage} onDismiss={() => setErrorMessage(null)} />
      )}
      {successMessage && (
        <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
      )}

      <div className="create-account-structure-form__field">
        <label htmlFor="accountCode" className="create-account-structure-form__label">
          勘定科目コード
        </label>
        <input
          id="accountCode"
          name="accountCode"
          type="text"
          className={`create-account-structure-form__input ${errors.accountCode ? 'create-account-structure-form__input--error' : ''}`}
          value={formData.accountCode}
          onChange={handleChange}
          disabled={isSubmitting}
          required
        />
        {errors.accountCode && (
          <div className="create-account-structure-form__error">{errors.accountCode}</div>
        )}
      </div>

      <div className="create-account-structure-form__field">
        <label htmlFor="parentAccountCode" className="create-account-structure-form__label">
          親科目コード
        </label>
        <input
          id="parentAccountCode"
          name="parentAccountCode"
          type="text"
          className="create-account-structure-form__input"
          value={formData.parentAccountCode}
          onChange={handleChange}
          disabled={isSubmitting}
        />
      </div>

      <div className="create-account-structure-form__field">
        <label htmlFor="displayOrder" className="create-account-structure-form__label">
          表示順
        </label>
        <input
          id="displayOrder"
          name="displayOrder"
          type="number"
          className="create-account-structure-form__input"
          value={formData.displayOrder}
          onChange={handleChange}
          disabled={isSubmitting}
        />
      </div>

      <div className="create-account-structure-form__actions">
        <Button
          type="button"
          variant="secondary"
          onClick={() => navigate('/master/account-structures')}
          disabled={isSubmitting}
        >
          戻る
        </Button>
        <Button type="submit" isLoading={isSubmitting}>
          登録
        </Button>
      </div>
    </form>
  );
};
