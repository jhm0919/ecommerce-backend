package com.team23.management.application.service;

import com.team23.management.application.command.ProductRegisterCommand;
import com.team23.management.application.command.SkuCommand;
import com.team23.management.domain.product.Product;
import com.team23.management.domain.sku.Sku;
import com.team23.management.domain.stock.Stock;
import com.team23.management.infrastructure.ProductRepository;
import com.team23.management.infrastructure.SkuRepository;
import com.team23.management.infrastructure.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRegisterService {

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
}

