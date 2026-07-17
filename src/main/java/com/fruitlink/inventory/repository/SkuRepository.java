package com.fruitlink.inventory.repository;

import com.fruitlink.inventory.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkuRepository extends JpaRepository<Sku, UUID> {
    Optional<Sku> findByCode(String code);
    List<Sku> findByIsActiveTrue();
    List<Sku> findByCategory(String category);
    boolean existsByCode(String code);
}
