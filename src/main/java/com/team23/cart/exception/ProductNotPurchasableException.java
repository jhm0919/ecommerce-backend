// ProductNotPurchasableException.java
package com.team23.cart.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class ProductNotPurchasableException extends BusinessException {

    public ProductNotPurchasableException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_PURCHASABLE,
                "Product is not purchasable: id=" + productId);
    }
}
