package com.fruitlink.delivery.repository;

import com.fruitlink.delivery.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {}
