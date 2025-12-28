# Futoru (フトル) - 健康的増量支援アプリ

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat-square&logo=docker&logoColor=white)

### デモサイト（稼働中）
**https://futoru-app.duckdns.org**  
AWS EC2 + Nginx + Docker 構成で常時SSL化しています。  
※ 現在開発中のため、機能やUIは順次アップデートされます。

---

## 目次
1. [概要](#概要)
2. [主な機能](#主な機能)
3. [使用技術](#使用技術)
4. [ディレクトリ構成](#ディレクトリ構成)
5. [環境構築手順（ローカル開発）](#環境構築手順ローカル開発)

---

## 概要
Futoru は、**体重が増えにくい人でも健康的に増量を管理できる**食事管理アプリです。

「痩せる」ためのアプリは多く存在しますが、「太る」ことに特化したサービスはまだ少ないのが現状です。  
本アプリでは、ユーザーの身体情報（身長・体重・年齢・活動レベル）をもとに、  
**1日に必要な目標カロリーを自動算出**し、日々の食事記録と進捗確認を通じて、無理のない増量をサポートします。

---

## 主な機能

### 目標カロリーの自動計算
- 身体情報（身長・体重・年齢・性別）から、Mifflin-St Jeor式を用いて基礎代謝を算出します。
- 活動レベルに応じた消費カロリーに**増量分（+300kcal）**を加算し、適切な目標値を自動設定します。

### 柔軟な食事記録
- 食品マスタからの選択に加え、マスタにない食品（コンビニ商品など）の手動入力にも対応しています。
- 外食や変則的な食事も、カロリーと食品名を直接入力して記録可能です。

### 正確な履歴保存（スナップショット）
- 食事記録作成時に、その時点の食品名とカロリーをコピーして保存します。
- 後にマスタデータが変更・削除されても、**過去の記録は変わらず維持されます**。

### 進捗ダッシュボード
- 目標カロリーに対する現在の摂取量と、「あと何kcal食べる必要があるか」を表示します。
- 増量に必要な余剰カロリーの摂取状況をひと目で把握できます。

---

## 使用技術

| カテゴリ | 技術・ツール |
| :--- | :--- |
| 言語 | Java 21 (LTS) |
| フレームワーク | Spring Boot 4.0.1, Spring Security |
| フロントエンド | Thymeleaf, HTML5, CSS3, JavaScript |
| データベース | MySQL 8.0（開発環境 / Docker）, MariaDB（本番環境） |
| インフラ | AWS EC2（Amazon Linux 2023）, Nginx, Let's Encrypt |
| ツール | IntelliJ IDEA, Maven, Git / GitHub |

---

## ディレクトリ構成

```text
src/main/java/org/example/futoru
├── config        # 設定クラス（SecurityConfigなど）
├── controller    # Web層（画面遷移、リクエスト受付）
├── dto           # データ転送用オブジェクト
├── entity        # ドメインモデル・DB定義
├── repository    # データアクセス層（Spring Data JPA）
└── service       # ビジネスロジック
```

---

## 環境構築手順（ローカル開発）

### 1. 前提条件
- Java 21（JDK）
- Docker Desktop
- Git

### 2. リポジトリのクローン
```bash
git clone https://github.com/kooooct/futoru.git
cd futoru
```

### 3. データベース起動
```bash
docker-compose up -d
```
※ 初回起動時にテーブルが自動生成されます。

### 4. アプリケーション起動
```bash
./mvnw spring-boot:run
```
起動後、`http://localhost:8080` にアクセスしてください。