package com.fruitlink.crates.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "crate_balance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"partyId", "partyType"})
})
@Data
public class CrateBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID partyId;

    // shop | vendor | driver
    @Column(nullable = false)
    private String partyType;

    @Column(nullable = false)
    private Integer currentBalance = 0;
}
