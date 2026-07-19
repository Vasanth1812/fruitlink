package com.fruitlink.crates.repository;

import com.fruitlink.crates.entity.CrateTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CrateTransactionRepository extends JpaRepository<CrateTransaction, UUID> {
    List<CrateTransaction> findByPartyIdOrderByDateDesc(UUID partyId);
}
