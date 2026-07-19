package com.fruitlink.crates.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "crate_transaction")
@Data
public class CrateTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // issue | return
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private UUID partyId;

    // shop | vendor | driver
    @Column(nullable = false)
    private String partyType;

    @Column(nullable = false)
    private Integer quantity;
    
    private String referenceId; // orderId or manifestId

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant date;
}
