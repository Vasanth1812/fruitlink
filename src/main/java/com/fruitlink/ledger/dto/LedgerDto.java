package com.fruitlink.ledger.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class LedgerDto {

    // ── Invoice ──────────────────────────────────────────

    @Data
    public static class GenerateInvoiceRequest {
        @jakarta.validation.constraints.NotBlank(message = "Order ID is required")
        private String orderId;
        private LocalDate dueDate;
        private List<TaxLineRequest> taxLines;
    }

    @Data
    public static class TaxLineRequest {
        @jakarta.validation.constraints.NotBlank(message = "Tax type is required")
        private String taxType; // CGST | SGST | IGST
        @jakarta.validation.constraints.PositiveOrZero(message = "Rate cannot be negative")
        private BigDecimal rate;
        @jakarta.validation.constraints.PositiveOrZero(message = "Amount cannot be negative")
        private Long amount; // paise
    }

    @Data
    public static class TaxLineResponse {
        private String id;
        private String taxType;
        private BigDecimal rate;
        private Long amount;
    }

    @Data
    public static class InvoiceResponse {
        private String id;
        private String invoiceNumber;
        private String orderId;
        private String shopId;
        private String shopName;
        private Long subtotal;
        private Long taxAmount;
        private Long total;
        private Long amountPaid;
        private Long amountDue;
        private String status;
        private LocalDate dueDate;
        private List<TaxLineResponse> taxLines;
        private Instant createdAt;
    }

    // ── Payment ──────────────────────────────────────────

    @Data
    public static class RecordPaymentRequest {
        @jakarta.validation.constraints.NotBlank(message = "Invoice ID is required")
        private String invoiceId;
        @jakarta.validation.constraints.Positive(message = "Amount must be positive")
        private Long amount; // paise
        @jakarta.validation.constraints.NotBlank(message = "Method is required")
        private String method; // cash | upi | credit
        private String reference;
    }

    @Data
    public static class PaymentResponse {
        private String id;
        private String invoiceId;
        private Long amount;
        private String method;
        private String reference;
        private Instant paidAt;
    }

    // ── Ledger ───────────────────────────────────────────

    @Data
    public static class LedgerEntryResponse {
        private String id;
        private String type;
        private Long amount;
        private Long balanceAfter;
        private String notes;
        private Instant createdAt;
    }

    @Data
    public static class ShopLedgerResponse {
        private String shopId;
        private String shopName;
        private Long currentBalance;
        private List<LedgerEntryResponse> entries;
    }
}
