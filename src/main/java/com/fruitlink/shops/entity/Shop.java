package com.fruitlink.shops.entity;

import com.fruitlink.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shop")
@Getter @Setter @NoArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    private String address;

    private String gstin;

    @Column(nullable = false)
    private String status = "pending_kyc"; // pending_kyc | active | inactive | critical_followup

    @Column(name = "credit_limit", nullable = false)
    private Long creditLimit = 0L; // paise

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_salesman_id")
    private User assignedSalesman;

    @Column(name = "route_id", columnDefinition = "uuid")
    private UUID routeId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
