package com.fruitlink.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class InventoryDto {

    @Data
    public static class CreateSkuRequest {
        @jakarta.validation.constraints.NotBlank(message = "Code is required")
        private String code;
        @jakarta.validation.constraints.NotBlank(message = "Name is required")
        private String name;
        private String category;
        private String hsnCode;
        private String unit = "kg";
        @jakarta.validation.constraints.PositiveOrZero(message = "Price cannot be negative")
        @jakarta.validation.constraints.PositiveOrZero(message = "Price cannot be negative")
        private Long currentPrice = 0L;
        @jakarta.validation.constraints.PositiveOrZero(message = "Safety threshold cannot be negative")
        private Double safetyThreshold = 10.0;
    }

    @Data
    public static class UpdateSkuRequest {
        private String name;
        private String category;
        private String hsnCode;
        private String unit;
        @jakarta.validation.constraints.PositiveOrZero(message = "Price cannot be negative")
        private Long currentPrice;
        private Boolean isActive;
        @jakarta.validation.constraints.PositiveOrZero(message = "Safety threshold cannot be negative")
        private Double safetyThreshold;
    }

    @Data
    public static class SkuResponse {
        private String id;
        private String code;
        private String name;
        private String category;
        private String hsnCode;
        private String unit;
        private Long currentPrice;
        private boolean isActive;
        private Double safetyThreshold;
    }

    @Data
    public static class InwardBatchRequest {
        @jakarta.validation.constraints.NotBlank(message = "SKU ID is required")
        private String skuId;
        private String vendorId;
        @jakarta.validation.constraints.Positive(message = "Weight must be positive")
        private BigDecimal receivedWeight;
        @jakarta.validation.constraints.NotNull(message = "Expiry estimate is required")
        private LocalDate expiryEstimate;
    }

    @Data
    public static class BatchResponse {
        private String id;
        private String skuId;
        private String skuName;
        private String vendorId;
        private BigDecimal receivedWeight;
        private LocalDate expiryEstimate;
        private String status;
        private Instant createdAt;
    }

    @Data
    public static class LogSpoilageRequest {
        @jakarta.validation.constraints.NotBlank(message = "Batch ID is required")
        private String batchId;
        @jakarta.validation.constraints.Positive(message = "Quantity must be positive")
        private BigDecimal quantity;
        private String reason;
    }

    @Data
    public static class SpoilageResponse {
        private String id;
        private String batchId;
        private BigDecimal quantity;
        private String reason;
        private Instant createdAt;
    }

    @Data
    public static class StockMovementResponse {
        private String id;
        private String batchId;
        private BigDecimal changeQty;
        private String reason;
        private String referenceId;
        private Instant createdAt;
    }
}
