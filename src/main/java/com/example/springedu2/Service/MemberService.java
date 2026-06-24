package com.example.springedu2.Service;

import com.example.springedu2.dto.MemberCreateForm;
import com.example.springedu2.dto.MemberUpdateForm;
import com.example.springedu2.entity.Member;
import com.example.springedu2.entity.Role;
import com.example.springedu2.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    // UserDetailsService를 implements 받으면 loadUserByUsername을 무조건 만들어야 됨
    // 로그인을 위해서 db에서 회원정보를 조회해서 UserDetails를 생성
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 회원 정보 db에서 회원이름으로 조회
        // throw는 return과 같이 함수를 정지시키는 역할도 함
        Member  member  =   memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다"));
                // orElseThrow()가 없으면 if로 exception에 대해 코딩을 따로 해줘야 됨
        // 조회한 결과 Member type의 data -> UserDetails type으로 변환
        // security는 UserDetails가 로그인을 수행하는 객체임
        UserDetails user =  User.builder()
                .username(member.getUsername())     // 사용자 id
                .password(member.getPassword())     // 사용자 비밀번호
                .disabled(!member.isEnabled())       // 계정 사용 가능
                .roles(member.getRole().toString()) // 사용자 권한 "ADMIN" -> ROLE_ADMIN 권한
                .build();
        return user;
    }

    // --------------------------------------------------------------------------------------------
    // 회원 조회
    // 전체 조회
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // id로 조회
    public Member findById(Long id) {
        return memberRepository.findById( id )
                .orElseThrow(() -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다"
                ));
    }
    // Username으로 조회
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "회원을 찾을 수 없습니다."
                ));
    }


    // 일반유저 회원가입
    public Member register(@Valid MemberCreateForm memberForm) {
        memberForm.setRole( Role.USER.name() ); // 일반유저
        return create(memberForm);
    }

    // 회원가입
    // transactional은 commit을 해야 할때 붙임
    @Transactional
    public Member create(@Valid MemberCreateForm memberForm) {
        // 기존회원인지 조회
        validNewMember(memberForm.getUsername(), memberForm.getEmail());

        Member member = new Member();
        member.setUsername(memberForm.getUsername());
        member.setPassword(passwordEncoder.encode(memberForm.getPassword()));
        member.setName(memberForm.getName());
        member.setEmail(memberForm.getEmail());
        member.setRole(parseRole(memberForm.getRole()));
        member.setEnabled(true);
        return memberRepository.save(member);
    }

    // 기존 회원 인지 체크
    private void validNewMember( String username, String email ) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다");
        }
        if (memberRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    // 권한 문자열을 변환 "ADMIN", "USER" -> Role.ADMIN
    private Role parseRole(String role) {
        if ( role == null || role.isBlank()) {
            return Role.USER;
        }
        return Role.valueOf(role.toUpperCase() );
    }

    public MemberUpdateForm toUpdateForm(Member member) {
        MemberUpdateForm form = new MemberUpdateForm();
        form.setEmail(member.getEmail());
        form.setName(member.getName());
        form.setPassword(member.getPassword());
        form.setRole(member.getRole().toString());
        form.setEnabled(member.isEnabled());
        return form;
    }

    // 회원 정보 수정
    @Transactional
    public Member update(Long id, @Valid MemberUpdateForm memberForm, boolean adminMode) {
        Member member = findById(id);

        if (memberRepository.existsByEmailAndIdNot(memberForm.getEmail(), id)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        member.setName(memberForm.getName());
        member.setEmail(memberForm.getEmail());

        if (memberForm.getPassword() != null && !memberForm.getPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(memberForm.getPassword()));
        }

        if (adminMode) {
            member.setRole(parseRole(memberForm.getRole()));
            member.setEnabled(memberForm.isEnabled());
        }

        return member;
    }

    // 회원 삭제
    @Transactional
    public void delete(Long id, String name) {
        Member member = findById(id);
        if(member.getUsername().equals(name)) {
            throw new IllegalArgumentException("현재 로그인한 자신은 삭제할 수 없습니다");
        }
        memberRepository.delete(member);
    }
}
