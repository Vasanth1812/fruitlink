package com.fruitlink.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sku")
@Getter @Setter @NoArgsConstructor
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(name = "hsn_code")
    private String hsnCode;

    @Column(nullable = false)
    private String unit = "kg";

    @Column(name = "current_price", nullable = false)
    private Long currentPrice = 0L; // paise per unit

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "safety_threshold")
    private Double safetyThreshold = 10.0; // Default safety threshold

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
