package com.fruitlink.notifications.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    @Data
    public static class SendNotificationRequest {
        @jakarta.validation.constraints.NotBlank(message = "User ID is required")
        private String userId;
        @jakarta.validation.constraints.NotBlank(message = "Type is required")
        private String type;
        @jakarta.validation.constraints.NotBlank(message = "Title is required")
        private String title;
        private String body;
        private String referenceId;
    }

    @Data
    public static class NotificationResponse {
        private String id;
        private String type;
        private String title;
        private String body;
        private boolean isRead;
        private String referenceId;
        private Instant createdAt;
    }

    @Data
    public static class UnreadCountResponse {
        private long unreadCount;
    }
}
