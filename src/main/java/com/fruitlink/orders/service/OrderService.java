package com.fruitlink.orders.service;

import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.inventory.entity.InventoryBatch;
import com.fruitlink.inventory.entity.Sku;
import com.fruitlink.inventory.entity.StockMovement;
import com.fruitlink.inventory.repository.InventoryBatchRepository;
import com.fruitlink.inventory.repository.SkuRepository;
import com.fruitlink.inventory.repository.StockMovementRepository;
import com.fruitlink.orders.dto.OrderDto.*;
import com.fruitlink.orders.entity.Order;
import com.fruitlink.orders.entity.OrderItem;
import com.fruitlink.orders.repository.OrderItemRepository;
import com.fruitlink.orders.repository.OrderRepository;
import com.fruitlink.shops.entity.Shop;
import com.fruitlink.shops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SkuRepository skuRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final com.fruitlink.ledger.repository.LedgerEntryRepository ledgerEntryRepository;
    private final com.fruitlink.audit.service.AuditService auditService;
    private final com.fruitlink.inventory.service.InventoryService inventoryService;

    // ── Create Order (draft) ───────────────────────────────

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        Shop shop = shopRepository.findById(UUID.fromString(req.getShopId()))
                .orElseThrow(() -> new BusinessException("Shop not found"));

        Order order = new Order();
        order.setShop(shop);
        order.setDeliveryDate(req.getDeliveryDate());
        order.setNotes(req.getNotes());

        if (req.getSalesmanId() != null) {
            userRepository.findById(UUID.fromString(req.getSalesmanId()))
                    .ifPresent(order::setSalesman);
        }

        if (req.getPaymentMode() != null) {
            order.setPaymentMode(req.getPaymentMode());
        }

        order = orderRepository.save(order);

        for (OrderItemRequest itemReq : req.getItems()) {
            Sku sku = skuRepository.findById(UUID.fromString(itemReq.getSkuId()))
                    .orElseThrow(() -> new BusinessException("SKU not found: " + itemReq.getSkuId()));

            long unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : sku.getCurrentPrice();
            BigDecimal disc = itemReq.getDiscountPct() != null ? itemReq.getDiscountPct() : BigDecimal.ZERO;
            long total = calcTotal(unitPrice, itemReq.getQuantity(), disc);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setSku(sku);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setDiscountPct(disc);
            item.setTotalAmount(total);
            orderItemRepository.save(item);
        }

        return toResponse(order);
    }

    // ── Confirm Order → deduct stock via FEFO ──────────────

    @Transactional
    public OrderResponse confirmOrder(String orderId, String actorPhone) {
        Order order = findOrder(orderId);
        if (!"pending".equals(order.getStatus()))
            throw new BusinessException("Only pending orders can be confirmed");

        // Credit Limit Check
        Long creditLimit = order.getShop().getCreditLimit();
        if (creditLimit != null) {
            long currentBalance = ledgerEntryRepository.findTopByShopIdOrderByCreatedAtDesc(order.getShop().getId())
                    .map(com.fruitlink.ledger.entity.LedgerEntry::getBalanceAfter).orElse(0L);
            long orderTotal = orderItemRepository.findByOrderId(order.getId())
                    .stream().mapToLong(OrderItem::getTotalAmount).sum();
            if (currentBalance + orderTotal > creditLimit) {
                throw new BusinessException("Credit limit exceeded. Current Balance: " + currentBalance + ", Limit: " + creditLimit);
            }
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : items) {
            List<InventoryBatch> fefo = batchRepository.findAvailableBySkuFefo(item.getSku().getId());
            if (fefo.isEmpty())
                throw new BusinessException("No stock available for SKU: " + item.getSku().getName());

            // Use first FEFO batch (earliest expiry)
            InventoryBatch batch = fefo.get(0);
            item.setBatch(batch);
            orderItemRepository.save(item);

            // Record stock deduction
            StockMovement movement = new StockMovement();
            movement.setBatch(batch);
            movement.setChangeQty(item.getQuantity().negate());
            movement.setReason("order_confirmed");
            movement.setReferenceId(order.getId());
            stockMovementRepository.save(movement);
            
            // Evaluate stock level for potential auto PO generation
            inventoryService.evaluateStockLevel(item.getSku().getId().toString());
        }

        order.setStatus("confirmed");
        order = orderRepository.save(order);

        auditService.logAction(actorPhone, "CONFIRM_ORDER", "ORDERS", "{\"status\":\"pending\"}", "{\"status\":\"confirmed\",\"orderId\":\"" + order.getId() + "\"}");

        return toResponse(order);
    }

    // ── Status transitions ─────────────────────────────────

    @Transactional
    public OrderResponse updateStatus(String orderId, UpdateStatusRequest req) {
        Order order = findOrder(orderId);
        order.setStatus(req.getStatus());
        return toResponse(orderRepository.save(order));
    }

    // ── Repeat Order & Catch Weight ────────────────────────

    @Transactional
    public OrderResponse repeatOrder(String orderId, String actorPhone) {
        Order oldOrder = findOrder(orderId);

        Order newOrder = new Order();
        newOrder.setShop(oldOrder.getShop());
        newOrder.setSalesman(oldOrder.getSalesman()); // keep salesman
        newOrder.setDeliveryDate(java.time.LocalDate.now().plusDays(1)); // set next day
        newOrder.setNotes("Repeat of " + oldOrder.getId());
        newOrder.setPaymentMode(oldOrder.getPaymentMode());
        newOrder.setSource("portal");
        newOrder = orderRepository.save(newOrder);

        List<OrderItem> oldItems = orderItemRepository.findByOrderId(oldOrder.getId());
        for (OrderItem oldItem : oldItems) {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(newOrder);
            newItem.setSku(oldItem.getSku());
            newItem.setQuantity(oldItem.getQuantity());
            newItem.setUnitPrice(oldItem.getUnitPrice());
            newItem.setDiscountPct(oldItem.getDiscountPct());
            newItem.setTotalAmount(oldItem.getTotalAmount());
            orderItemRepository.save(newItem);
        }

        auditService.logAction(actorPhone, "REPEAT_ORDER", "ORDERS", "{\"orderId\":\"" + oldOrder.getId() + "\"}", "{\"newOrderId\":\"" + newOrder.getId() + "\"}");

        return toResponse(newOrder);
    }

    @Transactional
    public OrderResponse updatePackedQty(String orderId, String itemId, BigDecimal packedQty, String actorPhone) {
        Order order = findOrder(orderId);
        if (!"confirmed".equals(order.getStatus()) && !"packed".equals(order.getStatus())) {
            throw new BusinessException("Only confirmed or packed orders can have packed quantities updated");
        }

        OrderItem item = orderItemRepository.findById(UUID.fromString(itemId))
                .orElseThrow(() -> new BusinessException("Order item not found"));

        if (!item.getOrder().getId().equals(order.getId())) {
            throw new BusinessException("Item does not belong to order");
        }

        item.setPackedQty(packedQty);
        if (item.getQuantity().compareTo(packedQty) != 0) {
            item.setWeightVarianceFlag(true);
        } else {
            item.setWeightVarianceFlag(false);
        }
        orderItemRepository.save(item);

        auditService.logAction(actorPhone, "UPDATE_PACKED_QTY", "ORDERS", "{\"itemId\":\"" + item.getId() + "\"}", "{\"packedQty\":" + packedQty + "}");

        return toResponse(order);
    }

    // ── Queries ────────────────────────────────────────────

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> getByStatus(String status) {
        return orderRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> getByShop(String shopId) {
        return orderRepository.findByShopId(UUID.fromString(shopId))
                .stream().map(this::toResponse).toList();
    }

    public List<OrderResponse> getBySalesman(String salesmanId) {
        return orderRepository.findBySalesmanId(UUID.fromString(salesmanId))
                .stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrder(String id) {
        return toResponse(findOrder(id));
    }

    // ── Helpers ────────────────────────────────────────────

    private Order findOrder(String id) {
        return orderRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Order not found"));
    }

    private long calcTotal(long unitPrice, BigDecimal qty, BigDecimal discountPct) {
        BigDecimal gross = BigDecimal.valueOf(unitPrice).multiply(qty);
        BigDecimal discount = gross.multiply(discountPct).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        return gross.subtract(discount).longValue();
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId().toString());
        r.setShopId(o.getShop().getId().toString());
        r.setShopName(o.getShop().getName());
        r.setStatus(o.getStatus());
        r.setDeliveryDate(o.getDeliveryDate());
        r.setNotes(o.getNotes());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        if (o.getSalesman() != null) {
            r.setSalesmanId(o.getSalesman().getId().toString());
            r.setSalesmanName(o.getSalesman().getFullName());
        }

        List<OrderItemResponse> itemResponses = orderItemRepository.findByOrderId(o.getId())
                .stream().map(item -> {
                    OrderItemResponse ir = new OrderItemResponse();
                    ir.setId(item.getId().toString());
                    ir.setSkuId(item.getSku().getId().toString());
                    ir.setSkuName(item.getSku().getName());
                    ir.setQuantity(item.getQuantity());
                    ir.setUnitPrice(item.getUnitPrice());
                    ir.setDiscountPct(item.getDiscountPct());
                    ir.setTotalAmount(item.getTotalAmount());
                    if (item.getBatch() != null) ir.setBatchId(item.getBatch().getId().toString());
                    return ir;
                }).toList();

        r.setItems(itemResponses);
        r.setGrandTotal(itemResponses.stream().mapToLong(OrderItemResponse::getTotalAmount).sum());
        return r;
    }
}
