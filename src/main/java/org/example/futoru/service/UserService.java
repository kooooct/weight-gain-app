package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.ActivityLevel;
import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.example.futoru.repository.UserRepository;
import org.example.futoru.repository.WeightLogRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * ユーザーのアカウント管理およびプロフィール操作を行うサービスクラス。
 * <p>
 * アカウントの登録、更新、検索に加え、Spring Securityの {@link UserDetailsService} を実装し、
 * 認証プロセスにおけるユーザー情報の取得も担当する。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final WeightLogRepository weightLogRepository;
    private final BmrService bmrService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Securityの認証プロセスで使用されるメソッド。
     * <p>
     * 指定されたユーザー名に基づいてデータベースからユーザー情報を取得する。
     * </p>
     *
     * @param username 認証対象のユーザー名
     * @return 認証用ユーザー詳細情報（{@link UserDetails}）
     * @throws UsernameNotFoundException 指定されたユーザー名が存在しない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * 新規ユーザーのアカウント登録を行う。
     * <p>
     * パスワードは {@link PasswordEncoder} を使用してハッシュ化された状態で保存される。
     * 身長などの身体情報は、初期状態では未設定（null）となる。
     * </p>
     *
     * @param username 登録するユーザー名（一意である必要がある）
     * @param password 登録するパスワード（平文）
     * @throws IllegalArgumentException 指定されたユーザー名が既に使用されている場合
     */
    @Transactional
    public void registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        userRepository.save(user);
    }

    /**
     * ユーザーの初期設定（プロフィール入力）が完了しているか判定する。
     * <p>
     * 判定基準:
     * <ul>
     * <li>身長が設定されていること</li>
     * <li>体重ログが少なくとも1件存在すること</li>
     * </ul>
     * </p>
     *
     * @param username 確認対象のユーザー名
     * @return プロフィール設定が完了している場合は true、未完了の場合は false
     * @throws java.util.NoSuchElementException ユーザーが存在しない場合
     */
    @Transactional(readOnly = true)
    public boolean isProfileCompleted(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        boolean hasWeightLog = weightLogRepository.findFirstByUserOrderByDateDesc(user).isPresent();

        return user.getHeight() != null && hasWeightLog;
    }

    /**
     * 初回プロフィール設定時の処理を一括で行う。
     * <p>
     * ユーザー情報の更新、初期体重ログの記録、および基礎代謝に基づく
     * 目標摂取カロリーの算出と保存をトランザクション内で実行する。
     * </p>
     *
     * @param username      更新対象のユーザー名
     * @param height        身長 (cm)
     * @param weight        体重 (kg) - 初回の体重ログとして記録される
     * @param age           年齢
     * @param gender        性別 ("MALE" または "FEMALE")
     * @param activityLevel 活動レベル
     * @throws RuntimeException 指定されたユーザーが見つからない場合
     */
    @Transactional
    public void saveInitialProfile(String username, Double height, Double weight, Integer age, String gender, ActivityLevel activityLevel) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setHeight(height);
        user.setAge(age);
        user.setGender(gender);
        user.setActivityLevel(activityLevel.name());

        userRepository.save(user);

        LocalDate today = LocalDate.now();
        WeightLog log = new WeightLog();
        log.setUser(user);
        log.setDate(today);
        log.setWeight(weight);
        weightLogRepository.save(log);

        int targetCalories = bmrService.calculateTargetCalories(user, weight);
        user.setTargetCalories(targetCalories);

        userRepository.save(user);
    }

    /**
     * 既存ユーザーのプロフィール情報を更新する。
     * <p>
     * 更新された身体情報を元に、目標摂取カロリーの再計算と保存も自動的に行われる。
     * 体重の更新は含まれない（WeightLogServiceで管理するため）。
     * </p>
     *
     * @param username      更新対象のユーザー名
     * @param height        身長 (cm)
     * @param age           年齢
     * @param gender        性別 ("MALE" または "FEMALE")
     * @param activityLevel 活動レベル
     * @throws RuntimeException 指定されたユーザーが見つからない場合
     */
    @Transactional
    public void updateProfile(String username, Double height, Integer age, String gender, ActivityLevel activityLevel) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setHeight(height);
        user.setAge(age);
        user.setGender(gender);
        user.setActivityLevel(activityLevel.name());

        userRepository.save(user);

        Double currentWeight = weightLogRepository.findFirstByUserOrderByDateDesc(user)
                .map(WeightLog::getWeight)
                .orElse(null);

        int targetCalories = bmrService.calculateTargetCalories(user, currentWeight);
        user.setTargetCalories(targetCalories);

        userRepository.save(user);
    }

    /**
     * ユーザーの目標摂取カロリーを更新する。
     * <p>
     * 体重記録の更新時など、外部サービスから目標値の更新が必要な場合に呼び出される。
     * </p>
     *
     * @param user              更新対象のユーザーエンティティ
     * @param newTargetCalories 新しい目標カロリー値
     */
    @Transactional
    public void updateTargetCalories(User user, int newTargetCalories) {
        user.setTargetCalories(newTargetCalories);
        userRepository.save(user);
    }

    /**
     * ユーザー名からユーザーエンティティを取得する便利メソッド。
     * <p>
     * 他のサービス（WeightLogService等）でユーザーIDが必要な場合に使用する。
     * 存在しない場合は例外をスローする。
     * </p>
     *
     * @param username 取得したいユーザー名
     * @return 該当するユーザーエンティティ
     * @throws RuntimeException ユーザーが見つからない場合
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}