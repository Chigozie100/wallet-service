package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Billers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillersRepository extends JpaRepository<Billers, Long> {

    @Override
    Optional<Billers> findById(Long aLong);


    @Query("select b from Billers b where b.billerAggregatorCode = :#{#billerAggregatorCode} and b.categoryName = :#{#categoryName}")
    Optional<Billers> findDetails(String billerAggregatorCode, String categoryName);
}
