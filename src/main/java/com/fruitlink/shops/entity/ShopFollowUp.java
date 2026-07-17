package com.fruitlink.shops.entity;

import com.fruitlink.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shop_follow_up")
@Getter @Setter @NoArgsConstructor
public class ShopFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(nullable = false)
    private String reason;

    private String remarks;

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "next_follow_up")
    private Instant nextFollowUp;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
