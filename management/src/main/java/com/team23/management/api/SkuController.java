package com.team23.management.api;

import com.team23.management.api.dto.request.SkuAddRequest;
import com.team23.management.api.dto.request.SkuUpdateRequest;
import com.team23.management.api.dto.response.SkuAddResponse;
import com.team23.management.api.dto.response.SkuUpdateResponse;
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

    @PatchMapping("/{skuId}")
    public ResponseEntity<SkuUpdateResponse> update(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody SkuUpdateRequest request
    ) {
        SkuUpdateResponse response = skuService.update(request.toCommand(productId, skuId));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @RequestParam Long sellerId
    ) {
        skuService.delete(productId, skuId, sellerId);
        return ResponseEntity.noContent().build();
    }
}
