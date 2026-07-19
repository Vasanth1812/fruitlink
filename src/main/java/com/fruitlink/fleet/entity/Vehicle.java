package com.fruitlink.fleet.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicle")
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String regNo;

    private String type; // e.g. "Truck", "Van", "Bike"

    private Double capacity; // in kg

    // on_route | available | maintenance | idle
    @Column(nullable = false)
    private String status = "available";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_driver_id")
    private Driver assignedDriver;

    private LocalDate lastService;
    
    private LocalDate nextService;
    
    private Double fuelLevel = 100.0; // percentage

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
