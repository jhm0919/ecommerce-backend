package com.team23.management.application.service;

import com.team23.management.api.dto.ProductDetailResponse;
import com.team23.management.application.command.ProductRegisterCommand;
import com.team23.management.application.command.ProductUpdateCommand;
import com.team23.management.application.command.SkuCommand;
import com.team23.management.domain.product.Category;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.product.ProductStatus;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.stock.Stock;
import com.team23.management.exception.ProductNotFoundException;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SkuRepository skuRepository;
    private final StockRepository stockRepository;

    @Transactional
    public Long register(ProductRegisterCommand command) {
        if (command.skus().isEmpty()) {
            throw new IllegalArgumentException("최소 1개의 SKU가 필요합니다.");
        }
        // === 1. Product 생성 + 저장 ===
        Product product = Product.create(
                command.name(),
                command.category(),
                command.basePrice(),
                command.description(),
                command.sellerId()
        );
        Product savedProduct = productRepository.save(product);

        // === 2. SKU 들 생성 + Stock 함께 ===
        for (SkuCommand skuCmd : command.skus()) {
            Sku sku = Sku.create(
                    savedProduct.getId(),     // ID 참조
                    skuCmd.options(),
                    skuCmd.additionalPrice()
            );
            Sku savedSku = skuRepository.save(sku);

            Stock stock = Stock.create(savedSku.getId(), skuCmd.initialStock());
            stockRepository.save(stock);
        }

        // === 3. productId 반환 ===
        return savedProduct.getId();
    }


    @Transactional(readOnly = true)
    public Page<Product> search(String name, Category category, Pageable pageable) {
        return productRepository.search(name, category, pageable);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStatus() == ProductStatus.DELETED) {
            throw new ProductNotFoundException(productId);
        }

        List<Sku> skus = skuRepository.findByProductIdWithOptions(productId);

        // After — IN 절로 해결
        List<Long> skuIds = skus.stream().map(Sku::getId).toList();
        List<Stock> stocks = stockRepository.findBySkuIdIn(skuIds);
        Map<Long, Integer> stockMap = stocks.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getQuantity));

        return ProductDetailResponse.from(product, skus, stockMap);
    }

    @Transactional
    public Product update(ProductUpdateCommand command) {
        // 1. 조회
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        // 2. 권한 검증
        if (!product.getSellerId().equals(command.sellerId())) {
            throw new IllegalArgumentException("본인이 등록한 상품만 수정할 수 있습니다");
        }

        // 3. 부분 수정 (null 체크)
        if (command.name() != null) {
            product.updateName(command.name());
        }
        if (command.category() != null) {
            product.updateCategory(command.category());
        }
        if (command.basePrice() != null) {
            product.updatePrice(command.basePrice());
        }
        if (command.description() != null) {
            product.updateDescription(command.description());
        }

        // 4. 변경 감지로 자동 UPDATE
        return product;
    }
}

