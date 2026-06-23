package com.example.springedu2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // 로그인 페이지로 이동
    @GetMapping("/login")
    public String login() {
        return "login"; // login.html 을 호출해라
    }

    // 로그인을 처리할 주소가 필요 X -> 만들지 않는다
    // @PostMapping("/login") 은 security filter가 처리하므로 코딩 X
    // db처리 로직을 별도의 클래스에 구현해서 (loadUserByUsername()) security가 자동으로 호출 처리
    // UserDetailsService에서 loadUserByUsername()를 실행 조회 결과 반환
    // 조회한 결과를 UserDetails 객체의 User로 저장해서 SpringSecurity에게 보낸다 : 로그인 Ok

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // access-denied.html 호출
    }


}
