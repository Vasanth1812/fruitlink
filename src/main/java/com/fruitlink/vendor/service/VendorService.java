package com.fruitlink.vendor.service;

import com.fruitlink.common.BusinessException;
import com.fruitlink.inventory.entity.Sku;
import com.fruitlink.inventory.repository.SkuRepository;
import com.fruitlink.vendor.dto.VendorDto.*;
import com.fruitlink.vendor.entity.PurchaseOrder;
import com.fruitlink.vendor.entity.PurchaseOrderItem;
import com.fruitlink.vendor.entity.Vendor;
import com.fruitlink.vendor.repository.PurchaseOrderItemRepository;
import com.fruitlink.vendor.repository.PurchaseOrderRepository;
import com.fruitlink.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poItemRepository;
    private final SkuRepository skuRepository;

    // ── Vendor CRUD ────────────────────────────────────────

    @Transactional
    public VendorResponse createVendor(CreateVendorRequest req) {
        Vendor v = new Vendor();
        v.setName(req.getName());
        v.setContactPhone(req.getContactPhone());
        return toVendorResponse(vendorRepository.save(v));
    }

    public List<VendorResponse> getAllVendors() {
        return vendorRepository.findAll().stream().map(this::toVendorResponse).toList();
    }

    public VendorResponse getVendor(String id) {
        return toVendorResponse(findVendor(id));
    }

    @Transactional
    public VendorResponse toggleActive(String id) {
        Vendor v = findVendor(id);
        v.setActive(!v.isActive());
        return toVendorResponse(vendorRepository.save(v));
    }

    // ── Purchase Orders ────────────────────────────────────

    @Transactional
    public PoResponse createPo(CreatePoRequest req, UUID createdBy) {
        Vendor vendor = findVendor(req.getVendorId());

        PurchaseOrder po = new PurchaseOrder();
        po.setVendor(vendor);
        po.setGeneratedBy("manual");
        po.setCreatedBy(createdBy);
        po = poRepository.save(po);

        for (PoItemRequest itemReq : req.getItems()) {
            Sku sku = skuRepository.findById(UUID.fromString(itemReq.getSkuId()))
                    .orElseThrow(() -> new BusinessException("SKU not found: " + itemReq.getSkuId()));
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setSku(sku);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitCost(itemReq.getUnitCost() != null ? itemReq.getUnitCost() : 0L);
            poItemRepository.save(item);
        }

        return toPoResponse(po);
    }

    public List<PoResponse> getAllPos() {
        return poRepository.findAll().stream().map(this::toPoResponse).toList();
    }

    public List<PoResponse> getPosByVendor(String vendorId) {
        return poRepository.findByVendorId(UUID.fromString(vendorId))
                .stream().map(this::toPoResponse).toList();
    }

    public PoResponse getPo(String id) {
        return toPoResponse(findPo(id));
    }

    @Transactional
    public PoResponse updatePoStatus(String id, UpdatePoStatusRequest req) {
        PurchaseOrder po = findPo(id);
        po.setStatus(req.getStatus());
        return toPoResponse(poRepository.save(po));
    }

    // ── Mappers ────────────────────────────────────────────

    private Vendor findVendor(String id) {
        return vendorRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Vendor not found"));
    }

    private PurchaseOrder findPo(String id) {
        return poRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Purchase Order not found"));
    }

    private VendorResponse toVendorResponse(Vendor v) {
        VendorResponse r = new VendorResponse();
        r.setId(v.getId().toString());
        r.setName(v.getName());
        r.setContactPhone(v.getContactPhone());
        r.setActive(v.isActive());
        r.setCreatedAt(v.getCreatedAt());
        return r;
    }

    private PoResponse toPoResponse(PurchaseOrder po) {
        PoResponse r = new PoResponse();
        r.setId(po.getId().toString());
        r.setVendorId(po.getVendor().getId().toString());
        r.setVendorName(po.getVendor().getName());
        r.setStatus(po.getStatus());
        r.setGeneratedBy(po.getGeneratedBy());
        r.setCreatedAt(po.getCreatedAt());
        r.setUpdatedAt(po.getUpdatedAt());
        r.setItems(
            poItemRepository.findByPurchaseOrderId(po.getId()).stream().map(item -> {
                PoItemResponse ir = new PoItemResponse();
                ir.setId(item.getId().toString());
                ir.setSkuId(item.getSku().getId().toString());
                ir.setSkuName(item.getSku().getName());
                ir.setQuantity(item.getQuantity());
                ir.setUnitCost(item.getUnitCost());
                return ir;
            }).toList()
        );
        return r;
    }
}
