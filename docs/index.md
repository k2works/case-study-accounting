# まずこれを読もうリスト

プロジェクトのドキュメント一覧です。開発を始める前に、以下のドキュメントを順番に確認してください。

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

### その他

- [UI 設計](./design/ui-design.md) - ユーザーインターフェース設計
- [テスト戦略](./design/test_strategy.md) - テスト計画と戦略
- [非機能要件](./design/non_functional.md) - 性能・セキュリティ要件
- [運用要件](./design/operation.md) - 運用・保守要件
- [技術スタック選定](./design/tech_stack.md) - 技術選定と理由

## 開発

- [リリース計画](./development/release_plan.md) - リリース計画とスケジュール
- [イテレーション計画 1](./development/iteration_plan-1.md) - イテレーション1の詳細計画

## 運用

- [バックエンド構築手順書](./operation/backend_setup.md) - バックエンド環境のセットアップ手順
- [SonarQube セットアップガイド](./operation/sonarqube_setup.md) - コード品質分析環境の構築

## リファレンス

- [開発ガイド](./reference/開発ガイド.md) - 開発の進め方
- [よいソフトウェアとは](./reference/よいソフトウェアとは.md) - 品質の考え方
- [コーディングとテストガイド](./reference/コーディングとテストガイド.md) - 実装ガイドライン

## テンプレート

- [ADR テンプレート](./template/ADR.md) - アーキテクチャ決定記録
- [イテレーション完了報告書](./template/イテレーション完了報告書.md) - 振り返りテンプレート

## 実践ガイド

財務会計システム開発の実践ガイドです。

- [実践ガイド 概要](./article/index.md) - シリーズ全体の概要
- [バックエンド編（Java版）](./article/backend/chapter00.md) - Java + Spring Boot による開発（全32章）
- [フロントエンド編（React版）](./article/frontend/chapter00.md) - React + TypeScript による開発（全24章）
