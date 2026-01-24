package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.entity.FoodItem;
import org.example.futoru.entity.MealLog;
import org.example.futoru.entity.User;
import org.example.futoru.repository.FoodItemRepository;
import org.example.futoru.repository.MealLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 食事記録および食品マスタに関するビジネスロジックを提供するサービスクラス。
 * <p>
 * 食品データの検索、食事の記録（登録）、削除機能を提供する。
 * 特に食事記録時は、マスタデータの値をコピーして保存する「スナップショット」方式を採用し、
 * 将来マスタが変更・削除されても過去の記録が整合性を保てるように設計されている。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FoodService {

    private final FoodItemRepository foodItemRepository;
    private final MealLogRepository mealLogRepository;
    private final UserService userService;

    /**
     * 指定されたユーザーが選択可能な食品リストを取得する。
     * <p>
     * 以下の2種類の食品を統合して返却する：
     * 1. システム標準食品 (全ユーザー共通, user_idがNULL)
     * 2. ユーザー自身のMy食品 (user_idが現在のユーザー)
     * </p>
     *
     * @param username 現在のユーザー名
     * @return 利用可能な食品のリスト
     */
    public List<FoodItem> getAvailableFoods(String username) {
        User user = userService.getUserByUsername(username);
        return foodItemRepository.findAllAvailable(user);
    }

    /**
     * ユーザーの「今日」の食事記録一覧を取得する。
     * サーバーの日時を基準に、当日の 00:00:00 から 23:59:59 までのデータを検索する。
     *
     * @param username 現在のユーザー名
     * @return 今日のMealLogリスト
     */
    @Transactional(readOnly = true)
    public List<MealLog> getTodayMealLogs(String username) {
        User user = userService.getUserByUsername(username);

        // 当日の範囲を設定 (例: 2025-01-01 00:00:00 ～ 23:59:59)
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        return mealLogRepository.findByUserAndEatenAtBetween(user, start, end);
    }

    /**
     * 食品マスタ（FoodItem）を選択して食事を記録する。
     * <p>
     * 選択された食品マスタの情報を元に、摂取カロリーを計算して保存する。
     * 重要な点として、食品名やカロリーはマスタへの参照だけでなく、
     * ログ自体にも値をコピー（スナップショット保存）する。
     * </p>
     *
     * @param username   現在のユーザー名
     * @param foodItemId 選択された食品マスタID
     * @param amount     摂取量（マスタの単位に対する倍率。例: 1.5倍）
     * @throws IllegalArgumentException 指定されたIDの食品が存在しない場合
     */
    public void recordMealFromMaster(String username, Long foodItemId, Double amount) {
        User user = userService.getUserByUsername(username);
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid food item ID"));

        MealLog log = new MealLog();
        log.setUser(user);
        log.setFoodItem(foodItem); // マスタとのリンクも一応残す

        // スナップショット保存: マスタの内容が変わっても履歴が変わらないように値をコピー
        log.setName(foodItem.getName());

        // カロリー計算: 基準値 * 量
        int totalCalories = (int) (foodItem.getCalories() * amount);
        log.setCalories(totalCalories);

        log.setAmount(amount);
        log.setEatenAt(LocalDateTime.now());

        mealLogRepository.save(log);
    }

    /**
     * 食品マスタを使用せず、手入力（アドホック）で食事を記録する。
     * コンビニ商品や、マスタに登録するまでもない食事の記録に使用する。
     *
     * @param username 現在のユーザー名
     * @param name     食品名
     * @param calories 合計カロリー (kcal)
     */
    public void recordManualMeal(String username, String name, int calories) {
        User user = userService.getUserByUsername(username);

        MealLog log = new MealLog();
        log.setUser(user);
        log.setFoodItem(null); // マスタ参照なし
        log.setName(name);
        log.setCalories(calories);
        log.setAmount(1.0); // 手入力の場合は倍率概念がないため便宜上1.0とする
        log.setEatenAt(LocalDateTime.now());

        mealLogRepository.save(log);
    }

    /**
     * 指定された食事記録を削除する。
     * セキュリティ対策として、削除リクエストを出したユーザーが
     * その記録の所有者であるかをチェックする。
     *
     * @param logId    削除対象のログID
     * @param username リクエストしたユーザー名
     * @throws SecurityException 所有者でないユーザーが削除を試みた場合
     * @throws IllegalArgumentException 指定されたIDのログが存在しない場合
     */
    public void deleteMealLog(Long logId, String username) {
        MealLog log = mealLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        if (!log.getUser().getUsername().equals(username)) {
            throw new SecurityException("You cannot delete this log");
        }
        mealLogRepository.delete(log);
    }
}