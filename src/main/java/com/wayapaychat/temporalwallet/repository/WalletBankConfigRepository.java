package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletBankConfig;

@Repository
public interface WalletBankConfigRepository extends JpaRepository<WalletBankConfig, Long> {
	
	WalletBankConfig findByCodeValue(String name);

}
