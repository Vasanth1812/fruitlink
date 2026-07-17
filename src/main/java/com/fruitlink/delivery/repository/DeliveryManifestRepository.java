package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.DeliveryManifest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

public interface DeliveryManifestRepository extends JpaRepository<DeliveryManifest, UUID> {
    List<DeliveryManifest> findByDispatchDate(LocalDate dispatchDate);
    List<DeliveryManifest> findByDriverIdAndDispatchDate(UUID driverId, LocalDate dispatchDate);
}
