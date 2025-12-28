# Futoru (フトル) - Healthy Weight Gain App

健康的に「太りたい」人のための、食事管理＆増量サポートアプリ。  
「痩せる」アプリはたくさんあるけれど、「太る」ためのアプリは少ない。そんな悩みを解決するために開発しました。

## 主な機能
- 身長・体重・活動レベルから、増量に必要な目標カロリーを自動計算
- 食事を簡単に記録（食品検索・自由入力の両方に対応）
- 今日の摂取カロリーと残り目標をダッシュボードで確認

## 技術スタック
- **言語:** Java 21
- **フレームワーク:** Spring Boot 4.0.1, Spring Security
- **フロントエンド:** Thymeleaf, HTML/CSS, JavaScript
- **DB:**
    - Local: MySQL (Docker)
    - Production: MariaDB
- **インフラ:** AWS EC2 (Amazon Linux 2023), Nginx (Reverse Proxy), Let's Encrypt (SSL)
- **ツール:** Maven, IntelliJ IDEA, Git/GitHub