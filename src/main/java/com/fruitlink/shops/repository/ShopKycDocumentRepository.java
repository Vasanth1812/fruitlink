package com.fruitlink.shops.repository;

import com.fruitlink.shops.entity.ShopKycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShopKycDocumentRepository extends JpaRepository<ShopKycDocument, UUID> {
    List<ShopKycDocument> findByShopId(UUID shopId);
    List<ShopKycDocument> findByReviewStatus(String reviewStatus);
}
