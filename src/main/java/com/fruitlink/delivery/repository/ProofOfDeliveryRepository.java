package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.ProofOfDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, UUID> {
    Optional<ProofOfDelivery> findByManifestStopId(UUID manifestStopId);
}
