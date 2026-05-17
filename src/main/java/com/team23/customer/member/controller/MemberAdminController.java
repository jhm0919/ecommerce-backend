package com.team23.customer.member.controller;

import com.team23.global.response.CommonResponse;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.domain.MemberStatus;
import com.team23.customer.member.dto.MemberAdminResponse;
import com.team23.customer.member.service.MemberAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 - 회원", description = "회원 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberAdminService memberAdminService;

    @Operation(summary = "회원 목록 조회", description = "상태/키워드 필터로 회원 목록을 조회한다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<MemberAdminResponse>>> findMembers(
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<Member> members = memberAdminService.findMembers(status, keyword, pageable);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(members.map(MemberAdminResponse::from))
        );
    }

    @Operation(summary = "회원 단건 조회", description = "회원 ID로 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<CommonResponse<MemberAdminResponse>> findMember(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.findMember(memberId);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(MemberAdminResponse.from(member))
        );
    }

    @Operation(summary = "블랙리스트 등록", description = "회원을 정지(SUSPENDED) 처리한다. ACTIVE 상태만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 성공"),
            @ApiResponse(responseCode = "400", description = "이미 정지된 회원"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @PostMapping("/{memberId}/blacklist")
    public ResponseEntity<CommonResponse<MemberAdminResponse>> blacklist(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.blacklist(memberId);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(MemberAdminResponse.from(member))
        );
    }

    @Operation(summary = "블랙리스트 해제", description = "정지된 회원을 ACTIVE로 복구한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해제 성공"),
            @ApiResponse(responseCode = "400", description = "정지 상태가 아닌 회원"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @DeleteMapping("/{memberId}/blacklist")
    public ResponseEntity<CommonResponse<MemberAdminResponse>> removeFromBlacklist(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.removeFromBlacklist(memberId);
        return ResponseEntity.ok(
                CommonResponse.createSuccess(MemberAdminResponse.from(member))
        );
    }

    @Operation(
            summary = "회원 삭제",
            description = "회원을 Soft Delete 처리한다. ACTIVE 회원은 불가 — 먼저 블랙리스트 등록 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "ACTIVE 회원 삭제 불가"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long memberId
    ) {
        memberAdminService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
