package com.fruitlink.ledger.repository;

import com.fruitlink.ledger.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByOrderId(UUID orderId);
    List<Invoice> findByShopId(UUID shopId);
    List<Invoice> findByStatus(String status);
    Optional<Invoice> findTopByOrderByCreatedAtDesc(); // for invoice number seq
}
