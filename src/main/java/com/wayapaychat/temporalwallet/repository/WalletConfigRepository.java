package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletConfig;

@Repository
public interface WalletConfigRepository extends JpaRepository<WalletConfig, Long> {
	
	WalletConfig findByCodeName(String name);

}
