package com.example.springedu2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Visitor {

    @Id // 기본키 : primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호자동증가
    private Integer id;     // 방명록 번호 id : 기본키

    // 작성자  name
    // @NotBlank : null, ""(빈문자열), " "(공백포함된 문자열) 전부 허용 X
    // @Size : 문자열(50자), 배열(50개), ArrayList(element 갯수 50개)
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50)
    private String  name;

    // 작성일  writeDate
    // data 등록일(Created Date) 자동입력 일일이 LocalDateTime.now()를 넣지 않아도 된다
    // updatable = false : update 할때는 수정하지 마라
    @CreationTimestamp // record가 생성된 시간
    @Column(name="writedate", nullable = false, updatable = false)
    private LocalDate writeDate;

    // 방명록 내용   memo
    @NotBlank(message = "내용을 필수입니다.")
    @Size(max = 1000)
    private String  memo;
}
