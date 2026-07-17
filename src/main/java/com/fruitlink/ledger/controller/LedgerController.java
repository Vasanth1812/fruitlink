package com.fruitlink.ledger.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.ledger.dto.LedgerDto.*;
import com.fruitlink.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Invoices, payments and shop ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    // ── Invoices ──────────────────────────────────────────

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generate(
            @RequestBody GenerateInvoiceRequest req,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice generated", ledgerService.generateInvoice(req, user.getUsername())));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(ledgerService.getInvoice(id)));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAll(
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) String status) {
        List<InvoiceResponse> result = shopId != null
                ? ledgerService.getByShop(shopId)
                : (status != null ? ledgerService.getByStatus(status) : ledgerService.getByStatus("unpaid"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Payments ──────────────────────────────────────────

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@RequestBody RecordPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded", ledgerService.recordPayment(req)));
    }

    @GetMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(ledgerService.getPaymentsByInvoice(id)));
    }

    // ── Shop Ledger ───────────────────────────────────────

    @GetMapping("/shops/{shopId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<ShopLedgerResponse>> getShopLedger(@PathVariable String shopId) {
        return ResponseEntity.ok(ApiResponse.success(ledgerService.getShopLedger(shopId)));
    }
}
