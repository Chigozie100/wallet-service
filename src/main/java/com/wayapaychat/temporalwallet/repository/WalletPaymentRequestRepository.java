package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletPaymentRequest;
import org.springframework.data.jpa.repository.Query;

public interface WalletPaymentRequestRepository extends JpaRepository<WalletPaymentRequest, Long> {

	@Query("select w from WalletPaymentRequest w where w.reference =:refNo")
	Optional<WalletPaymentRequest> findByReference(String refNo);

}
