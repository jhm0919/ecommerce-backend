package com.team23.product.domain;

/**
 * 상품의 판매 상태.
 *
 * <p>상태 의미:
 * <ul>
 *   <li>{@link #ACTIVE} — 정상 판매 중 (재고 있음)</li>
 *   <li>{@link #SOLD_OUT} — 품절 (재고 0이지만 화면에는 노출, "품절" 표시)</li>
 *   <li>{@link #DISCONTINUED} — 단종 (영구 판매 중지, Soft Delete 역할)</li>
 * </ul>
 *
 * <p>상태 전이:
 * <pre>
 *   ACTIVE ←──→ SOLD_OUT
 *      │           │
 *      └───────────┴──→ DISCONTINUED (영구)
 * </pre>
 */
public enum ProductStatus {
    ACTIVE,
    SOLD_OUT,
    DISCONTINUED;

    /**
     * 사용자에게 노출 가능한 상태인지 확인.
     * DISCONTINUED는 사용자 화면에 노출하지 않는다.
     */
    public boolean isVisibleToCustomer() {
        return this != DISCONTINUED;
    }

    /**
     * 구매 가능한 상태인지 확인.
     * ACTIVE만 구매 가능 (SOLD_OUT은 노출되지만 구매 불가).
     */
    public boolean isPurchasable() {
        return this == ACTIVE;
    }
}
