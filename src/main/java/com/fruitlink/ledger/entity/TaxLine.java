package com.fruitlink.ledger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_line")
@Getter @Setter @NoArgsConstructor
public class TaxLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "tax_type", nullable = false)
    private String taxType; // CGST | SGST | IGST

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate; // e.g. 9.00 for 9%

    @Column(nullable = false)
    private Long amount = 0L; // paise
}
