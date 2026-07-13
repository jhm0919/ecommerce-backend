package com.shop.member.service;

import com.shop.member.domain.Member;
import com.shop.member.domain.MemberStatus;
import com.shop.member.exception.MemberNotFoundException;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAdminService {

    private final MemberRepository memberRepository;

    // ─────────────────────────────────────
    // 조회
    // ─────────────────────────────────────

    /**
     * 회원 목록 조회.
     * status, keyword 모두 선택적.
     */
    @Transactional(readOnly = true)
    public Page<Member> findMembers(
            MemberStatus status,
            String keyword,
            Pageable pageable
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return memberRepository.findMembersForAdmin(status, normalizedKeyword, pageable);
    }

    /**
     * 회원 단건 조회.
     */
    @Transactional(readOnly = true)
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    // ─────────────────────────────────────
    // 블랙리스트 (= 정지)
    // ─────────────────────────────────────

    /**
     * 회원을 블랙리스트(정지)에 등록한다.
     * ACTIVE → SUSPENDED 전이.
     */
    @Transactional
    public Member blacklist(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.suspend();  // ACTIVE → SUSPENDED

        log.info("Member blacklisted(suspended): memberId={}, email={}",
                memberId, member.getEmail());
        return member;
    }

    /**
     * 블랙리스트(정지) 해제.
     * SUSPENDED → ACTIVE 복구.
     */
    @Transactional
    public Member removeFromBlacklist(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.activate();  // SUSPENDED → ACTIVE

        log.info("Member removed from blacklist: memberId={}, email={}",
                memberId, member.getEmail());
        return member;
    }

    // ─────────────────────────────────────
    // 삭제
    // ─────────────────────────────────────

    /**
     * 회원 삭제 (Soft Delete).
     * ACTIVE 회원은 삭제 불가 — 정지 후 삭제 필요.
     */
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.deleteByAdmin();  // ACTIVE면 예외, 나머지 → WITHDRAWN

        log.info("Member deleted: memberId={}, email={}", memberId, member.getEmail());
    }

    // ─────────────────────────────────────
    // 헬퍼
    // ─────────────────────────────────────

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
