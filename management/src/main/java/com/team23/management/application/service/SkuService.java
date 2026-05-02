package com.team23.management.application.service;

import com.team23.management.api.dto.response.SkuAddResponse;
import com.team23.management.api.dto.response.SkuUpdateResponse;
import com.team23.management.application.command.SkuAddCommand;
import com.team23.management.application.command.SkuUpdateCommand;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.sku.SkuOptionInput;
import com.team23.management.domain.stock.Stock;
import com.team23.management.exception.ProductNotFoundException;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkuService {

    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final StockRepository stockRepository;

    public SkuAddResponse addSku(SkuAddCommand command) {
        // 1. 기존 Product 조회
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // 2. DELETED 차단
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품에는 SKU 를 추가할 수 없습니다");
        }

        // 3. 권한 검증
        if (!product.getSellerId().equals(command.sellerId())) {
            throw new IllegalArgumentException("본인이 등록한 상품에만 SKU 를 추가할 수 있습니다");
        }

        // 4. 옵션 조합 중복 검증
        List<Sku> existingSkus = skuRepository.findByProductIdWithOptions(command.productId());
        validateOptionCombinationNotDuplicate(existingSkus, command.options());

        // 5. SKU 생성 (도메인 검증 — 옵션 0개, 옵션명 중복 등)
        Sku newSku = Sku.create(
                product.getId(),
                command.options(),
                command.additionalPrice()
        );
        skuRepository.save(newSku);

        // 6. Stock 생성
        Stock stock = Stock.create(newSku.getId(), command.initialStock());
        stockRepository.save(stock);

        return SkuAddResponse.of(newSku.getId(), product.getId());
    }

    private void validateOptionCombinationNotDuplicate(List<Sku> existingSkus, List<SkuOptionInput> newOptions) {
        Set<String> newCombination = toOptionKey(newOptions);

        boolean duplicate = existingSkus.stream()
                .map(sku -> sku.getOptions().stream()
                        .map(opt -> opt.getName() + "=" + opt.getValue())
                        .collect(Collectors.toSet()))
                .anyMatch(existing -> existing.equals(newCombination));

        if (duplicate) {
            throw new IllegalArgumentException("이미 존재하는 옵션 조합입니다.");
        }
    }

    private Set<String> toOptionKey(List<SkuOptionInput> options) {
        return options.stream()
                .map(opt -> opt.name() + "=" + opt.value())
                .collect(Collectors.toSet());
    }

    public SkuUpdateResponse update(SkuUpdateCommand command) {
        // 1. SKU 조회
        Sku sku = skuRepository.findById(command.skuId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "SKU 를 찾을 수 없습니다: " + command.skuId()));

        // 2. URL 의 productId 와 SKU 의 productId 일치 확인
        if (!sku.getProductId().equals(command.productId())) {
            throw new IllegalArgumentException("URL 의 상품 ID 와 SKU 의 상품 ID 가 일치하지 않습니다");
        }

        // 3. Product 조회 (권한 + 상태 검증용)
        Product product = productRepository.findById(sku.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sku.getProductId()));

        // 4. DELETED 차단
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품의 SKU 는 수정할 수 없습니다");
        }

        // 5. 권한 검증
        if (!product.getSellerId().equals(command.sellerId())) {
            throw new IllegalArgumentException("본인 상품의 SKU 만 수정할 수 있습니다");
        }

        // 6. 변경 감지로 자동 UPDATE
        sku.updateAdditionalPrice(command.additionalPrice());

        return SkuUpdateResponse.from(sku);
    }

    public void delete(Long productId, Long skuId, Long sellerId) {
        // 1. SKU 조회
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "SKU 를 찾을 수 없습니다: " + skuId));

        // 2. URL 의 productId 와 SKU 의 productId 일치 검증
        if (!sku.getProductId().equals(productId)) {
            throw new IllegalArgumentException(
                    "URL 의 상품 ID 와 SKU 의 상품 ID 가 일치하지 않습니다");
        }

        // 3. Product 조회
        Product product = productRepository.findById(sku.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(sku.getProductId()));

        // 4. DELETED 차단
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new IllegalStateException(
                    "삭제된 상품의 SKU 는 삭제할 수 없습니다");
        }

        // 5. 권한 검증
        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException(
                    "본인 상품의 SKU 만 삭제할 수 있습니다");
        }

        // 6. 재고 0 검증
        Stock stock = stockRepository.findBySkuId(skuId);
        if (stock.getQuantity() > 0) {
            throw new IllegalStateException(
                    "재고가 있는 SKU 는 삭제할 수 없습니다");
        }

        // 7. 마지막 SKU 차단
        long skuCount = skuRepository.countByProductId(productId);
        if (skuCount <= 1) {
            throw new IllegalStateException(
                    "마지막 SKU 는 삭제할 수 없습니다");
        }

        // 8. Stock 먼저 삭제 (외래키 의존)
        stockRepository.deleteBySkuId(skuId);

        // 9. SKU 삭제 (OptionValue 자동 cascade)
        skuRepository.delete(sku);
    }
}
