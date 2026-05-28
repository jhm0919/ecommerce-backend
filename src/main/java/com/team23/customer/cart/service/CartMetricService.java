package com.team23.customer.cart.service;

import com.team23.customer.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartMetricService {

    private final CartRepository cartRepository;

    public long getNonEmptyCartCount() {
        return cartRepository.countNonEmptyCarts();
    }
}
