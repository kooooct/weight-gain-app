package org.example.futoru.repository;

import org.example.futoru.entity.MealLog;
import org.example.futoru.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 食事記録データ(MealLog)へのデータベースアクセスを行うリポジトリ。
 */
public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    /**
     * 指定されたユーザーの、指定期間内における食事記録を検索する。
     * ダッシュボードでの「今日の食事」表示などで使用される。
     *
     * @param user  検索対象のユーザー
     * @param start 検索開始日時 (例: 2025-01-01 00:00:00)
     * @param end   検索終了日時 (例: 2025-01-01 23:59:59)
     * @return 指定期間内に記録された食事ログのリスト
     */
    List<MealLog> findByUserAndEatenAtBetween(User user, LocalDateTime start, LocalDateTime end);
}