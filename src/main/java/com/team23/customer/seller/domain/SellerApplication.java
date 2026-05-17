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
@Table(name = "seller_applications", indexes = {
        @Index(name = "idx_sa_brn", columnList = "business_registration_number", unique = true),
        @Index(name = "idx_sa_mosn", columnList = "mail_order_sales_number", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SellerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;           // 상호명

    @Column(name = "business_registration_number",
            nullable = false, unique = true, length = 10)
    private String businessRegistrationNumber;  // 사업자등록번호

    @Column(name = "mail_order_sales_number",
            nullable = false, unique = true, length = 50)
    private String mailOrderSalesNumber;   // 통신판매업신고번호

    @Column(name = "business_type", nullable = false, length = 100)
    private String businessType;           // 업태

    @Column(name = "business_category", nullable = false, length = 100)
    private String businessCategory;       // 업종

    @Column(name = "manager_name", nullable = false, length = 50)
    private String managerName;            // 담당자명

    @Column(name = "manager_email", nullable = false, length = 100)
    private String managerEmail;           // 담당자 이메일

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;            // 유선전화번호

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;           // 휴대전화번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SellerApplication apply(
            String businessName,
            String businessRegistrationNumber,
            String mailOrderSalesNumber,
            String businessType,
            String businessCategory,
            String managerName,
            String managerEmail,
            String phoneNumber,
            String mobileNumber
    ) {
        SellerApplication app = new SellerApplication();
        app.businessName = businessName;
        app.businessRegistrationNumber = businessRegistrationNumber;
        app.mailOrderSalesNumber = mailOrderSalesNumber;
        app.businessType = businessType;
        app.businessCategory = businessCategory;
        app.managerName = managerName;
        app.managerEmail = managerEmail;
        app.phoneNumber = phoneNumber;
        app.mobileNumber = mobileNumber;
        app.status = ApplicationStatus.PENDING;
        return app;
    }
}
