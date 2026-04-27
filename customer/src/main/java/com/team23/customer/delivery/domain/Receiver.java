package com.team23.customer.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receiver {
    @Column(name = "receiver_name", nullable = false)
    private String name;

    @Column(name = "receiver_phone", nullable = false)
    private String phone;

    public Receiver(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}