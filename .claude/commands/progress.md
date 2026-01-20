## Progress

プロジェクトの開発進捗を包括的に確認し、現在の状況を詳細に報告するコマンド。

### 使い方

```bash
/progress [オプション]
```

### オプション

- なし : 全体的な進捗確認（デフォルト）
- `--brief` : 簡潔な進捗サマリー表示
- `--detailed` : 詳細な進捗レポート生成
- `--iteration <番号>` : 特定イテレーションの進捗確認
- `--technical` : 技術的な実装状況に焦点
- `--docs` : ドキュメント更新状況の確認
- `--update` : `docs/development/` 内のドキュメントを現在の進捗に合わせて更新

### 基本例

```bash
# 基本的な進捗確認
/progress
「プロジェクト全体の開発進捗を包括的にレポート」

# 簡潔なサマリー
/progress --brief
「要点のみを絞った進捗状況を表示」

# 詳細レポート
/progress --detailed
「技術実装・テスト・ドキュメント・品質指標を含む詳細分析」

# 特定イテレーションの確認
/progress --iteration 1
「イテレーション1の完了状況と成果を詳細分析」

# 技術的な実装状況
/progress --technical
「コード実装・テスト・アーキテクチャの技術的進捗を確認」

# ドキュメント状況
/progress --docs
「計画・仕様・READMEなどのドキュメント更新状況を確認」

# 進捗をドキュメントに反映
/progress --update
「release_plan.md と iteration_plan-*.md を現在の進捗に合わせて更新」
```

### 詳細機能

#### 包括的な進捗分析

プロジェクトの多角的な視点から現在の進捗状況を分析：

- **イテレーション達成度**: 計画vs実績の比較分析
- **技術実装状況**: 機能完成度・品質指標・テスト結果
- **アーキテクチャ進展**: 設計の実装状況と技術的負債
- **ドキュメント整備**: 計画書・仕様書・実行手順書の最新性

#### 動的な状況判定

```bash
# Git履歴の分析
git log --oneline -10
git status --porcelain

# テスト実行状況の確認
dotnet test --logger trx
npm test -- --coverage

# 実行環境の状況確認
ps aux | grep -E "dotnet|node|npm"
netstat -tlnp | grep -E ":3000|:5150"
```

#### 品質メトリクス取得

実装品質の定量的評価：

- **テストカバレッジ**: 単体・統合・E2Eテストの網羅率
- **コード品質**: 静的解析結果・複雑度測定
- **性能指標**: レスポンス時間・スループット測定
- **セキュリティ**: 脆弱性スキャン結果

#### --update オプションの詳細

`--update` オプションは `docs/development/` 内のドキュメントを現在の進捗に合わせて自動更新します。

**更新対象ファイル**:

- `docs/development/release_plan.md` - リリース計画
- `docs/development/iteration_plan-*.md` - イテレーション計画

**release_plan.md の更新内容**:

1. **進捗状況テーブル**: 各イテレーションの実績 SP、達成率、状態を更新
2. **リリース条件チェックボックス**: 完了した条件にチェックを入れる
3. **バーンダウンチャート**: 実績値を反映

**iteration_plan-*.md の更新内容**:

1. **成功基準チェックボックス**: 達成した基準にチェックを入れる
2. **タスク分解の状態**: 完了したタスクのチェックボックスを更新
3. **完了条件（DoD）**: 達成した条件にチェックを入れる
4. **振り返りセクション**: メトリクス実績値を記入

**更新判定の情報源**:

```bash
# Git コミット履歴から実装状況を分析
git log --oneline --since="2 weeks ago"

# テスト実行結果から品質指標を取得
dotnet test --logger trx
npm test -- --coverage

# ディレクトリ構造から機能実装状況を確認
ls -la apps/backend/src/
ls -la apps/frontend/src/
```

**出力例**:

```
ドキュメント更新
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 docs/development/release_plan.md を更新しています...
   ✅ イテレーション 1 の進捗状況を更新
   ✅ Release 1.0 のリリース条件を更新

📝 docs/development/iteration_plan-1.md を更新しています...
   ✅ 成功基準のチェック状態を更新 (3/5 完了)
   ✅ タスク分解の状態を更新 (15/21 完了)
   ✅ メトリクス実績値を更新

更新完了！
```

### 出力例

```
プロジェクト進捗レポート
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 Overall Status: フルスタック実装完成 (MVP準備段階)

🎯 Iteration Progress:
├─ Iteration 1 (認証・閲覧): ✅ 完了 (計画10日→実績4日, 60%短縮)
├─ Iteration 2 (予約機能): 📋 計画済み (8営業日予定)
├─ Iteration 3 (キャンセル): ⏳ 待機中
└─ Iteration 4 (運用準備): ⏳ 待機中

💻 Technical Implementation:
├─ Backend (.NET 9): ✅ 完全実装 (181テスト全通過)
├─ Frontend (React TS): ✅ 完全実装 (認証・会議室一覧)
├─ Database (SQLite): ✅ 稼働中 (自動初期化対応)
├─ Authentication: ✅ JWT完全実装 (ログアウト問題解決済)
└─ API Integration: ✅ フルスタック連携確認

🚀 Runtime Status:
├─ Backend API: ✅ http://localhost:5150 (稼働中)
├─ Frontend: ✅ http://localhost:3000 (稼働中)
├─ Test Coverage: ✅ 95%+ (181件全通過)
└─ Build Status: ✅ 警告・エラーなし

📚 Documentation:
├─ 計画書: ✅ 最新 (イテレーション1完了・イテレーション2計画)
├─ リリース計画: ✅ 更新済み (実績反映・短縮スケジュール)
├─ README: ✅ 実行手順書完備
└─ API仕様: ✅ Swagger UI統合

🎉 Key Achievements:
• フルスタック基盤完成 (計画を大幅上回る成果)
• 即座ログアウト問題の完全解決
• React TypeScript UI の完全実装
• 181テスト全通過の高品質実装

🔜 Next Steps:
• イテレーション2開始準備完了
• 予約機能実装によるMVP達成
• 楽観的ロック・重複防止実装
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Claude との連携

```bash
# 現在の実行状況と組み合わせ
ps aux | grep -E "dotnet|node"
netstat -tlnp | grep -E ":3000|:5150"
/progress --technical
「実行中のサービスを含めた技術的進捗分析を依頼」

# Git状況と組み合わせ
git log --oneline -5
git status
/progress --detailed
「最新のコミット情報を含めた詳細進捗レポート」

# テスト結果と組み合わせ
dotnet test --logger trx
/progress --brief
「テスト実行結果を含めた簡潔な進捗確認」

# ドキュメント状況と組み合わせ
find docs/ -name "*.md" -newer docs/development/イテレーション計画1.md
/progress --docs
「最新のドキュメント更新を含めた文書整備状況の確認」

# 進捗をドキュメントに反映
/progress --update
「現在の実装状況を分析し、release_plan.md と iteration_plan-*.md を更新」

# イテレーション完了時のドキュメント更新
git log --oneline -20
/progress --update
「イテレーション完了時に振り返りセクションを含む全ドキュメントを更新」
```

### 注意事項

- **前提条件**: Git リポジトリ内で実行、プロジェクト構造を理解
- **情報取得**: 実行時点での動的な状況を総合的に分析
- **推奨事項**: 重要な節目や問題発生時の定期的な進捗確認
- **--update 使用時**: 更新前に Git でコミットしておくことを推奨（変更のロールバック用）

### ベストプラクティス

1. **定期実行**: イテレーション開始・終了時の進捗確認
2. **問題発見**: 遅延や品質低下の早期発見に活用
3. **ステークホルダー報告**: 詳細レポートを会議資料として活用
4. **改善指標**: 進捗データを基にした開発プロセス改善
5. **ドキュメント同期**: イテレーション完了時に `--update` で docs/development を同期
6. **振り返り記録**: `--update` 実行後、KPT セクションを手動で記入して振り返りを完了

### 関連コマンド

- `/docs` : ドキュメント一覧と進捗状況の確認
- `/docs --update` : docs/index.md と mkdocs.yml の更新
- `/git-commit` : 進捗に合わせた適切なコミット実行
- `/plan` : イテレーション計画の作成・更新