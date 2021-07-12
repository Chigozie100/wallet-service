package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletProduct;

public interface WalletProductRepository extends JpaRepository<WalletProduct, Long> {
	
	WalletProduct findByProductCode(String name);

}
