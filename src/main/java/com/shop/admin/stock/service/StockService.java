package com.shop.admin.stock.service;

import com.shop.admin.stock.domain.StockType;
import com.shop.admin.stock.domain.StockHistory;
import com.shop.admin.stock.repository.StockHistoryRepository;
import com.shop.product.domain.Product;
import com.shop.product.domain.Sku;
import com.shop.product.domain.StockChangedEvent;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockHistoryRepository stockHistoryRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 재고를 증가시킨다.
     */
    @Transactional
    public void increaseStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Sku sku = product.findSkuById(skuId).orElseThrow();
        int stockBefore = sku.getStock();

        product.increaseSkuStock(skuId, quantity);

        eventPublisher.publishEvent(StockChangedEvent.of(
                product, sku,
                StockType.ADMIN_INCREASE,
                quantity,
                stockBefore,
                sku.getStock(),
                null  // orderId 없음
        ));
    }

    /**
     * 재고를 감소시킨다.
     */
    @Transactional
    public void decreaseStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Sku sku = product.findSkuById(skuId).orElseThrow();
        int stockBefore = sku.getStock();

        product.decreaseSkuStock(skuId, quantity);

        // 이력 이벤트
        eventPublisher.publishEvent(StockChangedEvent.of(
                product, sku,
                StockType.ADMIN_DECREASE,
                quantity,
                stockBefore,
                sku.getStock(),
                null
        ));
    }

    @Transactional(readOnly = true)
    public Page<StockHistory> findHistories(
            Long productId,
            Long skuId,
            StockType changeType,
            Pageable pageable
    ) {
        return stockHistoryRepository.findHistories(
                productId, skuId, changeType, pageable
        );
    }
}
