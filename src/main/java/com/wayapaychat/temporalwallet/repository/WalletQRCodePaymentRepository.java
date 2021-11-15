package com.wayapaychat.temporalwallet.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletQRCodePayment;

public interface WalletQRCodePaymentRepository extends JpaRepository<WalletQRCodePayment, Long> {
	
	@Query("SELECT u FROM WalletQRCodePayment u " + "WHERE UPPER(u.referenceNo) = UPPER(:refNo) " + " AND u.del_flg = false"
			+ " AND u.tranDate = UPPER(:refDate)")
	Optional<WalletQRCodePayment> findByReferenceNo(String refNo, LocalDate refDate);

}
