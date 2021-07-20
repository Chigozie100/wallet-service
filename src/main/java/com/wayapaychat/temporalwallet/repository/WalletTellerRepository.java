package com.wayapaychat.temporalwallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletTeller;

public interface WalletTellerRepository extends JpaRepository<WalletTeller, Long> {
	
	Optional<WalletTeller> findByUserId(Long id);
	
	@Query("SELECT u FROM WalletTeller u WHERE u.userId = (:id)" + " AND u.crncyCode = (:crncycode)" + " AND u.adminCashAcct = (:cash)")
    Optional<WalletTeller> findByUserCashAcct(Long id, String crncycode, String cash);
	
	@Query("SELECT u FROM WalletTeller u WHERE u.userId = (:id)" + " AND u.crncyCode = (:crncycode)" + " AND u.sol_id = (:solId)")
    Optional<WalletTeller> findByUserSol(Long id, String crncycode, String solId);

}
