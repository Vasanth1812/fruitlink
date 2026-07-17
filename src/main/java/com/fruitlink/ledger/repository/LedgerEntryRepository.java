package com.fruitlink.ledger.repository;

import com.fruitlink.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByShopIdOrderByCreatedAtDesc(UUID shopId);

    Optional<LedgerEntry> findTopByShopIdOrderByCreatedAtDesc(UUID shopId);
}
