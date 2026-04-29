package com.team23.management.api;

import com.team23.management.api.dto.ProductRegisterRequest;
import com.team23.management.api.dto.ProductRegisterResponse;
import com.team23.management.application.service.ProductRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRegisterService productRegisterService;

    @PostMapping
    public ResponseEntity<ProductRegisterResponse> register(
            @Valid @RequestBody ProductRegisterRequest request
    ) {
        Long productId = productRegisterService.register(request.toCommand());

        ProductRegisterResponse response = new ProductRegisterResponse(
                productId,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
