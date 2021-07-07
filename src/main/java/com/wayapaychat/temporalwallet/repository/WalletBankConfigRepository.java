package com.wayapaychat.temporalwallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletBankConfig;
import com.wayapaychat.temporalwallet.entity.WalletConfig;

@Repository
public interface WalletBankConfigRepository extends JpaRepository<WalletBankConfig, Long> {
	
	WalletBankConfig findByCodeValue(String name);
	
	List<WalletBankConfig> findByConfig(WalletConfig config);
	
	

}
