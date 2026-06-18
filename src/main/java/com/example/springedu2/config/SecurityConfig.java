package com.example.springedu2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Springsecurity의 설정
@Configuration
public class SecurityConfig {

    // filterchain을 부르는 곳임, 그중 어디는 적용하고, 어느 부분은 적용하지 않고를 설정함
    // 즉, 경로 지정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf ->csrf.disable()) // 현업에서는 설정(enable), 공부할 때는 설정안함(disable)
            .authorizeHttpRequests(auth->auth
                .requestMatchers(
                        "/", "/index.html",
                        "/css/**", "/img/**", "js/**", "/fonts/**",
                        "/login", "/members/register"
                ).permitAll() // 로그인 없이 사용 가능하다
                .requestMatchers("/admin/**", "/vupdate", "/vdelete").hasRole("ADMIN")
                .requestMatchers(
                        "/visitorMain.html", "/visitorForm.html",
                        "/vlist", "/vinsert", "/vsearch", "/one",
                        "/members/me"
                ).authenticated() // 로그인이 필요해
                .anyRequest().authenticated() // 설정하지 않은 다른 요청도 로그인 필요
            )
            .formLogin(form->form.loginPage("/login"))
            .logout(logout->logout.logoutUrl("logout"))
            .exceptionHandling(exception->
                    exception.accessDeniedPage("/access-denied")
            ); // 접근 거부 페이지 처리
        return http.build();
    }

    // 비밀번호를 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
