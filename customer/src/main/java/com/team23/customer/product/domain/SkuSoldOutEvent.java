package com.team23.customer.product.domain;

/**
 * SKU 재고가 0이 되었을 때 발행되는 도메인 이벤트.
 *
 * <p>주문/어드민 수동 감소 등 경로와 무관하게
 * product.decreaseSkuStock()에서 자동 발행된다.
 */
public record SkuSoldOutEvent(
        Long productId,
        String productName,
        Long skuId,
        String skuCode,
        String skuOptionsSnapshot  // "색상=검정,사이즈=S" 형태
) {
    public static SkuSoldOutEvent of(Product product, SKU sku) {
        String optionsSnapshot = sku.getOptions().stream()
                .map(opt -> opt.getOptionName() + "=" + opt.getOptionValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return new SkuSoldOutEvent(
                product.getId(),
                product.getName(),
                sku.getId(),
                sku.getSkuCode(),
                optionsSnapshot
        );
    }
}
