package com.fruitlink.inventory.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.inventory.dto.InventoryDto.*;
import com.fruitlink.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "SKU catalog, batch inwarding, FEFO, spoilage")
public class InventoryController {

    private final InventoryService inventoryService;

    // ── SKUs ──────────────────────────────────────────────

    @PostMapping("/skus")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<SkuResponse>> createSku(@RequestBody CreateSkuRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SKU created", inventoryService.createSku(req)));
    }

    @GetMapping("/skus")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<List<SkuResponse>>> getSkus(
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success(
                activeOnly ? inventoryService.getActiveSkus() : inventoryService.getAllSkus()));
    }

    @GetMapping("/skus/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<SkuResponse>> getSku(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getSku(id)));
    }

    @PutMapping("/skus/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<SkuResponse>> updateSku(
            @PathVariable String id, @RequestBody UpdateSkuRequest req) {
        return ResponseEntity.ok(ApiResponse.success("SKU updated", inventoryService.updateSku(id, req)));
    }

    // ── Batches ───────────────────────────────────────────

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<BatchResponse>> inwardBatch(@RequestBody InwardBatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch inwarded", inventoryService.inwardBatch(req)));
    }

    @GetMapping("/batches/sku/{skuId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getBatchesBySku(@PathVariable String skuId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getBatchesBySku(skuId)));
    }

    @GetMapping("/batches/fefo/{skuId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getFefoStock(@PathVariable String skuId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getFefoStock(skuId)));
    }

    // ── Stock Movements ───────────────────────────────────

    @GetMapping("/movements/{batchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getMovements(@PathVariable String batchId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getStockMovements(batchId)));
    }

    // ── Spoilage ──────────────────────────────────────────

    @PostMapping("/spoilage")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<SpoilageResponse>> logSpoilage(
            @RequestBody LogSpoilageRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Spoilage logged", inventoryService.logSpoilage(req, user.getUsername())));
    }
}
