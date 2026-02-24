# プロジェクトポータル

- [フロントエンド](http://localhost:3001){:target="_blank"} - React アプリケーション
- [バックエンド API](http://localhost:8081){:target="_blank"} - Spring Boot API サーバー
- [Swagger UI](http://localhost:8081/swagger-ui.html){:target="_blank"} - API ドキュメント
- [Adminer](http://localhost:8888){:target="_blank"} - データベース管理ツール
- [SonarQube](http://localhost:9000){:target="_blank"} - コード品質分析ツール
- [SchemaSpy ER 図](./assets/schemaspy-output/index.html){:target="_blank"} - データベース ER 図

## 要件定義

- [要件定義書](./requirements/requirements_definition.md) - RDRA 2.0 に基づくシステム要件定義
- [ビジネスユースケース](./requirements/business_usecase.md) - ビジネスレベルのユースケース
- [システムユースケース](./requirements/system_usecase.md) - システムレベルのユースケース
- [ユーザーストーリー](./requirements/user_story.md) - ユーザー視点での要件

## 設計ドキュメント

### アーキテクチャ

- [バックエンドアーキテクチャ](./design/architecture_backend.md) - サーバーサイドの設計
- [フロントエンドアーキテクチャ](./design/architecture_frontend.md) - クライアントサイドの設計
- [インフラストラクチャ](./design/architecture_infrastructure.md) - インフラ構成

### モデル設計

- [データモデル](./design/data-model.md) - データベース設計
- [ドメインモデル](./design/domain-model.md) - ドメイン駆動設計

### ユースケース設計

- [仕訳一覧取得ユースケース設計](./design/usecase/JNL-004-01_GetJournalEntries_UseCase_Design.md) - 仕訳一覧取得機能の詳細設計

### ワークフロー設計

- [仕訳ステータス遷移図](./design/journal-entry-status-diagram.md) - 仕訳のステータス遷移と操作権限

### その他

- [UI 設計](./design/ui-design.md) - ユーザーインターフェース設計
- [テスト戦略](./design/test_strategy.md) - テスト計画と戦略
- [非機能要件](./design/non_functional.md) - 性能・セキュリティ要件
- [運用要件](./design/operation.md) - 運用・保守要件
- [技術スタック選定](./design/tech_stack.md) - 技術選定と理由

## 開発

### 計画

- [リリース計画](./development/release_plan.md) - リリース計画とスケジュール

### リリースノート

- [リリースノート v1.0 MVP](./development/release_notes-v1.0.md) - リリース 1.0 MVP の変更内容
- [リリースノート v2.0 機能拡張版](./development/release_notes-v2.0.md) - リリース 2.0 機能拡張版の変更内容
- [リリースノート v3.0 完成版](./development/release_notes-v3.0.md) - リリース 3.0 完成版の変更内容

### リリース報告書

- [Release 3.0 リリース報告書](./development/release_3_report.md) - プロジェクト完了報告（計画 vs 実績分析、品質メトリクス、工期短縮分析）

### イテレーション

- [イテレーション計画 1](./development/iteration_plan-1.md) - イテレーション1の詳細計画
- [イテレーション計画 2](./development/iteration_plan-2.md) - イテレーション2の詳細計画
- [イテレーション計画 3](./development/iteration_plan-3.md) - イテレーション3の詳細計画
- [イテレーション計画 4](./development/iteration_plan-4.md) - イテレーション4の詳細計画
- [イテレーション計画 5](./development/iteration_plan-5.md) - イテレーション5の詳細計画
- [イテレーション計画 6](./development/iteration_plan-6.md) - イテレーション6の詳細計画
- [イテレーション計画 7](./development/iteration_plan-7.md) - イテレーション7の詳細計画
- [イテレーション計画 8](./development/iteration_plan-8.md) - イテレーション8の詳細計画
- [イテレーション計画 9](./development/iteration_plan-9.md) - イテレーション9の詳細計画
- [イテレーション計画 10](./development/iteration_plan-10.md) - イテレーション10の詳細計画
- [イテレーション計画 11](./development/iteration_plan-11.md) - イテレーション11の詳細計画
- [イテレーション計画 12](./development/iteration_plan-12.md) - イテレーション12の詳細計画（統合テスト・リリース準備）

### ふりかえり

- [イテレーション 1 ふりかえり](./development/retrospective-1.md) - イテレーション1の KPT 分析
- [イテレーション 2 ふりかえり](./development/retrospective-2.md) - イテレーション2の KPT 分析
- [イテレーション 3 ふりかえり](./development/retrospective-3.md) - イテレーション3の KPT 分析
- [イテレーション 4 ふりかえり](./development/retrospective-4.md) - イテレーション4の KPT 分析
- [イテレーション 5 ふりかえり](./development/retrospective-5.md) - イテレーション5の KPT 分析
- [イテレーション 6 ふりかえり](./development/retrospective-6.md) - イテレーション6の KPT 分析
- [イテレーション 7 ふりかえり](./development/retrospective-7.md) - イテレーション7の KPT 分析
- [イテレーション 8 ふりかえり](./development/retrospective-8.md) - イテレーション8の KPT 分析・リリース 2.0 総括
- [イテレーション 9 ふりかえり](./development/retrospective-9.md) - イテレーション9の KPT 分析・マスタ拡張完了
- [イテレーション 10 ふりかえり](./development/retrospective-10.md) - イテレーション10の KPT 分析・自動仕訳生成/財務分析完了
- [イテレーション 11 ふりかえり](./development/retrospective-11.md) - イテレーション11の KPT 分析・監査ログ/データダウンロード完了・全機能実装完了
- [イテレーション 12 ふりかえり](./development/retrospective-12.md) - イテレーション12の KPT 分析・品質保証完了・プロジェクト完了

## 運用

### 環境構築

- [開発環境解説](./operation/dev_env.md) - 開発環境の全体像
- [開発コンテナ構築手順書](./operation/dev_container.md) - Docker コンテナ環境のセットアップ手順

### バックエンド

- [バックエンド構築手順書](./operation/backend_setup.md) - バックエンド環境のセットアップ手順
- [バックエンドデモ環境](./operation/backend_demo_env.md) - H2 インメモリデータベースを使用したデモ環境

### フロントエンド

- [フロントエンド構築手順書](./operation/frontend_setup.md) - フロントエンド環境のセットアップ手順
- [フロントエンド開発手順](./operation/frontend_dev.md) - フロントエンド開発の日常的な手順
- [フロントエンド共通 UI](./operation/frontend_common_ui.md) - 共通レイアウト・UI コンポーネント
- [フロントエンドデモ環境](./operation/frontend_demo_env.md) - nginx を使用したデモ環境

### デプロイ・ツール

- [デモ環境デプロイ](./operation/deploy_demo.md) - Heroku へのデプロイ手順
- [外部サービス連携](./operation/third_party_service.md) - 外部サービスとの連携設定
- [SonarQube セットアップガイド](./operation/sonarqube_setup.md) - コード品質分析環境の構築

### テスト・プロジェクト管理

- [E2E テスト実行手順書](./operation/e2e_test.md) - Cypress と MSW を使った E2E テストの実行方法
- [GitHub Project 運用ガイド](./operation/github_project.md) - GitHub Projects V2 によるプロジェクト管理

## リファレンス

### 開発ガイドライン

- [開発ガイド](./reference/開発ガイド.md) - 開発の進め方
- [よいソフトウェアとは](./reference/よいソフトウェアとは.md) - 品質の考え方
- [コーディングとテストガイド](./reference/コーディングとテストガイド.md) - 実装ガイドライン
- [エクストリームプログラミング](./reference/エクストリームプログラミング.md) - XP プラクティス

### 分析・設計ガイド

- [要件定義ガイド](./reference/要件定義ガイド.md) - 要件定義の進め方
- [ユースケース作成ガイド](./reference/ユースケース作成ガイド.md) - ユースケース記述方法
- [ビジネスアーキテクチャ分析ガイド](./reference/ビジネスアーキテクチャ分析ガイド.md) - ビジネスアーキテクチャの分析手法
- [ビジネスアーキテクチャ設計ガイド](./reference/ビジネスアーキテクチャ設計ガイド.md) - ビジネスアーキテクチャの設計指針
- [アーキテクチャ設計ガイド](./reference/アーキテクチャ設計ガイド.md) - アーキテクチャ設計の進め方
- [データモデル設計ガイド](./reference/データモデル設計ガイド.md) - データモデリング手法
- [ドメインモデル設計ガイド](./reference/ドメインモデル設計ガイド.md) - DDD の実践方法
- [UI設計ガイド](./reference/UI設計ガイド.md) - UI 設計の進め方
- [インフラ設計ガイド](./reference/インフラ設計ガイド.md) - インフラ構成の考え方

### 非機能・運用ガイド

- [テスト戦略ガイド](./reference/テスト戦略ガイド.md) - テスト計画の立て方
- [非機能要件定義ガイド](./reference/非機能要件定義ガイド.md) - 非機能要件の定義方法
- [運用要件定義ガイド](./reference/運用要件定義ガイド.md) - 運用要件の定義方法
- [リリース・イテレーション計画ガイド](./reference/リリース・イテレーション計画ガイド.md) - アジャイル計画の立て方
- [リリースガイド](./reference/リリースガイド.md) - リリースワークフロー

### 環境構築ガイド

- [Javaアプリケーション環境構築ガイド](./reference/Javaアプリケーション環境構築ガイド.md) - Java 開発環境の構築
- [TypeScriptアプリケーション環境構築ガイド](./reference/TypeScriptアプリケーション環境構築ガイド.md) - TypeScript 開発環境の構築
- [言語別開発ガイド](./reference/言語別開発ガイド.md) - Nix による言語別開発環境
- [環境変数管理ガイド](./reference/環境変数管理ガイド.md) - 環境変数の管理方法
- [Vim操作マニュアル](./reference/Vim操作マニュアル.md) - プロジェクトの Vim 操作ガイド
- [Codex CLI MCP サーバー設定手順](./reference/CodexCLIMCPサーバー設定手順.md) - AI アシスタント連携設定
- [Codex CLI MCP アプリケーション開発フロー](./reference/CodexCLIMCPアプリケーション開発フロー.md) - AI アシスタントを活用した開発フロー

## テンプレート

- [ADR テンプレート](./template/ADR.md) - アーキテクチャ決定記録
- [ビジネスアーキテクチャ](./template/ビジネスアーキテクチャ.md) - ビジネスアーキテクチャ分析テンプレート
- [イテレーション完了報告書](./template/イテレーション完了報告書.md) - 振り返りテンプレート
- [イテレーション計画](./template/イテレーション計画.md) - イテレーション計画テンプレート
- [インセプションデッキ](./template/インセプションデッキ.md) - プロジェクト開始時のテンプレート
- [まずこれを読もうリスト](./template/まずこれを読もうリスト.md) - オンボーディング資料
- [リリース計画](./template/リリース計画.md) - リリース計画テンプレート
- [完全形式のユースケース](./template/完全形式のユースケース.md) - ユースケース記述テンプレート
- [要件定義](./template/要件定義.md) - 要件定義テンプレート
- [設計](./template/設計.md) - 設計ドキュメントテンプレート
- [README](./template/README.md) - プロジェクト README テンプレート

## 実践ガイド

財務会計システム開発の実践ガイドです。

- [実践ガイド 概要](./article/index.md) - シリーズ全体の概要
- [バックエンド編（Java版）](./article/backend/chapter00.md) - Java + Spring Boot による開発（全32章）
- [フロントエンド編（React版）](./article/frontend/chapter00.md) - React + TypeScript による開発（全24章）

### 関数型プログラミング

- [関数型プログラミング（Java版）](./article/functional-java/index.md) - Java での関数型プログラミング実践
