package com.team23.management.api.dto.response;

import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import com.team23.management.domain.sku.Sku;

import java.util.List;
import java.util.Map;

public record ProductDetailResponse(
        Long id,
        String name,
        Category category,
        int basePrice,
        String description,
        ProductStatus status,
        Long sellerId,
        List<SkuDetail> skus
) {
    public static ProductDetailResponse from(
            Product product, List<Sku> skus, Map<Long, Integer> stockMap
    ) {
        List<SkuDetail> skuDetails = skus.stream()
                .map(sku -> SkuDetail.from(sku, stockMap.getOrDefault(sku.getId(), 0)))
                .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getBasePrice(),
                product.getDescription(),
                product.getStatus(),
                product.getSellerId(),
                skuDetails
        );
    }

    public record SkuDetail(
            Long id,
            int additionalPrice,
            List<OptionDetail> options,
            int stock
    ) {
        public static SkuDetail from(Sku sku, int stock) {
            List<OptionDetail> options = sku.getOptions().stream()
                    .map(opt -> new OptionDetail(opt.getName(), opt.getValue()))
                    .toList();
            return new SkuDetail(sku.getId(), sku.getAdditionalPrice(), options, stock);
        }
    }

    public record OptionDetail(String name, String value) {}
}
