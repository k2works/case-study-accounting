import React from 'react';
import { ManagerPage } from './ManagerPage';
import { CreateAutoJournalPatternForm } from '../views/auto-journal-pattern/CreateAutoJournalPatternForm';

const breadcrumbs = [
  { label: 'ホーム' },
  { label: 'マスタ管理' },
  { label: '自動仕訳パターン一覧', path: '/master/auto-journal-patterns' },
  { label: '自動仕訳パターン登録' },
];

const CreateAutoJournalPatternPage: React.FC = () => {
  return (
    <ManagerPage breadcrumbs={breadcrumbs}>
      <div data-testid="create-auto-journal-pattern-page">
        <h1>自動仕訳パターン登録</h1>
        <CreateAutoJournalPatternForm />
      </div>
    </ManagerPage>
  );
};

export default CreateAutoJournalPatternPage;
