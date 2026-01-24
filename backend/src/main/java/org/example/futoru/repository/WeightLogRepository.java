package org.example.futoru.repository;

import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 体重記録（WeightLog）エンティティへのデータベース操作を行うリポジトリ。
 * <p>
 * Spring Data JPAにより、基本的なCRUD操作は自動実装される。
 * グラフ表示や重複チェックに必要なクエリメソッドを定義する。
 * </p>
 */
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

    /** 指定されたユーザーの体重記録を日付の昇順で取得する（グラフ表示用）。 */
    List<WeightLog> findByUserOrderByDateAsc(User user);

    /** 指定されたユーザーと日付に一致する体重記録を取得する（重複チェックや更新用）。 */
    Optional<WeightLog> findByUserAndDate(User user, LocalDate date);

    /** 最新の体重を取得（日付の新しい順に並べて、最初の1件を取る）。 */
    Optional<WeightLog> findFirstByUserOrderByDateDesc(User user);
}