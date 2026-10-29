package com.shop.member.controller;

import com.shop.global.response.CommonResponse;
import com.shop.member.dto.MemberRegisterRequest;
import com.shop.member.dto.MemberResponse;
import com.shop.member.service.MemberService;
import com.shop.global.security.jwt.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원", description = "회원 정보 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "신규 회원을 가입시킨다.")
    @PostMapping("/register")
    public ResponseEntity<CommonResponse<MemberResponse>> registerMember(
            @RequestBody MemberRegisterRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.createSuccess(memberService.register(request)));
    }

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
        MemberResponse response = memberService.findById(principal.memberId());
        return ResponseEntity.ok(
                CommonResponse.createSuccess(response)
        );
    }
}