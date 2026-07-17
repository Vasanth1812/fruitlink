package com.fruitlink.ledger.entity;

import com.fruitlink.orders.entity.Order;
import com.fruitlink.shops.entity.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter @Setter @NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long subtotal = 0L; // paise

    @Column(name = "tax_amount", nullable = false)
    private Long taxAmount = 0L; // paise

    @Column(nullable = false)
    private Long total = 0L; // paise

    private String gstin;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "hsn_summary", columnDefinition = "jsonb")
    private String hsnSummary;

    private Long cgst = 0L;
    private Long sgst = 0L;
    private Long igst = 0L;

    // unpaid | paid | partial | overdue
    @Column(nullable = false)
    private String status = "unpaid";

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
