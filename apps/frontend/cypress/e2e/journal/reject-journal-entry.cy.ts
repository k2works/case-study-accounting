/**
 * 仕訳差し戻し E2E テスト
 *
 * US-JNL-009: 仕訳差し戻し
 *
 * 受入条件:
 * - 「承認待ち」ステータスの仕訳のみ差し戻しできる
 * - 差し戻し理由を入力できる
 * - 差し戻し後、ステータスが「下書き」に戻る
 * - 差し戻し成功時、確認メッセージが表示される
 */

import {
  createWorkflowTests,
  rejectJournalEntryConfig,
} from '../../support/journal-workflow-tests';

createWorkflowTests(rejectJournalEntryConfig);
