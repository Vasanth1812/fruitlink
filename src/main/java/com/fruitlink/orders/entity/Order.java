package com.fruitlink.orders.entity;

import com.fruitlink.auth.entity.User;
import com.fruitlink.shops.entity.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesman_id")
    private User salesman;

    // pending | confirmed | packed | dispatched | delivered | cancelled
    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    private String notes;

    @Column(name = "payment_mode", nullable = false)
    private String paymentMode = "credit";

    @Column(nullable = false)
    private String source = "salesman";

    @Column(name = "total_value", nullable = false)
    private Long totalValue = 0L;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
