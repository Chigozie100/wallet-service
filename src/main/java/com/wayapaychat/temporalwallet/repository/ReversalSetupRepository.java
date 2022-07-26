package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.ReversalSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReversalSetupRepository extends JpaRepository<ReversalSetup, Long> {

    @Query("select r from ReversalSetup r where r.days =:#{#day}")
    Optional<ReversalSetup> findByDetails(Integer day);

    @Query("select r from ReversalSetup  r where r.isActive = 'true'")
    Optional<ReversalSetup> findByActive();
}
