package com.team23.customer.seller.service;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;
import com.team23.customer.seller.domain.SellerApplication;
import com.team23.customer.seller.dto.SellerApplicationRequest;
import com.team23.customer.seller.dto.SellerApplicationResponse;
import com.team23.customer.seller.repository.SellerApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerApplicationService {
    private final SellerApplicationRepository sellerApplicationRepository;

    public SellerApplicationResponse apply(SellerApplicationRequest request) {

        // 중복 검증
        if (sellerApplicationRepository
                .existsByBusinessRegistrationNumber(
                        request.businessRegistrationNumber())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_BUSINESS_REGISTRATION_NUMBER) {};
        }

        if (sellerApplicationRepository
                .existsByMailOrderSalesNumber(
                        request.mailOrderSalesNumber())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_MAIL_ORDER_SALES_NUMBER) {};
        }

        SellerApplication app = SellerApplication.apply(
                request.businessName(),
                request.businessRegistrationNumber(),
                request.mailOrderSalesNumber(),
                request.businessType(),
                request.businessCategory(),
                request.managerName(),
                request.managerEmail(),
                request.phoneNumber(),
                request.mobileNumber()
        );

        return SellerApplicationResponse.from(
                sellerApplicationRepository.save(app));
    }
}
