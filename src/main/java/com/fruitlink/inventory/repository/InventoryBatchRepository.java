package com.fruitlink.inventory.repository;

import com.fruitlink.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, UUID> {

    // FEFO: order by expiry_estimate ascending (earliest expiry first)
    @Query("SELECT b FROM InventoryBatch b WHERE b.sku.id = :skuId AND b.status = 'available' ORDER BY b.expiryEstimate ASC NULLS LAST")
    List<InventoryBatch> findAvailableBySkuFefo(UUID skuId);

    @Query("SELECT SUM(s.changeQty) FROM StockMovement s WHERE s.batch.sku.id = :skuId AND s.batch.status = 'available'")
    Double sumAvailableStock(UUID skuId);

    List<InventoryBatch> findBySkuId(UUID skuId);
    List<InventoryBatch> findByStatus(String status);
}
