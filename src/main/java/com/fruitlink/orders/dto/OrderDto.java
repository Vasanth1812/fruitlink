package com.fruitlink.orders.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class OrderDto {

    @Data
    public static class OrderItemRequest {
        @jakarta.validation.constraints.NotBlank(message = "SKU ID is required")
        private String skuId;
        @jakarta.validation.constraints.Positive(message = "Quantity must be positive")
        private BigDecimal quantity;
        @jakarta.validation.constraints.PositiveOrZero(message = "Price cannot be negative")
        private Long unitPrice; // paise
        private BigDecimal discountPct;
    }

    @Data
    public static class CreateOrderRequest {
        @jakarta.validation.constraints.NotBlank(message = "Shop ID is required")
        private String shopId;
        private String salesmanId;
        @jakarta.validation.constraints.NotNull(message = "Delivery date is required")
        private LocalDate deliveryDate;
        private String notes;
        @jakarta.validation.constraints.NotEmpty(message = "Items list cannot be empty")
        @jakarta.validation.Valid
        private List<OrderItemRequest> items;
        private String paymentMode; // credit | cash | upi
    }

    @Data
    public static class UpdateStatusRequest {
        @jakarta.validation.constraints.NotBlank(message = "Status is required")
        private String status; // confirmed | packed | shipped | delivered | returned
    }

    @Data
    public static class OrderItemResponse {
        private String id;
        private String skuId;
        private String skuName;
        private String batchId;
        private BigDecimal quantity;
        private Long unitPrice;
        private BigDecimal discountPct;
        private Long totalAmount;
    }

    @Data
    public static class OrderResponse {
        private String id;
        private String shopId;
        private String shopName;
        private String salesmanId;
        private String salesmanName;
        private String status;
        private LocalDate deliveryDate;
        private String notes;
        private Long grandTotal;
        private List<OrderItemResponse> items;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
