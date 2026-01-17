package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * ユーザー認証情報および身体データを管理するエンティティクラス。
 * <p>
 * Spring SecurityのUserDetailsインターフェースを実装しており、
 * 認証（ログイン）および認可（権限チェック）の主役となるクラス。
 * BMR計算に必要な身体データもここで保持する。
 * </p>
 */
@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ログインID（一意制約あり） */
    @Column(unique = true, nullable = false)
    private String username;

    /** ハッシュ化されたパスワード */
    @Column(nullable = false)
    private String password;

    /** 権限ロール (例: "USER", "ADMIN") */
    private String role;

    /** 年齢 */
    private Integer age;

    /** 性別 ("MALE" または "FEMALE") */
    private String gender;

    /** 身長 (cm) */
    private Double height;

    /** 活動レベル ("LOW", "MID", "HIGH") */
    private String activityLevel;

    /**
     * 目標カロリー (kcal)。
     * 手動で目標値を設定した場合に値が入る。NULLの場合は自動計算値を使用する想定。
     */
    private Integer targetCalories;

    /**
     * ユーザーに付与された権限リストを返却する。
     *
     * @return 権限オブジェクトのリスト。roleフィールドの値をそのまま権限として扱う。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    /**
     * アカウントの有効期限が切れていないかを判定する。
     * 現状は管理しないため、常に true (有効) を返す。
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * アカウントがロックされていないかを判定する。
     * 現状は管理しないため、常に true (ロックされていない) を返す。
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * パスワードの有効期限が切れていないかを判定する。
     * 現状は管理しないため、常に true (有効) を返す。
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * アカウントが有効(Enabled)かどうかを判定する。
     * メール認証等を導入する場合はここを制御するが、現状は常に true を返す。
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}