package com.fruitlink.vendor.repository;

import com.fruitlink.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    boolean existsByContactPhone(String contactPhone);
}
