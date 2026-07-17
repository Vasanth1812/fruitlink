package com.fruitlink.inventory.service;

import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.inventory.dto.InventoryDto.*;
import com.fruitlink.inventory.entity.*;
import com.fruitlink.inventory.repository.*;
import com.fruitlink.vendor.entity.Vendor;
import com.fruitlink.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import com.fruitlink.procurement.event.StockLowEvent;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final SkuRepository skuRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SpoilageLogRepository spoilageLogRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── SKU ───────────────────────────────────────────────

    @Transactional
    public SkuResponse createSku(CreateSkuRequest req) {
        if (skuRepository.existsByCode(req.getCode()))
            throw new BusinessException("SKU code already exists: " + req.getCode());
        Sku sku = new Sku();
        sku.setCode(req.getCode());
        sku.setName(req.getName());
        sku.setCategory(req.getCategory());
        sku.setHsnCode(req.getHsnCode());
        sku.setUnit(req.getUnit() != null ? req.getUnit() : "kg");
        sku.setCurrentPrice(req.getCurrentPrice() != null ? req.getCurrentPrice() : 0L);
        if (req.getSafetyThreshold() != null) sku.setSafetyThreshold(req.getSafetyThreshold());
        return toSkuResponse(skuRepository.save(sku));
    }

    public List<SkuResponse> getAllSkus() {
        return skuRepository.findAll().stream().map(this::toSkuResponse).toList();
    }

    public List<SkuResponse> getActiveSkus() {
        return skuRepository.findByIsActiveTrue().stream().map(this::toSkuResponse).toList();
    }

    public SkuResponse getSku(String id) {
        return toSkuResponse(findSkuById(id));
    }

    @Transactional
    public SkuResponse updateSku(String id, UpdateSkuRequest req) {
        Sku sku = findSkuById(id);
        if (req.getName() != null) sku.setName(req.getName());
        if (req.getCategory() != null) sku.setCategory(req.getCategory());
        if (req.getHsnCode() != null) sku.setHsnCode(req.getHsnCode());
        if (req.getUnit() != null) sku.setUnit(req.getUnit());
        if (req.getCurrentPrice() != null) sku.setCurrentPrice(req.getCurrentPrice());
        if (req.getIsActive() != null) sku.setActive(req.getIsActive());
        if (req.getSafetyThreshold() != null) sku.setSafetyThreshold(req.getSafetyThreshold());
        return toSkuResponse(skuRepository.save(sku));
    }

    // ── Batches (Inwarding) ────────────────────────────────

    @Transactional
    public BatchResponse inwardBatch(InwardBatchRequest req) {
        Sku sku = findSkuById(req.getSkuId());
        Vendor vendor = null;
        if (req.getVendorId() != null) {
            vendor = vendorRepository.findById(UUID.fromString(req.getVendorId()))
                    .orElseThrow(() -> new BusinessException("Vendor not found"));
        }
        InventoryBatch batch = new InventoryBatch();
        batch.setSku(sku);
        batch.setVendor(vendor);
        batch.setReceivedWeight(req.getReceivedWeight());
        batch.setExpiryEstimate(req.getExpiryEstimate());

        batch = batchRepository.save(batch);

        // Record stock movement
        StockMovement movement = new StockMovement();
        movement.setBatch(batch);
        movement.setChangeQty(req.getReceivedWeight());
        movement.setReason("inward");
        stockMovementRepository.save(movement);

        return toBatchResponse(batch);
    }

    public List<BatchResponse> getBatchesBySku(String skuId) {
        return batchRepository.findBySkuId(UUID.fromString(skuId))
                .stream().map(this::toBatchResponse).toList();
    }

    public List<BatchResponse> getFefoStock(String skuId) {
        return batchRepository.findAvailableBySkuFefo(UUID.fromString(skuId))
                .stream().map(this::toBatchResponse).toList();
    }

    // ── Spoilage ──────────────────────────────────────────

    @Transactional
    public SpoilageResponse logSpoilage(LogSpoilageRequest req, String loggerPhone) {
        InventoryBatch batch = batchRepository.findById(UUID.fromString(req.getBatchId()))
                .orElseThrow(() -> new BusinessException("Batch not found"));

        SpoilageLog log = new SpoilageLog();
        log.setBatch(batch);
        log.setQuantity(req.getQuantity());
        log.setReason(req.getReason());
        userRepository.findByPhone(loggerPhone).ifPresent(log::setLoggedBy);

        // Record negative stock movement
        StockMovement movement = new StockMovement();
        movement.setBatch(batch);
        movement.setChangeQty(req.getQuantity().negate());
        movement.setReason("spoilage");
        stockMovementRepository.save(movement);

        SpoilageLog saved = spoilageLogRepository.save(log);

        // Evaluate stock level for potential auto PO generation
        evaluateStockLevel(batch.getSku().getId().toString());

        SpoilageResponse r = new SpoilageResponse();
        r.setId(saved.getId().toString());
        r.setBatchId(batch.getId().toString());
        r.setQuantity(saved.getQuantity());
        r.setReason(saved.getReason());
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public List<StockMovementResponse> getStockMovements(String batchId) {
        return stockMovementRepository.findByBatchId(UUID.fromString(batchId))
                .stream().map(this::toMovementResponse).toList();
    }

    @Transactional(readOnly = true)
    public void evaluateStockLevel(String skuId) {
        Sku sku = findSkuById(skuId);
        if (sku.getSafetyThreshold() == null) return;
        
        Double currentStock = batchRepository.sumAvailableStock(sku.getId());
        if (currentStock == null) currentStock = 0.0;

        if (currentStock < sku.getSafetyThreshold()) {
            eventPublisher.publishEvent(new StockLowEvent(this, skuId, currentStock));
        }
    }

    // ── Mappers ───────────────────────────────────────────

    private Sku findSkuById(String id) {
        return skuRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("SKU not found"));
    }

    private SkuResponse toSkuResponse(Sku s) {
        SkuResponse r = new SkuResponse();
        r.setId(s.getId().toString());
        r.setCode(s.getCode());
        r.setName(s.getName());
        r.setCategory(s.getCategory());
        r.setHsnCode(s.getHsnCode());
        r.setUnit(s.getUnit());
        r.setCurrentPrice(s.getCurrentPrice());
        r.setActive(s.isActive());
        r.setSafetyThreshold(s.getSafetyThreshold());
        return r;
    }

    private BatchResponse toBatchResponse(InventoryBatch b) {
        BatchResponse r = new BatchResponse();
        r.setId(b.getId().toString());
        r.setSkuId(b.getSku().getId().toString());
        r.setSkuName(b.getSku().getName());
        if (b.getVendor() != null) r.setVendorId(b.getVendor().getId().toString());
        r.setReceivedWeight(b.getReceivedWeight());
        r.setExpiryEstimate(b.getExpiryEstimate());
        r.setStatus(b.getStatus());
        r.setCreatedAt(b.getCreatedAt());
        return r;
    }

    private StockMovementResponse toMovementResponse(StockMovement m) {
        StockMovementResponse r = new StockMovementResponse();
        r.setId(m.getId().toString());
        r.setBatchId(m.getBatch().getId().toString());
        r.setChangeQty(m.getChangeQty());
        r.setReason(m.getReason());
        if (m.getReferenceId() != null) r.setReferenceId(m.getReferenceId().toString());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
