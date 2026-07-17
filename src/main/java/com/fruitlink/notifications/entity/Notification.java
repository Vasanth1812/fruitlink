package com.fruitlink.notifications.entity;

import com.fruitlink.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter @Setter @NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // order_update | payment_reminder | kyc_update | system | return_update
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    private String channel = "in_app"; // in_app | push | sms | whatsapp

    @Column(name = "delivery_status")
    private String deliveryStatus = "pending";

    @Column(name = "target_group")
    private String targetGroup;

    @Column(name = "reference_id", columnDefinition = "uuid")
    private UUID referenceId; // order/invoice/return ID

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
