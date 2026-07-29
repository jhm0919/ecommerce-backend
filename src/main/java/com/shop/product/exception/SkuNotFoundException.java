package com.shop.product.exception;

import com.shop.global.exception.BusinessException;
import com.shop.global.exception.ErrorCode;

public class SkuNotFoundException extends BusinessException {

    public SkuNotFoundException(Long skuId) {
        super(ErrorCode.SKU_NOT_FOUND, "Sku not found: id=" + skuId);
    }
}
