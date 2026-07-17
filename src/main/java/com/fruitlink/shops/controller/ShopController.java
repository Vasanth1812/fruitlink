package com.fruitlink.shops.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.shops.dto.ShopDto.*;
import com.fruitlink.shops.service.ShopService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.fruitlink.shops.dto.ShopDto.DashboardResponse;
import com.fruitlink.shops.dto.ShopDto.ShopAnalyticsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
@Tag(name = "Shops", description = "Shop management and KYC")
public class ShopController {

    private final ShopService shopService;

    // ── Shop CRUD ──────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<ShopResponse>> create(@RequestBody CreateShopRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shop created", shopService.createShop(req)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<List<ShopResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String salesman,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String route,
            @RequestParam(required = false) String creditStatus) {
        
        List<ShopResponse> shops = shopService.getFilteredShops(search, status, salesman, area, route, creditStatus);
        return ResponseEntity.ok(ApiResponse.success(shops));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(shopService.getDashboard()));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<ShopAnalyticsResponse>> getShopAnalytics(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getShopAnalytics(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<ShopResponse>> getOne(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getShop(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<ShopResponse>> update(
            @PathVariable String id, @RequestBody UpdateShopRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Shop updated", shopService.updateShop(id, req)));
    }

    @PostMapping("/{id}/assign-salesman")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<ShopResponse>> assignSalesman(
            @PathVariable String id, @RequestBody AssignSalesmanRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Salesman assigned", shopService.assignSalesman(id, req)));
    }

    // ── Follow-ups ────────────────────────────────────────

    @PostMapping("/{id}/follow-ups")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> addFollowUp(
            @PathVariable String id, @RequestBody CreateFollowUpRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Follow-up added", shopService.addFollowUp(id, req)));
    }

    @GetMapping("/{id}/follow-ups")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getFollowUps(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getFollowUps(id)));
    }

    @PutMapping("/follow-ups/{followUpId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','SALESMAN')")
    public ResponseEntity<ApiResponse<FollowUpResponse>> updateFollowUp(
            @PathVariable String followUpId, @RequestBody UpdateFollowUpRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Follow-up updated", shopService.updateFollowUp(followUpId, req)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ShopResponse>> changeStatus(
            @PathVariable String id, @RequestBody ChangeStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", shopService.changeStatus(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        shopService.deleteShop(id);
        return ResponseEntity.ok(ApiResponse.success("Shop deleted", null));
    }

    // ── KYC ───────────────────────────────────────────────

    @PostMapping("/{id}/kyc")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> addKyc(
            @PathVariable String id, @RequestBody KycDocumentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded", shopService.addKycDocument(id, req)));
    }

    @GetMapping("/{id}/kyc")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getKyc(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shopService.getKycDocuments(id)));
    }

    @PatchMapping("/kyc/{docId}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> reviewKyc(
            @PathVariable String docId,
            @RequestBody KycReviewRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("KYC reviewed",
                shopService.reviewKycDocument(docId, req, userDetails.getUsername())));
    }
}
