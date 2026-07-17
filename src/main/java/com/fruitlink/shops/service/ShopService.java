package com.fruitlink.shops.service;

import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.shops.dto.ShopDto.*;
import com.fruitlink.shops.entity.Shop;
import com.fruitlink.shops.entity.ShopKycDocument;
import com.fruitlink.shops.repository.ShopKycDocumentRepository;
import com.fruitlink.shops.repository.ShopRepository;
import com.fruitlink.orders.entity.Order;
import com.fruitlink.orders.repository.OrderRepository;
import com.fruitlink.ledger.entity.LedgerEntry;
import com.fruitlink.ledger.repository.LedgerEntryRepository;
import com.fruitlink.shops.dto.ShopDto.DashboardResponse;
import com.fruitlink.shops.dto.ShopDto.ShopAnalyticsResponse;
import com.fruitlink.shops.entity.ShopFollowUp;
import com.fruitlink.shops.repository.ShopFollowUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopKycDocumentRepository kycRepository;
    private final UserRepository userRepository;
    private final ShopFollowUpRepository followUpRepository;
    private final OrderRepository orderRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    // ── Shop CRUD ──────────────────────────────────────────

    @Transactional
    public ShopResponse createShop(CreateShopRequest req) {
        Shop shop = new Shop();
        shop.setName(req.getName());
        shop.setContactPhone(req.getContactPhone());
        shop.setAddress(req.getAddress());
        shop.setGstin(req.getGstin());
        shop.setCreditLimit(req.getCreditLimit() != null ? req.getCreditLimit() : 0L);
        return toResponse(shopRepository.save(shop));
    }

    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ShopResponse> getFilteredShops(String search, String status, String salesman, String area, String route, String creditStatus) {
        return shopRepository.findAll().stream()
                .filter(s -> status == null || status.equals(s.getStatus()))
                .filter(s -> search == null || s.getName().toLowerCase().contains(search.toLowerCase()) || s.getContactPhone().contains(search))
                .filter(s -> salesman == null || (s.getAssignedSalesman() != null && s.getAssignedSalesman().getId().toString().equals(salesman)))
                .filter(s -> route == null || (s.getRouteId() != null && s.getRouteId().toString().equals(route)))
                // Note: 'area' is part of 'address' in current schema, matching loosely
                .filter(s -> area == null || (s.getAddress() != null && s.getAddress().toLowerCase().contains(area.toLowerCase())))
                .map(this::toResponse).toList();
    }

    public List<ShopResponse> getShopsByStatus(String status) {
        return shopRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    public ShopResponse getShop(String id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ShopResponse updateShop(String id, UpdateShopRequest req) {
        Shop shop = findById(id);
        if (req.getName() != null) shop.setName(req.getName());
        if (req.getContactPhone() != null) shop.setContactPhone(req.getContactPhone());
        if (req.getAddress() != null) shop.setAddress(req.getAddress());
        if (req.getGstin() != null) shop.setGstin(req.getGstin());
        if (req.getCreditLimit() != null) shop.setCreditLimit(req.getCreditLimit());
        return toResponse(shopRepository.save(shop));
    }

    @Transactional
    public ShopResponse assignSalesman(String shopId, AssignSalesmanRequest req) {
        Shop shop = findById(shopId);
        User salesman = userRepository.findById(UUID.fromString(req.getSalesmanId()))
                .orElseThrow(() -> new BusinessException("Salesman not found"));
        shop.setAssignedSalesman(salesman);
        return toResponse(shopRepository.save(shop));
    }

    @Transactional
    public ShopResponse changeStatus(String shopId, ChangeStatusRequest req) {
        Shop shop = findById(shopId);
        shop.setStatus(req.getStatus());
        return toResponse(shopRepository.save(shop));
    }

    public void deleteShop(String id) {
        shopRepository.delete(findById(id));
    }

    // ── KYC Documents ──────────────────────────────────────

    @Transactional
    public KycDocumentResponse addKycDocument(String shopId, KycDocumentRequest req) {
        Shop shop = findById(shopId);
        ShopKycDocument doc = new ShopKycDocument();
        doc.setShop(shop);
        doc.setDocumentType(req.getDocumentType());
        doc.setFileUrl(req.getFileUrl());
        return toKycResponse(kycRepository.save(doc));
    }

    public List<KycDocumentResponse> getKycDocuments(String shopId) {
        return kycRepository.findByShopId(UUID.fromString(shopId))
                .stream().map(this::toKycResponse).toList();
    }

    @Transactional
    public KycDocumentResponse reviewKycDocument(String docId, KycReviewRequest req, String reviewerPhone) {
        ShopKycDocument doc = kycRepository.findById(UUID.fromString(docId))
                .orElseThrow(() -> new BusinessException("KYC document not found"));
        doc.setReviewStatus(req.getReviewStatus());
        userRepository.findByPhone(reviewerPhone).ifPresent(doc::setReviewedBy);
        return toKycResponse(kycRepository.save(doc));
    }

    // ── Helpers ────────────────────────────────────────────

    private Shop findById(String id) {
        return shopRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException("Shop not found"));
    }

    private ShopResponse toResponse(Shop s) {
        ShopResponse r = new ShopResponse();
        r.setId(s.getId().toString());
        r.setName(s.getName());
        r.setContactPhone(s.getContactPhone());
        r.setAddress(s.getAddress());
        r.setGstin(s.getGstin());
        r.setStatus(s.getStatus());
        r.setCreditLimit(s.getCreditLimit());
        r.setCreatedAt(s.getCreatedAt());
        if (s.getAssignedSalesman() != null) {
            r.setAssignedSalesmanId(s.getAssignedSalesman().getId().toString());
            r.setAssignedSalesmanName(s.getAssignedSalesman().getFullName());
        }
        return r;
    }

    private KycDocumentResponse toKycResponse(ShopKycDocument d) {
        KycDocumentResponse r = new KycDocumentResponse();
        r.setId(d.getId().toString());
        r.setShopId(d.getShop().getId().toString());
        r.setDocumentType(d.getDocumentType());
        r.setFileUrl(d.getFileUrl());
        r.setReviewStatus(d.getReviewStatus());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }

    // ── Follow-ups ─────────────────────────────────────────

    @Transactional
    public FollowUpResponse addFollowUp(String shopId, CreateFollowUpRequest req) {
        Shop shop = findById(shopId);
        ShopFollowUp followUp = new ShopFollowUp();
        followUp.setShop(shop);
        followUp.setReason(req.getReason());
        followUp.setRemarks(req.getRemarks());
        followUp.setNextFollowUp(req.getNextFollowUp());
        
        if (req.getAssignedTo() != null) {
            userRepository.findById(UUID.fromString(req.getAssignedTo()))
                .ifPresent(followUp::setAssignedTo);
        }
        
        return toFollowUpResponse(followUpRepository.save(followUp));
    }

    public List<FollowUpResponse> getFollowUps(String shopId) {
        return followUpRepository.findByShopIdOrderByCreatedAtDesc(UUID.fromString(shopId))
                .stream().map(this::toFollowUpResponse).toList();
    }

    @Transactional
    public FollowUpResponse updateFollowUp(String followUpId, UpdateFollowUpRequest req) {
        ShopFollowUp followUp = followUpRepository.findById(UUID.fromString(followUpId))
                .orElseThrow(() -> new BusinessException("Follow-up not found"));
                
        if (req.getStatus() != null) followUp.setStatus(req.getStatus());
        if (req.getRemarks() != null) followUp.setRemarks(req.getRemarks());
        if (req.getNextFollowUp() != null) followUp.setNextFollowUp(req.getNextFollowUp());
        
        return toFollowUpResponse(followUpRepository.save(followUp));
    }

    private FollowUpResponse toFollowUpResponse(ShopFollowUp f) {
        FollowUpResponse r = new FollowUpResponse();
        r.setId(f.getId().toString());
        r.setShopId(f.getShop().getId().toString());
        r.setReason(f.getReason());
        r.setRemarks(f.getRemarks());
        r.setStatus(f.getStatus());
        r.setNextFollowUp(f.getNextFollowUp());
        r.setCreatedAt(f.getCreatedAt());
        if (f.getAssignedTo() != null) {
            r.setAssignedTo(f.getAssignedTo().getId().toString());
            r.setAssignedToName(f.getAssignedTo().getFullName());
        }
        return r;
    }

    // ── Dashboard & Analytics ──────────────────────────────

    public DashboardResponse getDashboard() {
        List<Shop> allShops = shopRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();
        
        long totalShops = allShops.size();
        long activeShops = allShops.stream().filter(s -> "active".equals(s.getStatus())).count();
        long inactiveShops = allShops.stream().filter(s -> "inactive".equals(s.getStatus())).count();
        
        long pendingFollowUps = followUpRepository.findByStatus("pending").size();
        
        // Calculate outstanding from all shops
        long outstandingAmount = 0L;
        for (Shop s : allShops) {
            long bal = ledgerEntryRepository.findTopByShopIdOrderByCreatedAtDesc(s.getId())
                .map(LedgerEntry::getBalanceAfter).orElse(0L);
            if (bal > 0) outstandingAmount += bal;
        }

        DashboardResponse.KpiCards kpis = new DashboardResponse.KpiCards();
        kpis.setTotalShops(totalShops);
        kpis.setActiveShops(activeShops);
        kpis.setInactiveShops(inactiveShops);
        kpis.setTodaysNewShops(0); // Mock for now
        kpis.setPendingFollowUps(pendingFollowUps);
        kpis.setCreditBlockedShops(0); // Mock for now
        kpis.setOutstandingAmount(outstandingAmount);
        kpis.setTodaysOrders(allOrders.size());

        DashboardResponse res = new DashboardResponse();
        res.setKpis(kpis);
        res.setMonthlySales(List.of()); // Empty lists for demo
        res.setTopCustomers(List.of());
        res.setAreaWiseShops(List.of());
        res.setOutstandingTrend(List.of());
        res.setNewShopGrowth(List.of());
        res.setRecentActivities(List.of());
        res.setTop10Shops(List.of());
        return res;
    }

    public ShopAnalyticsResponse getShopAnalytics(String shopId) {
        List<Order> shopOrders = orderRepository.findByShopId(UUID.fromString(shopId));
        
        long totalRevenue = shopOrders.stream().mapToLong(Order::getTotalValue).sum();
        long averageBasket = shopOrders.isEmpty() ? 0 : totalRevenue / shopOrders.size();

        ShopAnalyticsResponse res = new ShopAnalyticsResponse();
        res.setAverageBasketValue(averageBasket);
        res.setOrderFrequencyDays(7); // Mock 7 days
        res.setMonthlyOrders(List.of());
        res.setMonthlyRevenue(List.of());
        res.setOutstandingTrend(List.of());
        
        return res;
    }
}
