package com.example.springedu2.controller;

import com.example.springedu2.Service.MemberService;
import com.example.springedu2.dto.MemberCreateForm;
import com.example.springedu2.dto.MemberUpdateForm;
import com.example.springedu2.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;

    // 회원 목록
    @GetMapping("/admin/members")
    public String members(Model model) {

        List<Member> memberList =   memberService.findAll();
        model.addAttribute("memberList", memberList);
        return "memberList"; // memberList.html
    }

    // 회원 추가를 위해 입력받는 화면
    @GetMapping("/admin/members/new")
    public String adminCreateForm(Model model) {
        model.addAttribute("memberForm", new MemberCreateForm());
        return "memberAdminForm";   // memberAdminForm.html
    }

    // 회원 추가 (관리자가)
    // @ModelAttribute("memberForm")
    // model.addAttibute("memberForm", memberForm); 을 대신 해주는거
    @PostMapping("/admin/members")
    @Transactional
    public String adminCreate(
            @Valid @ModelAttribute("memberForm") MemberCreateForm memberCreateForm,
            BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            return "memberAdminForm"; // 다시 입력받아라
        }

        // 새 회원 추가 관리자가
        try {
            memberService.create(memberCreateForm);
        } catch (IllegalThreadStateException e){
            bindingResult.reject("createFail", e.getMessage());
            return "memberAdminForm"; // 회원 추가 실패 -> 다시 추가화면으로 이동
        }
        return "redirect:/admin/members"; // 회원 목록 조회
    }

    // /admin/members/1/edit
    // 회원 정보 수정 - 입력받는 화면으로 이동
    @GetMapping("/admin/members/{id}/edit")
    public String adminEditForm(
            @PathVariable Long id,
            Model model
    ) {
        // 수정을 위한 db데이터를 조회 (entity type 데이터를 조회)
        Member member = memberService.findById(id); // memberService에 이미 orElseThrow가 있음
        // db에서 조회한 member -> memberAdminEditForm 에 사용할
        // 객체인 MemberUpdateForm 구조로 변경
        MemberUpdateForm memberForm = memberService.toUpdateForm(member);
        model.addAttribute("member",     member); // id로 조회한 정보
        model.addAttribute("memberForm", memberForm); // 수정을 위해 필요한 별도의 DTO memberUpdateForm
        return "memberAdminEditForm"; // memberAdminEditForm.html로 이동
    }

    // 넘어온 수정 정보를 가지고 member 정보를 수정
    // 넘어온 값을 model에도 담아 줄거야 : @ModelAttribute("memberForm")
    @PostMapping("/admin/members/{id}/edit")
    public String adminEdit(
                            @PathVariable Long id,
                            @Valid @ModelAttribute("memberForm") MemberUpdateForm form,
                            BindingResult bindingResult,
                            Model model) {
        // 넘어온 정보로 수정한다.
        Member member = memberService.findById(id);
        if(bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            return "memberAdminEditForm";
        }
        try {
            memberService.update(id, form, true);
        } catch (IllegalThreadStateException e) {
            bindingResult.reject("updateFail", e.getMessage());
            model.addAttribute("member", member);
            return "memberAdminEditForm";
        }
        return "redirect:/admin/members";
    }

    // 회원삭제 관리자가
    // redirect로 가는 곳에만 일회성 msg를 넘겨주는 객체 : RedirectAttributes redirectAttributes
    @PostMapping("/admin/members/{id}/delete")
    public String adminDelete(
                                @PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        try {
            memberService.delete(id, authentication.getName());
        } catch (IllegalThreadStateException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}
