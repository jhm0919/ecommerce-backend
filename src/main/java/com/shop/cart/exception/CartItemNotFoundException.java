// CartItemNotFoundException.java
package com.shop.cart.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException(Long itemId) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found: id=" + itemId);
    }
}