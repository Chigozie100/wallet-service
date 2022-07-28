package com.wayapaychat.temporalwallet.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;
import org.springframework.data.repository.query.Param;

public interface WalletNonWayaPaymentRepository extends JpaRepository<WalletNonWayaPayment, Long> {
	
	@Query("SELECT u FROM WalletNonWayaPayment u " + "WHERE UPPER(u.tokenId) = UPPER(:tokenId) " + " AND u.del_flg = false"
			+ " AND u.crncyCode = UPPER(:tranCrncy)"+ "order by  u.createdAt DESC ")
	Optional<WalletNonWayaPayment> findByTransaction(String tokenId, String tranCrncy);

	@Query("SELECT u FROM WalletNonWayaPayment u WHERE u.status =:status and u.del_flg = false order by u.id")
	List<WalletNonWayaPayment> findByAllByStatus(PaymentStatus status);


//	@Query(value ="SELECT * FROM m_wallet_nonwaya_payment WHERE created_at >= current_date - INTERVAL '30' DAY AND status =:status and del_flg = false" , nativeQuery = true)
//	List<WalletNonWayaPayment> findByAllByStatus1(String status);
	
	//ELECT * FROM product WHERE pdate >= DATEADD(day, -30, getdate()). > now() - INTERVAL 30 day
	////	@Query("SELECT u FROM WalletNonWayaPayment u WHERE u.status =:status and u.del_flg = false " +
	////			"and u.createdAt > (now() - INTERVAL 30 day) order by u.id")
	
	@Query("SELECT u FROM WalletNonWayaPayment u " + "WHERE UPPER(u.tokenId) = UPPER(:tokenId) " + " AND u.del_flg = false"
			+ " AND u.confirmPIN = UPPER(:pin)")
	Optional<WalletNonWayaPayment> findByTokenPIN(String tokenId, String pin);

	@Query("SELECT u FROM WalletNonWayaPayment u " + "WHERE UPPER(u.tokenId) = UPPER(:tokenId) ")
	Optional<WalletNonWayaPayment> findByToken(String tokenId);


	@Query("SELECT u FROM WalletNonWayaPayment u WHERE u.createdBy =:userId order by u.createdAt desc")
	Page<WalletNonWayaPayment> findAllByCreatedBy(String userId, Pageable pageable);

	@Query("SELECT u FROM WalletNonWayaPayment u order by u.createdAt desc")
	Page<WalletNonWayaPayment> findAllDetails(Pageable pageable);

	@Query("SELECT count(u.id) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='PENDING' ")
	long findAllByPending(String userId);

	@Query("SELECT count(u.id) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='PAYOUT' ")
	long findAllByPayout(String userId);

	@Query("SELECT count(u.id) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='RESERVED' ")
	long findAllByReserved(String userId);

	@Query("SELECT count(u.id) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId ")
	long findAllByTotal(String userId);

	@Query("SELECT count(u.id) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status = 'EXPIRED'")
	long findAllByExpired(String userId);


	// for amount

	@Query("SELECT sum(u.tranAmount) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='PENDING' ")
	BigDecimal findAllByPendingAmount(String userId);

	@Query("SELECT sum(u.tranAmount) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='PAYOUT' ")
	BigDecimal findAllByPayoutAmount(String userId);

	@Query("SELECT sum(u.tranAmount) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status ='RESERVED' ")
	BigDecimal findAllByReservedAmount(String userId);

	@Query("SELECT sum(u.tranAmount) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId ")
	BigDecimal findAllByTotalAmount(String userId);

	@Query("SELECT sum(u.tranAmount) FROM WalletNonWayaPayment u WHERE u.createdBy =:userId and u.status = 'EXPIRED'")
	BigDecimal findAllByExpiredAmount(String userId);

}
