package com.team23.management.api;

import com.team23.management.api.dto.SkuAddRequest;
import com.team23.management.api.dto.SkuAddResponse;
import com.team23.management.application.service.SkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/products/{productId}/skus")
public class SkuController {

    private final SkuService skuService;

    @PostMapping
    public ResponseEntity<SkuAddResponse> addSku(
            @PathVariable Long productId,
            @Valid @RequestBody SkuAddRequest request
    ) {
        SkuAddResponse response = skuService.addSku(request.toCommand(productId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
