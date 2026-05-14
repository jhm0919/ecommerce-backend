package com.team23.customer.seller.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_seller_login_id",
                columnList = "login_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;               // BCrypt 암호화

    @Column(name = "is_temporary_password", nullable = false)
    private boolean temporaryPassword;     // 임시 비밀번호 여부

    @Column(name = "application_id", updatable = false)
    private Long applicationId;            // SellerApplication 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SellerStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────
    // 정적 팩토리 — Admin 승인 시 호출
    // ─────────────────────────────────────

    public static Seller create(
            String loginId,
            String encodedPassword,
            Long applicationId
    ) {
        Seller seller = new Seller();
        seller.loginId = loginId;
        seller.password = encodedPassword;
        seller.temporaryPassword = true;    // 최초는 항상 임시
        seller.applicationId = applicationId;
        seller.status = SellerStatus.ACTIVE;
        return seller;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.temporaryPassword = false;     // 변경 후 임시 해제
    }

}
