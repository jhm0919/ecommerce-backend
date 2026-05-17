// ProductNotPurchasableException.java
package com.team23.customer.cart.exception;

import com.team23.customer.member.exception.BusinessException;
import com.team23.customer.member.exception.ErrorCode;

public class ProductNotPurchasableException extends BusinessException {

    public ProductNotPurchasableException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_PURCHASABLE,
                "Product is not purchasable: id=" + productId);
    }
}
