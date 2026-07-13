package com.shop.product.dto.response;

import com.shop.category.dto.CategoryResponse;
import com.shop.product.domain.Product;
import com.shop.product.domain.ProductStatus;
import com.shop.product.domain.Sku;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 조회 응답.
 * 상세 페이지에 필요한 모든 정보 포함.
 * 재고는 모든 SKU 합계로 표시되며, SKU 목록도 함께 반환된다.
 */
public record ProductDetailResponse(
        Long id,
        String name,
        BigDecimal price,
//        String currency,
        String description,
        String mainImageUrl,
        CategoryResponse category,
        ProductStatus status,
        boolean inStock,
        int totalStock,      // ★ SKU 합계 재고
        List<SkuInfo> skus,  // ★ SKU 목록 (옵션 선택용)
        LocalDateTime createdAt
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
//                product.getPrice().getCurrency(),
                product.getDescription(),
                product.getMainImageUrl(),
                CategoryResponse.from(product.getCategory()),
                product.getStatus(),
                product.isInStock(),
                product.getTotalSkuStock(),         // ★ SKU 합계
                product.getSkuses().stream()
                        .map(SkuInfo::from)
                        .toList(),                  // ★ SKU 목록
                product.getCreatedAt()
        );
    }

    /**
     * SKU 정보 (사용자가 옵션 선택 시 사용).
     */
    public record SkuInfo(
            Long skuId,
            String skuCode,
            List<OptionInfo> options,
            int stock,
            boolean inStock
    ) {
        public static SkuInfo from(Sku sku) {
            return new SkuInfo(
                    sku.getId(),
                    sku.getSkuCode(),
                    sku.getOptions().stream()
                            .map(opt -> new OptionInfo(opt.getOptionName(), opt.getOptionValue()))
                            .toList(),
                    sku.getStock(),
                    sku.isInStock()
            );
        }
    }

    public record OptionInfo(String name, String value) {}
}
