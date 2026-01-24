package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.example.futoru.repository.WeightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 体重記録（WeightLog）に関するビジネスロジックを提供するサービスクラス。
 * <p>
 * 日々の体重の記録・更新処理や、
 * ダッシュボードのグラフ表示用にデータを整形して返却する機能を持つ。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final UserService userService;
    private final BmrService bmrService;

    /**
     * 指定された日付の体重を記録する。
     * <p>
     * 既に同日の記録が存在する場合は上書き更新し、存在しない場合は新規作成する。
     * 体重記録の更新に伴い、ユーザーの目標カロリー（BMR）も再計算して更新する。
     * </p>
     *
     * @param username 記録するユーザーの名前
     * @param date     記録日
     * @param weight   体重 (kg)
     */
    @Transactional
    public void saveWeightLog(String username, LocalDate date, Double weight) {
        User user = userService.getUserByUsername(username);

        WeightLog log = weightLogRepository.findByUserAndDate(user, date)
                .orElse(new WeightLog());

        log.setUser(user);
        log.setDate(date);
        log.setWeight(weight);

        weightLogRepository.save(log);

        // 体重変化に伴う目標カロリーの再計算
        int newTargetCalories = bmrService.calculateTargetCalories(user, weight);

        userService.updateTargetCalories(user, newTargetCalories);
    }

    /**
     * 指定された日付の体重を取得する。
     * 記録がない場合は null を返す。
     */
    @Transactional(readOnly = true)
    public Double getWeightByDate(String username, LocalDate date) {
        User user = userService.getUserByUsername(username);

        return weightLogRepository.findByUserAndDate(user, date)
                .map(WeightLog::getWeight)
                .orElse(null);
    }

    /**
     * グラフ表示用の日付ラベルリストを取得する。
     *
     * @param username 対象ユーザー名
     * @return "M/d" 形式の日付文字列リスト
     */
    public List<String> getGraphLabels(String username) {
        User user = userService.getUserByUsername(username);
        List<WeightLog> logs = weightLogRepository.findByUserOrderByDateAsc(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        return logs.stream()
                .map(log -> log.getDate().format(formatter))
                .collect(Collectors.toList());
    }

    /**
     * グラフ表示用の体重データリストを取得する。
     *
     * @param username 対象ユーザー名
     * @return 体重数値のリスト
     */
    public List<Double> getGraphValues(String username) {
        User user = userService.getUserByUsername(username);

        List<WeightLog> logs = weightLogRepository.findByUserOrderByDateAsc(user);

        return logs.stream()
                .map(WeightLog::getWeight)
                .collect(Collectors.toList());
    }
}