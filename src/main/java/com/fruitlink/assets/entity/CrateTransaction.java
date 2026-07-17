package com.fruitlink.assets.entity;

import com.fruitlink.delivery.entity.ManifestStop;
import com.fruitlink.shops.entity.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crate_transaction")
@Getter @Setter @NoArgsConstructor
public class CrateTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_stop_id")
    private ManifestStop manifestStop;

    @Column(name = "delivered_count")
    private Integer deliveredCount = 0;

    @Column(name = "reclaimed_count")
    private Integer reclaimedCount = 0;

    @Column(name = "deposit_fee_applied")
    private Long depositFeeApplied = 0L;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
