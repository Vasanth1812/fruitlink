package com.fruitlink.crates.repository;

import com.fruitlink.crates.entity.CrateBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrateBalanceRepository extends JpaRepository<CrateBalance, UUID> {
    Optional<CrateBalance> findByPartyIdAndPartyType(UUID partyId, String partyType);
}
