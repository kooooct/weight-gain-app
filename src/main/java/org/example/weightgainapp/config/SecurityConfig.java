package org.example.weightgainapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. URLごとのアクセス許可設定
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll() // ログイン画面や登録画面は誰でもOK
                        .anyRequest().authenticated() // それ以外はログイン必須
                )
                // 2. ログイン設定
                .formLogin(login -> login
                         .loginPage("/login")
                        .defaultSuccessUrl("/", true) // ログイン成功したらトップページへ
                        .permitAll()
                )
                // 3. ログアウト設定
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // ログアウトしたらログイン画面へ
                        .permitAll()
                );

        return http.build();
    }

    // パスワードを暗号化するツール (BCryptという強力な暗号化を使います)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
