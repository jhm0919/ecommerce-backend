// ProductNotPurchasableException.java
package com.shop.cart.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class ProductNotPurchasableException extends BusinessException {

    public ProductNotPurchasableException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_PURCHASABLE,
                "Product is not purchasable: id=" + productId);
    }
}
