package com.team23.customer.member.controller;

import com.team23.customer.member.domain.Member;
import com.team23.customer.member.domain.MemberStatus;
import com.team23.customer.member.dto.MemberAdminResponse;
import com.team23.customer.member.service.MemberAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {

    private final MemberAdminService memberAdminService;

    /**
     * 회원 목록 조회.
     * GET /api/admin/members?status=SUSPENDED&keyword=홍&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<MemberAdminResponse>> findMembers(
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<Member> members = memberAdminService.findMembers(status, keyword, pageable);
        return ResponseEntity.ok(members.map(MemberAdminResponse::from));
    }

    /**
     * 회원 단건 조회.
     * GET /api/admin/members/{memberId}
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberAdminResponse> findMember(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.findMember(memberId);
        return ResponseEntity.ok(MemberAdminResponse.from(member));
    }

    /**
     * 블랙리스트 등록 (정지).
     * POST /api/admin/members/{memberId}/blacklist
     */
    @PostMapping("/{memberId}/blacklist")
    public ResponseEntity<MemberAdminResponse> blacklist(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.blacklist(memberId);
        return ResponseEntity.ok(MemberAdminResponse.from(member));
    }

    /**
     * 블랙리스트 해제.
     * DELETE /api/admin/members/{memberId}/blacklist
     */
    @DeleteMapping("/{memberId}/blacklist")
    public ResponseEntity<MemberAdminResponse> removeFromBlacklist(
            @PathVariable Long memberId
    ) {
        Member member = memberAdminService.removeFromBlacklist(memberId);
        return ResponseEntity.ok(MemberAdminResponse.from(member));
    }

    /**
     * 회원 삭제 (Soft Delete).
     * DELETE /api/admin/members/{memberId}
     * ACTIVE 회원은 불가 → 먼저 블랙리스트 등록 필요.
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long memberId
    ) {
        memberAdminService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
