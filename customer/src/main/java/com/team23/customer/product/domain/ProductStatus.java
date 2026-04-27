package com.team23.customer.product.domain;

public enum ProductStatus {
    DRAFT,       // 작성 중 (등록 전)
    ACTIVE,      // 판매 중
    SOLD_OUT,    // 일시 품절 (재입고 예정)
    DISCONTINUED // 단종
}
