package com.team23.customer.member.controller;

import com.team23.customer.member.dto.MemberInfo;
import com.team23.customer.member.dto.MemberResponse;
import com.team23.customer.member.service.MemberService;
import com.team23.customer.security.jwt.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyProfile(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        MemberInfo info = memberService.findById(principal.memberId());  // ★ memberId 사용
        return ResponseEntity.ok(MemberResponse.from(info));
    }
}