package com.fruitlink.shops.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

import java.util.List;
import java.util.UUID;

public class ShopDto {

    @Data
    public static class CreateShopRequest {
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Contact phone is required")
        private String contactPhone;
        private String address;
        private String gstin;
        @PositiveOrZero(message = "Credit limit cannot be negative")
        private Long creditLimit = 0L;
        private String routeId;
    }

    @Data
    public static class UpdateShopRequest {
        private String name;
        private String contactPhone;
        private String address;
        private String gstin;
        @PositiveOrZero(message = "Credit limit cannot be negative")
        private Long creditLimit;
        private String routeId;
    }

    @Data
    public static class AssignSalesmanRequest {
        private String salesmanId;
    }

    @Data
    public static class ChangeStatusRequest {
        private String status; // pending_kyc | active | inactive | critical_followup
    }

    @Data
    public static class ShopResponse {
        private String id;
        private String name;
        private String contactPhone;
        private String address;
        private String gstin;
        private String status;
        private Long creditLimit;
        private String routeId;
        private String assignedSalesmanId;
        private String assignedSalesmanName;
        private Instant createdAt;
    }

    @Data
    public static class KycDocumentRequest {
        private String documentType;
        private String fileUrl; // S3 key
    }

    @Data
    public static class KycReviewRequest {
        private String reviewStatus; // approved | rejected
    }

    @Data
    public static class KycDocumentResponse {
        private String id;
        private String shopId;
        private String documentType;
        private String fileUrl;
        private String reviewStatus;
        private Instant createdAt;
    }

    @Data
    public static class CreateFollowUpRequest {
        @NotBlank(message = "Reason is required")
        private String reason;
        private String remarks;
        private String assignedTo; // user id
        private Instant nextFollowUp;
    }

    @Data
    public static class UpdateFollowUpRequest {
        private String status;
        private String remarks;
        private Instant nextFollowUp;
    }

    @Data
    public static class FollowUpResponse {
        private String id;
        private String shopId;
        private String assignedTo;
        private String assignedToName;
        private String reason;
        private String remarks;
        private String status;
        private Instant nextFollowUp;
        private Instant createdAt;
    }
    @Data
    public static class DashboardResponse {
        
        @Data
        public static class KpiCards {
            private long totalShops;
            private long activeShops;
            private long inactiveShops;
            private long todaysNewShops;
            private long pendingFollowUps;
            private long creditBlockedShops;
            private long outstandingAmount; // in paise
            private long todaysOrders;
        }

        @Data
        public static class ChartData {
            private String label;
            private long value;
        }

        @Data
        public static class RecentActivity {
            private String id;
            private String title;
            private String type; // new_shop | salesman_assigned | credit_updated | order | payment
            private String timestamp;
        }

        @Data
        public static class TopShop {
            private String shopId;
            private String shopName;
            private long totalRevenue; // in paise
        }

        private KpiCards kpis;
        private List<ChartData> monthlySales;
        private List<TopShop> topCustomers;
        private List<ChartData> areaWiseShops;
        private List<ChartData> outstandingTrend;
        private List<ChartData> newShopGrowth;
        private List<RecentActivity> recentActivities;
        private List<TopShop> top10Shops;
    }

    @Data
    public static class ShopAnalyticsResponse {
        
        @Data
        public static class ChartData {
            private String label;
            private long value;
        }

        private List<ChartData> monthlyOrders;
        private List<ChartData> monthlyRevenue; // in paise
        private List<ChartData> outstandingTrend;
        private long orderFrequencyDays; // average days between orders
        private long averageBasketValue; // in paise
    }
}
