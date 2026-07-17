package com.fruitlink.ledger.entity;

import com.fruitlink.orders.entity.Order;
import com.fruitlink.shops.entity.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
@Getter @Setter @NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // invoice | payment | credit_note | deposit_fee
    @Column(name = "entry_type", nullable = false)
    private String type;

    @Column(nullable = false)
    private Long amount = 0L; // paise, signed (negative = credit)

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter = 0L; // running balance

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
