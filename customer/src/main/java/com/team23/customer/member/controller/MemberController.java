package com.team23.customer.member.controller;

import com.team23.customer.global.response.CommonResponse;
import com.team23.customer.member.dto.MemberInfo;
import com.team23.customer.member.dto.MemberResponse;
import com.team23.customer.member.service.MemberService;
import com.team23.customer.security.jwt.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 정보 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 프로필 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<MemberResponse>> getMyProfile(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        MemberInfo info = memberService.findById(principal.memberId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(MemberResponse.from(info))
        );
    }
}