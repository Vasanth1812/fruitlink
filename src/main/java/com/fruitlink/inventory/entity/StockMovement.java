package com.fruitlink.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movement")
@Getter @Setter @NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private InventoryBatch batch;

    @Column(name = "change_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal changeQty; // signed: negative = deduction

    @Column(nullable = false)
    private String reason;

    @Column(name = "reference_id", columnDefinition = "uuid")
    private UUID referenceId; // order_id, po_id, etc.

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
