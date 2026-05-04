package com.team23.customer.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 회원을 표현하는 Aggregate Root.
 *
 * <p>OAuth Provider(Google, Kakao, Naver 등)를 통한 가입 및 로그인을 지원한다.
 * Provider별 식별자는 {@code (authProvider, providerSub)} 조합으로 유니크하게 관리된다.
 *
 * <p>회원 상태는 PENDING → ACTIVE → SUSPENDED/WITHDRAWN 순서로 전이된다.
 */
@Entity
@Table(name = "members", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_member_provider_sub",
                columnNames = {"auth_provider", "provider_sub"}
        )
}, indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_provider_sub", columnList = "auth_provider, provider_sub"),
        @Index(name = "idx_member_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, updatable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_sub", nullable = false, updatable = false, length = 100)
    private String providerSub;

    @Column(nullable = false, length = MAX_EMAIL_LENGTH)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(length = MAX_NAME_LENGTH)
    private String name;

    @Column(length = 500)
    private String picture;

    @Column(length = 10)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리 메서드
    // ─────────────────────────────────────

    /**
     * OAuth Provider를 통한 신규 회원 가입.
     *
     * <p>가입 직후 상태는 ACTIVE이며, 권한은 USER이다.
     */
    public static Member registerFromOAuth(
            AuthProvider provider,
            String providerSub,
            String email,
            boolean emailVerified,
            String name,
            String picture,
            String locale
    ) {
        validateProvider(provider);
        validateProviderSub(providerSub);
        validateEmail(email);
        validateName(name);

        Member member = new Member();
        member.authProvider = provider;
        member.providerSub = providerSub.trim();
        member.email = email.trim();
        member.emailVerified = emailVerified;
        member.name = (name == null) ? null : name.trim();
        member.picture = picture;
        member.locale = locale;
        member.status = MemberStatus.ACTIVE;
        member.role = MemberRole.USER;
        return member;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    /**
     * 프로필 정보 갱신 (OAuth 로그인 시마다 호출).
     */
    public void updateProfile(String name, String picture, String locale) {
        if (name != null) {
            validateName(name);
            this.name = name.trim();
        }
        this.picture = picture;
        this.locale = locale;
    }

    /**
     * 이메일 변경. OAuth provider에서 이메일이 변경되었을 때 호출.
     */
    public void changeEmail(String newEmail, boolean emailVerified) {
        validateEmail(newEmail);
        this.email = newEmail.trim();
        this.emailVerified = emailVerified;
    }

    /**
     * 회원을 일시 정지한다 (관리자 액션).
     * ACTIVE 상태에서만 가능.
     */
    public void suspend() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE members can be suspended. Current: " + status);
        }
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * 정지 상태에서 활동을 재개한다 (관리자 액션).
     */
    public void activate() {
        if (this.status != MemberStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Only SUSPENDED members can be activated. Current: " + status);
        }
        this.status = MemberStatus.ACTIVE;
    }

    /**
     * 회원 탈퇴 처리 (논리적 삭제).
     * WITHDRAWN 상태에서는 다시 활성화할 수 없다 (재가입 필요).
     */
    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("Already withdrawn");
        }
        this.status = MemberStatus.WITHDRAWN;
    }

    /**
     * 관리자에 의한 회원 삭제 (Soft Delete).
     * ACTIVE 회원은 삭제 불가 — 정지 처리 후 삭제 필요.
     * 이미 WITHDRAWN이면 무동작 (멱등).
     */
    public void deleteByAdmin() {
        if (this.status == MemberStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot delete ACTIVE member. Suspend first.");
        }
        if (this.status == MemberStatus.WITHDRAWN) {
            return;  // 이미 삭제됨 — 멱등
        }
        this.status = MemberStatus.WITHDRAWN;
    }

    /**
     * 관리자 권한으로 승격.
     */
    public void promoteToAdmin() {
        if (this.role == MemberRole.ADMIN) {
            throw new IllegalStateException("Already an admin");
        }
        this.role = MemberRole.ADMIN;
    }

    // ─────────────────────────────────────
    // 질의 메서드
    // ─────────────────────────────────────

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public boolean canLogin() {
        return this.status.canLogin();
    }

    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }

    // ─────────────────────────────────────
    // 검증 메서드
    // ─────────────────────────────────────

    private static void validateProvider(AuthProvider provider) {
        Objects.requireNonNull(provider, "authProvider must not be null");
    }

    private static void validateProviderSub(String providerSub) {
        Objects.requireNonNull(providerSub, "providerSub must not be null");
        if (providerSub.isBlank()) {
            throw new IllegalArgumentException("providerSub must not be blank");
        }
    }

    private static void validateEmail(String email) {
        Objects.requireNonNull(email, "email must not be null");
        if (email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                    "email must not exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        // 이메일 형식 검증은 OAuth Provider가 이미 했으므로 여기선 생략
    }

    private static void validateName(String name) {
        if (name != null && name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }
}
