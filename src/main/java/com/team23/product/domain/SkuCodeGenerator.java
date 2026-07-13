package com.team23.product.domain;

/**
 * SKU 코드 자동 생성기.
 *
 * <p>형식: {@code SKU-{productId}-{순번}}
 * <p>예: {@code SKU-1-001}, {@code SKU-1-002}, {@code SKU-2-001}
 */
public final class SkuCodeGenerator {

    private SkuCodeGenerator() {
        // utility class
    }

    /**
     * SKU 코드 생성.
     *
     * @param productId Product의 ID
     * @param sequence  Product 내 순번 (1부터)
     * @return "SKU-{productId}-{순번}" 형식
     */
    public static String generate(Long productId, int sequence) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (sequence < 1) {
            throw new IllegalArgumentException("sequence must be >= 1: " + sequence);
        }
        return String.format("SKU-%d-%03d", productId, sequence);
    }
}
