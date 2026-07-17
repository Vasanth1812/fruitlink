package com.fruitlink.inventory.repository;

import com.fruitlink.inventory.entity.SpoilageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpoilageLogRepository extends JpaRepository<SpoilageLog, UUID> {
    List<SpoilageLog> findByBatchId(UUID batchId);
}
