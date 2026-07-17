package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.GeofenceCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface GeofenceCheckInRepository extends JpaRepository<GeofenceCheckIn, UUID> {
    List<GeofenceCheckIn> findByShopIdAndSalesmanId(UUID shopId, UUID salesmanId);
}
