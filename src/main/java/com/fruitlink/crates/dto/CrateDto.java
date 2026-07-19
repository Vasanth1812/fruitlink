package com.fruitlink.crates.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.Instant;

public class CrateDto {

    @Data
    public static class RecordTransactionRequest {
        @NotBlank(message = "Type is required (issue | return)")
        private String type;
        
        @NotBlank(message = "Party ID is required")
        private String partyId;
        
        @NotBlank(message = "Party Type is required (shop | vendor | driver)")
        private String partyType;
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        private String referenceId;
    }

    @Data
    public static class CrateTransactionResponse {
        private String id;
        private String type;
        private String partyId;
        private String partyType;
        private Integer quantity;
        private String referenceId;
        private Instant date;
    }

    @Data
    public static class CrateBalanceResponse {
        private String partyId;
        private String partyType;
        private Integer currentBalance;
    }
}
