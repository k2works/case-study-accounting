import React from 'react';
import { ManagerPage } from './ManagerPage';
import { CreateAccountStructureForm } from '../views/account-structure/CreateAccountStructureForm';

const breadcrumbs = [
  { label: 'ホーム' },
  { label: 'マスタ管理' },
  { label: '勘定科目体系', path: '/master/account-structures' },
  { label: '新規登録' },
];

const CreateAccountStructurePage: React.FC = () => {
  return (
    <ManagerPage breadcrumbs={breadcrumbs}>
      <div data-testid="create-account-structure-page">
        <h1>勘定科目体系 新規登録</h1>
        <CreateAccountStructureForm />
      </div>
    </ManagerPage>
  );
};

export default CreateAccountStructurePage;
