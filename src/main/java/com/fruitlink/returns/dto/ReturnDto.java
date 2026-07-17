package com.fruitlink.returns.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ReturnDto {

    @Data
    public static class ReturnItemRequest {
        @jakarta.validation.constraints.NotBlank(message = "SKU ID is required")
        private String skuId;
        @jakarta.validation.constraints.Positive(message = "Quantity must be positive")
        private BigDecimal quantity;
        private String reason;
    }

    @Data
    public static class CreateReturnRequest {
        @jakarta.validation.constraints.NotBlank(message = "Order ID is required")
        private String orderId;
        @jakarta.validation.constraints.NotBlank(message = "Reason is required")
        private String reason;
        @jakarta.validation.constraints.NotEmpty(message = "Items cannot be empty")
        @jakarta.validation.Valid
        private List<ReturnItemRequest> items;
    }

    @Data
    public static class ReviewReturnRequest {
        @jakarta.validation.constraints.NotBlank(message = "Status is required")
        private String status; // approved | rejected
    }

    @Data
    public static class IssueCreditNoteRequest {
        @jakarta.validation.constraints.NotBlank(message = "Return ID is required")
        private String returnId;
        @jakarta.validation.constraints.NotBlank(message = "Invoice ID is required")
        private String invoiceId;
        @jakarta.validation.constraints.Positive(message = "Amount must be positive")
        private Long amount; // paise
    }

    @Data
    public static class ReturnItemResponse {
        private String id;
        private String skuId;
        private String skuName;
        private BigDecimal quantity;
        private String reason;
    }

    @Data
    public static class ReturnResponse {
        private String id;
        private String orderId;
        private String shopId;
        private String shopName;
        private String requestedById;
        private String reason;
        private String status;
        private List<ReturnItemResponse> items;
        private CreditNoteResponse creditNote;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    public static class CreditNoteResponse {
        private String id;
        private String returnId;
        private String invoiceId;
        private Long amount;
        private String status;
        private Instant createdAt;
    }
}
