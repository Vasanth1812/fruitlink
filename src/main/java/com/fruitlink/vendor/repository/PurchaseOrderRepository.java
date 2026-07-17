package com.fruitlink.vendor.repository;

import com.fruitlink.vendor.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByVendorId(UUID vendorId);
    List<PurchaseOrder> findByStatus(String status);
}
