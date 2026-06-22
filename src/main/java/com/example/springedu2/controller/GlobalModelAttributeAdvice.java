package com.example.springedu2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/*
contoller에서 따로 login 정보를 들고 다니는 코딩을 controller의 method 마다 넣어줬었는데
이를 controlleradvice에서 한번에 처리한거임.

`@ControllerAdvice`는 여러 컨트롤러에 공통으로 적용할 기능을 정의할 때 사용합니다.
이 클래스는 특정 컨트롤러 하나에 붙는 것이 아니라, 여러 `@Controller`에 공통으로 적용됩니다.
@Controller 가 있는 컨트롤러들이 실행되기 전에 GlobalModelAttributeAdvice의 @ModelAttribute 메서드가 먼저 실행됩니다.

GlobalModelAttributeAdvice는 모든 MVC 컨트롤러 실행 전에 공통으로 실행되어, 현재 요청을 처리할 컨트롤러 정보를 로그로 출력하고,
 로그인 상태와 관리자 여부를 Model에 담아 Thymeleaf 화면에서 사용할 수 있게 해주는 클래스입니다.
 */

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributeAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributeAdvice.class);

    /* 이 메서드는 컨트롤러의 요청 처리 메서드가 실행되기 전에 자동 실행됩니다.
        사용자가 URL 요청
           ↓
        Spring Security가 Authentication(객체) 준비
           ↓
        Spring MVC가 요청을 처리할 Controller 메서드 결정
           ↓
        @ControllerAdvice의 @ModelAttribute 메서드 실행
           ↓
        HandlerMethod에서 Controller명과 메서드명 확인
           ↓
        로그 출력
           ↓
        Authentication으로 로그인 여부 확인
           ↓
        권한 목록에서 ROLE_ADMIN 여부 확인
           ↓
        Model에 loggedIn, loginUsername, isAdmin 추가
           ↓
        실제 Controller 메서드 실행
           ↓
        Thymeleaf 화면에서 Model 값 사용
    */
    @ModelAttribute
    public void addLoginInfo(
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        // 현재 요청을 처리할 Controller와 메서드 찾기
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (handler instanceof HandlerMethod handlerMethod) {
            // `handler`가
            // 일반적인 `@GetMapping`, `@PostMapping` 요청이면 대부분 `HandlerMethod`입니다.
            // 하지만 다음과 같은 경우 '/css/style.css', '/img/logo.png', '/error' 는 아닐 수 있습니다.
            // 그래서 바로 형변환하지 않고 안전하게 타입을 확인합니다.

            //  Controller 클래스명 가져오기
            String controllerName = handlerMethod.getBeanType().getSimpleName();

            //  Controller 메서드명 가져오기
            String methodName = handlerMethod.getMethod().getName();

            // 로긏 룰력 : [GET] /vlist -> VisitorController.list()
            log.info("[{}] {} -> {}.{}()",
                    request.getMethod(),        // GET, POST, PUT(insert)
                    request.getRequestURI(),    // 요청주소
                    controllerName,             // 어떤 Controller
                    methodName                  // 어떤 함수
            );
        }

        // 로그인 여부 확인
        // Authentication 객체가 있는가?
        // 인증 상태인가? (로그인 되었나?)
        // 익명 사용자가 아닌가? (권한이 있는가?)
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        // Model에 로그인 정보 추가
        // 로그인 여부
        // <span th:if="${loggedIn}">로그인 상태입니다.</span>
        // <span th:if="${!loggedIn}">로그인하지 않았습니다.</span>
        model.addAttribute("loggedIn", loggedIn);

        // 로그인한 사용자의 이름 또는 아이디
        // <span th:text="${loginUsername}">username</span>님
        // 로그인하지 않은 상태라면 빈 문자열이 들어갑니다.
        model.addAttribute("loginUsername", loggedIn ? authentication.getName() : "");


        //  관리자 여부 확인
        // 현재 로그인 사용자가 관리자 권한을 가지고 있는지 확인
        // ROLE_ADMIN 관리자  ROLE_USER :일반 사용자
        // 권한을 들고와서 배열로 바꾸어서 그 권한이 "ROLE_ADMIN"이냐?
        boolean isAdmin = loggedIn && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // 권한 객체 목록에서 권한 문자열만 꺼내는 코드
                // == .map(authority -> authority.getAuthority()).toList()   //  ["ROLE_ADMIN", "ROLE_USER"]
                .anyMatch("ROLE_ADMIN"::equals);
        // == .anyMatch(authority -> "ROLE_ADMIN".equals(authority))  // 하지만 이 방식은 authority가 null이면 오류가 납니다.

        // Model에 관리자 여부 추가
        model.addAttribute("isAdmin", isAdmin);
        System.out.println("model"+model.toString());
    }
}