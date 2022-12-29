package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.ProductPricing;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing,Long> {
    @Query("select u from ProductPricing u where u.name = :#{#name}")
    Optional<ProductPricing> findDetails(@Param("name") String name);
}
