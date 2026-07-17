package com.fruitlink.vendor.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class VendorDto {

    // ── Vendor ──────────────────────────────────────────

    @Data
    public static class CreateVendorRequest {
        private String name;
        private String contactPhone;
    }

    @Data
    public static class VendorResponse {
        private String id;
        private String name;
        private String contactPhone;
        private boolean isActive;
        private Instant createdAt;
    }

    // ── Purchase Order ───────────────────────────────────

    @Data
    public static class CreatePoRequest {
        private String vendorId;
        private List<PoItemRequest> items;
    }

    @Data
    public static class PoItemRequest {
        private String skuId;
        private BigDecimal quantity;
        private Long unitCost; // paise
    }

    @Data
    public static class UpdatePoStatusRequest {
        private String status; // draft | sent | confirmed | received
    }

    @Data
    public static class PoItemResponse {
        private String id;
        private String skuId;
        private String skuName;
        private BigDecimal quantity;
        private Long unitCost;
    }

    @Data
    public static class PoResponse {
        private String id;
        private String vendorId;
        private String vendorName;
        private String status;
        private String generatedBy;
        private List<PoItemResponse> items;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
