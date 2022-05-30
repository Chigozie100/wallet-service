package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.ReversalSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReversalSetupRepository extends JpaRepository<ReversalSetup, Long> {

}
