package com.shop.member.service;

import com.shop.member.domain.Member;
import com.shop.member.dto.MemberRegisterRequest;
import com.shop.member.dto.MemberResponse;
import com.shop.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    private MemberRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new MemberRegisterRequest(
                "test@example.com",
                "password",
                "홍길동",
                "https://picture.url"
        );
    }

    @Test
    void 회원_가입_성공() {
        // Given
        given(memberRepository.save(any(Member.class)))
                .willAnswer(i -> i.getArguments()[0]);

        // When
        MemberResponse response = memberService.register(request);

        // Then
        assertThat(response.email()).isEqualTo(request.email());
    }

    @Test
    void 회원가입_실패_이메일_중복() {
        // Given
        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        // When & Then
        Assertions.assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }


}
