package com.shop.admin.stock.service;

import com.shop.product.domain.Product;
import com.shop.product.exception.ProductNotFoundException;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;

    /**
     * 재고를 증가시킨다.
     */
    @Transactional
    public void increaseStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product.increaseSkuStock(skuId, quantity);
    }

    /**
     * 재고를 감소시킨다.
     */
    @Transactional
    public void decreaseStock(Long productId, Long skuId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product.decreaseSkuStock(skuId, quantity);
    }
}
