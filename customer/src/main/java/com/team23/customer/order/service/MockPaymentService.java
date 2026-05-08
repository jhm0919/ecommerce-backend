package com.team23.customer.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockPaymentService implements PaymentService{
    @Override
    public void refund(String orderNumber, int amount) {
        // 실제 PG사 연동 전 Mock 처리
        log.info("[Mock] 환불 처리 - orderNumber: {}, amount: {}", orderNumber,amount);
    }
}

