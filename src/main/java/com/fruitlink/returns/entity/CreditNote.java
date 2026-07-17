package com.fruitlink.returns.entity;

import com.fruitlink.ledger.entity.Invoice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_note")
@Getter @Setter @NoArgsConstructor
public class CreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false, unique = true)
    private ReturnRequest returnRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false)
    private Long amount = 0L; // paise

    // pending | issued
    @Column(nullable = false)
    private String status = "pending";

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
