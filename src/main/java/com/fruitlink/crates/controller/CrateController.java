package com.fruitlink.crates.controller;


import com.fruitlink.common.ApiResponse;
import com.fruitlink.crates.dto.CrateDto.*;
import com.fruitlink.crates.service.CrateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crates")
@RequiredArgsConstructor
@Tag(name = "Crates", description = "Crate issue, return, and balance tracking")
public class CrateController {

    private final CrateService crateService;

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','DRIVER')")
    public ResponseEntity<ApiResponse<CrateTransactionResponse>> recordTransaction(
            @Valid @RequestBody RecordTransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction recorded", crateService.recordTransaction(req)));
    }

    @GetMapping("/transactions/{partyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<CrateTransactionResponse>>> getTransactions(
            @PathVariable String partyId) {
        return ResponseEntity.ok(ApiResponse.success(crateService.getTransactions(partyId)));
    }

    @GetMapping("/balances/{partyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','DRIVER')")
    public ResponseEntity<ApiResponse<CrateBalanceResponse>> getBalance(
            @PathVariable String partyId, @RequestParam String partyType) {
        return ResponseEntity.ok(ApiResponse.success(crateService.getBalance(partyId, partyType)));
    }
}
