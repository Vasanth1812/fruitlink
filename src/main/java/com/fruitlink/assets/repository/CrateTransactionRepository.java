package com.fruitlink.assets.repository;

import com.fruitlink.assets.entity.CrateTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface CrateTransactionRepository extends JpaRepository<CrateTransaction, UUID> {
    List<CrateTransaction> findByShopId(UUID shopId);
}
