package com.team23.customer.member.service;

import com.team23.customer.member.domain.AuthProvider;
import com.team23.customer.member.domain.Member;
import com.team23.customer.member.domain.MemberStatus;
import com.team23.customer.member.exception.MemberNotFoundException;
import com.team23.customer.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberAdminServiceTest {

    @Mock private MemberRepository memberRepository;

    @InjectMocks private MemberAdminService memberAdminService;

    private Member activeMember;

    @BeforeEach
    void setUp() {
        activeMember = Member.registerFromOAuth(
                AuthProvider.GOOGLE,
                "google-sub-123",
                "test@example.com",
                true,
                "홍길동",
                "https://picture.url",
                "ko"
        );
    }

    @Nested
    @DisplayName("회원 목록 조회")
    class FindMembers {

        @Test
        @DisplayName("필터 없이 전체 조회")
        void findAll() {
            given(memberRepository.findMembersForAdmin(
                    eq(null), eq(null), any()))
                    .willReturn(new PageImpl<>(List.of(activeMember)));

            Page<Member> result = memberAdminService.findMembers(
                    null, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("상태 필터 적용")
        void filterByStatus() {
            given(memberRepository.findMembersForAdmin(
                    eq(MemberStatus.SUSPENDED), eq(null), any()))
                    .willReturn(new PageImpl<>(List.of()));

            Page<Member> result = memberAdminService.findMembers(
                    MemberStatus.SUSPENDED, null, PageRequest.of(0, 20));

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("키워드 trim 적용")
        void trimKeyword() {
            given(memberRepository.findMembersForAdmin(
                    eq(null), eq("홍길동"), any()))
                    .willReturn(new PageImpl<>(List.of()));

            memberAdminService.findMembers(null, "  홍길동  ", PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("빈 키워드는 null로 처리")
        void emptyKeywordBecomesNull() {
            given(memberRepository.findMembersForAdmin(
                    eq(null), eq(null), any()))
                    .willReturn(new PageImpl<>(List.of()));

            memberAdminService.findMembers(null, "   ", PageRequest.of(0, 20));
        }
    }

    @Nested
    @DisplayName("회원 단건 조회")
    class FindMember {

        @Test
        @DisplayName("존재하는 회원 조회")
        void findExisting() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            Member result = memberAdminService.findMember(1L);

            assertThat(result).isEqualTo(activeMember);
        }

        @Test
        @DisplayName("존재하지 않는 회원은 MemberNotFoundException")
        void rejectUnknown() {
            given(memberRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberAdminService.findMember(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("블랙리스트 등록")
    class Blacklist {

        @Test
        @DisplayName("ACTIVE 회원을 블랙리스트(정지) 등록")
        void blacklistActive() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            Member result = memberAdminService.blacklist(1L);

            assertThat(result.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
        }

        @Test
        @DisplayName("이미 SUSPENDED 회원은 예외")
        void rejectAlreadySuspended() {
            activeMember.suspend();

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberAdminService.blacklist(1L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("존재하지 않는 회원은 MemberNotFoundException")
        void rejectUnknown() {
            given(memberRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberAdminService.blacklist(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("블랙리스트 해제")
    class RemoveFromBlacklist {

        @Test
        @DisplayName("SUSPENDED 회원을 ACTIVE로 복구")
        void removeNormal() {
            activeMember.suspend();

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            Member result = memberAdminService.removeFromBlacklist(1L);

            assertThat(result.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("ACTIVE 회원 해제 시도는 예외")
        void rejectActiveNotSuspended() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberAdminService.removeFromBlacklist(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class DeleteMember {

        @Test
        @DisplayName("SUSPENDED 회원은 삭제 가능")
        void deleteSuspended() {
            activeMember.suspend();

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            assertThatCode(() -> memberAdminService.deleteMember(1L))
                    .doesNotThrowAnyException();

            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("ACTIVE 회원은 삭제 불가")
        void rejectActive() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberAdminService.deleteMember(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE");
        }

        @Test
        @DisplayName("이미 WITHDRAWN 회원은 무동작 (멱등)")
        void deleteWithdrawnIsIdempotent() {
            activeMember.withdraw();

            given(memberRepository.findById(1L))
                    .willReturn(Optional.of(activeMember));

            assertThatCode(() -> memberAdminService.deleteMember(1L))
                    .doesNotThrowAnyException();

            assertThat(activeMember.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("존재하지 않는 회원은 MemberNotFoundException")
        void rejectUnknown() {
            given(memberRepository.findById(999L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> memberAdminService.deleteMember(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }
}