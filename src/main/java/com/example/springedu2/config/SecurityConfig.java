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
            // 서버가 내려주는 csrf 토큰값을 사용하지 않겠다.
            // visitorForm.html은 순수 html이므로 서버의 csrf 토큰을 보관 처리 불가능하다.
            .csrf(csrf ->csrf.disable()) // 현업에서는 설정(enable), 공부할 때는 설정안함(disable)
            .authorizeHttpRequests(auth->auth
                .requestMatchers(
                        "/", "/index.html",
                        "/css/**", "/img/**", "/js/**", "/fonts/**",
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
            // formLogin() 는 사용자가 <form>으로 입력한 username, password를 기반으로 인증 처리
            // 로그인 기초데이터를 미리 db에 만들어 둔다
            // DataInitailizer 클래스를 미리 db에 저장한다 -> Member table이 필요함
            .formLogin(form -> form
                    .loginPage("/login")
                    // GET 요청으로 /login -> PageController에 /login 주소로 이동 -> login.html로 보낸다는 뜻
                    // 내가 만든 로그인화면 사용
                    // 만약 <input name="username"/> -> <input name="loginId" />
                    // 만약 <input name="password"/> -> <input name="loginPwd" />
                    // security 설정에서
                    // .formLogin(form->form
                    //          .usernameParameter("loginId")
                    //          .passwordParameter("loginPwd")
                    // )
                    .loginProcessingUrl("/login") // 이 줄은 생략이 가능함
                    // Post /login 를 의미함, 로그인 처리
                    // springsecurity가 username, password를 읽어서 인증처리한다. : 자동
                    // UserDtailsService 안의 loadUserByUsername()를 실행해서 DB 검색 후 로그인 처리까지진행
                    .defaultSuccessUrl("/visitorMain.html", true)
                    // 로그인이 성공하면 "/"나 "/visitorMain.html" 로 이동하도록 설정 가능
                    // 비밀번호가 틀리거나 사용자가 없으면
                    // '/login?error' 또는 .failureUrl("/login?error")로 이동해서 thymeleaf에서 처리
                    // <p th:if="${param.error}" class="error">
                    //      아이디 또는 비밀번호가 올바르지 않습니다
                    // </p>
                    .permitAll() // 로그인 페이지는 누구나 접근가능하다
                    // 로그인화면, 로그인처리 url, 로그인 실패 url 은 인증없이 접근가능해야 한다
            )
            .logout(logout->logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )
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
