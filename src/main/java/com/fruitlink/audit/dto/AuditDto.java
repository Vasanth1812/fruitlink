package com.fruitlink.audit.dto;

import lombok.Data;

import java.time.Instant;

public class AuditDto {

    @Data
    public static class AuditLogResponse {
        private String id;
        private String userId;
        private String userName;
        private String action;
        private String module;
        private String details;
        private Instant timestamp;
    }
}
