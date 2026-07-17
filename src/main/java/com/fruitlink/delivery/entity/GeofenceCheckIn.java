package com.fruitlink.delivery.entity;

import com.fruitlink.auth.entity.User;
import com.fruitlink.shops.entity.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "geofence_check_in")
@Getter @Setter @NoArgsConstructor
public class GeofenceCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salesman_id", nullable = false)
    private User salesman;

    private String coordinates;

    @Column(name = "distance_from_shop_m")
    private Integer distanceFromShopM;

    private String result;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;
}
