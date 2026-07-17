package com.fruitlink.procurement.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.inventory.entity.Sku;
import com.fruitlink.inventory.repository.SkuRepository;
import com.fruitlink.vendor.entity.PurchaseOrder;
import com.fruitlink.vendor.entity.PurchaseOrderItem;
import com.fruitlink.vendor.entity.Vendor;
import com.fruitlink.vendor.repository.PurchaseOrderItemRepository;
import com.fruitlink.vendor.repository.PurchaseOrderRepository;
import com.fruitlink.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final SkuRepository skuRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public void evaluateAndGeneratePO(String skuId) {
        log.info("Evaluating stock level and potential auto-PO generation for SKU: {}", skuId);

        UUID skuUuid = UUID.fromString(skuId);
        Sku sku = skuRepository.findById(skuUuid)
                .orElseThrow(() -> new BusinessException("SKU not found for auto PO generation"));

        // Check if there is already a draft PO for this SKU
        boolean hasDraftPo = purchaseOrderItemRepository.existsBySkuIdAndPurchaseOrderStatus(skuUuid, "draft");
        if (hasDraftPo) {
            log.info("A draft Purchase Order already exists for SKU {}. Skipping auto generation.", sku.getCode());
            return;
        }

        // Find the last vendor we purchased this SKU from
        PurchaseOrderItem lastItem = purchaseOrderItemRepository.findTopBySkuIdOrderByPurchaseOrderCreatedAtDesc(skuUuid);
        Vendor preferredVendor;
        
        if (lastItem != null) {
            preferredVendor = lastItem.getPurchaseOrder().getVendor();
            log.info("Found last vendor {} for SKU {}", preferredVendor.getName(), sku.getCode());
        } else {
            // If no previous purchase, find any active vendor as a fallback, or we can't generate PO
            preferredVendor = vendorRepository.findAll().stream()
                    .filter(Vendor::isActive)
                    .findFirst()
                    .orElse(null);
            
            if (preferredVendor == null) {
                log.warn("No active vendors found to generate PO for SKU {}", sku.getCode());
                return;
            }
            log.info("No previous purchase found for SKU {}. Using first active vendor {}", sku.getCode(), preferredVendor.getName());
        }

        // Generate the new PO
        PurchaseOrder po = new PurchaseOrder();
        po.setVendor(preferredVendor);
        po.setStatus("draft");
        po.setGeneratedBy("system_auto");
        po = purchaseOrderRepository.save(po);

        // Fixed quantity as requested
        BigDecimal orderQuantity = BigDecimal.valueOf(100);
        
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(po);
        item.setSku(sku);
        item.setQuantity(orderQuantity);
        
        // Use last unit cost if available, otherwise current SKU price
        long unitCost = lastItem != null ? lastItem.getUnitCost() : sku.getCurrentPrice();
        item.setUnitCost(unitCost);
        
        purchaseOrderItemRepository.save(item);

        log.info("Successfully generated auto-PO {} for SKU {} with quantity {}", po.getId(), sku.getCode(), orderQuantity);
    }
}
