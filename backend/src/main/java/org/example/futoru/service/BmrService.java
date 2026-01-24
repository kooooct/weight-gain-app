package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.ActivityLevel;
import org.example.futoru.dto.BmrRequest;
import org.example.futoru.dto.BmrResponse;
import org.example.futoru.dto.Gender;
import org.example.futoru.entity.User;
import org.springframework.stereotype.Service;

/**
 * 基礎代謝(BMR)および増量目標カロリーの計算ロジックを提供するサービスクラス。
 * <p>
 * Mifflin-St Jeor式を用いてBMRを算出し、
 * ActivityLevel（活動レベル）に応じた係数を掛けてTDEE（総エネルギー消費量）を求める。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BmrService {

    // === 定数定義 ===

    /** 増量目的のためにTDEEに上乗せする余剰カロリー (kcal) */
    private static final int SURPLUS_CALORIES_FOR_GAIN = 300;

    // --- Mifflin-St Jeor計算式の係数 ---
    private static final double WEIGHT_MULTIPLIER = 10.0;
    private static final double HEIGHT_MULTIPLIER = 6.25;
    private static final double AGE_MULTIPLIER = 5.0;

    // 性別による補正値
    private static final int MALE_OFFSET = 5;
    private static final int FEMALE_OFFSET = -161;

    /**
     * ユーザー情報と体重を受け取り、目標摂取カロリーを計算する。
     * <p>
     * データの取得（DBアクセス）は呼び出し元で行い、このメソッドには
     * 確定した User エンティティと 現在の体重 を渡すこと。
     * </p>
     *
     * @param user          ユーザーエンティティ
     * @param currentWeight 現在の体重 (kg)
     * @return 1日の目標摂取カロリー (kcal)
     */
    public int calculateTargetCalories(User user, Double currentWeight) {
        if (currentWeight == null) {
            throw new IllegalStateException("体重データが存在しません。ユーザー: " + user.getUsername());
        }
        if (user.getHeight() == null || user.getAge() == null) {
            throw new IllegalStateException("プロフィール情報（身長・年齢）が不足しています。ユーザー: " + user.getUsername());
        }

        // 計算用リクエストDTOの構築
        BmrRequest request = new BmrRequest();
        request.setHeight(user.getHeight());
        request.setWeight(currentWeight);
        request.setAge(user.getAge());
        request.setGender(convertGender(user.getGender()));
        request.setActivityLevel(convertActivityLevel(user.getActivityLevel()));

        BmrResponse response = calculate(request);

        return (int) response.getTargetCalories();
    }

    /**
     * リクエストDTO（身体データ）に基づき、各種指標を計算する。
     * <ul>
     * <li>BMR (基礎代謝)</li>
     * <li>TDEE (総エネルギー消費量)</li>
     * <li>Target Calories (増量用目標カロリー)</li>
     * </ul>
     *
     * @param request 計算用パラメータ (身長, 体重, 年齢, 性別, 活動レベル)
     * @return 計算結果を含むレスポンスDTO
     */
    public BmrResponse calculate(BmrRequest request) {
        double bmr = calculateBmr(request);
        double tdee = calculateTdee(bmr, request.getActivityLevel());
        double targetCalories = tdee + SURPLUS_CALORIES_FOR_GAIN;

        BmrResponse response = new BmrResponse();
        // 表示用に小数点第1位で四捨五入を行う
        response.setBmr(Math.round(bmr * 10.0) / 10.0);
        response.setTdee(Math.round(tdee * 10.0) / 10.0);
        response.setTargetCalories(Math.round(targetCalories * 10.0) / 10.0);

        return response;
    }

    /**
     * Mifflin-St Jeor式を用いて基礎代謝(BMR)を計算する。
     * <br>
     * 計算式: (10 × 体重kg) + (6.25 × 身長cm) - (5 × 年齢) + 性別補正値
     *
     * @param req 身体データ
     * @return 基礎代謝量 (kcal)
     */
    private double calculateBmr(BmrRequest req) {
        double baseResult = (WEIGHT_MULTIPLIER * req.getWeight())
                + (HEIGHT_MULTIPLIER * req.getHeight())
                - (AGE_MULTIPLIER * req.getAge());

        if (req.getGender() == Gender.MALE) {
            return baseResult + MALE_OFFSET;
        } else {
            return baseResult + FEMALE_OFFSET;
        }
    }

    /**
     * 活動レベルに基づいてTDEE(総エネルギー消費量)を算出する。
     * <p>
     * {@link ActivityLevel} Enumに定義された倍率(multiplier)を使用する。
     * </p>
     *
     * @param bmr   基礎代謝量
     * @param level 活動レベル
     * @return TDEE (kcal)
     */
    private double calculateTdee(double bmr, ActivityLevel level) {
        if (level == null) {
            // 未設定の場合は最も低い活動レベルの係数を適用する
            return bmr * ActivityLevel.LOW.getMultiplier();
        }
        return bmr * level.getMultiplier();
    }

    /**
     * DB保存値(String)をGender Enumに変換する。
     * nullまたは不正値の場合はデフォルトでMALEを返す。
     */
    private Gender convertGender(String genderStr) {
        if (genderStr == null) return Gender.MALE;
        if (genderStr.equalsIgnoreCase("MALE") || genderStr.equals("男性")) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }

    /**
     * DB保存値(String)をActivityLevel Enumに変換する。
     * 変換失敗時はデフォルトでLOWを返す。
     */
    private ActivityLevel convertActivityLevel(String levelStr) {
        if (levelStr == null) return ActivityLevel.LOW;
        try {
            return ActivityLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ActivityLevel.LOW;
        }
    }
}