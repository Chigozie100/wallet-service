package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletProductCode;

@Repository
public interface WalletProductCodeRepository extends JpaRepository<WalletProductCode, Long> {
	
	WalletProductCode findByProductCode(String name);

}
