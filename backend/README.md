# Futoru (フトル) - 増量支援アプリ

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=flat-square&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
<br>
![AWS](https://img.shields.io/badge/AWS-EC2-232F3E?style=flat-square&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=flat-square&logo=github-actions&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-Reverse_Proxy-009639?style=flat-square&logo=nginx&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

### サービスURL
**https://futoru-app.duckdns.org**  
AWS EC2 上でDocker Composeを用いてコンテナ運用しています。  
GitHub Actionsによる CI/CD パイプラインを構築しており、mainブランチへのプッシュで自動デプロイされます。  
（Nginx + Let's Encrypt により常時SSL化済み）

※ 現在開発中のため、機能やUIは順次アップデートされます。


## 目次
1. [概要](#概要)
2. [使用技術](#使用技術)
3. [スクリーンショット](#スクリーンショット)
4. [インフラ構成図](#インフラ構成図) 
5. [データベース設計（ER図）](#データベース設計-er図)
6. [環境構築手順（ローカル開発）](#環境構築手順ローカル開発)


## 概要
Futoru は、体重が増えにくい人のための増量特化型食事管理アプリです。

「痩せる」ためのアプリは多く存在しますが、「太る」ことに特化したサービスはまだ少ないのが現状です。  
本アプリでは、ユーザーの身体情報（身長・体重・年齢・活動レベル）をもとに、  
1日に必要な目標カロリーを自動算出し、日々の食事記録と進捗確認を通じて、無理のない増量をサポートします。

## 使用技術

| カテゴリ | 技術・ツール |
| :--- | :--- |
| 言語 | Java 21 (LTS) |
| フレームワーク | Spring Boot 4.0.1, Spring Security |
| フロントエンド | Thymeleaf, HTML5, CSS3, JavaScript |
| データベース | MySQL 8.0 (Docker Container) |
| インフラ | AWS EC2, Docker Compose |
| CI/CD | GitHub Actions (自動ビルド・デプロイ) |
| Webサーバー | Nginx (リバースプロキシ/SSL終端) |
| ツール | IntelliJ IDEA, Maven, Git |


## スクリーンショット

### 日々の記録（メイン機能）
|                                               ホーム                                               |                                              体重記録                                               |
|:-----------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------:|
|         <a href="docs/images/home.png"><img src="docs/images/home.png" width="400"></a>         | <a href="docs/images/weight-input.png"><img src="docs/images/weight-input.png" width="400"></a> |
|                                            **食事記録**                                             |                                            **食事記録後**                                            |
|   <a href="docs/images/meal-input.png"><img src="docs/images/meal-input.png" width="400"></a>   |     <a href="docs/images/meal-log.png"><img src="docs/images/meal-log.png" width="400"></a>     |
|                                            **体重管理**                                             |                                            **レシピ登録**                                            |
|<a href="docs/images/weight-chart.png"><img src="docs/images/weight-chart.png" width="400"></a> |       <a href="docs/images/recipe.png"><img src="docs/images/recipe.png" width="400"></a>       |

### アカウント管理
| ログイン | 新規登録 | 登録入力 |
| :---: | :---: | :---: |
| <a href="docs/images/login.png"><img src="docs/images/login.png" width="250"></a> | <a href="docs/images/register-empty.png"><img src="docs/images/register-empty.png" width="250"></a> | <a href="docs/images/register-input.png"><img src="docs/images/register-input.png" width="250"></a> |

### 工事中
<a href="docs/images/under-construction.gif"><img src="docs/images/under-construction.gif" width="600"></a>

## インフラ構成図
AWS EC2インスタンス内に Docker Compose 環境を構築し、Nginx をリバースプロキシとして配置しています。  
アプリケーションとデータベースは内部ネットワークで接続され、外部からの直接アクセスを遮断しています。

```mermaid
graph TD
    %% スタイル定義（色や形の設定）
    classDef user fill:#FFD700,stroke:#333,stroke-width:2px,color:#333;
    classDef proxy fill:#4682B4,stroke:#333,stroke-width:2px,color:#fff;
    classDef app fill:#3CB371,stroke:#333,stroke-width:2px,color:#fff;
    classDef db fill:#D2691E,stroke:#333,stroke-width:2px,color:#fff;
    classDef plain fill:none,stroke:none,color:#555;

    %% ノード定義
    User((User)):::user
    
    subgraph AWS ["☁️ AWS EC2 Instance"]
        direction LR
        
        Nginx[/"🦁 Nginx<br/>(Reverse Proxy)"/]:::proxy
        
        subgraph DCS ["🐳 Docker Compose"]
            style DCS fill:#f4f4f4,stroke:#666,stroke-dasharray: 5 5,color:#333
            
            Boot["🍃 Spring Boot<br/>(Backend)"]:::app
            DB[("🐬 MySQL 8.0<br/>(Database)")]:::db
        end
    end

    %% 通信フロー
    User -- "HTTPS:443" --> Nginx
    Nginx -- "Proxy Pass<br/>:8080" --> Boot
    Boot -- "Internal Network<br/>:3306" --> DB
```
## データベース設計 (ER図)
コンポジットパターンを採用し、食材・料理・定食を統一的に管理しています。  
また、食事記録にはスナップショット方式を採用し、マスタデータが変更されても過去の記録（カロリー計算）が整合性を保つ設計としています。

```mermaid
erDiagram
    %% ユーザーテーブル
    %% UserDetailsの実装を含む
    USERS {
        bigint id PK
        varchar username "ログインID (Unique)"
        varchar password "PW(Hash)"
        varchar role "権限(USER/ADMIN)"
        int age "年齢"
        varchar gender "性別"
        double height "身長(cm)"
        varchar activity_level "活動レベル"
        int target_calories "目標カロリー(手動設定時)"
    }

    %% 体重記録テーブル
    %% ユーザーの体重推移を管理
    WEIGHT_LOGS {
        bigint id PK
        bigint user_id FK "所有者"
        date date "記録日"
        double weight "体重(kg)"
    }

    %% 食品マスタ
    %% 食材・料理・定食を統一管理
    FOOD_ITEMS {
        bigint id PK
        bigint user_id FK "作成者(Null=標準)"
        varchar name "食品名"
        int calories "基準カロリー"
        varchar unit "単位(個/g等)"
        varchar type "INGREDIENT/DISH/MEAL_SET"
    }

    %% レシピ構成（中間テーブル）
    %% 親子関係と手入力を管理
    RECIPES {
        bigint id PK
        bigint parent_food_id FK "親(完成品)"
        bigint child_food_id FK "子(材料: Null可)"
        double amount "使用量(倍率)"
        varchar manual_name "手入力名"
        int manual_calories "手入力カロリー"
    }

    %% 食事記録
    %% スナップショット保存
    MEAL_LOGS {
        bigint id PK
        bigint user_id FK "食べた人"
        bigint food_item_id FK "参照元(Null可)"
        datetime eaten_at "食べた日時"
        varchar name "記録時の名前"
        int calories "記録時の合計カロリー"
        double amount "食べた量(倍率)"
    }

    %% --- リレーション定義 ---

    %% ユーザーは複数の体重記録を持つ
    USERS ||--o{ WEIGHT_LOGS : "体重を記録"

    %% ユーザーは複数の食事記録を持つ
    USERS ||--o{ MEAL_LOGS : "食事を記録"

    %% ユーザーは独自の食品マスタを作れる
    USERS ||--o{ FOOD_ITEMS : "My食品を作成"

    %% 食品マスタとレシピの関係（コンポジット）
    FOOD_ITEMS ||--o{ RECIPES : "親(Parent)"
    FOOD_ITEMS |o--o{ RECIPES : "子(Child/Ingredient)"

    %% 食品マスタと食事記録の関係
    FOOD_ITEMS |o--o{ MEAL_LOGS : "参照される"
```

## 環境構築手順（ローカル開発）

### 1. 前提条件
- Java 21（JDK）
- Docker Desktop (または Docker Engine)
- Git

### 2. リポジトリのクローン
```bash
git clone https://github.com/kooooct/futoru.git
cd futoru
```

### 3. データベース起動 (Docker)
開発用のデータベースをDockerで立ち上げます。
```bash
docker compose up -d db
```
※ 初回起動時にテーブルが自動生成されます。

### 4. アプリケーション起動
Spring Bootアプリケーションを起動します。
```bash
./mvnw spring-boot:run
```
起動後、`http://localhost:8080` にアクセスしてください。
