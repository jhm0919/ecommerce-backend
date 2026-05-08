package com.team23.customer.order.service;

public interface PaymentService {
    void refund(String OrderNumber, int amount);
}
