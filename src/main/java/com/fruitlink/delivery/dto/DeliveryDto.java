package com.fruitlink.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class DeliveryDto {

    @Data
    public static class RouteRequest {
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
    }

    @Data
    public static class RouteResponse {
        private String id;
        private String name;
        private String description;
    }

    @Data
    public static class GenerateManifestRequest {
        @NotBlank(message = "Route ID is required")
        private String routeId;
        
        @NotBlank(message = "Driver ID is required")
        private String driverId;
        
        private String vehicleId;
        
        @NotNull(message = "Dispatch date is required")
        private LocalDate dispatchDate;
    }

    @Data
    public static class ManifestResponse {
        private String id;
        private String routeId;
        private String driverId;
        private LocalDate dispatchDate;
        private String status;
        private List<ManifestStopResponse> stops;
    }

    @Data
    public static class ManifestStopResponse {
        private String id;
        private String shopId;
        private String shopName;
        private String orderId;
        private Integer sequence;
        private String status;
    }

    @Data
    public static class GeofenceCheckInRequest {
        @NotBlank(message = "Shop ID is required")
        private String shopId;
        
        @NotBlank(message = "Coordinates (lat,lng) are required")
        private String coordinates;
    }

    @Data
    public static class ProofOfDeliveryRequest {
        private String confirmationCode;
        
        private String photoUrl;
        
        @PositiveOrZero(message = "Delivered crates cannot be negative")
        private Integer cratesDelivered = 0;
        
        @PositiveOrZero(message = "Reclaimed crates cannot be negative")
        private Integer cratesReclaimed = 0;
    }
}
