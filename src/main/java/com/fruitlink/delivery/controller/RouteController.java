package com.fruitlink.delivery.controller;

import com.fruitlink.common.ApiResponse;
import com.fruitlink.delivery.entity.Route;
import com.fruitlink.delivery.repository.RouteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "Delivery Route Management")
public class RouteController {

    private final RouteRepository routeRepository;

    @Data
    public static class CreateRouteRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN')")
    @Operation(summary = "Create a new route")
    public ResponseEntity<ApiResponse<Route>> createRoute(@Valid @RequestBody CreateRouteRequest req) {
        Route route = new Route();
        route.setName(req.getName());
        route.setDescription(req.getDescription());
        route = routeRepository.save(route);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route created successfully", route));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SHOP_ADMIN','DRIVER')")
    @Operation(summary = "Get all routes")
    public ResponseEntity<ApiResponse<List<Route>>> getRoutes() {
        return ResponseEntity.ok(ApiResponse.success(routeRepository.findAll()));
    }
}
