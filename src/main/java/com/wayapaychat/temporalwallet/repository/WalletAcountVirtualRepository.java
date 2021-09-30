package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletAcountVirtual;

public interface WalletAcountVirtualRepository extends JpaRepository<WalletAcountVirtual, Long> {
    
	@Query(value = "SELECT u FROM WalletAcountVirtual u " + "WHERE (u.virtuId) = (:id) " + " AND u.accountNumber = (:account)" + " AND u.deleted = false")
	WalletAcountVirtual findByIdAccount(Long id, String account);
}
