package com.wayapaychat.temporalwallet.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.wayapaychat.temporalwallet.enumm.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long>{
	
	List<WalletTransaction> findByAcctNumEquals(String accountNumber);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranId) = UPPER(:value) " + " AND u.del_flg = false")
	Optional<List<WalletTransaction>> findByTranIdIgnoreCase(@Param("value") String value);
	
	Page<WalletTransaction> findAllByAcctNum(String accountNumber, Pageable pageable);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false" + " AND u.tranCrncyCode = UPPER(:tranCrncy)" + " AND u.tranDate = (:tranDate)")
	List<WalletTransaction> findByTransaction(String tranId, LocalDate tranDate, String tranCrncy);

	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false")
	List<WalletTransaction> findByTransaction(String tranId);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.relatedTransId) = UPPER(:tranId) " + " AND u.del_flg = false" + " AND u.tranCrncyCode = UPPER(:tranCrncy)" + " AND u.tranDate = (:tranDate)")
	List<WalletTransaction> findByRevTrans(String tranId, LocalDate tranDate, String tranCrncy);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranId) = UPPER(:tranId) " + " AND u.del_flg = false" + " AND u.tranCrncyCode = UPPER(:tranCrncy)" + " AND u.tranDate = (:tranDate)" + " AND u.acctNum = UPPER(:accountNo)")
	WalletTransaction findByAcctNumTran(String accountNo, String tranId, LocalDate tranDate, String tranCrncy);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranType) = UPPER('REVERSAL') " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)")
	List<WalletTransaction> findByReverse(LocalDate fromtranDate, LocalDate totranDate);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranType) = UPPER('REVERSAL') " + " AND u.del_flg = false")
	List<WalletTransaction> findByAllReverse();
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) = UPPER(:account) " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)")
	List<WalletTransaction> findByStatement(LocalDate fromtranDate, LocalDate totranDate, String account);

	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) = UPPER(:account) " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)")
	List<WalletTransaction> findByVirtualStatement(LocalDate fromtranDate, LocalDate totranDate, String account);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.paymentReference) = UPPER(:ref) " + " AND u.del_flg = false" + " AND u.tranCrncyCode = UPPER(:tranCrncy)" + " AND u.tranDate = (:tranDate)")
	List<WalletTransaction> findByReference(String ref, LocalDate tranDate, String tranCrncy);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) = UPPER(:account) " + "AND UPPER(u.tranType) = UPPER('REVERSAL') " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)")
	List<WalletTransaction> findByAccountReverse(LocalDate fromtranDate, LocalDate totranDate, String account);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) = UPPER(:wayaNo) " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)" + " order by u.tranDate DESC ")
	List<WalletTransaction> findByOfficialAccount(LocalDate fromtranDate, LocalDate totranDate, String wayaNo);
	
	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) LIKE UPPER('NGN%') " + " AND u.del_flg = false" + " order by u.createdAt DESC ")
	Page<WalletTransaction> findByAccountOfficial(Pageable pageable);

	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) LIKE UPPER('NGN%') " + " AND u.del_flg = false" + " AND u.partTranType = 'C'"  +" AND u.partTranType = 'D'" + " order by u.createdAt DESC ")
	Page<WalletTransaction> findByAccountOfficial2(Pageable pageable);

	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) LIKE UPPER('NGN%') AND u.del_flg = false AND u.partTranType = UPPER(:partTranType)" + " order by u.createdAt DESC ")
	Page<WalletTransaction> findByAccountOfficial3(Pageable pageable, @Param("partTranType") String partTranType);


	@Query("SELECT sum(u.tranAmount) FROM WalletTransaction u " + "WHERE u.partTranType = 'D'" + " AND u.del_flg = false")
	BigDecimal findByAllDTransaction();

	@Query("SELECT sum(u.tranAmount) FROM WalletTransaction u " + "WHERE u.partTranType = 'C'" + " AND u.del_flg = false")
	BigDecimal findByAllCTransaction();


	@Query("SELECT sum(u.tranAmount) FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) LIKE UPPER('NGN%') " + "AND u.partTranType = 'D'" + " AND u.del_flg = false")
	BigDecimal findByAllDTransactionOfficial();

	@Query("SELECT sum(u.tranAmount) FROM WalletTransaction u " + "WHERE UPPER(u.acctNum) LIKE UPPER('NGN%') " + "AND u.partTranType = 'C'" + " AND u.del_flg = false")
	BigDecimal findByAllCTransactionOfficial();


	@Query("SELECT u FROM WalletTransaction u " + "WHERE UPPER(u.tranCategory) = UPPER(:categoryType) " + " AND u.del_flg = false" + " AND u.tranDate BETWEEN  (:fromtranDate)" + " AND (:totranDate)")
	List<WalletTransaction> filterByDateAndTranCategory(CategoryType categoryType, LocalDate fromtranDate, LocalDate totranDate);
}
