package org.example.futoru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * アプリケーションのセキュリティ設定クラス。
 * Spring Securityを利用した認証・認可ルール、およびパスワードエンコーダーの定義を行う。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTPリクエストに対するセキュリティフィルターチェーンを定義する。
     * アクセス許可ルール、フォームログイン、ログアウトの挙動を設定。
     *
     * @param http HttpSecurity設定ビルダー
     * @return 構築されたSecurityFilterChain
     * @throws Exception 設定時の例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 認可設定 (Authorization)
                .authorizeHttpRequests(auth -> auth
                        // ログイン、登録、静的リソースは認証なしでアクセス許可
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        // その他のリクエストは全て認証が必要
                        .anyRequest().authenticated()
                )
                // フォームログイン設定
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true) // ログイン成功時はダッシュボードへリダイレクト
                        .permitAll()
                )
                // ログアウト設定
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // ログアウト後はログイン画面へ
                        .permitAll()
                );

        return http.build();
    }

    /**
     * パスワードハッシュ化に使用するエンコーダーBeanを提供する。
     * セキュリティ強度の高いBCryptアルゴリズムを使用。
     *
     * @return PasswordEncoder実装 (BCryptPasswordEncoder)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}