package com.fruitlink.ledger.repository;

import com.fruitlink.ledger.entity.TaxLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TaxLineRepository extends JpaRepository<TaxLine, UUID> {
    List<TaxLine> findByInvoiceId(UUID invoiceId);
}
