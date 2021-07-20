package com.wayapaychat.temporalwallet.repository;

import java.util.List;
import java.util.Optional;

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
}
