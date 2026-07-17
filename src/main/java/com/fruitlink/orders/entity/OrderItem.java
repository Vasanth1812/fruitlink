package com.fruitlink.orders.entity;

import com.fruitlink.inventory.entity.InventoryBatch;
import com.fruitlink.inventory.entity.Sku;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@Getter @Setter @NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id") // FEFO resolved on confirm
    private InventoryBatch batch;

    @Column(name = "ordered_qty", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price_at_order", nullable = false)
    private Long unitPrice = 0L; // paise

    @Column(name = "discount_pct", precision = 5, scale = 2)
    private BigDecimal discountPct = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L; // paise

    @Column(name = "packed_qty", precision = 12, scale = 3)
    private BigDecimal packedQty;

    @Column(name = "weight_variance_flag")
    private boolean weightVarianceFlag = false;
}
