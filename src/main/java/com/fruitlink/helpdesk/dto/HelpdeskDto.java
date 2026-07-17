package com.fruitlink.helpdesk.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

public class HelpdeskDto {

    @Data
    public static class CreateTicketRequest {
        private String shopId;
        @jakarta.validation.constraints.NotBlank(message = "Subject is required")
        private String subject;
        private String description;
        private String priority = "medium"; // low | medium | high | critical
    }

    @Data
    public static class UpdateTicketRequest {
        private String status;   // open | in_progress | resolved | closed
        private String priority;
        private String assignedToId;
    }

    @Data
    public static class AddMessageRequest {
        @jakarta.validation.constraints.NotBlank(message = "Message cannot be empty")
        private String message;
    }

    @Data
    public static class MessageResponse {
        private String id;
        private String senderId;
        private String senderName;
        private String message;
        private Instant createdAt;
    }

    @Data
    public static class TicketResponse {
        private String id;
        private String subject;
        private String description;
        private String status;
        private String priority;
        private String raisedById;
        private String raisedByName;
        private String shopId;
        private String shopName;
        private String assignedToId;
        private String assignedToName;
        private List<MessageResponse> messages;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
