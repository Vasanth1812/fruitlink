package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrderId(UUID orderId);
    List<Delivery> findByStatus(String status);

    @Query("SELECT d FROM Delivery d WHERE d.driver.id = :driverId")
    List<Delivery> findByDriverId(UUID driverId);
}
