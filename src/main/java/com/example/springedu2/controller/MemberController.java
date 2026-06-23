package com.example.springedu2.controller;

import com.example.springedu2.Service.MemberService;
import com.example.springedu2.dto.MemberCreateForm;
import com.example.springedu2.dto.MemberUpdateForm;
import com.example.springedu2.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    /* 생성자 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    */

    // 회원가입페이지로 이동
    @GetMapping("/members/register")
    public String registerForm(Model model) {
        model.addAttribute("memberForm", new MemberCreateForm()); // MemberCreateForm DTO class
        // MemberCreateForm 객체를 만들어서 이동함
        // th:object="${memberForm}"을 사용하기 위해서 객체를 만드는 거임
        return "memberRegister"; // memberRegister.html로 보낸다
    }

    // 회원가입
    // RedirectAttributes 다음 controller에 일회성 message를 던지는
    // BindingResult error message
    // @Valid는 서버에서 입력값 체크
    @PostMapping("/members/register")
    public String registerMember(@Valid @ModelAttribute("memberForm") MemberCreateForm memberForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        // 입력에 오류가 있다면 다시 입력화면으로 돌아가
        if (bindingResult.hasErrors()) {
            return "memberRegister"; // memberRegister.html으로 돌아가라는 의미 -> field에 errors를 들고 있음
        }
        // 회원가입 : db에 저장
        try {
            memberService.register(memberForm);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("가입실패", e.getMessage());
            redirectAttributes.addFlashAttribute("msg", "회원가입이 실패했습니다." + e.getMessage());
            return "memberRegister";
        }
        // 반드시 redirect 할때만 사용가능하다 redirect:/login
        // 1회성 msg
        redirectAttributes.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인하세요");
        return "redirect:/login"; // GET 호출임 redirect가 a태그로 바뀜
    }

    // 내정보
    @GetMapping("/members/me")
    public String myPage(Authentication authentication, Model model) {
        System.out.println("authentication:" + authentication);
        Member member = memberService.findByUsername(authentication.getName());
        model.addAttribute("member", member);
        model.addAttribute("memberForm", memberService.toUpdateForm(member));
        return "memberMyPage"; // memberMyPage.html
    }

    // 내정보 수정
    // db에 저장하는 거
    @PostMapping("/members/me")
    public String updateMyPage(
            Authentication authentication,
            @Valid @ModelAttribute("memberForm") MemberUpdateForm memberForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        Member member = memberService.findByUsername(authentication.getName());

        if(bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            return "memberMyPage";
        }
        try {
            memberService.update(member.getId(), memberForm, false);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("member", member);
            return "memberMyPage";
        }
        redirectAttributes.addFlashAttribute("msg", "내 정보가 수정되었습니다.");
        return "redirect:/members/me";
    }

    // 방명록으로
    // 로그인 성공 후 이동하는 방명록 메인 페이지
    // visitorMain.html은 templates의 thymeleaf 템플릿이므로 컨트롤러를 거쳐 렌더링해야 함
    @GetMapping("/visitorMain")
    public String visitorMain() { return  "visitorMain"; } // visitorMain.html 호출

}
