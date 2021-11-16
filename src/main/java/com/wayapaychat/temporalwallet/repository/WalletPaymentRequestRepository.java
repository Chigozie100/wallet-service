package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletPaymentRequest;

public interface WalletPaymentRequestRepository extends JpaRepository<WalletPaymentRequest, Long> {
	
	Optional<WalletPaymentRequest> findByReference(String refNo);

}
