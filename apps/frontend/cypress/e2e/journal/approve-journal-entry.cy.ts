/**
 * 仕訳承認 E2E テスト
 *
 * US-JNL-008: 仕訳承認
 *
 * 受入条件:
 * - 「承認待ち」ステータスの仕訳のみ承認できる
 * - 承認後、ステータスが「承認済み」に変わる
 * - 承認者と承認日時が記録される
 * - 承認成功時、確認メッセージが表示される
 */

import {
  createWorkflowTests,
  approveJournalEntryConfig,
} from '../../support/journal-workflow-tests';

createWorkflowTests(approveJournalEntryConfig);
