package com.team23.customer.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MemberTest {

    // 테스트 데이터 생성 헬퍼
    private Member createActiveMember() {
        return Member.registerFromOAuth(
                AuthProvider.GOOGLE,
                "google-sub-123",
                "test@example.com",
                true,
                "홍길동",
                "https://example.com/picture.jpg",
                "ko"
        );
    }

    @Nested
    @DisplayName("회원 가입 (registerFromOAuth)")
    class Registration {

        @Test
        @DisplayName("OAuth 정보로 신규 회원을 가입시킬 수 있다")
        void registerNewMember() {
            Member member = createActiveMember();

            assertThat(member.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(member.getProviderSub()).isEqualTo("google-sub-123");
            assertThat(member.getEmail()).isEqualTo("test@example.com");
            assertThat(member.isEmailVerified()).isTrue();
            assertThat(member.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("신규 가입 시 상태는 ACTIVE이다")
        void newMemberIsActive() {
            Member member = createActiveMember();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.isActive()).isTrue();
        }

        @Test
        @DisplayName("신규 가입 시 권한은 USER이다")
        void newMemberHasUserRole() {
            Member member = createActiveMember();

            assertThat(member.getRole()).isEqualTo(MemberRole.USER);
            assertThat(member.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("provider가 null이면 예외")
        void rejectNullProvider() {
            assertThatThrownBy(() -> Member.registerFromOAuth(
                    null, "sub", "test@test.com", true, "홍길동", null, "ko"
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("providerSub이 null/빈 문자열이면 예외")
        void rejectInvalidProviderSub() {
            assertThatThrownBy(() -> Member.registerFromOAuth(
                    AuthProvider.GOOGLE, null, "test@test.com", true, "홍길동", null, "ko"
            )).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Member.registerFromOAuth(
                    AuthProvider.GOOGLE, "", "test@test.com", true, "홍길동", null, "ko"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("email이 null/빈 문자열이면 예외")
        void rejectInvalidEmail() {
            assertThatThrownBy(() -> Member.registerFromOAuth(
                    AuthProvider.GOOGLE, "sub", null, true, "홍길동", null, "ko"
            )).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> Member.registerFromOAuth(
                    AuthProvider.GOOGLE, "sub", "", true, "홍길동", null, "ko"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name이 null이어도 가입 가능 (선택 정보)")
        void allowNullName() {
            Member member = Member.registerFromOAuth(
                    AuthProvider.GOOGLE, "sub", "test@test.com", true, null, null, null
            );

            assertThat(member.getName()).isNull();
        }
    }

    @Nested
    @DisplayName("프로필 갱신 (updateProfile)")
    class ProfileUpdate {

        @Test
        @DisplayName("프로필 정보를 갱신할 수 있다")
        void updateProfile() {
            Member member = createActiveMember();

            member.updateProfile("김철수", "https://new-pic.jpg", "en");

            assertThat(member.getName()).isEqualTo("김철수");
            assertThat(member.getPicture()).isEqualTo("https://new-pic.jpg");
            assertThat(member.getLocale()).isEqualTo("en");
        }

        @Test
        @DisplayName("name이 null이면 기존 값을 유지한다")
        void keepNameWhenNull() {
            Member member = createActiveMember();

            member.updateProfile(null, "https://new-pic.jpg", "en");

            assertThat(member.getName()).isEqualTo("홍길동");  // 기존 값 유지
            assertThat(member.getPicture()).isEqualTo("https://new-pic.jpg");
        }
    }

    @Nested
    @DisplayName("이메일 변경 (changeEmail)")
    class EmailChange {

        @Test
        @DisplayName("이메일을 변경할 수 있다")
        void changeEmail() {
            Member member = createActiveMember();

            member.changeEmail("new@example.com", true);

            assertThat(member.getEmail()).isEqualTo("new@example.com");
            assertThat(member.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("미인증 이메일로도 변경 가능")
        void changeToUnverifiedEmail() {
            Member member = createActiveMember();

            member.changeEmail("new@example.com", false);

            assertThat(member.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("새 이메일이 null이면 예외")
        void rejectNullEmail() {
            Member member = createActiveMember();

            assertThatThrownBy(() -> member.changeEmail(null, true))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("회원 정지 (suspend)")
    class Suspension {

        @Test
        @DisplayName("ACTIVE 회원을 정지시킬 수 있다")
        void suspendActiveMember() {
            Member member = createActiveMember();

            member.suspend();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
            assertThat(member.canLogin()).isFalse();
        }

        @Test
        @DisplayName("이미 SUSPENDED 회원은 다시 정지할 수 없다")
        void cannotSuspendAlreadySuspended() {
            Member member = createActiveMember();
            member.suspend();

            assertThatThrownBy(member::suspend)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ACTIVE");
        }

        @Test
        @DisplayName("WITHDRAWN 회원은 정지할 수 없다")
        void cannotSuspendWithdrawn() {
            Member member = createActiveMember();
            member.withdraw();

            assertThatThrownBy(member::suspend)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("회원 활성화 (activate)")
    class Activation {

        @Test
        @DisplayName("SUSPENDED 회원을 활성화할 수 있다")
        void activateSuspendedMember() {
            Member member = createActiveMember();
            member.suspend();

            member.activate();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(member.canLogin()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 회원은 활성화할 수 없다")
        void cannotActivateActive() {
            Member member = createActiveMember();

            assertThatThrownBy(member::activate)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("WITHDRAWN 회원은 활성화할 수 없다")
        void cannotActivateWithdrawn() {
            Member member = createActiveMember();
            member.withdraw();

            assertThatThrownBy(member::activate)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("탈퇴 (withdraw)")
    class Withdrawal {

        @Test
        @DisplayName("ACTIVE 회원이 탈퇴할 수 있다")
        void withdrawActive() {
            Member member = createActiveMember();

            member.withdraw();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.canLogin()).isFalse();
        }

        @Test
        @DisplayName("SUSPENDED 회원도 탈퇴 가능")
        void withdrawSuspended() {
            Member member = createActiveMember();
            member.suspend();

            member.withdraw();

            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("이미 WITHDRAWN인 회원은 다시 탈퇴할 수 없다")
        void cannotWithdrawAlreadyWithdrawn() {
            Member member = createActiveMember();
            member.withdraw();

            assertThatThrownBy(member::withdraw)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Already withdrawn");
        }
    }

    @Nested
    @DisplayName("권한 관리")
    class RoleManagement {

        @Test
        @DisplayName("일반 사용자를 관리자로 승격할 수 있다")
        void promoteToAdmin() {
            Member member = createActiveMember();

            member.promoteToAdmin();

            assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
            assertThat(member.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("이미 관리자인 회원은 다시 승격할 수 없다")
        void cannotPromoteAlreadyAdmin() {
            Member member = createActiveMember();
            member.promoteToAdmin();

            assertThatThrownBy(member::promoteToAdmin)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("상태 머신 시나리오")
    class StateMachineScenarios {

        @Test
        @DisplayName("ACTIVE → SUSPENDED → ACTIVE → WITHDRAWN 정상 흐름")
        void normalLifecycleFlow() {
            Member member = createActiveMember();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

            member.suspend();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);

            member.activate();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

            member.withdraw();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("WITHDRAWN은 모든 상태 전이를 거부한다")
        void withdrawnRejectsAllTransitions() {
            Member member = createActiveMember();
            member.withdraw();

            assertThatThrownBy(member::suspend).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(member::activate).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(member::withdraw).isInstanceOf(IllegalStateException.class);
        }
    }
}