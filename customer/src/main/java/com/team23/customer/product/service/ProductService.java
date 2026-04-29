package com.team23.customer.product.service;

import com.team23.customer.product.domain.Product;
import com.team23.customer.product.domain.ProductStatus;
import com.team23.customer.product.exception.ProductNotFoundException;
import com.team23.customer.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 사용자에게 노출 가능한 상품 목록을 조회한다.
     * categoryId가 주어지면 해당 카테고리로 필터링.
     */
    @Transactional(readOnly = true)
    public Page<Product> findVisibleProducts(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return productRepository.findVisibleProducts(
                    ProductStatus.DISCONTINUED, pageable);
        }
        return productRepository.findVisibleProductsByCategory(
                categoryId, ProductStatus.DISCONTINUED, pageable);
    }

    /**
     * 상품 상세 조회.
     * DISCONTINUED 상품은 사용자에게 노출하지 않으므로 NotFound로 응답.
     */
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // DISCONTINUED 상품은 사용자에게 "없는 것처럼" 응답
        if (!product.isVisibleToCustomer()) {
            throw new ProductNotFoundException(id);
        }

        return product;
    }
}