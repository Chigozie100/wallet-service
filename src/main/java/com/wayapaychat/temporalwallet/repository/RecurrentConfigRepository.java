package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.RecurrentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RecurrentConfigRepository extends JpaRepository<RecurrentConfig, Long> {

    @Query("select r from RecurrentConfig r where r.isActive = 'true'")
    Optional<RecurrentConfig> findByActive();
}
