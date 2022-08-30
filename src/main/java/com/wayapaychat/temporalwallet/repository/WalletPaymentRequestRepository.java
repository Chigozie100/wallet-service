package com.wayapaychat.temporalwallet.repository;

import java.util.List;
import java.util.Optional;

import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;
import com.wayapaychat.temporalwallet.enumm.PaymentRequestStatus;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wayapaychat.temporalwallet.entity.WalletPaymentRequest;
import org.springframework.data.jpa.repository.Query;

public interface WalletPaymentRequestRepository extends JpaRepository<WalletPaymentRequest, Long> {

	@Query("select w from WalletPaymentRequest w where w.reference =:refNo")
	Optional<WalletPaymentRequest> findByReference(String refNo);

	@Query("select w from WalletPaymentRequest w where w.status =:status and w.rejected = false ")
	List<WalletPaymentRequest> findByAllByStatus(PaymentRequestStatus status);

	@Query("select w from WalletPaymentRequest w where w.reference =:refNo and w.status =:status")
	Optional<WalletPaymentRequest> findByReferenceAndStatus(String refNo, PaymentRequestStatus status);


	//    @Enumerated(EnumType.STRING)
	//    private PaymentRequestStatus status;

}
