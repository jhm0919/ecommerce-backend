package com.team23.customer.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_provider_sub", columnList = "auth_provider, provider_sub")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, updatable = false)
    private AuthProvider authProvider;

    @Column(name = "provider_sub", nullable = false, updatable = false)
    private String providerSub;  // googleSub의 일반화 (구글뿐만 아니라 카카오 등도)

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    private String name;
    private String picture;
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Member registerFromOAuth(
            AuthProvider provider, String providerSub,
            String email, boolean emailVerified,
            String name, String picture, String locale
    ) {
        Member member = new Member();
        member.authProvider = provider;
        member.providerSub = providerSub;
        member.email = email;
        member.emailVerified = emailVerified;
        member.name = name;
        member.picture = picture;
        member.locale = locale;
        member.status = MemberStatus.ACTIVE;  // OAuth는 즉시 ACTIVE
        member.role = MemberRole.USER;
        return member;
    }

    public void updateProfile(String name, String picture, String locale) {
        this.name = name;
        this.picture = picture;
        this.locale = locale;
    }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        // 개인정보 삭제는 별도 정책 (GDPR 등)
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }
}
