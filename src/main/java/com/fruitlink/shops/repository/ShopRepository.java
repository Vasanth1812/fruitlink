package com.fruitlink.shops.repository;

import com.fruitlink.shops.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

    List<Shop> findByStatus(String status);

    @Query("SELECT s FROM Shop s WHERE s.assignedSalesman.id = :salesmanId")
    List<Shop> findBySalesmanId(UUID salesmanId);

    boolean existsByContactPhone(String contactPhone);
}
