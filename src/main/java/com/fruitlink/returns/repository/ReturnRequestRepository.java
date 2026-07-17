package com.fruitlink.returns.repository;

import com.fruitlink.returns.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    List<ReturnRequest> findByShopId(UUID shopId);
    List<ReturnRequest> findByStatus(String status);
    List<ReturnRequest> findByOrderId(UUID orderId);
}
