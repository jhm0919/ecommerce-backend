package com.shop.product.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: id=" + productId);
    }
}
