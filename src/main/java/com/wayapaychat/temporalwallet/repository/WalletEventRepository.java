package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletEventCharges;

public interface WalletEventRepository extends JpaRepository <WalletEventCharges, Long> {
	
    Optional<WalletEventCharges> findByEventId(String id);
}
