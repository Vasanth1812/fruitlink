package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, UUID> {
    List<DeliveryEvent> findByDeliveryIdOrderByCreatedAtAsc(UUID deliveryId);
}
