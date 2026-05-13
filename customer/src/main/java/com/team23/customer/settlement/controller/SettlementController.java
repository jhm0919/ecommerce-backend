package com.team23.customer.settlement.controller;

import com.team23.customer.global.response.ApiResponse;
import com.team23.customer.settlement.dto.SettlementConfirmRequest;
import com.team23.customer.settlement.dto.SettlementConfirmResponse;
import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import com.team23.customer.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seller/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettlementSummaryResponse>> search(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(
                ApiResponse.createSuccess(settlementService.search(from, to))
        );
    }

    @PatchMapping("/confirm")
    public ResponseEntity<ApiResponse<SettlementConfirmResponse>> confirm(
            @Valid @RequestBody SettlementConfirmRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.createSuccess(settlementService.confirm(request.settledMonth()))
        );
    }
}
