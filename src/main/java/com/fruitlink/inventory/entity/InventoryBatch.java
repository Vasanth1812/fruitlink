package com.fruitlink.inventory.entity;

import com.fruitlink.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inventory_batch")
@Getter @Setter @NoArgsConstructor
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "received_weight", nullable = false, precision = 12, scale = 3)
    private BigDecimal receivedWeight;

    @Column(name = "expiry_estimate")
    private LocalDate expiryEstimate; // drives FEFO allocation

    @Column(nullable = false)
    private String status = "available"; // available | depleted | spoiled

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
