/**
 * 仕訳承認申請 E2E テスト
 *
 * US-JNL-007: 仕訳承認申請
 *
 * 受入条件:
 * - 「下書き」ステータスの仕訳のみ承認申請できる
 * - 承認申請後、ステータスが「承認待ち」に変わる
 * - 承認申請成功時、確認メッセージが表示される
 */

import {
  createWorkflowTests,
  submitJournalEntryConfig,
} from '../../support/journal-workflow-tests';

createWorkflowTests(submitJournalEntryConfig);
