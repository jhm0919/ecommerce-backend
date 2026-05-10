package com.team23.customer.settlement.controller;

import com.team23.customer.settlement.dto.SettlementSummaryResponse;
import com.team23.customer.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/seller/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<SettlementSummaryResponse> search(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(
                settlementService.search(from, to)
        );
    }
}
