package com.team23.customer.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Column(name = "image_url", nullable = false)
    private String url;

    @Column(name = "image_alt_text")
    private String altText;

    public ProductImage(String url, String altText) {
        this.url = url;
        this.altText = altText;
    }
}
