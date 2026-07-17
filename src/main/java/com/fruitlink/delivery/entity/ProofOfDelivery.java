package com.fruitlink.delivery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "proof_of_delivery")
@Getter @Setter @NoArgsConstructor
public class ProofOfDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_stop_id", nullable = false, unique = true)
    private ManifestStop manifestStop;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "crates_delivered")
    private Integer cratesDelivered = 0;

    @Column(name = "crates_reclaimed")
    private Integer cratesReclaimed = 0;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
