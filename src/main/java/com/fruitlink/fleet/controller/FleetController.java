package com.fruitlink.fleet.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.fleet.dto.FleetDto.*;
import com.fruitlink.fleet.service.FleetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fleet")
@RequiredArgsConstructor
@Tag(name = "Fleet", description = "Fleet and Driver Management")
public class FleetController {

    private final FleetService fleetService;

    @PostMapping("/drivers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(@Valid @RequestBody CreateDriverRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created", fleetService.createDriver(req)));
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAllDrivers() {
        return ResponseEntity.ok(ApiResponse.success(fleetService.getAllDrivers()));
    }

    @PostMapping("/vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(@Valid @RequestBody CreateVehicleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created", fleetService.createVehicle(req)));
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        return ResponseEntity.ok(ApiResponse.success(fleetService.getAllVehicles()));
    }

    @PostMapping("/vehicles/{id}/assign-driver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> assignDriver(
            @PathVariable String id, @RequestBody AssignDriverRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Driver assigned", fleetService.assignDriverToVehicle(id, req)));
    }
}
