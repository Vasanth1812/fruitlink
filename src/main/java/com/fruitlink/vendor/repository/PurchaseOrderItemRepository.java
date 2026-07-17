package com.fruitlink.vendor.repository;

import com.fruitlink.vendor.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findByPurchaseOrderId(UUID poId);

    PurchaseOrderItem findTopBySkuIdOrderByPurchaseOrderCreatedAtDesc(UUID skuId);

    boolean existsBySkuIdAndPurchaseOrderStatus(UUID skuId, String status);
}
