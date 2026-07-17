package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.ManifestStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ManifestStopRepository extends JpaRepository<ManifestStop, UUID> {
    List<ManifestStop> findByManifestIdOrderBySequenceAsc(UUID manifestId);
    List<ManifestStop> findByOrderId(UUID orderId);
}
