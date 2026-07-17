package com.fruitlink.delivery.entity;

import com.fruitlink.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "delivery_manifest")
@Getter @Setter @NoArgsConstructor
public class DeliveryManifest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Column(name = "vehicle_id", columnDefinition = "uuid")
    private UUID vehicleId;

    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
