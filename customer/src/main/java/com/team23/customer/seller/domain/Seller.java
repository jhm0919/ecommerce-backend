package com.team23.customer.seller.domain;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;
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

    // ───── 계정 정보 ─────
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
    // ───── 운영 정보 (추가) ─────
    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name = "manager_name", nullable = false, length = 50)
    private String managerName;

    @Column(name = "manager_email", nullable = false, length = 100)
    private String managerEmail;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    // ───── 시간 ─────
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
            Long applicationId,
            String businessName,
            String managerName,
            String managerEmail,
            String phoneNumber,
            String mobileNumber
    ) {
        Seller seller = new Seller();
        seller.loginId = loginId;
        seller.password = encodedPassword;
        seller.temporaryPassword = true;
        seller.applicationId = applicationId;
        seller.status = SellerStatus.ACTIVE;
        seller.businessName = businessName;
        seller.managerName = managerName;
        seller.managerEmail = managerEmail;
        seller.phoneNumber = phoneNumber;
        seller.mobileNumber = mobileNumber;
        return seller;
    }

    // ─────────────────────────────────────
    // 비즈니스 메서드
    // ─────────────────────────────────────
    public void changeLoginId(String newLoginId) {
        if (this.loginId.equals(newLoginId)) {
            throw new BusinessException(ErrorCode.SAME_AS_CURRENT_LOGIN_ID) {};
        }
        this.loginId = newLoginId;
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.temporaryPassword = false;     // 변경 후 임시 해제
    }

    public void updateProfile(
            String businessName,
            String managerName,
            String managerEmail,
            String phoneNumber,
            String mobileNumber
    ) {
        this.businessName = businessName;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.phoneNumber = phoneNumber;
        this.mobileNumber = mobileNumber;
    }

    public void suspend() {
        this.status = SellerStatus.SUSPENDED;
    }
}
