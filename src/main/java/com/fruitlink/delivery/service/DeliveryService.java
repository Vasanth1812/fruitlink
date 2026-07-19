package com.fruitlink.delivery.service;

import com.fruitlink.crates.service.CrateService;
import com.fruitlink.crates.dto.CrateDto.RecordTransactionRequest;
import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.delivery.dto.DeliveryDto.*;
import com.fruitlink.delivery.entity.*;
import com.fruitlink.delivery.repository.*;
import com.fruitlink.orders.entity.Order;
import com.fruitlink.orders.repository.OrderRepository;
import com.fruitlink.shops.entity.Shop;
import com.fruitlink.shops.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryManifestRepository manifestRepository;
    private final ManifestStopRepository stopRepository;
    private final ProofOfDeliveryRepository podRepository;
    private final GeofenceCheckInRepository geofenceRepository;
    private final RouteRepository routeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final CrateService crateService;

    // ── Manifest Generation ─────────────────────────────────

    @Transactional
    public ManifestResponse generateManifest(GenerateManifestRequest req) {
        Route route = routeRepository.findById(UUID.fromString(req.getRouteId()))
                .orElseThrow(() -> new BusinessException("Route not found"));
        User driver = userRepository.findById(UUID.fromString(req.getDriverId()))
                .orElseThrow(() -> new BusinessException("Driver not found"));

        DeliveryManifest manifest = new DeliveryManifest();
        manifest.setRoute(route);
        manifest.setDriver(driver);
        if (req.getVehicleId() != null && !req.getVehicleId().trim().isEmpty()) {
            manifest.setVehicleId(UUID.fromString(req.getVehicleId()));
        }
        manifest.setDispatchDate(req.getDispatchDate());
        manifest = manifestRepository.save(manifest);

        // Find all confirmed/packed orders for shops on this route
        List<Order> orders = orderRepository.findByStatusIn(List.of("packed", "confirmed"));
        List<Order> routeOrders = orders.stream()
                .filter(o -> o.getShop().getRouteId() != null && o.getShop().getRouteId().equals(route.getId()))
                .collect(Collectors.toList());

        int seq = 1;
        for (Order o : routeOrders) {
            ManifestStop stop = new ManifestStop();
            stop.setManifest(manifest);
            stop.setShop(o.getShop());
            stop.setOrder(o);
            stop.setSequence(seq++);
            stop.setStatus("pending");
            stopRepository.save(stop);

            o.setStatus("dispatched");
            orderRepository.save(o);
        }

        return toManifestResponse(manifest);
    }

    public List<ManifestResponse> getManifestsByDate(java.time.LocalDate date) {
        return manifestRepository.findByDispatchDate(date).stream()
                .map(this::toManifestResponse)
                .collect(Collectors.toList());
    }

    // ── Delivery Status & Proof of Delivery ─────────────────

    @Transactional
    public ManifestStopResponse updateStopStatus(String stopId, String status) {
        ManifestStop stop = stopRepository.findById(UUID.fromString(stopId))
                .orElseThrow(() -> new BusinessException("Stop not found"));
        stop.setStatus(status);
        return toStopResponse(stopRepository.save(stop));
    }

    @Transactional
    public void submitProofOfDelivery(String stopId, ProofOfDeliveryRequest req) {
        ManifestStop stop = stopRepository.findById(UUID.fromString(stopId))
                .orElseThrow(() -> new BusinessException("Stop not found"));

        if (!"arrived".equals(stop.getStatus()) && !"pending".equals(stop.getStatus())) {
            throw new BusinessException("Stop must be arrived or pending to submit POD");
        }

        ProofOfDelivery pod = new ProofOfDelivery();
        pod.setManifestStop(stop);
        pod.setConfirmationCode(req.getConfirmationCode());
        pod.setPhotoUrl(req.getPhotoUrl());
        pod.setCratesDelivered(req.getCratesDelivered());
        pod.setCratesReclaimed(req.getCratesReclaimed());
        pod.setConfirmedAt(Instant.now());
        podRepository.save(pod);

        stop.setStatus("completed");
        stop.getOrder().setStatus("delivered");
        stopRepository.save(stop);
        orderRepository.save(stop.getOrder());

        // Update crate ledger
        if (req.getCratesDelivered() > 0) {
            RecordTransactionRequest issueReq = new RecordTransactionRequest();
            issueReq.setType("issue");
            issueReq.setPartyId(stop.getShop().getId().toString());
            issueReq.setPartyType("shop");
            issueReq.setQuantity(req.getCratesDelivered());
            issueReq.setReferenceId(stop.getId().toString());
            crateService.recordTransaction(issueReq);
        }
        if (req.getCratesReclaimed() > 0) {
            RecordTransactionRequest returnReq = new RecordTransactionRequest();
            returnReq.setType("return");
            returnReq.setPartyId(stop.getShop().getId().toString());
            returnReq.setPartyType("shop");
            returnReq.setQuantity(req.getCratesReclaimed());
            returnReq.setReferenceId(stop.getId().toString());
            crateService.recordTransaction(returnReq);
        }
    }

    // ── Geofence Check In ───────────────────────────────────

    @Transactional
    public void checkInGeofence(GeofenceCheckInRequest req, String salesmanPhone) {
        User salesman = userRepository.findByPhone(salesmanPhone)
                .orElseThrow(() -> new BusinessException("Salesman not found"));
        Shop shop = shopRepository.findById(UUID.fromString(req.getShopId()))
                .orElseThrow(() -> new BusinessException("Shop not found"));

        // Dummy calculation for distance based on coordinates
        int distance = calculateDistance(shop.getAddress(), req.getCoordinates());
        String result = distance <= 200 ? "inside" : "outside";

        GeofenceCheckIn checkIn = new GeofenceCheckIn();
        checkIn.setShop(shop);
        checkIn.setSalesman(salesman);
        checkIn.setCoordinates(req.getCoordinates());
        checkIn.setDistanceFromShopM(distance);
        checkIn.setResult(result);
        checkIn.setCheckedInAt(Instant.now());
        geofenceRepository.save(checkIn);
    }

    private int calculateDistance(String shopCoords, String actualCoords) {
        // Stub implementation, just returning 100 meters
        return 100;
    }

    // ── Mappers ─────────────────────────────────────────────

    private ManifestResponse toManifestResponse(DeliveryManifest m) {
        ManifestResponse r = new ManifestResponse();
        r.setId(m.getId().toString());
        r.setRouteId(m.getRoute().getId().toString());
        r.setDriverId(m.getDriver() != null ? m.getDriver().getId().toString() : null);
        r.setDispatchDate(m.getDispatchDate());
        
        List<ManifestStop> stops = stopRepository.findByManifestIdOrderBySequenceAsc(m.getId());
        r.setStops(stops.stream().map(this::toStopResponse).collect(Collectors.toList()));
        return r;
    }

    private ManifestStopResponse toStopResponse(ManifestStop s) {
        ManifestStopResponse r = new ManifestStopResponse();
        r.setId(s.getId().toString());
        r.setShopId(s.getShop().getId().toString());
        r.setShopName(s.getShop().getName());
        r.setOrderId(s.getOrder().getId().toString());
        r.setSequence(s.getSequence());
        r.setStatus(s.getStatus());
        return r;
    }
}
