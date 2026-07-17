package com.fruitlink.delivery.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.delivery.dto.DeliveryDto.*;
import com.fruitlink.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Delivery Manifests, Proof of Delivery and Geofencing")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/manifests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    @Operation(summary = "Generate a daily manifest for a route")
    public ResponseEntity<ApiResponse<ManifestResponse>> generateManifest(
            @Valid @RequestBody GenerateManifestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manifest generated", deliveryService.generateManifest(req)));
    }

    @GetMapping("/manifests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','DRIVER')")
    @Operation(summary = "Get manifests by date")
    public ResponseEntity<ApiResponse<List<ManifestResponse>>> getManifests(
            @RequestParam java.time.LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getManifestsByDate(date)));
    }

    @PatchMapping("/manifests/{manifestId}/stops/{stopId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DRIVER')")
    @Operation(summary = "Update the status of a manifest stop")
    public ResponseEntity<ApiResponse<ManifestStopResponse>> updateStopStatus(
            @PathVariable String stopId,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", deliveryService.updateStopStatus(stopId, status)));
    }

    @PostMapping("/manifests/{manifestId}/stops/{stopId}/pod")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DRIVER')")
    @Operation(summary = "Submit Proof of Delivery for a stop")
    public ResponseEntity<ApiResponse<Void>> submitPoD(
            @PathVariable String stopId,
            @Valid @RequestBody ProofOfDeliveryRequest req) {
        deliveryService.submitProofOfDelivery(stopId, req);
        return ResponseEntity.ok(ApiResponse.success("Proof of delivery submitted successfully", null));
    }

    @PostMapping("/geofence/check-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALESMAN')")
    @Operation(summary = "Check in to a shop geofence")
    public ResponseEntity<ApiResponse<Void>> geofenceCheckIn(
            @Valid @RequestBody GeofenceCheckInRequest req,
            @AuthenticationPrincipal UserDetails user) {
        deliveryService.checkInGeofence(req, user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Geofence check-in recorded", null));
    }
}
