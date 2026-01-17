package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.example.futoru.repository.UserRepository;
import org.example.futoru.repository.WeightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogRepository weightLogRepository;
    private final UserRepository userRepository;
    private final BmrService bmrService;

    /**
     * 体重を記録する。
     * すでに同日の記録がある場合は上書きする。
     * 同時にユーザープロフィールの現在体重も更新する。
     */
    @Transactional
    public void saveWeightLog(String username, LocalDate date, Double weight) {
        User user = userRepository.findByUsername(username).orElseThrow();

        WeightLog log = weightLogRepository.findByUserAndDate(user, date)
                .orElse(new WeightLog());

        log.setUser(user);
        log.setDate(date);
        log.setWeight(weight);

        weightLogRepository.save(log);

        int newTargetCalories = bmrService.calculateTargetCalories(username);
        user.setTargetCalories(newTargetCalories);

        userRepository.save(user);
    }

    /**
     * グラフ表示用の日付リスト ("1/15", "1/16"...) を取得
     */
    public List<String> getGraphLabels(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<WeightLog> logs = weightLogRepository.findByUserOrderByDateAsc(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        return logs.stream()
                .map(log -> log.getDate().format(formatter))
                .collect(Collectors.toList());
    }

    /**
     * グラフ表示用の体重リスト (60.5, 60.2...) を取得
     */
    public List<Double> getGraphValues(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<WeightLog> logs = weightLogRepository.findByUserOrderByDateAsc(user);

        return logs.stream()
                .map(WeightLog::getWeight)
                .collect(Collectors.toList());
    }
}