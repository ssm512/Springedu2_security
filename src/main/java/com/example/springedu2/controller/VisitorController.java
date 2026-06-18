package com.example.springedu2.controller;

import com.example.springedu2.entity.Visitor;
import com.example.springedu2.repository.VisitorRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor // 3. 생성자 주입 다른 방법 시 @추가해야됨, 필수적인 인자만 사용한 생성자
public class VisitorController {

    // 1. @Autowired 이용 생성자 주입
    /* @Autowired
    private VisitorRepository visitorRepository; */

    // 2. 생성자 주입 : 요즘 방식
    /*private VisitorRepository visitorRepository;
    // 생성자 주입
    public VisitorController(VisitorRepository visitorRepository){
        this.visitorRepository  =   visitorRepository;
    } // 생성자 주입, 생성자를 통해서 전역변수로 사용*/

    // 3. 생성자 주입 다른 방법, final 가능
    // @RequiredArgsConstructor 필수 : Lombok를 꼭 써야됨
    private final VisitorRepository visitorRepository;

    // 방명록 조회
    @GetMapping("/vlist")
    public ModelAndView vlist() {
        List<Visitor> visitors        =   visitorRepository.findAll(); // 목록 조회
        return visitorView(visitors, null);
    }

    // visitorView() 함수
    private ModelAndView visitorView(List<Visitor> visitors, String buttonText ) {
        ModelAndView    mv      =   new ModelAndView("visitorView");
        //mv.setViewName("visitorView"); // visitorView.html(Model사용) - thymeleaf 문법
        if ( visitors.isEmpty() ) { // 조회한 결과가 없다면
            mv.addObject("msg","조회된 결과가 없습니다.");
        } else {
            mv.addObject("vList", visitors);
        }
        if (buttonText != null) {
            mv.addObject("buttonText", buttonText);
        }
        return mv;
    }

    // /vinsert : 방명록 추가
    // @Valid : form에서 넘어온 자료를 @Entity에 있는 설정
    // (@Id, @NotBlank, @Column(nullable=false))과 비교해서 입력 data를 검증하는 역할
    // data를 check 해서 bindingReuslt에 error message를 넣어줌
    @PostMapping("/vinsert")
    @Transactional
    public String vinsert(@Valid Visitor visitor, BindingResult bindingResult, Model model) {
        System.out.println("visitor: " + visitor);
        System.out.println("bindingResult: " + bindingResult);
        if(bindingResult.hasErrors()){
            model.addAttribute("msg","이름과 내용을 모두 입력하세요");
            return "visitorView"; // visitorView.html을 의미함 (temlpates)
        }
        visitorRepository.save(visitor); // entity 객체를 사용해야 한다  , save()의 인자는 무조건 entity type을 넣어줘야 함
        return "redirect:/vlist";
    }

    // /one : 방명록 id 로 조회 : Rest방식 호출, 결과가 json으로 return되어야 함
    // return 되는 값이 Visitor 객체일때 이것은 json으로 변경되어 다운로드 된다
    // @ResponseBody : model이 아닌 다른 걸로 return하겠다, message convertor가 숨어있음
    // return 값이 ResponseEntity<Visitor> 일때는 data는 json으로 상태코드로 리턴가능, 내가 error를 만들수 있음(예를 들어 id가 없어요 같은)
    // id를 가지고 조회를 했어, 자료가 있으면 ResponseEntity<Visitor>에 닮고 map의
    @GetMapping(value="/one", produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseEntity<Visitor> one (@RequestParam Integer id) {
        return visitorRepository.findById(Long.valueOf(id)) // data를 id로 조회. 있으면 Visitor 리턴
                .map(ResponseEntity::ok) // 상태 코드 ok 200을 추가해서 리턴
                .orElseGet(() -> ResponseEntity.notFound().build());
                    // 못찾으면 null 대신에 404 코드를 객체로 바꿔서(build()) 리턴한다
    }

    /*
    // /vupdate 수정 - 1번 방법 save() 사용
    @PostMapping("/vupdate")
    public String   vupdate(@Valid Visitor visitor,
                            BindingResult bindingResult,
                            Model model, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mgs", "수정할 이름과 내용을 모두입력하세요");
            return "redirect:/vlist";
        }
        // 수정
        visitorRepository.save(visitor);
        return "redirect:/vlist";
    }
    */

    // /vupdate 수정 - 2번 방법 @Transactional 사용
    @PostMapping("/vupdate")
    @Transactional
    public String   vupdate(@Valid Visitor visitor,
                            BindingResult bindingResult,
                            Model model, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("msg", "수정할 이름과 내용을 모두 입력하세요");
            return "redirect:/vlist";
        }
        Visitor     entity  =   visitorRepository.findById(Long.valueOf(visitor.getId()))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방명록 입니다."));
        entity.setName(visitor.getName());
        entity.setMemo(visitor.getMemo());
        // persist-context(1차캐시)에 먼저 값을 들고 있다가, persist-context의 내용과 db와의 내용이 다르면 자동으로 수정 쿼리문이 작성이 됨
        // 이를 활용해서 save() 기능을 사용하지 않고 update 시킴, 이때 @Transactional 이 필수임
        // @Transactional을 사용하면 이점이 있음, 2건을 한번에 commit 할때, 첫번쨰는 실패, 두번째는 성공 일때 수정해줌
        return "redirect:/vlist";
    }

    // RedirectAttributes 일회성 메세지를 뿌려는 놈, 메세지 정보를 다음 페이지로 넘기는 명령어
    // /vdelete
    @PostMapping("/vdelete")
    @Transactional
    public String delete( @RequestParam Integer id, RedirectAttributes redirectAttributes ) {
        if ( !visitorRepository.existsById(Long.valueOf(id)) ) {
            redirectAttributes.addFlashAttribute("msg", "삭제할 방명록찾을 수 없습니다.");
            return "redirect:/vlist";
        }
        visitorRepository.deleteById(Long.valueOf(id));
        return "redirect:/vlist";
    }

    // /vsearch
    // 검색 : 모두 대문자로 검색어를 포함한 data
    // 단 정렬은 id를 내림차순으로 출력한다
    // findByMemoContainingIgnoreCaseOrderByIdDesc(key)
    @GetMapping("/vsearch")
    public ModelAndView search (@RequestParam(defaultValue = "") String key) {
        List<Visitor>   visitors    =   key.isBlank()
                ? visitorRepository.findAll()   //  isBlank()가 true이면 findAll()을 수행해라
                : visitorRepository.findByIrum(key);
                //: visitorRepository.findByMemoContainingIgnoreCaseOrderByIdDesc(key);
                //: visitorRepository.findByName(key);
        System.out.println(visitors);
        return visitorView(visitors, "메인으로 돌아가기");
    }

} // controller end
