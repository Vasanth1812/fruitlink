package com.fruitlink.shops.repository;

import com.fruitlink.shops.entity.ShopFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ShopFollowUpRepository extends JpaRepository<ShopFollowUp, UUID> {
    List<ShopFollowUp> findByShopIdOrderByCreatedAtDesc(UUID shopId);
    List<ShopFollowUp> findByStatus(String status);
}
