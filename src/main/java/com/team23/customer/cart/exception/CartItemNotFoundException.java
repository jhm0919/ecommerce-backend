// CartItemNotFoundException.java
package com.team23.customer.cart.exception;

import com.team23.global.exception.BusinessException;
import com.team23.global.exception.ErrorCode;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException(Long itemId) {
        super(ErrorCode.CART_ITEM_NOT_FOUND, "Cart item not found: id=" + itemId);
    }
}