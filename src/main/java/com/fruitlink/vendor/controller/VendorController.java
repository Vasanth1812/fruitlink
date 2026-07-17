package com.fruitlink.vendor.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.vendor.dto.VendorDto.*;
import com.fruitlink.vendor.service.VendorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Vendor", description = "Vendor management and Purchase Orders")
public class VendorController {

    private final VendorService vendorService;

    // ── Vendors ────────────────────────────────────────────

    @PostMapping("/vendors")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<VendorResponse>> create(@RequestBody CreateVendorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor created", vendorService.createVendor(req)));
    }

    @GetMapping("/vendors")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getAllVendors()));
    }

    @GetMapping("/vendors/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<VendorResponse>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getVendor(id)));
    }

    @PatchMapping("/vendors/{id}/toggle-active")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VendorResponse>> toggleActive(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Status toggled", vendorService.toggleActive(id)));
    }

    // ── Purchase Orders ────────────────────────────────────

    @PostMapping("/vendors/{vendorId}/po")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<PoResponse>> createPo(
            @PathVariable String vendorId,
            @RequestBody CreatePoRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        req.setVendorId(vendorId);
        // Note: userId from JWT would be resolved here in a full impl
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("PO created", vendorService.createPo(req, null)));
    }

    @GetMapping("/vendors/{vendorId}/po")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<PoResponse>>> getPosByVendor(@PathVariable String vendorId) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getPosByVendor(vendorId)));
    }

    @GetMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<PoResponse>>> getAllPos() {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getAllPos()));
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<PoResponse>> getPo(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getPo(id)));
    }

    @PatchMapping("/purchase-orders/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<PoResponse>> updateStatus(
            @PathVariable String id, @RequestBody UpdatePoStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", vendorService.updatePoStatus(id, req)));
    }
}
