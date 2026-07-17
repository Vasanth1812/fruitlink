package com.fruitlink.orders.repository;

import com.fruitlink.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByShopId(UUID shopId);
    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.salesman.id = :salesmanId")
    List<Order> findBySalesmanId(UUID salesmanId);

    List<Order> findByStatusIn(List<String> statuses);
}
