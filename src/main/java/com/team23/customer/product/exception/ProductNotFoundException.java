package com.team23.customer.product.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: id=" + productId);
    }
}
