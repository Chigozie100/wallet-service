package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;

public interface WalletNonWayaPaymentRepository extends JpaRepository<WalletNonWayaPayment, Long> {
	
	@Query("SELECT u FROM WalletNonWayaPayment u " + "WHERE UPPER(u.tokenId) = UPPER(:tokenId) " + " AND u.del_flg = false"
			+ " AND u.crncyCode = UPPER(:tranCrncy)")
	Optional<WalletNonWayaPayment> findByTransaction(String tokenId, String tranCrncy);
	
	@Query("SELECT u FROM WalletNonWayaPayment u " + "WHERE UPPER(u.tokenId) = UPPER(:tokenId) " + " AND u.del_flg = false"
			+ " AND u.confirmPIN = UPPER(:pin)")
	Optional<WalletNonWayaPayment> findByTokenPIN(String tokenId, String pin);

	@Query("SELECT u FROM WalletNonWayaPayment u WHERE u.createdBy =:userId order by u.createdAt desc")
	Page<WalletNonWayaPayment> findAllByCreatedBy(String userId, Pageable pageable);

}
